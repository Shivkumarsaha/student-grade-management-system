package dao;

import db.DatabaseConnection;
import model.Student;
import java.sql.*;
import java.util.*;

public class StudentDAO {

    public void addStudent(String name, String className) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO students (name, class) VALUES (?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, className);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM students";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                list.add(new Student(
                        rs.getInt("student_id"),
                        rs.getString("name"),
                        rs.getString("class")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Student> searchStudent(String keyword) {
        List<Student> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM students WHERE name LIKE ? OR student_id LIKE ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Student(
                        rs.getInt("student_id"),
                        rs.getString("name"),
                        rs.getString("class")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 🆕 Update student info
    public void updateStudent(int id, String name, String className) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE students SET name=?, class=? WHERE student_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, className);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 🗑️ Delete student (also delete marks)
    public void deleteStudent(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Delete marks first due to foreign key
            String deleteMarks = "DELETE FROM marks WHERE student_id=?";
            PreparedStatement psMarks = conn.prepareStatement(deleteMarks);
            psMarks.setInt(1, id);
            psMarks.executeUpdate();

            // Delete student record
            String sql = "DELETE FROM students WHERE student_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
