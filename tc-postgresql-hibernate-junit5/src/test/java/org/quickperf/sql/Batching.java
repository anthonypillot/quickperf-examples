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

import org.junit.jupiter.api.Test;
import org.quickperf.annotation.MeasureExecutionTime;
import org.quickperf.jvm.annotations.MeasureHeapAllocation;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class Batching extends PostgreSqlTest {

    @MeasureExecutionTime
    @MeasureHeapAllocation
    //@ProfileJvm
    @Test
    void execute_insert_without_batching_one_insert() throws SQLException {

        PreparedStatement playerStatement = connection.prepareStatement("INSERT INTO PLAYER VALUES"
                + "(?, ?, ?)");

        playerStatement.setLong(1, 1);
        playerStatement.setString(2, "LAST NAME " + "GRIEZMANN");
        playerStatement.setString(3, "TEAM " + 1);
        playerStatement.execute();

        PreparedStatement teamStatement = connection.prepareStatement("INSERT INTO TEAM VALUES (" + "?" + ",?)");
        teamStatement.setLong(1, 1);
        teamStatement.setString(2, "TEAM " + "FRANCE");
        teamStatement.execute();
    }

    @MeasureExecutionTime
    @MeasureHeapAllocation
    //@ProfileJvm
    @Test
    void execute_insert_without_batching() throws SQLException {
        PreparedStatement playerStatement = connection.prepareStatement("INSERT INTO PLAYER VALUES"
                + "(?, ?, ?)");

        List<String> lastNames = Arrays.asList("POGBA", "GRIEZMANN", "GIROUD", "PAVARD", "MARTIAL", "KANTÉ", "MBAPPÉ",
                "LLORIS", "RABIOT", "VARANE", "FEKIR", "DIGNE", "THAUVIN", "LEMAR", "TOLISSO", "HERNANDEZ", "COMAN",
                "UPAMECANO", "MATUIDI", "AOUAR");

        int lastNameIndex = 0;

        for (int i = 1; i <= 10_000; i++) {
            playerStatement.setLong(1, i);
            playerStatement.setString(2, "LAST NAME " + lastNames.get(lastNameIndex));

            lastNameIndex++;

            if (lastNameIndex > lastNames.size() - 1) {
                lastNameIndex = 0;
            }

            playerStatement.setString(3, String.valueOf(i));

            playerStatement.execute();
        }

        PreparedStatement teamStatement = connection.prepareStatement("INSERT INTO TEAM VALUES (" + "?" + ",?)");

        List<String> teamNames = Arrays.asList("FRANCE", "GERMANY", "GREECE", "AUSTRIA", "FINLAND", "PORTUGAL", "SPAIN",
                "SWEDEN", "SLOVAKIA", "LUXEMBOURG");

        int teamNameIndex = 0;

        for (int i = 1; i <= 10_000; i++) {

            teamStatement.setLong(1, i);
            teamStatement.setString(2, "TEAM " + teamNames.get(teamNameIndex));

            teamNameIndex++;

            if (teamNameIndex > teamNames.size() - 1) {
                teamNameIndex = 0;
            }
            teamStatement.execute();
        }
    }

    @MeasureExecutionTime
    @MeasureHeapAllocation
    //@ProfileJvm
    @Test
    void execute_insert_with_batching() throws SQLException {
        PreparedStatement playerStatement = connection.prepareStatement("INSERT INTO PLAYER VALUES"
                + "(?, ?, ?)");

        int playerCount = 0;

        List<String> lastNames = Arrays.asList("POGBA", "GRIEZMANN", "GIROUD", "PAVARD", "MARTIAL", "KANTÉ", "MBAPPÉ",
                "LLORIS", "RABIOT", "VARANE", "FEKIR", "DIGNE", "THAUVIN", "LEMAR", "TOLISSO", "HERNANDEZ", "COMAN",
                "UPAMECANO", "MATUIDI", "AOUAR");

        int lastNameIndex = 0;

        for (int i = 1; i <= 100_000; i++) {
            playerCount++;
            playerStatement.setLong(1, i);
            playerStatement.setString(2, "LAST NAME " + lastNames.get(lastNameIndex));

            lastNameIndex++;

            if (lastNameIndex > lastNames.size() - 1) {
                lastNameIndex = 0;
            }

            playerStatement.setString(3, String.valueOf(i));

            playerStatement.addBatch();

            if (playerCount % 50 == 0) {
                playerStatement.executeBatch();
            }
        }
        playerStatement.executeBatch();

        PreparedStatement teamStatement = connection.prepareStatement("INSERT INTO TEAM VALUES (" + "?" + ",?)");
        int teamCount = 0;

        List<String> teamNames = Arrays.asList("FRANCE", "GERMANY", "GREECE", "AUSTRIA", "FINLAND", "PORTUGAL", "SPAIN",
                "SWEDEN", "SLOVAKIA", "LUXEMBOURG");

        int teamNameIndex = 0;

        for (int i = 1; i <= 100_000; i++) {

            teamStatement.setLong(1, i);
            teamStatement.setString(2, "TEAM " + teamNames.get(teamNameIndex));

            teamNameIndex++;

            if (teamNameIndex > teamNames.size() - 1) {
                teamNameIndex = 0;
            }

            teamStatement.addBatch();

            teamCount++;
            if (teamCount % 50 == 0) {
                teamStatement.executeBatch();
            }
        }
        teamStatement.executeBatch();
    }

}
