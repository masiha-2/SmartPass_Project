package forms;

import dao.ConnectionProvider;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import utility.PhotoHelper;

public class ManageAttendance extends JFrame {

    private final JComboBox<String> cmbStudents, cmbStatus;
    private final JTextField txtDate, txtSearch;
    private final JTable attTable;
    private final DefaultTableModel tableModel;

    public ManageAttendance() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen.width, screen.height);
        setUndecorated(true);
        setLocationRelativeTo(null);
        setLayout(null);

        getContentPane().setBackground(new Color(15, 23, 42));
        getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(56, 189, 248)));

        JLabel lblTitle = new JLabel("ATTENDANCE LOGS & ADMINISTRATIVE OVERRIDE");
        lblTitle.setBounds(40, 20, 780, 35);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(56, 189, 248));
        add(lblTitle);

        JButton btnBack = new JButton("Return to Console");
        btnBack.setBounds(screen.width - 200, 20, 160, 36);
        btnBack.setMargin(new Insets(0, 0, 0, 0));
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        PhotoHelper.applyHoverEffect(btnBack, new Color(239, 68, 68), new Color(220, 38, 38));
        btnBack.addActionListener(e -> this.dispose());
        add(btnBack);

        JPanel manualPanel = new JPanel();
        manualPanel.setBounds(40, 80, 400, screen.height - 120);
        manualPanel.setBackground(new Color(30, 41, 59));
        manualPanel.setBorder(BorderFactory.createLineBorder(new Color(56, 189, 248), 1));
        manualPanel.setLayout(null);
        add(manualPanel);

        JLabel lblHead = new JLabel("Manual Attendance Override", SwingConstants.CENTER);
        lblHead.setBounds(0, 20, 400, 30);
        lblHead.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHead.setForeground(new Color(56, 189, 248));
        manualPanel.add(lblHead);

        JLabel l1 = new JLabel("Select Student Record:");
        l1.setBounds(30, 75, 340, 25);
        l1.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l1.setForeground(Color.WHITE);
        manualPanel.add(l1);

        cmbStudents = new JComboBox<>();
        cmbStudents.setBounds(30, 105, 340, 35);
        cmbStudents.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        manualPanel.add(cmbStudents);

        JLabel l2 = new JLabel("Session Date (YYYY-MM-DD):");
        l2.setBounds(30, 160, 340, 25);
        l2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l2.setForeground(Color.WHITE);
        manualPanel.add(l2);

        txtDate = new JTextField(LocalDate.now().toString());
        txtDate.setBounds(30, 190, 340, 35);
        txtDate.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        manualPanel.add(txtDate);

        JLabel l3 = new JLabel("Attendance Status:");
        l3.setBounds(30, 245, 340, 25);
        l3.setFont(new Font("Segoe UI", Font.BOLD, 14));
        l3.setForeground(Color.WHITE);
        manualPanel.add(l3);

        cmbStatus = new JComboBox<>(new String[]{"Present (Full Session)", "Absent", "Check-In Only"});
        cmbStatus.setBounds(30, 275, 340, 35);
        cmbStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
        manualPanel.add(cmbStatus);

        JButton btnMark = new JButton("Commit Attendance Record");
        btnMark.setBounds(30, 340, 340, 42);
        btnMark.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnMark.setForeground(Color.WHITE);
        btnMark.setFocusPainted(false);
        PhotoHelper.applyHoverEffect(btnMark, new Color(34, 197, 94), new Color(22, 163, 74));
        btnMark.addActionListener(e -> markManualAttendance());
        manualPanel.add(btnMark);

        JButton btnDeleteAtt = new JButton("Delete Selected Log Entry");
        btnDeleteAtt.setBounds(30, 405, 340, 42);
        btnDeleteAtt.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnDeleteAtt.setForeground(Color.WHITE);
        btnDeleteAtt.setFocusPainted(false);
        PhotoHelper.applyHoverEffect(btnDeleteAtt, new Color(239, 68, 68), new Color(220, 38, 38));
        btnDeleteAtt.addActionListener(e -> deleteSelectedAttendance());
        manualPanel.add(btnDeleteAtt);

        JLabel lblSearch = new JLabel("Filter Logs (Name / Roll No / Branch / Date):");
        lblSearch.setBounds(470, 80, 340, 32);
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSearch.setForeground(Color.WHITE);
        add(lblSearch);

        txtSearch = new JTextField();
        txtSearch.setBounds(810, 80, screen.width - 850, 32);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadAttendanceRecords(txtSearch.getText().trim());
            }
        });
        add(txtSearch);

        String[] cols = {"Log ID", "Roll No", "Student Name", "Branch", "Date", "Check-In", "Check-Out", "Duration / Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        attTable = new JTable(tableModel);
        PhotoHelper.styleTable(attTable);

        JScrollPane scrollPane = new JScrollPane(attTable);
        scrollPane.getViewport().setBackground(new Color(30, 41, 59));
        scrollPane.setBounds(470, 125, screen.width - 510, screen.height - 165);
        add(scrollPane);

        loadStudentList();
        loadAttendanceRecords("");
    }

    private void loadStudentList() {
        cmbStudents.removeAllItems();
        try (Connection con = ConnectionProvider.getCon()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT id, roll_no, name FROM students ORDER BY roll_no ASC");
            while (rs.next()) {
                cmbStudents.addItem(rs.getInt("id") + " - " + rs.getString("roll_no") + " (" + rs.getString("name") + ")");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void markManualAttendance() {
        if (cmbStudents.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "No student records available. Please enroll a student first.", "Validation Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String selected = cmbStudents.getSelectedItem().toString();
        int studentId = Integer.parseInt(selected.split(" - ")[0]);
        String dateStr = txtDate.getText().trim();
        String status = cmbStatus.getSelectedItem().toString();

        String nowTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String checkIn = dateStr + " " + nowTime;
        String checkOut = null;
        String duration = null;

        if (status.startsWith("Present")) {
            checkOut = dateStr + " 17:00:00";
            duration = "Manual Present (Admin Override)";
        } else if (status.equals("Absent")) {
            checkIn = null;
            checkOut = null;
            duration = "ABSENT";
        }

        try (Connection con = ConnectionProvider.getCon()) {
            PreparedStatement del = con.prepareStatement("DELETE FROM attendance WHERE student_id = ? AND att_date = ?");
            del.setInt(1, studentId);
            del.setString(2, dateStr);
            del.executeUpdate();

            PreparedStatement ins = con.prepareStatement("INSERT INTO attendance (student_id, att_date, check_in, check_out, work_duration) VALUES (?, ?, ?, ?, ?)");
            ins.setInt(1, studentId);
            ins.setString(2, dateStr);
            ins.setString(3, checkIn);
            ins.setString(4, checkOut);
            ins.setString(5, duration);
            ins.executeUpdate();

            JOptionPane.showMessageDialog(this, "Attendance record committed successfully.", "Override Complete", JOptionPane.INFORMATION_MESSAGE);
            loadAttendanceRecords(txtSearch.getText().trim());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedAttendance() {
        int row = attTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an attendance log entry from the table to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int attId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        try (Connection con = ConnectionProvider.getCon()) {
            PreparedStatement pst = con.prepareStatement("DELETE FROM attendance WHERE id = ?");
            pst.setInt(1, attId);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Attendance log entry deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            loadAttendanceRecords(txtSearch.getText().trim());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Deletion Error: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAttendanceRecords(String search) {
        tableModel.setRowCount(0);
        try (Connection con = ConnectionProvider.getCon()) {
            String query = "SELECT a.id, s.roll_no, s.name, s.branch, a.att_date, a.check_in, a.check_out, a.work_duration "
                    + "FROM attendance a INNER JOIN students s ON a.student_id = s.id "
                    + "WHERE s.name LIKE ? OR s.roll_no LIKE ? OR s.branch LIKE ? OR a.att_date LIKE ? "
                    + "ORDER BY a.att_date DESC, a.id DESC";
            PreparedStatement pst = con.prepareStatement(query);
            String p = "%" + search + "%";
            pst.setString(1, p);
            pst.setString(2, p);
            pst.setString(3, p);
            pst.setString(4, p);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String cIn = rs.getString("check_in");
                String cOut = rs.getString("check_out");
                String dur = rs.getString("work_duration");

                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("roll_no"),
                    rs.getString("name"),
                    rs.getString("branch"),
                    rs.getString("att_date"),
                    (cIn != null) ? cIn : "---",
                    (cOut != null) ? cOut : "---",
                    (dur != null) ? dur : "Checked-In Only"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load attendance logs: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}