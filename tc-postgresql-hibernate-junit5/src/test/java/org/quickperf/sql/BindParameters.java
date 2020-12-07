package org.quickperf.sql;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.quickperf.annotation.MeasureExecutionTime;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class BindParameters extends PostgreSqlTest2 {

    @BeforeAll
    public static void before() throws SQLException {
        int batchSize = 50;
        List<Long> idsTeamList = TestData.insertTeams(connection, 10_000, batchSize);
        TestData.insertPlayers(connection, 10_000, idsTeamList, batchSize);
    }

    @MeasureExecutionTime
    @Test
    public void execute_several_select_with_binds() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Player WHERE id = ?");

        preparedStatement.setLong(1, 1);
        preparedStatement.execute();

        preparedStatement.setLong(1, 2);
        preparedStatement.execute();

        preparedStatement.setLong(1, 3);
        preparedStatement.execute();

        preparedStatement.setLong(1, 4);
        preparedStatement.execute();

        preparedStatement.setLong(1, 5);
        preparedStatement.execute();

        preparedStatement.setLong(1, 6);
        preparedStatement.execute();

        preparedStatement.setLong(1, 7);
        preparedStatement.execute();

        preparedStatement.setLong(1, 8);
        preparedStatement.execute();

        preparedStatement.setLong(1, 9);
        preparedStatement.execute();

        preparedStatement.setLong(1, 10);
        preparedStatement.execute();

        preparedStatement.close();
    }

    @MeasureExecutionTime
    @Test
    public void execute_several_select_without_binds() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Player WHERE id = 1");
        preparedStatement.execute();

        preparedStatement = connection.prepareStatement("SELECT * FROM Player WHERE id = 2");
        preparedStatement.execute();

        preparedStatement = connection.prepareStatement("SELECT * FROM Player WHERE id = 3");
        preparedStatement.execute();

        preparedStatement = connection.prepareStatement("SELECT * FROM Player WHERE id = 4");
        preparedStatement.execute();

        preparedStatement = connection.prepareStatement("SELECT * FROM Player WHERE id = 5");
        preparedStatement.execute();

        preparedStatement = connection.prepareStatement("SELECT * FROM Player WHERE id = 6");
        preparedStatement.execute();

        preparedStatement = connection.prepareStatement("SELECT * FROM Player WHERE id = 7");
        preparedStatement.execute();

        preparedStatement = connection.prepareStatement("SELECT * FROM Player WHERE id = 8");
        preparedStatement.execute();

        preparedStatement = connection.prepareStatement("SELECT * FROM Player WHERE id = 9");
        preparedStatement.execute();

        preparedStatement = connection.prepareStatement("SELECT * FROM Player WHERE id = 10");
        preparedStatement.execute();

        preparedStatement.close();
    }

    @MeasureExecutionTime
    @Test
    public void execute_several_select_with_binds_and_iteration() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Player WHERE id = ?");

        for (int i = 0; i < 10; i++) {
            preparedStatement.setLong(1, i);
            preparedStatement.execute();
        }
        preparedStatement.close();
    }

    @MeasureExecutionTime
    @Test
    public void execute_several_select_without_binds_and_iteration() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Player WHERE id = 1");
        preparedStatement.execute();

        for (int i = 2; i <= 10; i++) {
            preparedStatement = connection.prepareStatement("SELECT * FROM Player WHERE id = " + i);
            preparedStatement.execute();
        }
        preparedStatement.close();
    }
}
