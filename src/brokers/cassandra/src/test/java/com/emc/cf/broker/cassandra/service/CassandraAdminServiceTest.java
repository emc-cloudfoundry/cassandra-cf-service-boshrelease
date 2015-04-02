package com.emc.cf.broker.cassandra.service;

import org.junit.Test;

public class CassandraAdminServiceTest {

  @Test
  public void testCreateKeySpaceForService() {
    CassandraAdminService cassandraAdminService = new CassandraAdminService();
    cassandraAdminService.setWithSSL(false);
    cassandraAdminService.createKeySpaceForService("10.8.9.80");
  }

  @Test
  public void testDeleteServiceKeySpace() throws Exception {
    CassandraAdminService cassandraAdminService = new CassandraAdminService();
    cassandraAdminService.deleteServiceKeySpace("10.8.9.80", "ngis_c67f2be14e3442bbac267333fae0ca1e");
  }
}
