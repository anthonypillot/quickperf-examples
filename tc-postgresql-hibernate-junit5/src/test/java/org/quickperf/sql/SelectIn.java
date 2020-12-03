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

import football.entity.Player;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.hibernate.internal.SessionImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.quickperf.sql.config.HibernateEntityManagerBuilder.anHibernateEntityManager;
import static org.quickperf.sql.config.TestDataSourceBuilder.aDataSource;

@Testcontainers
@QuickPerfTest
public class SelectIn {

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

        List<Long> idsTeamList = insertTeams(1000, batchSize);
        insertPlayers(1000, idsTeamList, batchSize);
    }

    private List<Long> insertTeams(int teamNumber, int batchSize) throws SQLException {

        PreparedStatement teamStatement = connection.prepareStatement("INSERT INTO TEAM VALUES (" + "?" + ",?)", Statement.RETURN_GENERATED_KEYS);
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

        return getIdsTeamList(teamStatement);
    }

    private List<Long> getIdsTeamList(PreparedStatement teamStatement) throws SQLException {

        List<Long> idsTeamList = new ArrayList<>();
        ResultSet resultSet = teamStatement.getGeneratedKeys();

        while (resultSet != null && resultSet.next()) {
            Long id = resultSet.getLong(1);
            idsTeamList.add(id);
        }
        return idsTeamList;
    }

    private void insertPlayers(int playerNumber, List<Long> idsTeamList, int batchSize) throws SQLException {
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
            playerStatement.setLong(4, idsTeamList.get(idsTeamListIndex));

            lastNameIndex++;
            firstNameIndex++;
            idsTeamListIndex++;

            if (lastNameIndex > lastNamesList.size()) {
                lastNameIndex = 0;
            }

            if (firstNameIndex > firstNamesList.size()) {
                firstNameIndex = 0;
            }

            if (idsTeamListIndex > idsTeamList.size()) {
                idsTeamListIndex = 0;
            }

            playerStatement.addBatch();

            if (playerCount % batchSize == 0) {
                playerStatement.executeBatch();
            }
        }
        playerStatement.executeBatch();
    }

    @Test
    void should_find_all_players_with_select_in() {

        List<String> teamNamesList = Arrays.asList("FRANCE", "GERMANY");

        String hql = "FROM Player p WHERE p.team.name IN (:teamNames)";

        Query query = entityManager.createQuery(hql, Player.class);
        query.setParameter("teamNames", teamNamesList);

        query.getResultList();

        // TODO Trigger Full GC and dump the heap
        // HeapDumper.dumpHeap("large-in.hprof");
    }

    @Test
    void should_find_all_players_with_select_large_in() {

        List<Long> idsList = new ArrayList<>();

        for (long i = 0; i < 32767; i++) {
            idsList.add(i);
        }

        String parameter = "playerIds";

        String hql = "FROM Player WHERE id IN (:" + parameter + ")";

        Query query = entityManager.createQuery(hql, Player.class);
        query.setParameter(parameter, idsList);

        query.getResultList();

        // TODO Trigger Full GC and dump the heap
        // HeapDumper.dumpHeap("large-in.hprof");
    }

    // -------------------------------------------------------------------------------------

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