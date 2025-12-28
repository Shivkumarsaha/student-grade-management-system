package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import db.DatabaseConnection;

public class StudentGradeManagementSystem extends JFrame {

    private JTextField txtId, txtName, txtClass, txtSearch, txtMarkId, txtSubject, txtMarks;
    private JTable table;                  // main students table
    private DefaultTableModel model;       // model for main table

    private JTable marksTable;             // subject-wise marks table
    private DefaultTableModel marksModel;  // model for marks table

    public StudentGradeManagementSystem() {
        setTitle("🎓 Student Grade Management System");
        setSize(1250, 720);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(createStudentPanel(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createMarksPanelWithMarksTable(), BorderLayout.EAST);

        loadStudents();
        setVisible(true);
    }

    /* -------------------------- UI CREATION -------------------------- */

    private JPanel createStudentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Student Details"));
        GridBagConstraints gbc = createGbc();

        txtId = addField(panel, gbc, 0, "Student ID:", 10);
        txtName = addField(panel, gbc, 2, "Name:", 15);
        txtClass = addField(panel, gbc, 4, "Class:", 10);

        JButton btnAdd = createButton("➕ Add", e -> addStudent());
        JButton btnUpdate = createButton("🔁 Update", e -> updateStudent());
        JButton btnDelete = createButton("🗑 Delete", e -> deleteStudent());

        gbc.gridx = 6; panel.add(btnAdd, gbc);
        gbc.gridx = 7; panel.add(btnUpdate, gbc);
        gbc.gridx = 8; panel.add(btnDelete, gbc);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel mid = new JPanel(new BorderLayout(5, 5));
        mid.setBorder(BorderFactory.createTitledBorder("Search Student"));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchPanel.add(new JLabel("Search by ID or Name:"));
        txtSearch = new JTextField(20);
        searchPanel.add(txtSearch);

        JButton btnSearch = createButton("🔍 Search", e -> searchStudent());
        JButton btnShowAll = createButton("📋 Show All", e -> loadStudents());
        searchPanel.add(btnSearch);
        searchPanel.add(btnShowAll);
        mid.add(searchPanel, BorderLayout.NORTH);

        // Table Columns
        String[] cols = {"ID", "Name", "Class", "Subjects", "Grade", "Average"};
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);
        table.setRowHeight(25);

      // When student is selected, show marks on the right panel
table.getSelectionModel().addListSelectionListener(e -> {
    if (!e.getValueIsAdjusting()) {
        int r = table.getSelectedRow();
        if (r >= 0) {
            String studentId = table.getValueAt(r, 0).toString();
            loadMarksForStudent(studentId);
        }
    }
});

        mid.add(new JScrollPane(table), BorderLayout.CENTER);

