package com.emc.cf.broker.cassandra.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CassandraInstanceRepository extends JpaRepository<CassandraInstance, Long> {
  CassandraInstance findOneByServiceInstanceId(String serviceInstanceId);
}
