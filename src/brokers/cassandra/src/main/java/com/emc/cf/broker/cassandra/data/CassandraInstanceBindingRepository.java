package com.emc.cf.broker.cassandra.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CassandraInstanceBindingRepository extends JpaRepository<CassandraInstanceBinding, Long> {
  CassandraInstanceBinding findOneByBindingId(String bindingId);
}
