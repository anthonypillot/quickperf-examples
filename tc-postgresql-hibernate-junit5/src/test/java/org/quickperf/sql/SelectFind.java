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
import org.quickperf.annotation.MeasureExecutionTime;

import javax.persistence.TypedQuery;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SelectFind extends PostgreSqlTest{

    int playerNumber = 1000;

    @BeforeEach
    public void before() throws SQLException {
        int batchSize = 25;
        List<Long> teamIds = insertTeams(10, batchSize);
        insertPlayers(playerNumber, teamIds, batchSize);
    }

    @MeasureExecutionTime
    @Test
    public void select_find() {

        TypedQuery<Player> fromPlayer = entityManager.createQuery("FROM Player", Player.class);

        fromPlayer.getResultList();
    }

    @MeasureExecutionTime
    @Test
    public void several_select_find() {

        TypedQuery<Player> fromPlayer = entityManager.createQuery("FROM Player WHERE ID=:id", Player.class);

        List<Player> players = new ArrayList<>();

        for (int i = 1; i < playerNumber; i++) {
            fromPlayer.setParameter("id", i);
            Player player = fromPlayer.getSingleResult();
            players.add(player);
        }
    }

}