package com.acsteknoloji.simulator.db;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:secure_messaging.db";

    private static final String CREATE_UNITS_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS units (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "unit_name TEXT NOT NULL UNIQUE," +
                    "password_hash TEXT NOT NULL," +
                    "role TEXT NOT NULL" +
                    ");";

    private static final String CREATE_MESSAGES_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS messages (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "sender_unit TEXT NOT NULL," +
                    "receiver_unit TEXT NOT NULL," +
                    "original_text TEXT," +
                    "encrypted_text TEXT NOT NULL," +
                    "message_hash TEXT NOT NULL," +
                    "timestamp TEXT NOT NULL," +
                    "integrity_status BOOLEAN NOT NULL," +
                    "status TEXT NOT NULL" +
                    "message_type TEXT NOT NULL DEFAULT 'TEXT'," +
                    "file_path TEXT" +
                    ");";

    private static final String CREATE_ACTIVITY_LOGS_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS activity_logs (" +
                    "log_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "unit_name TEXT NOT NULL," +
                    "ip_address TEXT," +
                    "hostname TEXT," +
                    "action_type TEXT NOT NULL," +
                    "action_details TEXT," +
                    "log_timestamp TEXT NOT NULL" +
                    ");";

    public static void initializeDatabase() throws SQLException {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             Statement statement = connection.createStatement()){
            statement.execute(CREATE_UNITS_TABLE_SQL);
            statement.execute(CREATE_MESSAGES_TABLE_SQL);
            statement.execute(CREATE_ACTIVITY_LOGS_TABLE_SQL);

            checkAndUpgradeMessagesTable(connection);
        }catch (Exception e){
            System.out.println(e.getLocalizedMessage());
        }
    }

    /**
     * 'messages' tablosunun şemasını kontrol eder ve eksikse
     * 'message_type' ve 'file_path' sütunlarını ekler.
     */
    private static void checkAndUpgradeMessagesTable(Connection connection) throws SQLException {
        List<String> columns = new ArrayList<>();
        try(ResultSet rs = connection.getMetaData().getColumns(null, null, "message", null)){
            while (rs.next()){
                columns.add(rs.getString("COLUMN_NAME").toLowerCase());
            }
        }

        try(Statement stmt = connection.createStatement()){
            if (!columns.contains("message_type")){
                System.out.println("'messages' tablosu güncelleniyor: 'message_type' sütunu ekleniyor...");
                stmt.execute("ALTER TABLE messages ADD COLUMN message_type TEXT NOT NULL DEFAULT 'TEXT'");
            }
            if (!columns.contains("file_path")){
                System.out.println("'messages' tablosu güncelleniyor: 'file_path' sütunu ekleniyor...");
                stmt.execute("ALTER TABLE messages ADD COLUMN file_path TEXT");
            }
        }
    }

    //Başlangıç ve test için veriler
    public static void seedInitialData() {
        String checkUnitsSql = "SELECT COUNT(*) FROM units";
        String insertUnitSql = "INSERT INTO units(unit_name, password_hash, role) VALUES(?,?,?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery(checkUnitsSql)) {

            if (rs.getInt(1) == 0) {
                System.out.println("Başlangıç verileri (yeni isimlerle) ekleniyor...");
                try (PreparedStatement pstmt = conn.prepareStatement(insertUnitSql)) {
                    // KKM - Komuta Kontrol Merkezi
                    pstmt.setString(1, "KKM");
                    pstmt.setString(2, BCrypt.hashpw("Acs06", BCrypt.gensalt()));
                    pstmt.setString(3, "KOMUTA");
                    pstmt.executeUpdate();

                    // YKI_1 - Yer Kontrol İstasyonu
                    pstmt.setString(1, "YKI_1");
                    pstmt.setString(2, BCrypt.hashpw("yki1_123", BCrypt.gensalt()));
                    pstmt.setString(3, "OPERATOR");
                    pstmt.executeUpdate();

                    // Mobil_Birim_3
                    pstmt.setString(1, "Mobil_Birim_3");
                    pstmt.setString(2, BCrypt.hashpw("mobil3_123", BCrypt.gensalt()));
                    pstmt.setString(3, "OPERATOR");
                    pstmt.executeUpdate();

                    // IHA_07 - İnsansız Hava Aracı
                    pstmt.setString(1, "IHA_07");
                    pstmt.setString(2, BCrypt.hashpw("iha7_123", BCrypt.gensalt()));
                    pstmt.setString(3, "OPERATOR");
                    pstmt.executeUpdate();

                    System.out.println("Başlangıç birimleri başarıyla eklendi.");
                }
            } else {
                System.out.println("Veritabanında zaten veri var, tohumlama atlandı.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
