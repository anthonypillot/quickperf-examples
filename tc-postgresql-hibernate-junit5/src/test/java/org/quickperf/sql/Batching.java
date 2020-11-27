/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright 2020-2020 the original author or authors.
 */

package org.quickperf.sql;

import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.hibernate.internal.SessionImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quickperf.annotation.ExpectMaxExecutionTime;
import org.quickperf.annotation.MeasureExecutionTime;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.jvm.annotations.MeasureHeapAllocation;
import org.quickperf.jvm.jfr.annotation.ProfileJvm;
import org.quickperf.sql.annotation.DisplaySqlOfTestMethodBody;
import org.quickperf.sql.annotation.ExpectMaxQueryExecutionTime;
import org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
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
public class Batching {

    @Container
    static final PostgreSQLContainer db =
            new PostgreSQLContainer<>("postgres:12.3")
                    .withDatabaseName("testcontainers")
                    .withUsername("nes")
                    .withPassword("quick");
    private Connection connection;

    @BeforeEach
    public void before() throws SQLException {
        insertTeams(100_000);
        insertPlayers(100_000);
    }

    private void insertPlayers(int playerNumber) throws SQLException {
        PreparedStatement playerStatement = connection.prepareStatement("INSERT INTO PLAYER VALUES"
                + "(?, ?, ?)");

        List<String> lastNames = Arrays.asList("POGBA", "GRIEZMANN", "GIROUD", "PAVARD", "MARTIAL", "KANTÉ", "MBAPPÉ",
                "LLORIS", "RABIOT", "VARANE", "FEKIR", "DIGNE", "THAUVIN", "LEMAR", "TOLISSO", "HERNANDEZ", "COMAN",
                "UPAMECANO", "MATUIDI", "AOUAR");

        int lastNameIndex = 0;

        for (int i = 1; i <= playerNumber; i++) {
            playerStatement.setLong(1, i);
            playerStatement.setString(2, "LAST NAME " + lastNames.get(lastNameIndex));

            lastNameIndex++;

            if (lastNameIndex > lastNames.size() - 1) {
                lastNameIndex = 0;
            }

            playerStatement.setString(3, "TEAM " + i);

            playerStatement.execute();
        }
    }

    private void insertTeams(int teamNumber) throws SQLException {
        PreparedStatement teamStatement = connection.prepareStatement("INSERT INTO TEAM VALUES (" + "?" + ",?)");

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
            teamStatement.execute();
        }
    }


    //@DisableLikeWithLeadingWildcard
    @MeasureExecutionTime
    //@MeasureHeapAllocation
    //@ProfileJvm
    @Test
    void execute_insert_without_batching() throws SQLException {


    }

    @MeasureExecutionTime
    //@MeasureHeapAllocation
    //@ProfileJvm
    @Test
    void execute_insert_with_batching() throws SQLException {


    }

    @DisplaySqlOfTestMethodBody
    @ExpectMaxQueryExecutionTime(thresholdInMilliSeconds = 20)
    //@DisableLikeWithLeadingWildcard
    @Test
    void execute_long_query_with_like() throws SQLException {

        String sqlQuery = "SELECT * FROM PLAYER WHERE firstName LIKE '%ANN'";

        PreparedStatement statement = connection.prepareStatement(sqlQuery);
        statement.execute();
    }

/*
    @Test
    public void should_find_all_players_with_their_team_name() {

            final TypedQuery<Player> fromPlayer = entityManager.createQuery("FROM Player", Player.class);

            final List<Player> players = fromPlayer.getResultList();

            final List<PlayerWithTeamName> playersWithTeamName = players.stream()
                            .map(player -> new PlayerWithTeamName(player.getFirstName(), player.getLastName(),
                                            player.getTeam().getName()))
                            .collect(Collectors.toList());

            assertThat(playersWithTeamName).hasSize(2);
    }
 */

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
