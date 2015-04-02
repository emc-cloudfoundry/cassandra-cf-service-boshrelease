package com.emc.cf.broker.cassandra.data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class CassandraInstance {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String serviceDefinitionId;
  private String serviceInstanceId;
  private String planId;
  private String organizationGuid;
  private String spaceGuid;
  private String dashboardUrl;
  private String keySpace;
  private String seeds;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getServiceDefinitionId() {
    return serviceDefinitionId;
  }

  public void setServiceDefinitionId(String serviceDefinitionId) {
    this.serviceDefinitionId = serviceDefinitionId;
  }

  public String getServiceInstanceId() {
    return serviceInstanceId;
  }

  public void setServiceInstanceId(String serviceInstanceId) {
    this.serviceInstanceId = serviceInstanceId;
  }

  public String getPlanId() {
    return planId;
  }

  public void setPlanId(String planId) {
    this.planId = planId;
  }

  public String getOrganizationGuid() {
    return organizationGuid;
  }

  public void setOrganizationGuid(String organizationGuid) {
    this.organizationGuid = organizationGuid;
  }

  public String getSpaceGuid() {
    return spaceGuid;
  }

  public void setSpaceGuid(String spaceGuid) {
    this.spaceGuid = spaceGuid;
  }

  public String getDashboardUrl() {
    return dashboardUrl;
  }

  public void setDashboardUrl(String dashboardUrl) {
    this.dashboardUrl = dashboardUrl;
  }

  public String getKeySpace() {
    return keySpace;
  }

  public void setKeySpace(String keySpace) {
    this.keySpace = keySpace;
  }

  public String getSeeds() {
    return seeds;
  }

  public void setSeeds(String seeds) {
    this.seeds = seeds;
  }
}
