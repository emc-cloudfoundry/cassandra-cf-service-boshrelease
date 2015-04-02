package com.emc.cf.broker.cassandra.data;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
public class CassandraInstanceBinding {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String bindingId;
  private String serviceInstanceId;
  private String syslogDrainUrl;
  private String appGuid;

  @ElementCollection(fetch = FetchType.EAGER)
  private Map<String,String> credentials = new HashMap<String,String>();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getBindingId() {
    return bindingId;
  }

  public void setBindingId(String bindingId) {
    this.bindingId = bindingId;
  }

  public String getServiceInstanceId() {
    return serviceInstanceId;
  }

  public void setServiceInstanceId(String serviceInstanceId) {
    this.serviceInstanceId = serviceInstanceId;
  }

  public String getSyslogDrainUrl() {
    return syslogDrainUrl;
  }

  public void setSyslogDrainUrl(String syslogDrainUrl) {
    this.syslogDrainUrl = syslogDrainUrl;
  }

  public String getAppGuid() {
    return appGuid;
  }

  public void setAppGuid(String appGuid) {
    this.appGuid = appGuid;
  }

  public void setCredentials(Map<String, String> credentials) {
    if (credentials == null) {
      credentials = new HashMap<String,String>();
    } else {
      this.credentials = credentials;
    }
  }

  public Map<String, Object> getCredentials() {
    Map<String, Object> cred = new HashMap< String, Object>(credentials);
    return cred;
  }
}
