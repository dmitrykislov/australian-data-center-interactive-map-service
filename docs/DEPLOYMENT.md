# Deployment Guide: DataCenter Mapping Application

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Docker Deployment](#docker-deployment)
3. [Kubernetes Deployment](#kubernetes-deployment)
4. [Configuration](#configuration)
5. [Scaling](#scaling)
6. [Monitoring](#monitoring)
7. [Troubleshooting](#troubleshooting)

## Prerequisites

### For Docker Deployment
- Docker 20.10+
- Docker Compose 2.0+
- 4GB RAM minimum
- 2 CPU cores minimum

### For Kubernetes Deployment
- Kubernetes 1.24+
- kubectl 1.24+
- 8GB RAM minimum
- 4 CPU cores minimum
- Persistent Volume support (optional, for Redis persistence)
- Metrics Server installed (for HPA)

## Docker Deployment

### Building the Docker Image

#### Standard Build
```bash
docker build -t datacenter-mapping:latest .
```

#### Optimized Multi-Stage Build
```bash
docker build -f Dockerfile.multistage -t datacenter-mapping:latest .
```

### Running with Docker Compose

Create a `docker-compose.yml` file:

```yaml
version: '3.8'

services:
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 3

  datacenter-app:
    build: .
    ports:
      - "8443:8443"
    environment:
      REDIS_HOST: redis
      REDIS_PORT: 6379
      SSL_KEYSTORE_PASSWORD: changeit
    depends_on:
      redis:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "-k", "https://localhost:8443/api/datacenters"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s

volumes:
  redis-data:
```

Run the application:
```bash
docker-compose up -d
```

Access the application:
```bash
curl -k https://localhost:8443/api/datacenters
```

### Docker Image Optimization

The multi-stage Dockerfile provides:
- **Smaller image size**: ~400MB vs ~600MB
- **Faster startup**: Optimized layer caching
- **Better security**: Non-root user, read-only filesystem
- **Health checks**: Automatic container restart on failure

## Kubernetes Deployment

### Prerequisites Setup

1. **Create namespace**:
```bash
kubectl apply -f kubernetes/namespace.yaml
```

2. **Create secrets** (replace with actual values):
```bash
kubectl create secret generic datacenter-secrets \
  --from-literal=keystore-password=changeit \
  -n datacenter-mapping
```

3. **Build and push Docker image**:
```bash
docker build -t your-registry/datacenter-mapping:v1.0.0 .
docker push your-registry/datacenter-mapping:v1.0.0
```

Update the image in `kubernetes/deployment.yaml`:
```yaml
image: your-registry/datacenter-mapping:v1.0.0
imagePullPolicy: Always
```

### Deploying to Kubernetes

1. **Deploy Redis**:
```bash
kubectl apply -f kubernetes/redis-deployment.yaml
kubectl apply -f kubernetes/redis-service.yaml
```

Verify Redis is running:
```bash
kubectl get pods -n datacenter-mapping -l app=redis
kubectl logs -n datacenter-mapping -l app=redis
```

2. **Deploy ConfigMap**:
```bash
kubectl apply -f kubernetes/configmap.yaml
```

3. **Deploy Application**:
```bash
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/service.yaml
```

Verify deployment:
```bash
kubectl get deployments -n datacenter-mapping
kubectl get pods -n datacenter-mapping
kubectl get svc -n datacenter-mapping
```

4. **Deploy HPA**:
```bash
kubectl apply -f kubernetes/hpa.yaml
```

Verify HPA:
```bash
kubectl get hpa -n datacenter-mapping
kubectl describe hpa datacenter-app-hpa -n datacenter-mapping
```

### Accessing the Application

#### Port Forwarding (Development)
```bash
kubectl port-forward -n datacenter-mapping svc/datacenter-app-service 8443:443
curl -k https://localhost:8443/api/datacenters
```

#### LoadBalancer (Production)
```bash
kubectl get svc -n datacenter-mapping datacenter-app-service
# Get the EXTERNAL-IP and access via https://EXTERNAL-IP/api/datacenters
```

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `REDIS_HOST` | localhost | Redis server hostname |
| `REDIS_PORT` | 6379 | Redis server port |
| `SSL_KEYSTORE_PASSWORD` | changeit | SSL keystore password |
| `JAVA_OPTS` | (empty) | JVM options |

### Application Configuration

Edit `kubernetes/configmap.yaml` to customize:

```yaml
server:
  port: 8443
  tomcat:
    threads:
      max: 200          # Max request threads
      min-spare: 10     # Min spare threads
    accept-count: 100   # Queue size
    max-connections: 10000

spring:
  redis:
    jedis:
      pool:
        max-active: 20  # Max Redis connections
        max-idle: 10    # Max idle connections
        min-idle: 5     # Min idle connections
```

### SSL/TLS Configuration

1. **Generate keystore** (if needed):
```bash
keytool -genkeypair -alias tomcat -keyalg RSA -keysize 2048 \
  -keystore keystore.p12 -storetype PKCS12 \
  -storepass changeit -validity 365
```

2. **Create Kubernetes secret**:
```bash
kubectl create secret generic datacenter-secrets \
  --from-file=keystore.p12 \
  --from-literal=keystore-password=changeit \
  -n datacenter-mapping
```

## Scaling

### Manual Scaling

Scale the deployment to N replicas:
```bash
kubectl scale deployment datacenter-app -n datacenter-mapping --replicas=5
```

### Automatic Scaling (HPA)

The HPA configuration scales based on:

1. **CPU Usage**: Scales up when CPU > 70%
2. **Memory Usage**: Scales up when memory > 80%
3. **WebSocket Connections**: Scales up when avg connections > 100

#### View HPA Status
```bash
kubectl get hpa -n datacenter-mapping
kubectl describe hpa datacenter-app-hpa -n datacenter-mapping
kubectl top pods -n datacenter-mapping
```

#### Scaling Behavior

- **Min replicas**: 3
- **Max replicas**: 20
- **Scale-up**: Aggressive (100% increase every 30s)
- **Scale-down**: Conservative (50% decrease every 60s)

#### Testing HPA

Generate load to trigger scaling:
```bash
# Using Apache Bench
ab -n 10000 -c 100 -k https://localhost:8443/api/datacenters

# Using wrk
wrk -t4 -c100 -d30s https://localhost:8443/api/datacenters
```

Monitor scaling:
```bash
watch -n 2 'kubectl get hpa -n datacenter-mapping && echo "---" && kubectl get pods -n datacenter-mapping'
```

## Monitoring

### Health Checks

The deployment includes:

1. **Liveness Probe**: Restarts unhealthy containers
   - Endpoint: `/api/datacenters`
   - Interval: 10s
   - Timeout: 5s
   - Failure threshold: 3

2. **Readiness Probe**: Removes unhealthy pods from load balancer
   - Endpoint: `/api/datacenters`
   - Interval: 5s
   - Timeout: 3s
   - Failure threshold: 2

### Viewing Logs

```bash
# Application logs
kubectl logs -n datacenter-mapping -l app=datacenter-app -f

# Redis logs
kubectl logs -n datacenter-mapping -l app=redis -f

# Previous pod logs (if crashed)
kubectl logs -n datacenter-mapping <pod-name> --previous
```

### Metrics

View resource usage:
```bash
kubectl top nodes
kubectl top pods -n datacenter-mapping
```

### Prometheus Integration

The deployment includes Prometheus annotations:
```yaml
prometheus.io/scrape: "true"
prometheus.io/port: "8443"
prometheus.io/path: "/actuator/prometheus"
```

Add to Prometheus scrape config:
```yaml
- job_name: 'datacenter-app'
  kubernetes_sd_configs:
  - role: pod
    namespaces:
      names:
      - datacenter-mapping
  relabel_configs:
  - source_labels: [__meta_kubernetes_pod_annotation_prometheus_io_scrape]
    action: keep
    regex: true
```

## Troubleshooting

### Pod Not Starting

```bash
# Check pod status
kubectl describe pod <pod-name> -n datacenter-mapping

# Check logs
kubectl logs <pod-name> -n datacenter-mapping

# Check events
kubectl get events -n datacenter-mapping --sort-by='.lastTimestamp'
```

### Redis Connection Issues

```bash
# Test Redis connectivity
kubectl exec -it <app-pod> -n datacenter-mapping -- \
  redis-cli -h redis-service -p 6379 ping

# Check Redis logs
kubectl logs -n datacenter-mapping -l app=redis
```

### High Memory Usage

1. **Check JVM settings**:
```bash
kubectl exec <pod-name> -n datacenter-mapping -- \
  ps aux | grep java
```

2. **Adjust memory limits** in `deployment.yaml`:
```yaml
resources:
  limits:
    memory: "2Gi"  # Increase if needed
```

3. **Restart pods**:
```bash
kubectl rollout restart deployment/datacenter-app -n datacenter-mapping
```

### HPA Not Scaling

1. **Verify Metrics Server**:
```bash
kubectl get deployment metrics-server -n kube-system
```

2. **Check HPA status**:
```bash
kubectl describe hpa datacenter-app-hpa -n datacenter-mapping
```

3. **View HPA events**:
```bash
kubectl get events -n datacenter-mapping | grep HorizontalPodAutoscaler
```

### SSL/TLS Issues

```bash
# Test HTTPS endpoint
kubectl exec <pod-name> -n datacenter-mapping -- \
  curl -k https://localhost:8443/api/datacenters

# Check certificate
kubectl get secret datacenter-secrets -n datacenter-mapping -o yaml
```

## Performance Tuning

### JVM Tuning

Edit `deployment.yaml` JAVA_OPTS:
```yaml
env:
- name: JAVA_OPTS
  value: "-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:MaxRAMPercentage=75.0"
```

### Tomcat Tuning

Edit `configmap.yaml`:
```yaml
server:
  tomcat:
    threads:
      max: 300          # Increase for high concurrency
    accept-count: 200   # Increase queue size
    max-connections: 20000
```

### Redis Tuning

For production, use Redis Cluster or Sentinel:
```bash
# Example: Redis Sentinel setup
kubectl apply -f kubernetes/redis-sentinel.yaml
```

## Rollback

### Rollback to Previous Version

```bash
# View rollout history
kubectl rollout history deployment/datacenter-app -n datacenter-mapping

# Rollback to previous version
kubectl rollout undo deployment/datacenter-app -n datacenter-mapping

# Rollback to specific revision
kubectl rollout undo deployment/datacenter-app -n datacenter-mapping --to-revision=2
```

## Load Testing

### Running Gatling Tests

```bash
# Run load tests
mvn gatling:test -Dgatling.simulationClass=com.datacenter.performance.DataCenterLoadSimulation

# View results
open target/gatling/*/index.html
```

### Expected Results

With 1000 concurrent users:
- **P99 Response Time**: < 200ms
- **P95 Response Time**: < 150ms
- **P50 Response Time**: < 50ms
- **Success Rate**: > 99%
- **Throughput**: > 5000 req/s

## Production Checklist

- [ ] SSL/TLS certificates configured
- [ ] Redis persistence enabled (if needed)
- [ ] Monitoring and alerting configured
- [ ] Log aggregation setup (ELK, Splunk, etc.)
- [ ] Backup strategy for data
- [ ] Disaster recovery plan
- [ ] Load testing completed
- [ ] Security scanning completed
- [ ] Documentation updated
- [ ] Team trained on deployment procedures

## Support

For issues or questions:
1. Check logs: `kubectl logs -n datacenter-mapping -l app=datacenter-app`
2. Check events: `kubectl get events -n datacenter-mapping`
3. Review this guide's troubleshooting section
4. Contact the development team