package org.quickperf.sql;

import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.hibernate.internal.SessionImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quickperf.annotation.MeasureExecutionTime;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.jvm.annotations.MeasureHeapAllocation;
import org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

import static org.quickperf.sql.config.HibernateEntityManagerBuilder.anHibernateEntityManager;
import static org.quickperf.sql.config.TestDataSourceBuilder.aDataSource;

@Testcontainers
@QuickPerfTest
public class CompareSelectStatement {

    @Container
    static final PostgreSQLContainer db =
            new PostgreSQLContainer<>("postgres:12.3")
                    .withDatabaseName("testcontainers")
                    .withUsername("nes")
                    .withPassword("quick");
    private Connection connection;

    @BeforeEach
    public void before() throws SQLException {

        int batchSize = 50;

        insertTeams(100_000, batchSize);
        insertPlayers(100_000, batchSize);
    }

    private int executionCount = 1000;
    private String query = "SELECT * FROM Player WHERE id = 10";

    @MeasureExecutionTime
    @MeasureHeapAllocation
    @Test
    public void execute_several_select_with_statement() throws SQLException {

        Statement statement = connection.createStatement();

        for (int i = 0; i < executionCount; i++) {
            statement.execute(query);
        }
        statement.close();
    }

    @MeasureExecutionTime
    @MeasureHeapAllocation
    @Test
    public void execute_several_select_with_prepared_statement() throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement(query);

        for (int i = 0; i < executionCount; i++) {
            preparedStatement.execute();
        }
        preparedStatement.close();
    }

    private void insertPlayers(int playerNumber, int batchSize) throws SQLException {
        PreparedStatement playerStatement = connection.prepareStatement("INSERT INTO PLAYER VALUES"
                + "(?, ?, ?)");

        int playerCount = 0;

        List<String> lastNames = Arrays.asList("POGBA", "GRIEZMANN", "GIROUD", "PAVARD", "MARTIAL", "KANTÉ", "MBAPPÉ",
                "LLORIS", "RABIOT", "VARANE", "FEKIR", "DIGNE", "THAUVIN", "LEMAR", "TOLISSO", "HERNANDEZ", "COMAN",
                "UPAMECANO", "MATUIDI", "AOUAR");

        int lastNameIndex = 0;

        for (int i = 1; i <= playerNumber; i++) {
            playerCount++;
            playerStatement.setLong(1, i);
            playerStatement.setString(2, "LAST NAME " + lastNames.get(lastNameIndex));

            lastNameIndex++;

            if (lastNameIndex > lastNames.size() - 1) {
                lastNameIndex = 0;
            }

            playerStatement.setString(3, String.valueOf(i));

            playerStatement.addBatch();

            if (playerCount % batchSize == 0) {
                playerStatement.executeBatch();
            }
        }
        playerStatement.executeBatch();
    }

    private void insertTeams(int teamNumber, int batchSize) throws SQLException {
        PreparedStatement teamStatement = connection.prepareStatement("INSERT INTO TEAM VALUES (" + "?" + ",?)");
        int teamCount = 0;

        List<String> teamNames = Arrays.asList("FRANCE", "GERMANY", "GREECE", "AUSTRIA", "FINLAND", "PORTUGAL", "SPAIN",
                "SWEDEN", "SLOVAKIA", "LUXEMBOURG");

        int teamNameIndex = 0;

        for (int i = 1; i <= teamNumber; i++) {

            teamStatement.setLong(1, i);
            teamStatement.setString(2, "TEAM " + teamNames.get(teamNameIndex));

            teamNameIndex++;

            if (teamNameIndex > teamNames.size() - 1) {
                teamNameIndex = 0;
            }

            teamStatement.addBatch();

            teamCount++;
            if (teamCount % batchSize == 0) {
                teamStatement.executeBatch();
            }
        }
        teamStatement.executeBatch();
    }


    // -------------------------------------------------------------------------------------

    private EntityManager entityManager;

    {
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
}
