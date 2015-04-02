package com.emc.cf.broker.cassandra.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SSLOptions;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.emc.cf.broker.cassandra.exception.CassandraServiceException;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;

@Service
public class CassandraAdminService {
  private static final Logger LOGGER = LoggerFactory.getLogger(CassandraAdminService.class);

  private Cluster cluster;
  private Builder builder = Cluster.builder();
  private Session session;
  private Boolean withSSL = false;

  private void connect(String seeds) {
    if (getWithSSL()) {
      LOGGER.info("SSL mode enabled");
	  try {
        SSLOptions sslOptions = new SSLOptions(SSLContext.getDefault(), CIPHERS);
        builder = Cluster.builder().withSSL(sslOptions);
      } catch (NoSuchAlgorithmException e) {
        LOGGER.error("Unable to setup SSL Options for Cassandra");
      }
    }

    String[] contactPoints = seeds.split(",");

    for (String contactPoint : contactPoints) {
      LOGGER.info("Adding Cassandra contact point " + contactPoint);
      builder.addContactPoints(contactPoint);
    }

    cluster = builder.build();
    Metadata metadata = cluster.getMetadata();
    for (Host host : metadata.getAllHosts()) {
      LOGGER.info("Datacenter "+ host.getDatacenter() + "Host " + host.getAddress() + "Rack " + host.getRack());
      session = cluster.connect();
    }

  }

  private void close() {
    cluster.close();
  }

  private String createKeySpace() {
    String keySpace= "ngis_" + UUID.randomUUID().toString().replace("-", "");
    LOGGER.info("Creating KeySpace " + keySpace);
    session.execute("CREATE KEYSPACE " + keySpace + " WITH REPLICATION "
            + "= {'class':'NetworkTopologyStrategy', 'DC1': 3};");

    return keySpace;
  }

  private void deleteKeySpace(String keySpace) throws CassandraServiceException {
    LOGGER.info("Deleting KeySpace " + keySpace);
    try {
      session.execute("DROP KEYSPACE " + keySpace);
    } catch (InvalidQueryException e) {
      throw new CassandraServiceException("keyspace " + keySpace + " doesn't exists", e);
    }
  }

  public String createKeySpaceForService(String seeds) {
    connect(seeds);
    String keySpace = createKeySpace();
    close();

    return keySpace;
  }

  public String deleteServiceKeySpace(String seeds, String keySpace) throws CassandraServiceException {
    connect(seeds);
    try {
      deleteKeySpace(keySpace);
    } catch (CassandraServiceException e) {
      LOGGER.info("Keyspace not found, force delete service metadata." + e);
    }
    close();
    return keySpace;
  }

  public Boolean getWithSSL() {
    return withSSL;
  }
  public final static String[] CIPHERS = {
    "TLS_RSA_WITH_AES_128_CBC_SHA", "TLS_RSA_WITH_AES_256_CBC_SHA",
    "TLS_DHE_RSA_WITH_AES_128_CBC_SHA", "TLS_DHE_RSA_WITH_AES_256_CBC_SHA",
    "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA"
  };

  public void setWithSSL(Boolean flag) {
    this.withSSL = flag;
  }
}
