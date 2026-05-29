package com.minesweeper.controller;
import com.minesweeper.data.DBConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//Lớp này để test kết nối thôi
public class DBTester {

    public static void main(String[] args) {
        System.out.println("Đang kiểm tra kết nối...");

        // 2. Thử kết nối
        try (Connection connection = DBConnection.getConnection()) {
            if (connection != null) {
                System.out.println("Chúc mừng! Kết nối đến Database [minesweeper_db] THÀNH CÔNG.");

                // Kiểm tra thử tên Database đang kết nối
                System.out.println("Catalog hiện tại: " + connection.getCatalog());
            }
        } catch (SQLException e) {
            System.err.println("Kết nối THẤT BẠI!");
            System.err.println("Lý do: " + e.getMessage());

            // Một số gợi ý lỗi thường gặp
            if (e.getMessage().contains("Access denied")) {
                System.err.println("=> Sai Username hoặc Password.");
            } else if (e.getMessage().contains("Communications link failure")) {
                System.err.println("=> Hãy kiểm tra xem đã bật MySQL trong XAMPP chưa.");
            } else if (e.getMessage().contains("Unknown database")) {
                System.err.println("=> Tên database 'minesweeper_db' chưa được tạo.");
            }
        }
    }
}