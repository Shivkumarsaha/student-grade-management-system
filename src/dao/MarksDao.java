package dao;

import db.DatabaseConnection;
import java.sql.*;

public class MarksDao {

    public void addMarks(int studentId, String subject, int marks) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO marks (student_id, subject, marks) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, studentId);
            ps.setString(2, subject);
            ps.setInt(3, marks);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public double getAverageMarks(int studentId) {
        double avg = 0;
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT AVG(marks) AS avg_marks FROM marks WHERE student_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, studentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                avg = rs.getDouble("avg_marks");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return avg;
    }

    public String calculateGrade(double avg) {
        if (avg >= 90)
            return "A+";
        else if (avg >= 80)
            return "A";
        else if (avg >= 70)
            return "B";
        else if (avg >= 60)
            return "C";
        else if (avg >= 50)
            return "D";
        else
            return "F";
    }
}
