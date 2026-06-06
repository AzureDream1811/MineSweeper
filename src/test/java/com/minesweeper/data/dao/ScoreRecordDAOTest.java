package com.minesweeper.data.dao;

import com.minesweeper.data.dao.ScoreRecordDAO.PvpMatchRow;
import com.minesweeper.data.dao.ScoreRecordDAO.SoloRankRow;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class cho ScoreRecordDAO.
 * Vị trí: src/test/java/com/minesweeper/data/dao/ScoreRecordDAOTest.java
 *
 * Dùng H2 in-memory DB thay MySQL — set System property TEST_DB_URL
 * để DBConnection tự chuyển sang H2, không cần sửa DAO.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ScoreRecordDAOTest {

    private ScoreRecordDAO dao;

    @BeforeAll
    static void setUpAll() throws SQLException {
        // Bật test mode: DBConnection sẽ dùng H2 thay MySQL
        TestDBHelper.enableTestMode();
        TestDBHelper.createSchema();
    }

    @AfterAll
    static void tearDownAll() {
        TestDBHelper.disableTestMode();
    }

    @BeforeEach
    void setUp() throws SQLException {
        dao = new ScoreRecordDAO();
        // Xoá sạch dữ liệu trước mỗi test để tránh ảnh hưởng lẫn nhau
        TestDBHelper.clearAll();
    }

    // ════════════════════════════════════════════════════════════
    // getSoloLeaderboard()
    // ════════════════════════════════════════════════════════════

    /**
     * UC11 - 11.1.3: Hệ thống truy vấn DB lấy danh sách best time,
     *                sắp xếp tăng dần theo thời gian.
     * UC11 - 11.1.4: Hiển thị bảng xếp hạng đúng thứ hạng.
     */
    @Test
    @Order(1)
    @DisplayName("getSoloLeaderboard - trả về đúng thứ hạng khi có nhiều người chơi")
    void getSoloLeaderboard_shouldReturnRankedRows_whenMultiplePlayers()
            throws SQLException {
        // Arrange
        TestDBHelper.insertSoloWin("Alice", "EASY", 42);
        TestDBHelper.insertSoloWin("Bob",   "EASY", 30);
        TestDBHelper.insertSoloWin("Carol", "EASY", 55);

        // Act
        List<SoloRankRow> result = dao.getSoloLeaderboard("EASY", 10);

        // Assert
        assertEquals(3, result.size(), "Phải có đúng 3 người chơi");

        // Thứ hạng 1 phải là Bob (thời gian thấp nhất = 30s)
        assertEquals(1,     result.get(0).rank,        "Rank 1 phải là Bob");
        assertEquals("Bob", result.get(0).playerName,  "Tên rank 1 phải là Bob");
        assertEquals(30,    result.get(0).bestSeconds, "Thời gian rank 1 phải là 30s");

        assertEquals(2,       result.get(1).rank);
        assertEquals("Alice", result.get(1).playerName);

        assertEquals(3,       result.get(2).rank);
        assertEquals("Carol", result.get(2).playerName);
    }

    /**
     * UC11 - 11.1.3: Chỉ tính lượt thắng (result = 'WIN'), bỏ qua LOSE.
     */
    @Test
    @Order(2)
    @DisplayName("getSoloLeaderboard - chỉ tính lượt WIN, bỏ qua LOSE")
    void getSoloLeaderboard_shouldExcludeLoseRecords() throws SQLException {
        // Arrange
        TestDBHelper.insertSoloWin("Alice", "EASY", 42);
        TestDBHelper.insertSoloLose("Bob",  "EASY", 99);

        // Act
        List<SoloRankRow> result = dao.getSoloLeaderboard("EASY", 10);

        // Assert
        assertEquals(1, result.size(), "Chỉ có 1 kết quả WIN, LOSE phải bị loại");
        assertEquals("Alice", result.get(0).playerName);
    }

    /**
     * UC11 - 11.1.5: Lọc đúng theo độ khó được chọn.
     */
    @Test
    @Order(3)
    @DisplayName("getSoloLeaderboard - lọc đúng theo difficulty")
    void getSoloLeaderboard_shouldFilterByDifficulty() throws SQLException {
        // Arrange
        TestDBHelper.insertSoloWin("Alice", "EASY",   42);
        TestDBHelper.insertSoloWin("Bob",   "MEDIUM", 60);
        TestDBHelper.insertSoloWin("Carol", "HARD",   90);

        // Act
        List<SoloRankRow> easyResult   = dao.getSoloLeaderboard("EASY",   10);
        List<SoloRankRow> mediumResult = dao.getSoloLeaderboard("MEDIUM", 10);
        List<SoloRankRow> hardResult   = dao.getSoloLeaderboard("HARD",   10);

        // Assert
        assertEquals(1,       easyResult.size(),           "EASY chỉ có 1 người");
        assertEquals(1,       mediumResult.size(),         "MEDIUM chỉ có 1 người");
        assertEquals(1,       hardResult.size(),           "HARD chỉ có 1 người");
        assertEquals("Alice", easyResult.get(0).playerName);
        assertEquals("Bob",   mediumResult.get(0).playerName);
        assertEquals("Carol", hardResult.get(0).playerName);
    }

    /**
     * UC11 - 11.1.3: Mỗi player chỉ lấy best time (MIN), không xuất hiện nhiều lần.
     */
    @Test
    @Order(4)
    @DisplayName("getSoloLeaderboard - mỗi player chỉ xuất hiện 1 lần với best time")
    void getSoloLeaderboard_shouldReturnBestTimePerPlayer() throws SQLException {
        // Arrange — Alice chơi 2 lần, lần 2 tốt hơn
        TestDBHelper.insertSoloWin("Alice", "EASY", 80);
        TestDBHelper.insertSoloWin("Alice", "EASY", 42);

        // Act
        List<SoloRankRow> result = dao.getSoloLeaderboard("EASY", 10);

        // Assert
        assertEquals(1,  result.size(),               "Alice chỉ xuất hiện 1 lần");
        assertEquals(42, result.get(0).bestSeconds,   "Phải lấy best time = 42s");
    }

    /**
     * UC11 - 11.2.2: Truy vấn DB trả về rỗng khi chưa có dữ liệu.
     */
    @Test
    @Order(5)
    @DisplayName("getSoloLeaderboard - trả về rỗng khi chưa có kỷ lục")
    void getSoloLeaderboard_shouldReturnEmpty_whenNoData() throws SQLException {
        // Arrange — DB rỗng (clearAll trong @BeforeEach)

        // Act
        List<SoloRankRow> result = dao.getSoloLeaderboard("EASY", 10);

        // Assert
        assertTrue(result.isEmpty(), "Kết quả phải rỗng khi chưa có dữ liệu");
    }

    /**
     * UC11 - 11.1.3: Giới hạn số hàng trả về theo tham số limit.
     */
    @Test
    @Order(6)
    @DisplayName("getSoloLeaderboard - giới hạn đúng số hàng theo limit")
    void getSoloLeaderboard_shouldRespectLimit() throws SQLException {
        // Arrange — 5 người chơi khác nhau
        TestDBHelper.insertSoloWin("P1", "EASY", 10);
        TestDBHelper.insertSoloWin("P2", "EASY", 20);
        TestDBHelper.insertSoloWin("P3", "EASY", 30);
        TestDBHelper.insertSoloWin("P4", "EASY", 40);
        TestDBHelper.insertSoloWin("P5", "EASY", 50);

        // Act
        List<SoloRankRow> result = dao.getSoloLeaderboard("EASY", 3);

        // Assert
        assertEquals(3, result.size(), "Chỉ trả về tối đa 3 hàng theo limit");
    }

    // ════════════════════════════════════════════════════════════
    // getPvpHistory()
    // ════════════════════════════════════════════════════════════

    /**
     * UC11 - 11.4.2: Hệ thống truy vấn lịch sử trận PvP từ DB.
     * UC11 - 11.4.3: Mỗi trận có đủ thông tin 2 player, tỉ số, thời gian.
     */
    @Test
    @Order(7)
    @DisplayName("getPvpHistory - trả về đúng thông tin 2 player mỗi trận")
    void getPvpHistory_shouldReturnMatchWithBothPlayers() throws SQLException {
        // Arrange
        TestDBHelper.insertPvpMatch("Alice", 42, "WIN", "Bob", 65, "LOSE");

        // Act
        List<PvpMatchRow> result = dao.getPvpHistory(10);

        // Assert
        assertEquals(1, result.size(), "Phải có 1 trận PvP");

        PvpMatchRow match = result.get(0);
        assertEquals("Alice", match.player1Name,    "Player 1 phải là Alice");
        assertEquals("Bob",   match.player2Name,    "Player 2 phải là Bob");
        assertEquals(42,      match.player1Seconds, "Thời gian P1 = 42s");
        assertEquals(65,      match.player2Seconds, "Thời gian P2 = 65s");
        assertEquals("WIN",   match.player1Status,  "P1 phải thắng");
        assertEquals("LOSE",  match.player2Status,  "P2 phải thua");
    }

    /**
     * UC11 - 11.4.4: Trả về rỗng khi chưa có trận PvP.
     */
    @Test
    @Order(8)
    @DisplayName("getPvpHistory - trả về rỗng khi chưa có trận PvP")
    void getPvpHistory_shouldReturnEmpty_whenNoMatches() throws SQLException {
        // Arrange — DB rỗng

        // Act
        List<PvpMatchRow> result = dao.getPvpHistory(10);

        // Assert
        assertTrue(result.isEmpty(), "Kết quả phải rỗng khi chưa có trận PvP");
    }

    /**
     * UC11 - 11.4.2: Giới hạn số trận trả về theo limit.
     */
    @Test
    @Order(9)
    @DisplayName("getPvpHistory - giới hạn đúng số trận theo limit")
    void getPvpHistory_shouldRespectLimit() throws SQLException {
        // Arrange — 5 trận PvP
        TestDBHelper.insertPvpMatch("P1A", 10, "WIN",  "P1B", 20, "LOSE");
        TestDBHelper.insertPvpMatch("P2A", 15, "WIN",  "P2B", 25, "LOSE");
        TestDBHelper.insertPvpMatch("P3A", 20, "WIN",  "P3B", 30, "LOSE");
        TestDBHelper.insertPvpMatch("P4A", 25, "LOSE", "P4B", 18, "WIN");
        TestDBHelper.insertPvpMatch("P5A", 30, "LOSE", "P5B", 22, "WIN");

        // Act
        List<PvpMatchRow> result = dao.getPvpHistory(3);

        // Assert
        assertEquals(3, result.size(), "Chỉ trả về tối đa 3 trận theo limit");
    }

    /**
     * UC11 - 11.4.2: Bỏ qua trận chỉ có 1 participant (dữ liệu không đầy đủ).
     */
    @Test
    @Order(10)
    @DisplayName("getPvpHistory - bỏ qua trận thiếu player")
    void getPvpHistory_shouldSkipIncompleteMatch() throws SQLException {
        // Arrange — session PvP chỉ có 1 participant
        String sesId = java.util.UUID.randomUUID().toString();
        String p1Id  = java.util.UUID.randomUUID().toString();
        String gp1Id = java.util.UUID.randomUUID().toString();

        try (var conn = TestDBHelper.getConnection();
             var st   = conn.createStatement()) {
            st.execute("INSERT INTO player (id, name) VALUES ('"
                    + p1Id + "', 'LonePlayer')");
            st.execute("INSERT INTO game_session "
                    + "(id, mode, difficulty, status, board_rows, board_cols, total_mines) "
                    + "VALUES ('" + sesId + "', 'PVP', 'EASY', 'finished', 9, 9, 10)");
            st.execute("INSERT INTO game_participant "
                    + "(id, session_id, player_id, player_order, status, elapsed_seconds) "
                    + "VALUES ('" + gp1Id + "', '" + sesId + "', '"
                    + p1Id + "', 1, 'WIN', 30)");
        }

        // Act
        List<PvpMatchRow> result = dao.getPvpHistory(10);

        // Assert
        assertTrue(result.isEmpty(), "Trận thiếu player 2 phải bị bỏ qua");
    }

    // ════════════════════════════════════════════════════════════
    // insert()
    // ════════════════════════════════════════════════════════════

    /**
     * UC11 - 11.1.3: Insert score_record vào DB thành công.
     * Lưu ý: H2 không enforce FK, nên test này chỉ verify insert logic.
     */
    @Test
    @Order(11)
    @DisplayName("insert - lưu score_record vào DB thành công")
    void insert_shouldPersistScoreRecord() throws SQLException {
        // Arrange — tạo đủ dữ liệu trong H2 trước
        String participantId = TestDBHelper.insertSoloWin("TestPlayer", "EASY", 99);

        com.minesweeper.data.model.ScoreRecord sr = new com.minesweeper.data.model.ScoreRecord(
                java.util.UUID.randomUUID().toString(),
                participantId,
                java.util.UUID.randomUUID().toString(),
                "SOLO", "EASY", 50, "WIN"
        );

        // Act
        boolean success = dao.insert(sr);

        // Assert
        assertTrue(success, "Insert phải trả về true khi thành công");

        // Verify dữ liệu thực sự đã được lưu
        try (var conn = TestDBHelper.getConnection();
             var rs = conn.createStatement()
                     .executeQuery("SELECT COUNT(*) FROM score_record WHERE elapsed_seconds = 50")) {
            rs.next();
            assertEquals(1, rs.getInt(1), "Phải có đúng 1 record với elapsed_seconds = 50");
        }
    }
}