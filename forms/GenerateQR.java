package forms;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import dao.ConnectionProvider;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import utility.PhotoHelper;

public class GenerateQR extends JFrame {

    private final JTable studentTable;
    private final DefaultTableModel tableModel;
    private final JLabel lblQRPreview, lblStudentInfo;
    private final JTextField txtSearch;

    private BufferedImage currentQRImage = null;
    private String selectedRollNo = null;
    private String selectedName = null;

    public GenerateQR() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen.width, screen.height);
        setUndecorated(true);
        setLocationRelativeTo(null);
        setLayout(null);

        getContentPane().setBackground(new Color(15, 23, 42));
        getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(56, 189, 248)));

        JLabel lblTitle = new JLabel("DIGITAL SMARTPASS STUDIO - QR GENERATOR");
        lblTitle.setBounds(40, 20, 750, 35);
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

        JLabel lblSearch = new JLabel("Filter Student Directory:");
        lblSearch.setBounds(40, 80, 190, 30);
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblSearch.setForeground(Color.WHITE);
        add(lblSearch);

        txtSearch = new JTextField();
        txtSearch.setBounds(230, 80, 350, 32);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadStudents(txtSearch.getText().trim());
            }
        });
        add(txtSearch);

        String[] cols = {"ID", "Full Name", "Roll No", "Branch", "Semester", "Email", "Pass ID"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        studentTable = new JTable(tableModel);
        PhotoHelper.styleTable(studentTable);
        studentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                generateQRForSelectedStudent();
            }
        });

        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.getViewport().setBackground(new Color(30, 41, 59));
        scrollPane.setBounds(40, 125, screen.width - 500, screen.height - 165);
        add(scrollPane);

        JPanel qrPanel = new JPanel();
        qrPanel.setBounds(screen.width - 430, 125, 390, screen.height - 165);
        qrPanel.setBackground(new Color(30, 41, 59));
        qrPanel.setBorder(BorderFactory.createLineBorder(new Color(56, 189, 248), 1));
        qrPanel.setLayout(null);
        add(qrPanel);

        JLabel lblCardHead = new JLabel("Encoded QR Pass Preview", SwingConstants.CENTER);
        lblCardHead.setBounds(0, 20, 390, 30);
        lblCardHead.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblCardHead.setForeground(new Color(56, 189, 248));
        qrPanel.add(lblCardHead);

        lblStudentInfo = new JLabel("Select a student record from table", SwingConstants.CENTER);
        lblStudentInfo.setBounds(10, 55, 370, 25);
        lblStudentInfo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblStudentInfo.setForeground(Color.WHITE);
        qrPanel.add(lblStudentInfo);

        lblQRPreview = new JLabel("No QR Generated", SwingConstants.CENTER);
        lblQRPreview.setBounds(55, 95, 280, 280);
        lblQRPreview.setForeground(Color.LIGHT_GRAY);
        lblQRPreview.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblQRPreview.setBorder(BorderFactory.createLineBorder(new Color(56, 189, 248), 2));
        qrPanel.add(lblQRPreview);

        JButton btnSaveProject = new JButton("Save to Project Repository");
        btnSaveProject.setBounds(55, 400, 280, 42);
        btnSaveProject.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSaveProject.setForeground(Color.WHITE);
        btnSaveProject.setFocusPainted(false);
        PhotoHelper.applyHoverEffect(btnSaveProject, new Color(34, 197, 94), new Color(22, 163, 74));
        btnSaveProject.addActionListener(e -> saveQRToProjectFolder());
        qrPanel.add(btnSaveProject);

        JButton btnSaveAs = new JButton("Export QR Pass As PNG...");
        btnSaveAs.setBounds(55, 455, 280, 42);
        btnSaveAs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSaveAs.setForeground(Color.WHITE);
        btnSaveAs.setFocusPainted(false);
        PhotoHelper.applyHoverEffect(btnSaveAs, new Color(59, 130, 246), new Color(37, 99, 235));
        btnSaveAs.addActionListener(e -> saveQRCustomLocation());
        qrPanel.add(btnSaveAs);

        loadStudents("");
    }

    private void loadStudents(String search) {
        tableModel.setRowCount(0);
        try (Connection con = ConnectionProvider.getCon()) {
            String query = "SELECT * FROM students WHERE name LIKE ? OR roll_no LIKE ? OR email LIKE ? ORDER BY id DESC";
            PreparedStatement pst = con.prepareStatement(query);
            String p = "%" + search + "%";
            pst.setString(1, p);
            pst.setString(2, p);
            pst.setString(3, p);
            ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("roll_no"),
                    rs.getString("branch"),
                    rs.getString("semester"),
                    rs.getString("email"),
                    rs.getString("unique_reg_id")
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generateQRForSelectedStudent() {
        int row = studentTable.getSelectedRow();
        if (row < 0) return;

        String id = tableModel.getValueAt(row, 0).toString();
        selectedName = tableModel.getValueAt(row, 1).toString();
        selectedRollNo = tableModel.getValueAt(row, 2).toString();
        String passId = tableModel.getValueAt(row, 6).toString();

        String qrData = "SMARTPASS|" + id + "|" + selectedRollNo + "|" + passId;

        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(qrData, BarcodeFormat.QR_CODE, 280, 280);
            currentQRImage = MatrixToImageWriter.toBufferedImage(matrix);

            lblQRPreview.setText("");
            lblQRPreview.setIcon(new ImageIcon(currentQRImage));
            lblStudentInfo.setText(selectedName + " (" + selectedRollNo + ")");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Failed to encode QR matrix: " + ex.getMessage(), "Encoding Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveQRToProjectFolder() {
        if (currentQRImage == null) {
            JOptionPane.showMessageDialog(this, "Please select a student record from the table to generate a QR Pass first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            File dir = new File("qr_codes");
            if (!dir.exists()) dir.mkdirs();
            File out = new File(dir, "QR_" + selectedRollNo + ".png");
            ImageIO.write(currentQRImage, "png", out);
            JOptionPane.showMessageDialog(this, "QR Pass archived at: qr_codes/" + out.getName(), "Saved Successfully", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "File Write Error: " + ex.getMessage(), "I/O Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveQRCustomLocation() {
        if (currentQRImage == null) {
            JOptionPane.showMessageDialog(this, "Please select a student record from the table to generate a QR Pass first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export QR Pass Image");
        chooser.setSelectedFile(new File("SmartPass_" + selectedRollNo + ".png"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                ImageIO.write(currentQRImage, "png", chooser.getSelectedFile());
                JOptionPane.showMessageDialog(this, "QR Pass exported successfully.", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Export Error: " + ex.getMessage(), "I/O Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}