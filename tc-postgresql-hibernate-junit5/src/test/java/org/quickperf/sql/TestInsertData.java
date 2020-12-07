import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.quickperf.sql.PostgreSqlTest2;
import org.quickperf.sql.TestData;

import java.sql.SQLException;
import java.util.List;

public class TestInsertData extends PostgreSqlTest2 {

    @BeforeAll
    public static void before() throws SQLException {
        int batchSize = 50;

        List<Long> idsTeamList = TestData.insertTeams(connection, 100_000, batchSize);
        TestData.insertPlayers(connection, 100_000, idsTeamList, batchSize);
    }

    @Test
    public void testingData() {
        
    }
}
