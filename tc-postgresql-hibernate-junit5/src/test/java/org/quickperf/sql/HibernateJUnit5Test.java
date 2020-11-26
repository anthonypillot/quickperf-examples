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

import football.dto.PlayerWithTeamName;
import football.entity.Player;
import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.junit.jupiter.api.Test;
import org.quickperf.annotation.DisableGlobalAnnotations;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.sql.annotation.DisplaySqlOfTestMethodBody;
import org.quickperf.sql.annotation.ExpectSelect;
import org.quickperf.sql.config.QuickPerfSqlDataSourceBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quickperf.sql.config.HibernateEntityManagerBuilder.anHibernateEntityManager;
import static org.quickperf.sql.config.TestDataSourceBuilder.aDataSource;

@Testcontainers
@QuickPerfTest
public class HibernateJUnit5Test {

    @Container
    static final PostgreSQLContainer db =
            new PostgreSQLContainer<>("postgres:12.3")
            .withDatabaseName("testcontainers")
            .withUsername("nes")
            .withPassword("quick");

    @ExpectSelect(3)
    @DisplaySqlOfTestMethodBody
    @Test
    public void should_find_all_players() {

            final TypedQuery<Player> fromPlayer = entityManager.createQuery("FROM Player", Player.class);

            final List<Player> players = fromPlayer.getResultList();

        System.out.println("\n--- TESTING CONSOLE ---\n");
        System.out.println("Each players in the list:");
        players.forEach((result) -> {
            System.out.println(result);
        });
            assertThat(players).hasSize(4);
    }

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
    }

}
