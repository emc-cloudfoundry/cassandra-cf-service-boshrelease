package com.emc.cf.broker.cassandra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AppConfig {

  @Value("${provision.store}")
  private String provisionStore;

  @Value("${provision.store.username}")
  private String provisionStoreUsername;

  @Value("${provision.store.password}")
  private String provisionStorePassword;

  @Value("${broker.username}")
  private String brokerUsername;

  @Value("${broker.password}")
  private String brokerPassword;

  @Value("${seeds}")
  private String seeds;

  @Value("${with.ssl}")
  private String withSSL;

  public String getProvisionStore() {
    return provisionStore;
  }

  public String getProvisionStoreUsername() {
    return provisionStoreUsername;
  }

  public String getProvisionStorePassword() {
    return provisionStorePassword;
  }

  public String getBrokerUsername() {
    return brokerUsername;
  }

  public String getBrokerPassword() {
    return brokerPassword;
  }

  public String getSeeds() {
    return seeds;
  }

  public String getWithSSL() {
    return withSSL;
  }
}
