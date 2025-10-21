package com.acsteknoloji.simulator.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ActivityLogger {
    private static final String DB_URL = "jdbc:sqlite:secure_messaging.db";

    public void log(String unitName, String actionType, String details){
        String sql = "INSERT INTO activity_logs(unit_name, ip_address, hostname, action_type, action_details, log_timestamp) " +
                "VALUES(?,?,?,?,?,?)";

        try (Connection connection = DriverManager .getConnection(DB_URL);
             PreparedStatement pstmt = connection.prepareStatement(sql)){

            String ipAddress = "N/A";
            String hostname = "N/A";
            try {
                InetAddress localhost = InetAddress.getLocalHost();
                ipAddress = localhost.getHostAddress();
                hostname = localhost.getHostName();
            } catch (UnknownHostException e){
                System.out.println("IP/Hostname alınmadı: " + e.getMessage());
            }

            pstmt.setString(1, unitName);
            pstmt.setString(2, ipAddress);
            pstmt.setString(3, hostname);
            pstmt.setString(4, actionType);
            pstmt.setString(5, details);
            pstmt.setString(6, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            pstmt.executeUpdate();
            System.out.println("Aktivite kaydedildi: [" + actionType + "] " + details);

        } catch (SQLException e){
            e.printStackTrace();
        }
    }
}
