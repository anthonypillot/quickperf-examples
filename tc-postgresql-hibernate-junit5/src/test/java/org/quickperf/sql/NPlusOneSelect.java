package org.quickperf.sql;

import football.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quickperf.annotation.MeasureExecutionTime;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.List;

//@org.quickperf.sql.annotation.DisplaySql
public class NPlusOneSelect extends PostgreSqlTest {

    @BeforeEach
    public void before() throws SQLException {

        int batchSize = 50;

        List<Long> teamIds = TestData.insertTeams(connection, 1000, batchSize);
        TestData.insertPlayers(connection, 1000, teamIds, batchSize);
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
