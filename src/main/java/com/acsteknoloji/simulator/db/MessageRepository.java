package com.acsteknoloji.simulator.db;

import com.acsteknoloji.simulator.model.Message;
import com.acsteknoloji.simulator.model.MessageStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository {
    private static final String DB_URL = "jdbc:sqlite:secure_messaging.db";

    public boolean saveMessage(Message message) {
        String sql = "INSERT INTO messages(sender_unit, receiver_unit, original_text, encrypted_text, " +
                "message_hash, timestamp, integrity_status, status) VALUES(?,?,?,?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, message.getSenderUnit());
            pstmt.setString(2, message.getReceiverUnit());
            pstmt.setString(3, message.getOriginalText());
            pstmt.setString(4, message.getEncryptedText());
            pstmt.setString(5, message.getMessageHash());
            pstmt.setString(6, message.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            pstmt.setBoolean(7, message.isIntegrityStatus());
            pstmt.setString(8, message.getStatus().name());

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Veritabanı kaydetme hatası: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Belirli bir birimin gönderdiği veya aldığı tüm mesajları çeker. (OPERATOR rolü için)
     * @param unitName İlgili birimin adı
     * @return Mesajların listesi
     */
    public List<Message> getMessagesForUnit(String unitName) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages WHERE sender_unit = ? OR receiver_unit = ? ORDER BY timestamp ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, unitName);
            pstmt.setString(2, unitName);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * Veritabanındaki tüm mesajları çeker. (KOMUTA rolü için)
     * @return Tüm mesajların listesi
     */
    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM messages ORDER BY timestamp ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                messages.add(mapResultSetToMessage(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * ResultSet'ten gelen bir satırı Message nesnesine dönüştüren yardımcı metod.
     */
    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setId(rs.getLong("id"));
        message.setSenderUnit(rs.getString("sender_unit"));
        message.setReceiverUnit(rs.getString("receiver_unit"));
        message.setOriginalText(rs.getString("original_text"));
        message.setEncryptedText(rs.getString("encrypted_text"));
        message.setMessageHash(rs.getString("message_hash"));
        message.setTimestamp(LocalDateTime.parse(rs.getString("timestamp")));
        message.setIntegrityStatus(rs.getBoolean("integrity_status"));
        message.setStatus(MessageStatus.valueOf(rs.getString("status")));
        return message;
    }
}
