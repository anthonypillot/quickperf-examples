package org.quickperf.sql;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.quickperf.annotation.MeasureExecutionTime;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class JoinWithoutIndex extends PostgreSqlTest2 {

    /**
     * Number of the SELECT PreparedStatement execution
     **/
    int executionCount = 100;

    @BeforeAll
    public static void before() throws SQLException {

        System.out.println("JoinWithoutIndex.before");

        PreparedStatement preparedStatement = connection.prepareStatement("DROP INDEX IF EXISTS index_team_id");
        preparedStatement.execute();

        preparedStatement = connection.prepareStatement("DROP INDEX IF EXISTS index_player_team");
        preparedStatement.execute();

        int batchSize = 50;
        List<Long> idsTeamList = TestData.insertTeams(connection, 10_000, batchSize);
        TestData.insertPlayers(connection, 10_000, idsTeamList, batchSize);
    }

    /**
     * Use EXPLAIN ANALYZE from Postgres to verify that the index isn't created
     **/
    @Test
    @MeasureExecutionTime
    public void sql_query_explain_analyse() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("EXPLAIN (ANALYZE true , VERBOSE true , BUFFERS true)" +
                "SELECT p1_0.id, p1_0.firstName," +
                "p1_0.lastName, t1_0.id, t1_0.name FROM Player AS p1_0 LEFT OUTER JOIN Team AS t1_0 ON p1_0.team_id = t1_0.id");

        /** Print the EXPLAIN ANALYZE response from the PostgreSQL database **/
        ResultSet resultSet = preparedStatement.executeQuery();
        while (resultSet.next()) {
            System.out.println(resultSet.getString(1));
        }
    }

    @Test
    @MeasureExecutionTime
    public void sql_query_without_index() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT p1_0.id, p1_0.firstName, p1_0.lastName, t1_0.id, t1_0.name" +
                " FROM Player AS p1_0 LEFT OUTER JOIN Team AS t1_0 ON p1_0.team_id = t1_0.id");
        preparedStatement.execute();
    }

    @Test
    @MeasureExecutionTime
    public void several_sql_query_without_index() throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement("SELECT p1_0.id, p1_0.firstName, p1_0.lastName, t1_0.id, t1_0.name" +
                " FROM Player AS p1_0 LEFT OUTER JOIN Team AS t1_0 ON p1_0.team_id = t1_0.id");

        for (int i = 0; i < executionCount; i++) {
            preparedStatement.execute();
        }
    }
}
