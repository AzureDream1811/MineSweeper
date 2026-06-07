package com.minesweeper.data.dao;

import com.minesweeper.data.DBConnection;
import com.minesweeper.data.model.Player;


import java.sql.*;
import java.util.UUID;

public class PlayerDAO {

    // Tạo player mới
    public boolean insert(Player player) {
        String sql = "INSERT INTO player (id, name) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (player.getId() == null) {
                player.setId(UUID.randomUUID().toString());
            }
            ps.setString(1, player.getId());
            ps.setString(2, player.getName());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
