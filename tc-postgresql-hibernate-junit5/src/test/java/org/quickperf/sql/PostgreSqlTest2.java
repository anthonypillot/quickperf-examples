package org.quickperf.sql;

import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.hibernate.internal.SessionImpl;
import org.junit.jupiter.api.AfterAll;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.sql.Connection;

import static org.quickperf.sql.config.HibernateEntityManagerBuilder.anHibernateEntityManager;
import static org.quickperf.sql.config.TestDataSourceBuilder.aDataSource;

@Testcontainers
@QuickPerfTest
public class PostgreSqlTest2 {

    static final PostgreSQLContainer db =
            new PostgreSQLContainer<>("postgres:12.3")
                    .withDatabaseName("testcontainers")
                    .withUsername("nes")
                    .withPassword("quick");

    protected static final Connection connection;

    protected static final EntityManager entityManager;

    static {

        db.start();

        //db.
        final DataSource dataSource = aDataSource().build(db);

        // A data source proxy is built to allow QuickPerf to intercept the SQL
        // statements
        final ProxyDataSource proxyDataSource = QuickPerfSqlDataSourceBuilder
                .aDataSourceBuilder()
                .buildProxy(dataSource);

        entityManager = anHibernateEntityManager(proxyDataSource);

        SessionImpl session = (SessionImpl) entityManager.getDelegate();

        connection = session.connection();
    }

    @AfterAll
    public static void afterAll() {
        db.stop();
    }

}
