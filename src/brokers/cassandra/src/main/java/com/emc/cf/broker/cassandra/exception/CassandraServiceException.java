package com.emc.cf.broker.cassandra.exception;

import com.pivotal.cf.broker.exception.ServiceBrokerException;

public class CassandraServiceException extends ServiceBrokerException {

  private static final long serialVersionUID = 8667141725171626000L;

  public CassandraServiceException(String message) {
    super(message);
  }

  public CassandraServiceException(String message, Throwable t) {
    super(message);
    initCause(t);
  }

}
