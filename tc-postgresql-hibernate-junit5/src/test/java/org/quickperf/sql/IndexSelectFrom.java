package org.quickperf.sql;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.quickperf.annotation.MeasureExecutionTime;
import org.quickperf.sql.annotation.DisplaySqlOfTestMethodBody;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class IndexSelectFrom extends PostgreSqlTest2 {

    @BeforeAll
    public static void before() throws SQLException {
        int batchSize = 50;

        List<Long> idsTeamList = TestData.insertTeams(connection, 100_000, batchSize);
        TestData.insertPlayers(connection, 100_000, idsTeamList, batchSize);
    }

    @Test
    @MeasureExecutionTime
    @DisplaySqlOfTestMethodBody
    public void update_id() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("UPDATE PLAYER SET id = '150000' WHERE id = '85000'");
        preparedStatement.executeUpdate();
    }

    @Test
    public void select_birthday_with_index() throws SQLException {
        // Creating INDEX:
        PreparedStatement preparedStatement = connection.prepareStatement("CREATE INDEX index_birthday_player ON player (birthday);");
        preparedStatement.execute();

        preparedStatement = connection.prepareStatement("SELECT lastname FROM Player WHERE birthday = 2003");
        preparedStatement.execute();

    }

    @Test
    public void select_birthday_without_index() throws SQLException {
        // Dropping INDEX:
        PreparedStatement preparedStatement = connection.prepareStatement("DROP INDEX IF EXISTS index_birthday_player");
        preparedStatement.execute();

        preparedStatement = connection.prepareStatement("SELECT lastname FROM Player WHERE birthday = 2003");
        preparedStatement.execute();
    }
}
