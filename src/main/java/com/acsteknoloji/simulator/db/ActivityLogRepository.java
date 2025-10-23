package com.acsteknoloji.simulator.db;

import com.acsteknoloji.simulator.model.ActivityLog;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ActivityLogRepository {

    private static final String DB_URL = "jdbc:sqlite:secure_messaging.db";

    private ActivityLog mapResultSetToLog(ResultSet rs) throws SQLException {
        ActivityLog log = new ActivityLog();
        log.setLogId(rs.getLong("log_id"));
        log.setUnitName(rs.getString("unit_name"));
        log.setIpAddress(rs.getString("ip_address"));
        log.setHostname(rs.getString("hostname"));
        log.setActionType(rs.getString("action_type"));
        log.setActionDetails(rs.getString("action_details"));
        log.setLogTimestamp(LocalDateTime.parse(rs.getString("log_timestamp")));
        return log;
    }

    public List<ActivityLog> getLogs(String unitFilter, String actionFilter){
        List<ActivityLog> logs = new ArrayList<>();

        StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM activity_logs");
        List<String> whereClauses = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        if (unitFilter != null && !unitFilter.isBlank()){
            whereClauses.add("unit_name LIKE ?");
            parameters.add("%" + unitFilter + "%");
        }

        if (actionFilter != null && !actionFilter.isBlank()) {
            whereClauses.add("action_type LIKE ?");
            parameters.add("%" + actionFilter + "%");
        }

        if (!whereClauses.isEmpty()) {
            sqlBuilder.append(" WHERE ").append(String.join(" AND ", whereClauses));
        }

        sqlBuilder.append(" ORDER BY log_timestamp DESC");

        String sql = sqlBuilder.toString();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < parameters.size(); i++){
                pstmt.setObject(i+1, parameters.get(i));
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                logs.add(mapResultSetToLog(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return logs;
    }
}
