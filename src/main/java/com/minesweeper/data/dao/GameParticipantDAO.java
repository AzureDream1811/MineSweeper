package com.minesweeper.data.dao;

import com.minesweeper.data.DBConnection;
import com.minesweeper.data.model.GameParticipant;


import java.sql.*;
import java.util.UUID;

public class GameParticipantDAO {

    public boolean insert(GameParticipant gp) {
        String sql = """
            INSERT INTO game_participant
              (id, session_id, player_id, player_order, status)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (gp.getId() == null) gp.setId(UUID.randomUUID().toString());
            ps.setString(1, gp.getId());
            ps.setString(2, gp.getSessionId());
            ps.setString(3, gp.getPlayerId());
            ps.setInt   (4, gp.getPlayerOrder());
            ps.setString(5, gp.getStatus() != null ? gp.getStatus() : "waiting");
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
