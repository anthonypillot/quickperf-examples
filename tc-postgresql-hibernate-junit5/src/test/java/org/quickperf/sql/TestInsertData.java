package org.quickperf.sql;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Testing scope: intended to verify if datas are correctly inserted.
 */
public class TestInsertData extends PostgreSqlTest2 {

    @BeforeAll
    public static void before() throws SQLException {
        int batchSize = 50;

        List<Long> idsTeamList = TestData.insertTeams(connection, 100_000, batchSize);
        TestData.insertPlayers(connection, 100_000, idsTeamList, batchSize);
    }

    @Test
    public void testingDataWithSelect() throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM PLAYER");
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            System.out.print("[");
            System.out.print(resultSet.getString(1) + " - ");
            System.out.print(resultSet.getString(2) + " - ");
            System.out.print(resultSet.getString(3) + " - ");
            System.out.print(resultSet.getString(4) + " - ");
            System.out.print(resultSet.getString(5) + " - ");
            System.out.print(resultSet.getString(6) + "]\n");
        }
    }
}
