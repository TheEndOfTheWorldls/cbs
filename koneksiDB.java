package com.mycompany.cat_boarding_system_tubes;
import java.sql.Connection;
import java.sql.DriverManager;

public class koneksiDB {
     private static final String URL = "jdbc:mysql://localhost:3306/cat_boarding_system_tubes";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Koneksi berhasil!");
            return conn;
        } catch (Exception e) {
            System.out.println("Koneksi gagal: " + e.getMessage());
            return null;
        }
    }
}
    

