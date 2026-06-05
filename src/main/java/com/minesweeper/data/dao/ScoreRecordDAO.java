package com.minesweeper.data.dao;

import com.minesweeper.data.DBConnection;
import com.minesweeper.data.model.ScoreRecord;

import java.sql.*;
import java.util.UUID;

public class ScoreRecordDAO {

    public boolean insert(ScoreRecord sr) {
        String sql = """
            INSERT INTO score_record
              (id, participant_id, session_id, mode, difficulty, elapsed_seconds, result)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (sr.getId() == null) sr.setId(UUID.randomUUID().toString());
            ps.setString(1, sr.getId());
            ps.setString(2, sr.getParticipantId());
            ps.setString(3, sr.getSessionId());
            ps.setString(4, sr.getMode());
            ps.setString(5, sr.getDifficulty());
            ps.setInt   (6, sr.getElapsedSeconds());
            ps.setString(7, sr.getResult());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
