package com.acsteknoloji.simulator.db;

import com.acsteknoloji.simulator.model.Unit;
import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UnitRepository {
    private static final String DB_URL = "jdbc:sqlite:secure_messaging.db";

    public List<String> getAllUnitNames(){
        List<String> unitNames = new ArrayList<>();
        String sql = "SELECT unit_name FROM units ORDER BY unit_name ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                unitNames.add(rs.getString("unit_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return unitNames;
    }

    public Optional<Unit> findUnitByName(String name) {
        String sql = "SELECT * FROM units WHERE unit_name = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Unit unit = new Unit();
                unit.setId(rs.getInt("id"));
                unit.setUnitName(rs.getString("unit_name"));
                unit.setPasswordHash(rs.getString("password_hash"));
                unit.setRole(rs.getString("role"));
                return Optional.of(unit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty(); // Birim bulunamazsa boş Optional döner
    }

    public List<String> getOtherUnitNames(String currentUnitName){
        List<String> unitNames = new ArrayList<>();
        String sql = "SELECT unit_name FROM units WHERE unit_name != ? ORDER BY unit_name ASC";

        try (Connection connection = DriverManager.getConnection(DB_URL);
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, currentUnitName);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()){
                unitNames.add(resultSet.getString("unit_name"));
            }

        }catch (SQLException e){
            e.printStackTrace();
        }

        return unitNames;
    }

    public List<Unit> getAllUnits(){
        List<Unit> units = new ArrayList<>();
        String sql = "SELECT * FROM units ORDER BY unit_name ASC";

        try (Connection conn = DriverManager.getConnection(DB_URL);
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)){
            while (rs.next()){
                Unit unit = new Unit();
                unit.setId(rs.getInt("id"));
                unit.setUnitName(rs.getString("unit_name"));
                unit.setPasswordHash(rs.getString("password_hash"));
                unit.setRole(rs.getString("role"));
                units.add(unit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return units;
    }

    public boolean addUnit(String unitName, String password, String role) {
        String sql = "INSERT INTO units(unit_name, password_hash, role) VALUES(?,?,?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
        PreparedStatement pstmt = conn.prepareStatement(sql)){

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            pstmt.setString(1, unitName);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, role);

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e){
            // UNIQUE kısıtlaması (unit_name) ihlal edilirse bu hata tetiklenir
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUnit(String unitName){
        if ("KKM".equals(unitName)){
            System.err.println("Güvenlik Uyarısı: Ana KOMUTA birimi ('KKM') silinemez.");
            return false;
        }

        String sql = "DELETE FROM units WHERE unit_name = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
        PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, unitName);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePassword(String unitName, String newPassword){
        String sql = "UPDATE units SET password_hash = ? WHERE unit_name = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

            pstmt.setString(1, hashedPassword);
            pstmt.setString(2, unitName);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }
}
