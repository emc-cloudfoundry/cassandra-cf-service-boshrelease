package com.emc.cf.broker.cassandra.data;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

import org.h2.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.support.ResourceTransactionManager;

import com.emc.cf.broker.cassandra.config.AppConfig;

@EnableJpaRepositories(basePackages = {"com.pivotal.cf.broker.model", "com.emc.cf.broker.cassandra.data"})
@Configuration
public class DataConfiguration {
  @Value("${jdbc.driverClassName:org.postgresql.Driver}")
  private String driverClassName;

  @Value("${hibernate.dialect:org.hibernate.dialect.H2Dialect}")
  private String hibernateDialect;

  @Value("${hibernate.show_sql:false}")
  private String hibernateShowSql;

  @Value("${hibernate.hbm2ddl.auto:update}")
  private String hibernateHbm2ddlAuto;

  @Value("${jpa.persistenceUnitName:persistenceUnit}")
  private String jpaPersistenceUnitName;

  @Value("${javaxPersistenceValidationMode:ddl}")
  private String javaxPersistenceValidationMode;

  @Bean
  @Autowired
  public DataSource dataSource(AppConfig appConfig) {
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName(Driver.class.getName());
    dataSource.setUrl(String.format("jdbc:h2:file:%s/cassandra_node", appConfig.getProvisionStore()));
    dataSource.setUsername(appConfig.getProvisionStoreUsername());
    dataSource.setUsername(appConfig.getProvisionStorePassword());
    return dataSource;
  }

  @Bean
  @Autowired
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
    LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
    emf.setPersistenceUnitName(jpaPersistenceUnitName);
    emf.setDataSource(dataSource);
    emf.setJpaProperties(hibernateProperties());
    return emf;
  }

  @Bean
  @Autowired
  public ResourceTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
    return new JpaTransactionManager(entityManagerFactory);
  }

  public Properties hibernateProperties() {
    Properties properties = new Properties();
    properties.put("hibernate.dialect", hibernateDialect);
    properties.put("hibernate.show_sql", hibernateShowSql);
    properties.put("hibernate.hbm2ddl.auto", hibernateHbm2ddlAuto);
    properties.put("javax.persistence.verification.mode", javaxPersistenceValidationMode);
    properties.put("hibernate.ejb.naming_strategy", org.hibernate.cfg.ImprovedNamingStrategy.class.getName());
    return properties;
  }
}
