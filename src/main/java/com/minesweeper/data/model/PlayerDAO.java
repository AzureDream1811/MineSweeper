package com.minesweeper.data.model;

import com.minesweeper.data.DBConnection;
import com.minesweeper.data.dao.Player;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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
