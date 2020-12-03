package org.quickperf.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quickperf.annotation.MeasureExecutionTime;
import org.quickperf.jvm.annotations.MeasureHeapAllocation;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class CompareSelectStatement extends PostgreSqlTest {

    @BeforeEach
    public void before() throws SQLException {

        int batchSize = 50;

        List<Long> idsTeamList = insertTeams(100_000, batchSize);
        insertPlayers(100_000, idsTeamList, batchSize);
    }

    private final int executionCount = 1000;
    @MeasureExecutionTime
    @MeasureHeapAllocation
    @Test
    public void execute_several_select_with_statement() throws SQLException {

        Statement statement = connection.createStatement();

        for (int i = 0; i < executionCount; i++) {
            statement.execute("SELECT * FROM Player WHERE id = 1");
            statement.execute("SELECT * FROM Player WHERE id = 2");
            statement.execute("SELECT * FROM Player WHERE id = 3");
            statement.execute("SELECT * FROM Player WHERE id = 4");
            statement.execute("SELECT * FROM Player WHERE id = 5");
            statement.execute("SELECT * FROM Player WHERE id = 6");
            statement.execute("SELECT * FROM Player WHERE id = 7");
            statement.execute("SELECT * FROM Player WHERE id = 8");
            statement.execute("SELECT * FROM Player WHERE id = 9");
            statement.execute("SELECT * FROM Player WHERE id = 10");
        }
        statement.close();
    }

    @MeasureExecutionTime
    @MeasureHeapAllocation
    @Test
    public void execute_several_select_with_prepared_statement() throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Player WHERE id = ?");

        for (int i = 0; i < executionCount; i++) {
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
        }
        preparedStatement.close();
    }

}
