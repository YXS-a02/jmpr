package yxs.a02.mper.service;

import yxs.a02.mper.model.ApiEndpoint;
import yxs.a02.mper.model.MediaFile;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private Connection connection;
    private ApiClient apiClient;

    public DatabaseManager() {
        this.apiClient = new ApiClient();
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            // 使用SQLite数据库
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:media_player.db");
            createTables();
            insertDefaultData();
        } catch (Exception e) {
            System.err.println("数据库初始化失败: " + e.getMessage());
            // 使用内存数据库作为fallback
            try {
                connection = DriverManager.getConnection("jdbc:sqlite::memory:");
                createTables();
                insertDefaultData();
            } catch (SQLException ex) {
                System.err.println("内存数据库也失败了: " + ex.getMessage());
            }
        }
    }

    private void createTables() {
        String createApiTable = """
            CREATE TABLE IF NOT EXISTS api_endpoints (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                url TEXT NOT NULL,
                added DATETIME DEFAULT CURRENT_TIMESTAMP,
                is_online BOOLEAN DEFAULT 0,
                last_checked DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """;

        String createPlaybackHistoryTable = """
            CREATE TABLE IF NOT EXISTS playback_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                file_path TEXT NOT NULL,
                file_name TEXT NOT NULL,
                played_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                play_duration INTEGER DEFAULT 0
            )
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createApiTable);
            stmt.execute(createPlaybackHistoryTable);
        } catch (SQLException e) {
            System.err.println("创建表失败: " + e.getMessage());
        }
    }

    private void insertDefaultData() {
        String checkData = "SELECT COUNT(*) as count FROM api_endpoints";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(checkData)) {

            if (rs.next() && rs.getInt("count") == 0) {
                // 插入默认API端点
                String insertDefault = """
                    INSERT INTO api_endpoints (name, url, is_online)
                    VALUES (?, ?, ?)
                    """;
                try (PreparedStatement pstmt = connection.prepareStatement(insertDefault)) {
                    pstmt.setString(1, "默认API");
                    pstmt.setString(2, "http://localhost:8080/api");
                    pstmt.setBoolean(3, false);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("插入默认数据失败: " + e.getMessage());
        }
    }

    public List<ApiEndpoint> getApiEndpoints() {
        List<ApiEndpoint> endpoints = new ArrayList<>();
        String sql = "SELECT * FROM api_endpoints ORDER BY added DESC";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ApiEndpoint endpoint = new ApiEndpoint();
                endpoint.setId(rs.getInt("id"));
                endpoint.setName(rs.getString("name"));
                endpoint.setUrl(rs.getString("url"));
                endpoint.setAdded(rs.getTimestamp("added"));
                endpoint.setOnline(rs.getBoolean("is_online"));
                endpoint.setLastChecked(rs.getTimestamp("last_checked"));

                // 测试连接状态
                boolean isOnline = apiClient.testConnection(endpoint.getUrl());
                endpoint.setOnline(isOnline);
                updateApiStatus(endpoint.getId(), isOnline);

                endpoints.add(endpoint);
            }
        } catch (SQLException e) {
            System.err.println("获取API端点失败: " + e.getMessage());
        }

        return endpoints;
    }

    public boolean addApiEndpoint(ApiEndpoint endpoint) {
        String sql = "INSERT INTO api_endpoints (name, url, is_online) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, endpoint.getName());
            pstmt.setString(2, endpoint.getUrl());
            pstmt.setBoolean(3, apiClient.testConnection(endpoint.getUrl()));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("添加API端点失败: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteApiEndpoint(int id) {
        String sql = "DELETE FROM api_endpoints WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("删除API端点失败: " + e.getMessage());
            return false;
        }
    }

    public boolean updateApiEndpoint(ApiEndpoint endpoint) {
        String sql = "UPDATE api_endpoints SET name = ?, url = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, endpoint.getName());
            pstmt.setString(2, endpoint.getUrl());
            pstmt.setInt(3, endpoint.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("更新API端点失败: " + e.getMessage());
            return false;
        }
    }

    private void updateApiStatus(int id, boolean isOnline) {
        String sql = "UPDATE api_endpoints SET is_online = ?, last_checked = CURRENT_TIMESTAMP WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setBoolean(1, isOnline);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("更新API状态失败: " + e.getMessage());
        }
    }

    public void addPlaybackHistory(String filePath, String fileName) {
        String sql = "INSERT INTO playback_history (file_path, file_name) VALUES (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, filePath);
            pstmt.setString(2, fileName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("记录播放历史失败: " + e.getMessage());
        }
    }

    public List<MediaFile> getPlaybackHistory(int limit) {
        List<MediaFile> history = new ArrayList<>();
        String sql = "SELECT * FROM playback_history ORDER BY played_at DESC LIMIT ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                MediaFile file = new MediaFile();
                file.setName(rs.getString("file_name"));
                file.setPath(rs.getString("file_path"));
                file.setType("audio"); // 默认为音频
                history.add(file);
            }
        } catch (SQLException e) {
            System.err.println("获取播放历史失败: " + e.getMessage());
        }

        return history;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
            if (apiClient != null) {
                apiClient.close();
            }
        } catch (Exception e) {
            System.err.println("关闭数据库连接失败: " + e.getMessage());
        }
    }
}