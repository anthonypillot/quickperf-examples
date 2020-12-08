package org.quickperf.sql;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.quickperf.annotation.MeasureExecutionTime;
import org.quickperf.sql.annotation.DisplaySqlOfTestMethodBody;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class IndexOnlyScan extends PostgreSqlTest2 {

    @BeforeAll
    public static void before() throws SQLException {
        int batchSize = 50;

        List<Long> idsTeamList = TestData.insertTeams(connection, 100_000, batchSize);
        TestData.insertPlayers(connection, 100_000, idsTeamList, batchSize);

        update_id();
        create_index();
    }

    private static void create_index() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("CREATE INDEX index_birthday ON player (birthday, clubentrydate)");
        preparedStatement.execute();

//        preparedStatement = connection.prepareStatement("CREATE INDEX index_clubentrydate ON player (clubentrydate);");
//        preparedStatement.execute();
    }

    public static void update_id() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE PLAYER SET id = '150000' WHERE id = '95000'");
        preparedStatement.executeUpdate();
    }

    @DisplaySqlOfTestMethodBody
    @MeasureExecutionTime
    @Test
    public void select_with_star() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT *" +
                " FROM PLAYER WHERE birthday = 2000 AND clubentrydate = 2018");
        preparedStatement.execute();
    }

    @DisplaySqlOfTestMethodBody
    @MeasureExecutionTime
    @Test
    public void select_with_specific_column() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT player.birthday , player.clubentrydate" +
                " FROM PLAYER WHERE birthday = 2000 AND clubentrydate = 2018");
        preparedStatement.execute();
    }

    @Test
    public void select_with_star_explain() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("EXPLAIN ANALYZE SELECT *" +
                " FROM PLAYER WHERE birthday = 2000");

        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            System.out.println(resultSet.getString(1));
        }
    }

    @Test
    public void select_with_specific_column_explain() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("EXPLAIN ANALYZE SELECT player.birthday , player.clubentrydate" +
                " FROM PLAYER WHERE birthday = 2000");

        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            System.out.println(resultSet.getString(1));
        }
    }
}
