package org.quickperf.sql;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.quickperf.annotation.MeasureExecutionTime;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class JoinWithIndex extends PostgreSqlTest2 {

    int executionCount = 100;

    @BeforeAll
    public static void before() throws SQLException {

        int batchSize = 50;
        System.out.println("JoinWithIndex.before");

        List<Long> idsTeamList = TestData.insertTeams(connection, 10_000, batchSize);
        TestData.insertPlayers(connection, 10_000, idsTeamList, batchSize);
    }

    @Test
    @MeasureExecutionTime
    public void sql_query_with_index() throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement("SELECT p1_0.id, p1_0.firstName, p1_0.lastName, t1_0.id, t1_0.name" +
                " FROM Player AS p1_0 LEFT OUTER JOIN Team AS t1_0 ON p1_0.team_id = t1_0.id");

        preparedStatement.execute();
    }

    @Test
    @MeasureExecutionTime
    public void sql_query_without_index() throws SQLException {

        PreparedStatement preparedStatement;

        preparedStatement = connection.prepareStatement("SELECT p1_0.id, p1_0.firstName, p1_0.lastName, t1_0.id, t1_0.name" +
                " FROM Player AS p1_0 LEFT OUTER JOIN Team AS t1_0 ON p1_0.team_id = t1_0.id");

        preparedStatement.execute();
    }

    @Test
    @MeasureExecutionTime
    public void several_sql_query_with_index() throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement("SELECT p1_0.id, p1_0.firstName, p1_0.lastName, t1_0.id, t1_0.name" +
                " FROM Player AS p1_0 LEFT OUTER JOIN Team AS t1_0 ON p1_0.team_id = t1_0.id");

        for (int i = 0; i < executionCount; i++) {
            preparedStatement.execute();
        }
    }

    @Test
    @MeasureExecutionTime
    public void several_sql_query_without_index() throws SQLException {

        PreparedStatement preparedStatement;

        preparedStatement = connection.prepareStatement("SELECT p1_0.id, p1_0.firstName, p1_0.lastName, t1_0.id, t1_0.name" +
                " FROM Player AS p1_0 LEFT OUTER JOIN Team AS t1_0 ON p1_0.team_id = t1_0.id");

        for (int i = 0; i < executionCount; i++) {
            preparedStatement.execute();
        }
    }

    //@After
    public void after() throws SQLException {

        PreparedStatement preparedStatement;

        preparedStatement = connection.prepareStatement("DROP INDEX IF EXISTS index_team_id");
        preparedStatement.execute();

        preparedStatement = connection.prepareStatement("DROP INDEX IF EXISTS index_player_team");
        preparedStatement.execute();

    }

}
