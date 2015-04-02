package com.emc.cf.broker.cassandra.util;

import java.util.ArrayList;
import java.util.List;

import com.emc.cf.broker.cassandra.data.CassandraInstance;
import com.emc.cf.broker.cassandra.data.CassandraInstanceBinding;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pivotal.cf.broker.model.ServiceInstance;
import com.pivotal.cf.broker.model.ServiceInstanceBinding;

public class DataConverter {
  private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static ServiceInstance convertToServiceInstance(CassandraInstance cassandraInstance) {
    if (cassandraInstance == null) {
      return null;
    }
    return new ServiceInstance(
            cassandraInstance.getServiceInstanceId(),
            cassandraInstance.getServiceDefinitionId(),
            cassandraInstance.getPlanId(),
            cassandraInstance.getOrganizationGuid(),
            cassandraInstance.getSpaceGuid(),
            cassandraInstance.getDashboardUrl());
  }

  public static List<ServiceInstance> convertToServiceInstances(List<CassandraInstance> cassandraInstances) {
    List<ServiceInstance> serviceInstances = new ArrayList<ServiceInstance>();
    for (CassandraInstance provisionInstance : cassandraInstances) {
      serviceInstances.add(convertToServiceInstance(provisionInstance));
    }
    return serviceInstances;
  }

  public static ServiceInstanceBinding convertToServiceInstanceBinding(CassandraInstanceBinding cassandraInstanceBinding) {
    return new ServiceInstanceBinding(
            cassandraInstanceBinding.getBindingId(),
            cassandraInstanceBinding.getServiceInstanceId(),
            cassandraInstanceBinding.getCredentials(),
            cassandraInstanceBinding.getSyslogDrainUrl(),
            cassandraInstanceBinding.getAppGuid());
  }
}