        return mid;
    }

    private JPanel createMarksPanelWithMarksTable() {
    JPanel container = new JPanel(new BorderLayout(30, 25));
    container.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    // Add marks form
    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBorder(BorderFactory.createTitledBorder("Add Marks"));
    GridBagConstraints gbc = createGbc();

    // 🔹 Larger and more readable text fields
    txtMarkId = addField(formPanel, gbc, 0, "Student ID:", 15);
    txtMarkId.setPreferredSize(new Dimension(220, 35));

    txtSubject = addField(formPanel, gbc, 2, "Subject:", 15);
    txtSubject.setPreferredSize(new Dimension(220, 35));

    txtMarks = addField(formPanel, gbc, 4, "Marks:", 15);
    txtMarks.setPreferredSize(new Dimension(120, 35));

    JButton btnAddMarks = createButton("➕ Add Marks", e -> {
        addMarks();
        int sel = table.getSelectedRow();
        if (sel >= 0) {
            loadMarksForStudent(table.getValueAt(sel, 0).toString());
        }
    });

    gbc.gridx = 6;
    formPanel.add(btnAddMarks, gbc);
    container.add(formPanel, BorderLayout.NORTH);

    // Marks table showing subject-wise data and total/average/SGPA
    JPanel marksPanel = new JPanel(new BorderLayout());
    marksPanel.setBorder(BorderFactory.createTitledBorder("Subject-wise Marks"));

    String[] marksCols = {"Subject", "Marks"};
    marksModel = new DefaultTableModel(marksCols, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    marksTable = new JTable(marksModel);
    marksTable.setRowHeight(24);
    marksPanel.add(new JScrollPane(marksTable), BorderLayout.CENTER);

    JLabel hint = new JLabel("Select a student to view subjects, total, and SGPA.");
    hint.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
    marksPanel.add(hint, BorderLayout.SOUTH);

    container.add(marksPanel, BorderLayout.CENTER);
    container.setPreferredSize(new Dimension(430, 0));

    return container;
}

    /* -------------------------- UTIL METHODS -------------------------- */

    private GridBagConstraints createGbc() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        return gbc;
    }

 private JTextField addField(JPanel panel, GridBagConstraints gbc, int x, String label, int width) {
    gbc.gridx = x;
    panel.add(new JLabel(label), gbc);
    gbc.gridx = x + 1;

    JTextField field = new JTextField(width);

    // Force fixed size (this overrides layout compression)
    Dimension fieldSize = new Dimension(25, 15); // wider + taller
    field.setPreferredSize(fieldSize);
    field.setMinimumSize(fieldSize);
    field.setMaximumSize(fieldSize);

    panel.add(field, gbc);
    return field;
}



    private JButton createButton(String text, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.addActionListener(action);
        btn.setBackground(new Color(150, 120, 120));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return btn;
    }

    /* -------------------------- DATABASE OPERATIONS -------------------------- */

    private void addStudent() {
        executeUpdate("INSERT INTO students (id, name, class, average, grade) VALUES (?, ?, ?, 0, '')",
                txtId.getText().trim(), txtName.getText().trim(), txtClass.getText().trim());
        JOptionPane.showMessageDialog(this, "✅ Student Added Successfully!");
        loadStudents();
    }

    private void updateStudent() {
        executeUpdate("UPDATE students SET name=?, class=? WHERE id=?",
                txtName.getText().trim(), txtClass.getText().trim(), txtId.getText().trim());
        JOptionPane.showMessageDialog(this, "✅ Student Updated!");
        loadStudents();
    }

    private void deleteStudent() {
        executeUpdate("DELETE FROM students WHERE id=?", txtId.getText().trim());
        JOptionPane.showMessageDialog(this, "🗑 Student Deleted!");
        loadStudents();
    }

    private void searchStudent() {
        String search = txtSearch.getText().trim();
        String sql = "SELECT s.id, s.name, s.class, " +
                "GROUP_CONCAT(m.subject SEPARATOR ', ') AS subjects, " +
                "s.grade, s.average " +
                "FROM students s LEFT JOIN marks m ON s.id = m.student_id " +
                "WHERE s.id = ? OR s.name LIKE ? " +
                "GROUP BY s.id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, search);
            ps.setString(2, "%" + search + "%");
            updateTable(ps);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void loadStudents() {
        String sql = "SELECT s.id, s.name, s.class, " +
                "GROUP_CONCAT(m.subject SEPARATOR ', ') AS subjects, " +
                "s.grade, s.average " +
                "FROM students s LEFT JOIN marks m ON s.id = m.student_id " +
                "GROUP BY s.id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            updateTable(ps);
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void addMarks() {
        String studentId = txtMarkId.getText().trim();
        String subject = txtSubject.getText().trim();
        String marksStr = txtMarks.getText().trim();

        if (studentId.isEmpty() || subject.isEmpty() || marksStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Please fill all fields to add marks.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            executeUpdate("INSERT INTO marks (student_id, subject, marks) VALUES (?, ?, ?)",
                    studentId, subject, marksStr);

            String avgSql = "SELECT AVG(marks) AS avgMarks FROM marks WHERE student_id=?";
            try (PreparedStatement ps = conn.prepareStatement(avgSql)) {
                ps.setString(1, studentId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    double avg = rs.getDouble("avgMarks");
                    String grade = getGrade(avg);
                    executeUpdate("UPDATE students SET average=?, grade=? WHERE id=?",
                            String.valueOf(avg), grade, studentId);
                }
            }

            JOptionPane.showMessageDialog(this, "✅ Marks Added & Grade Updated!");
            loadStudents();

        } catch (Exception ex) {
            showError(ex);
        }
    }

    /* -------------------------- HELPERS -------------------------- */

    private void executeUpdate(String sql, String... params) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            ps.executeUpdate();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void updateTable(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getString("class"),
                        rs.getString("subjects"),
                        rs.getString("grade"),
                        rs.getString("average")
                });
            }
        }
    }

    private void loadMarksForStudent(String studentId) {
        marksModel.setRowCount(0);
        if (studentId == null || studentId.trim().isEmpty()) return;

        String sql = "SELECT subject, marks FROM marks WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();

            int count = 0;
            double total = 0.0;

            while (rs.next()) {
                String subject = rs.getString("subject");
                double marks = rs.getDouble("marks");
                marksModel.addRow(new Object[]{subject, marks});
                total += marks;
                count++;
            }

            DecimalFormat df = new DecimalFormat("#.##");
            if (count > 0) {
                double avg = total / count;
                double sgpa = avg / 10.0;

                marksModel.addRow(new Object[]{"", ""});
                marksModel.addRow(new Object[]{"Total", df.format(total)});
                marksModel.addRow(new Object[]{"Average", df.format(avg)});
                marksModel.addRow(new Object[]{"SGPA", df.format(sgpa)});
            } else {
                marksModel.addRow(new Object[]{"No subjects found", ""});
            }

        } catch (Exception ex) {
            showError(ex);
        }
    }

    private String getGrade(double avg) {
        if (avg >= 90) return "A+";
        if (avg >= 80) return "A";
        if (avg >= 70) return "B";
        if (avg >= 60) return "C";
        if (avg >= 50) return "D";
        return "F";
    }

    private void showError(Exception ex) {
        JOptionPane.showMessageDialog(this, "❌ Error: " + ex.getMessage());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StudentGradeManagementSystem::new);
    }
}
