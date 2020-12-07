package org.quickperf.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestData {

    private TestData() {
    }

    public static List<Long> insertTeams(Connection connection, int teamNumber, int batchSize) throws SQLException {
        PreparedStatement teamStatement = connection.prepareStatement("INSERT INTO TEAM VALUES (" + "?" + ",?)");
        int teamCount = 0;

        List<String> teamNames = Arrays.asList("FRANCE", "GERMANY", "GREECE", "AUSTRIA", "FINLAND", "PORTUGAL", "SPAIN",
                "SWEDEN", "SLOVAKIA", "LUXEMBOURG");

        int teamNameIndex = 0;

        List<Long> teamIds = new ArrayList<>();

        for (long i = 1; i <= teamNumber; i++) {

            teamIds.add(i);

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
        return teamIds;
    }

    public static void insertPlayers(Connection connection, int playerNumber, List<Long> idsTeamList, int batchSize) throws SQLException {
        PreparedStatement playerStatement = connection.prepareStatement("INSERT INTO PLAYER VALUES"
                + "(?, ?, ?, ?, ?, ?)");

        int playerCount = 0;

        List<String> lastNamesList = Arrays.asList("POGBA", "GRIEZMANN", "GIROUD", "PAVARD", "MARTIAL", "KANTÉ", "MBAPPÉ",
                "LLORIS", "RABIOT", "VARANE", "FEKIR", "DIGNE", "THAUVIN", "LEMAR", "TOLISSO", "HERNANDEZ", "COMAN",
                "UPAMECANO", "MATUIDI", "AOUAR");

        List<String> firstNamesList = Arrays.asList("PAUL", "ANTOINE", "OLIVIER", "BENJAMIN", "ANTHONY", "NGOLO", "KYLIAN",
                "HUGO", "ADRIEN", "RAPHAEL", "NABIL", "LUCAS", "FLORIAN", "THOMAS", "CORENTIN", "LUCAS", "KINGSLEY",
                "DAYOT", "BLAISE", "HOUSSEM");

        List<Integer> birthDayList = Arrays.asList(1980, 1981, 1982, 1983, 1984, 1985, 1986, 1987, 1988, 1989, 1990, 1991,
                1992, 1993, 1994, 1995, 1996, 1997, 1998, 1999, 2000, 2001, 2002, 2003);

        List<Integer> clubEntryDateList = Arrays.asList(1997, 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007,
                2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020);

        int lastNameIndex = 0;
        int firstNameIndex = 0;
        int birthDayIndex = 0;
        int clubEntryDateIndex = 0;
        int idsTeamListIndex = 0;

        ArrayDeque<Long> teamIdStack = new ArrayDeque<>(idsTeamList);

        for (int i = 0; i <= playerNumber; i++) {
            if (teamIdStack.isEmpty()) {
                teamIdStack = new ArrayDeque<>(idsTeamList);
            }

            Long teamId = teamIdStack.poll();

            playerCount++;

            idsTeamListIndex++;
            playerStatement.setLong(1, i);
            playerStatement.setString(2, "LAST NAME " + lastNamesList.get(lastNameIndex));
            playerStatement.setString(3, "FIRST NAME " + firstNamesList.get(firstNameIndex));
            playerStatement.setInt(4, birthDayList.get(birthDayIndex));
            playerStatement.setInt(5, clubEntryDateList.get(clubEntryDateIndex));
            playerStatement.setLong(6, teamId);

            lastNameIndex++;
            firstNameIndex++;
            birthDayIndex++;
            clubEntryDateIndex++;

            if (lastNameIndex > lastNamesList.size() - 1) {
                lastNameIndex = 0;
            }

            if (firstNameIndex > firstNamesList.size() - 1) {
                firstNameIndex = 0;
            }

            if (birthDayIndex > birthDayList.size() - 1) {
                birthDayIndex = 0;
            }

            if (clubEntryDateIndex > clubEntryDateList.size() - 1) {
                clubEntryDateIndex = 0;
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
}
