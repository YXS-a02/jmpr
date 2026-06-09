package fur.yxs.mper.core;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Data {
    public static class srcList {
        private final Connection db;
        public final static String TYPE_FileDir = "FileDir";
        public final static String TYPE_YNetApi = "YNetApi";

        srcList(Connection s) {
            db = s;
            try {
                Statement stmt = db.createStatement();
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS src (name TEXT PRIMARY KEY, type TEXT, url TEXT)");
                stmt.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public String[] getAllSrcName(){
            List<String> names = new ArrayList<>();
            try (Statement stmt = db.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT name FROM src")) {
                while (rs.next()) {
                    names.add(rs.getString("name"));
                }
                return names.toArray(new String[0]);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        public void setSrcItem(String name,String type,String url){
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("name 不能为空");
            }
            try {
                PreparedStatement stmt = db.prepareStatement("SELECT name FROM src WHERE name = ?");
                stmt.setString(1, name);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    PreparedStatement updateStmt = db.prepareStatement("UPDATE src SET type = ?, url = ? WHERE name = ?");
                    updateStmt.setString(1, type);
                    updateStmt.setString(2, url);
                    updateStmt.setString(3, name);
                    updateStmt.executeUpdate();
                    updateStmt.close();
                } else {
                    PreparedStatement insertStmt = db.prepareStatement("INSERT INTO src (name, type, url) VALUES (?, ?, ?)");
                    insertStmt.setString(1, name);
                    insertStmt.setString(2, type);
                    insertStmt.setString(3, url);
                    insertStmt.executeUpdate();
                    insertStmt.close();
                }
                stmt.close();
            } catch (SQLException e) {
                System.out.println("[E]" + e);
                throw new RuntimeException(e);
            }
        }

        public String getSrcUrl(String name){
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("name 不能为空");
            }
            String sql = "SELECT url FROM src WHERE name = ?";
            try (PreparedStatement stmt = db.prepareStatement(sql)) {
                stmt.setString(1, name);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("url");
                    } else {
                        return "";
                    }
                }
            } catch (SQLException e) {
                System.out.println("[E] 查询失败: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

        public String getSrcType(String name){
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("name 不能为空");
            }
            String sql = "SELECT type FROM src WHERE name = ?";
            try (PreparedStatement stmt = db.prepareStatement(sql)) {
                stmt.setString(1, name);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("type");
                    } else {
                        return "";
                    }
                }
            } catch (SQLException e) {
                System.out.println("[E] 查询失败: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

        public void clearAllItem(){
            String sql = "DELETE FROM src";
            try (Statement stmt = db.createStatement()) {
                int rowsAffected = stmt.executeUpdate(sql);
            } catch (SQLException e) {
                System.out.println("[E] 清空失败: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        /*
        public void removeItem(String name){
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("name 不能为空");
            }
        }
         */
        public void removeItem(String name) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("name 不能为空");
            }
            String sql = "DELETE FROM src WHERE name = ?";
            try (PreparedStatement stmt = db.prepareStatement(sql)) {
                stmt.setString(1, name);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    System.out.println("[W] 未找到要删除的项目: " + name);
                }
            } catch (SQLException e) {
                System.out.println("[E] 删除失败: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    public static void srcListTest() {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:data.db");
            srcList s = new srcList(conn);
            s.setSrcItem("a",srcList.TYPE_FileDir,"a");
            s.setSrcItem("b",srcList.TYPE_FileDir,"c");
            s.setSrcItem("c",srcList.TYPE_FileDir,"b");
            System.out.println(Arrays.toString(s.getAllSrcName()));
            System.out.println(s.getSrcType("a"));
            System.out.println(s.getSrcUrl("a"));
            //s.clearAllItem();
            s.removeItem("a");
            System.out.println(Arrays.toString(s.getAllSrcName()));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        srcListTest();
    }
}
