package org.quickperf.sql;

import football.entity.Player;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.hibernate.internal.SessionImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quickperf.annotation.MeasureExecutionTime;
import org.quickperf.junit5.QuickPerfTest;
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
import java.util.Arrays;
import java.util.List;

import static org.quickperf.sql.config.HibernateEntityManagerBuilder.anHibernateEntityManager;
import static org.quickperf.sql.config.TestDataSourceBuilder.aDataSource;

@Testcontainers
@QuickPerfTest
public class NPlusOneSelect {

    @Container
    static final PostgreSQLContainer db =
            new PostgreSQLContainer<>("postgres:12.3")
                    .withDatabaseName("testcontainers")
                    .withUsername("nes")
                    .withPassword("quick");
    private final Connection connection;

    @BeforeEach
    public void before() throws SQLException {

        int batchSize = 50;

        insertTeams(1000, batchSize);
        insertPlayers(1000, batchSize);
    }

    private void insertPlayers(int playerNumber, int batchSize) throws SQLException {
        PreparedStatement playerStatement = connection.prepareStatement("INSERT INTO PLAYER VALUES"
                + "(?, ?, ?, ?)");

        int playerCount = 0;

        List<String> lastNamesList = Arrays.asList("POGBA", "GRIEZMANN", "GIROUD", "PAVARD", "MARTIAL", "KANTÉ", "MBAPPÉ",
                "LLORIS", "RABIOT", "VARANE", "FEKIR", "DIGNE", "THAUVIN", "LEMAR", "TOLISSO", "HERNANDEZ", "COMAN",
                "UPAMECANO", "MATUIDI", "AOUAR");

        List<String> firstNamesList = Arrays.asList("PAUL", "ANTOINE", "OLIVIER", "BENJAMIN", "ANTHONY", "NGOLO", "KYLIAN",
                "HUGO", "ADRIEN", "RAPHAEL", "NABIL", "LUCAS", "FLORIAN", "THOMAS", "CORENTIN", "LUCAS", "KINGSLEY",
                "DAYOT", "BLAISE", "HOUSSEM");

        int lastNameIndex = 0;
        int firstNameIndex = 0;
        int idsTeamListIndex = 0;

        for (int i = 0; i <= playerNumber; i++) {
            playerCount++;

            playerStatement.setLong(1, i);
            playerStatement.setString(2, "LAST NAME " + lastNamesList.get(lastNameIndex));
            playerStatement.setString(3, "FIRST NAME " + firstNamesList.get(firstNameIndex));
            playerStatement.setLong(4, 5);

            lastNameIndex++;
            firstNameIndex++;
            idsTeamListIndex++;

            if (lastNameIndex > lastNamesList.size() - 1) {
                lastNameIndex = 0;
            }

            if (firstNameIndex > firstNamesList.size() - 1) {
                firstNameIndex = 0;
            }

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

    @Test
    @MeasureExecutionTime
    //@ExpectSelect(1)
    //@DisplaySqlOfTestMethodBody
    public void n_plus_one_select() {
        String hql = "FROM Player";
        Query query = entityManager.createQuery(hql, Player.class);
        query.getResultList();
    }

    private final EntityManager entityManager;

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
