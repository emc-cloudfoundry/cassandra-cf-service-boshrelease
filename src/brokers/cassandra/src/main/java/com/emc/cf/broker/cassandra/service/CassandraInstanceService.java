package com.emc.cf.broker.cassandra.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.emc.cf.broker.cassandra.config.AppConfig;
import com.emc.cf.broker.cassandra.data.CassandraInstance;
import com.emc.cf.broker.cassandra.data.CassandraInstanceRepository;
import com.emc.cf.broker.cassandra.exception.CassandraServiceException;
import com.emc.cf.broker.cassandra.util.DataConverter;
import com.pivotal.cf.broker.exception.ServiceBrokerException;
import com.pivotal.cf.broker.exception.ServiceInstanceExistsException;
import com.pivotal.cf.broker.model.ServiceDefinition;
import com.pivotal.cf.broker.model.ServiceInstance;
import com.pivotal.cf.broker.service.ServiceInstanceService;

@Service
public class CassandraInstanceService implements ServiceInstanceService {

  private static final String DEVELOPER_PLAN_ID = "0cd54ad0-dcf5-developer-0800200c9a66";
  private static final Logger LOGGER = LoggerFactory.getLogger(CassandraInstanceService.class);

  private CassandraAdminService cassandraAdminService;
  private CassandraInstanceRepository cassandraInstanceRepository;

  @Autowired
  AppConfig appConfig;

  @Autowired
  public CassandraInstanceService(CassandraAdminService cassandra, CassandraInstanceRepository repository) {
    this.cassandraAdminService = cassandra;
    this.cassandraInstanceRepository = repository;
  }

  @Override
  public List<ServiceInstance> getAllServiceInstances() {
    return DataConverter.convertToServiceInstances(cassandraInstanceRepository.findAll());
  }

  @Override
  public ServiceInstance createServiceInstance(ServiceDefinition service,
                                               String serviceInstanceId,
                                               String planId,
                                               String organizationGuid,
                                               String spaceGuid)
          throws ServiceInstanceExistsException, ServiceBrokerException {
    LOGGER.info("creating serviceInstance: {}", serviceInstanceId);

    CassandraInstance instance = cassandraInstanceRepository.findOneByServiceInstanceId(serviceInstanceId);
    if (instance != null) {
      throw new ServiceInstanceExistsException(DataConverter.convertToServiceInstance(instance));
    }

    String seeds = appConfig.getSeeds();
    cassandraAdminService.setWithSSL(Boolean.parseBoolean(appConfig.getWithSSL()));
    String keySpace = cassandraAdminService.createKeySpaceForService(seeds);

    LOGGER.info("KeySpace " + keySpace + " created");

    instance = new CassandraInstance();
    instance.setServiceDefinitionId(service.getId());
    instance.setServiceInstanceId(serviceInstanceId);
    instance.setPlanId(planId);
    instance.setOrganizationGuid(organizationGuid);
    instance.setSpaceGuid(spaceGuid);
    instance.setKeySpace(keySpace);
    instance.setSeeds(seeds);

    cassandraInstanceRepository.saveAndFlush(instance);
    return DataConverter.convertToServiceInstance(instance);

  }

  @Override
  public ServiceInstance getServiceInstance(String id) {
    LOGGER.info("get for serviceInstance: {}", id);
    return DataConverter.convertToServiceInstance(
            cassandraInstanceRepository.findOneByServiceInstanceId(id));
  }

  @Override
  public ServiceInstance deleteServiceInstance(String id) throws CassandraServiceException {
    LOGGER.info("Deleting serviceInstance: {}", id);

    CassandraInstance cassandraInstance = cassandraInstanceRepository.findOneByServiceInstanceId(id);

    if (cassandraInstance == null) {
      LOGGER.info("No instance found for serviceId: {} ", id);
      // If instance not found we still return empty instance so that
      // Cloud Controller can delete the service meta data from its database and deletion succeeds
      cassandraInstance = new CassandraInstance();
      return DataConverter.convertToServiceInstance(cassandraInstance);
    }

    if (DEVELOPER_PLAN_ID.equals(cassandraInstance.getPlanId())) {
      String keySpace = cassandraAdminService.deleteServiceKeySpace(cassandraInstance.getSeeds(), cassandraInstance.getKeySpace());
      LOGGER.info("KeySpace " + keySpace + " deleted");
    }
    cassandraInstanceRepository.delete(cassandraInstance.getId());

    return DataConverter.convertToServiceInstance(cassandraInstance);
  }
}
