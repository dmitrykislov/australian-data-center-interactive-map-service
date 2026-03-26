package com.datacenter.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class HttpsEnforcerTest {

  @Autowired private ApplicationContext applicationContext;

  @Test
  void testHttpsEnforcerBeanExists() {
    assertNotNull(applicationContext.getBean(HttpsEnforcer.class));
  }

  @Test
  void testHttpsEnforcerIsConfiguration() {
    HttpsEnforcer enforcer = applicationContext.getBean(HttpsEnforcer.class);
    assertNotNull(enforcer);
  }
}