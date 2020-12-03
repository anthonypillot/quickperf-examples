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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.Query;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectIn extends PostgreSqlTest {

    @BeforeEach
    public void before() throws SQLException {

        int batchSize = 50;

        List<Long> idsTeamList = insertTeams(1000, batchSize);
        insertPlayers(1000, idsTeamList, batchSize);
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

}