package com.emc.cf.broker.cassandra.service;


import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.emc.cf.broker.cassandra.data.CassandraInstance;
import com.emc.cf.broker.cassandra.data.CassandraInstanceBinding;
import com.emc.cf.broker.cassandra.data.CassandraInstanceBindingRepository;
import com.emc.cf.broker.cassandra.data.CassandraInstanceRepository;
import com.emc.cf.broker.cassandra.exception.CassandraServiceException;
import com.emc.cf.broker.cassandra.util.DataConverter;
import com.pivotal.cf.broker.exception.ServiceBrokerException;
import com.pivotal.cf.broker.exception.ServiceInstanceBindingExistsException;
import com.pivotal.cf.broker.model.ServiceInstance;
import com.pivotal.cf.broker.model.ServiceInstanceBinding;
import com.pivotal.cf.broker.service.ServiceInstanceBindingService;

@Service
public class CassandraInstanceBindingService implements ServiceInstanceBindingService {

  private static final Logger LOGGER = LoggerFactory
          .getLogger(CassandraInstanceBindingService.class);

  // These string variables are only returned in credentials to support V1 service code in NGIS
  // They will be removed once NGIS remove V1 credentials structure
  private static final String CLUSTER_NAME = "IIGCluster";
  private static final String TRANSPORT_PORT = "9042";
  private static final String SSL_STORAGE_PORT = "7001";
  private static final String STORAGE_PORT = "7000";
  private static final String JMX_PORT = "7199";
  private static final String RPC_PORT = "9160";

  @Autowired
  private CassandraInstanceRepository cassandraInstanceRepository;
  private CassandraInstanceBindingRepository cassandraInstanceBindingRepository;

  @Autowired
  public CassandraInstanceBindingService(CassandraAdminService cassandra,
                                         CassandraInstanceBindingRepository repository) {
    this.cassandraInstanceBindingRepository = repository;
  }

  @Override
  public ServiceInstanceBinding createServiceInstanceBinding(
          String bindingId, ServiceInstance serviceInstance,
          String serviceId, String planId, String appGuid)
          throws ServiceInstanceBindingExistsException, ServiceBrokerException {

    LOGGER.info("binding for serviceInstance: {}, app {}: ", serviceInstance.getId(), appGuid);

    CassandraInstanceBinding binding = cassandraInstanceBindingRepository.findOneByBindingId(bindingId);
    if (binding != null) {
      throw new ServiceInstanceBindingExistsException(DataConverter.convertToServiceInstanceBinding(binding));
    }

    CassandraInstance cassandraInstance = cassandraInstanceRepository.findOneByServiceInstanceId(serviceInstance.getId());
    String seeds = cassandraInstance.getSeeds();
    String keySpace = cassandraInstance.getKeySpace();

    // When binding to service for an app is done, we put credentials information which can be
    // accessed only by the app that has bind the service.
    Map<String,String> credentials = new HashMap<String,String>();
    credentials.put("seeds", seeds);
    credentials.put("key_space", keySpace);
    credentials.put("cluster_name", CLUSTER_NAME);
    credentials.put("port", RPC_PORT);
    credentials.put("rpc_port", RPC_PORT);
    credentials.put("transport_port", TRANSPORT_PORT);
    credentials.put("storage_port", STORAGE_PORT);
    credentials.put("ssl_storage_port", SSL_STORAGE_PORT);
    credentials.put("jmx_port", JMX_PORT);

    binding= new CassandraInstanceBinding();
    binding.setBindingId(bindingId);
    binding.setServiceInstanceId(serviceInstance.getId());
    binding.setSyslogDrainUrl(null);
    binding.setAppGuid(appGuid);
    binding.setCredentials(credentials);
    cassandraInstanceBindingRepository.saveAndFlush(binding);

    return DataConverter.convertToServiceInstanceBinding(binding);
  }

  @Override
  public ServiceInstanceBinding getServiceInstanceBinding(String id) {
    return DataConverter.convertToServiceInstanceBinding(
            cassandraInstanceBindingRepository.findOneByBindingId(id));
  }

  @Override
  public ServiceInstanceBinding deleteServiceInstanceBinding(String id) throws CassandraServiceException{
    CassandraInstanceBinding cassandraInstanceBinding = cassandraInstanceBindingRepository.findOneByBindingId(id);
    if (cassandraInstanceBinding!= null) {
      cassandraInstanceBindingRepository.delete(cassandraInstanceBinding.getId());
    }
    else {
      // If instance not found we still return empty instance so that
      // Cloud Controller can delete the service meta data from its database and deletion succeeds
      cassandraInstanceBinding = new CassandraInstanceBinding();
    }

    return DataConverter.convertToServiceInstanceBinding(cassandraInstanceBinding);
  }

}
