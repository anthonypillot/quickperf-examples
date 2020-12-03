package org.quickperf.sql;

import football.entity.Player;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.hibernate.internal.SessionImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quickperf.annotation.MeasureExecutionTime;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.jvm.jfr.annotation.ProfileJvm;
import org.quickperf.sql.annotation.ExpectSelect;
import org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.quickperf.sql.config.HibernateEntityManagerBuilder.anHibernateEntityManager;
import static org.quickperf.sql.config.TestDataSourceBuilder.aDataSource;

//@org.quickperf.sql.annotation.DisplaySql
public class NPlusOneSelect extends PostgreSqlTest {

    @BeforeEach
    public void before() throws SQLException {

        int batchSize = 50;

        List<Long> teamIds = insertTeams(1000, batchSize);
        insertPlayers(1000, teamIds, batchSize);
    }

    @Test
    @MeasureExecutionTime
    //@ExpectSelect(1)
    //@DisplaySqlOfTestMethodBody
    public void n_plus_one_select() {
        Query query = entityManager.createQuery("FROM Player", Player.class);
        query.getResultList();
    }

}
