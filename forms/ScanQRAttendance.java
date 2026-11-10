package forms;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import dao.ConnectionProvider;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.imageio.ImageIO;
import javax.swing.*;
import utility.PhotoHelper;

public class ScanQRAttendance extends JFrame {

    private final JLabel lblClock, lblStudentPhoto, lblStudentName, lblStudentBranch, lblStatusBadge;
    private final JTextField txtQuickScan;

    public ScanQRAttendance() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(screen.width, screen.height);
        setUndecorated(true);
        setLocationRelativeTo(null);
        setLayout(null);

        getContentPane().setBackground(new Color(15, 23, 42));
        getRootPane().setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(56, 189, 248)));

        JLabel lblTitle = new JLabel("CAMPUS ENTRY & ATTENDANCE SCANNER TERMINAL");
        lblTitle.setBounds(40, 20, 680, 38);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(56, 189, 248));
        add(lblTitle);

        lblClock = new JLabel("Synchronizing Clock...", SwingConstants.CENTER);
        lblClock.setBounds(screen.width - 520, 20, 300, 38);
        lblClock.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblClock.setForeground(new Color(34, 197, 94));
        lblClock.setBorder(BorderFactory.createLineBorder(new Color(34, 197, 94), 1));
        add(lblClock);

        Timer clockTimer = new Timer(1000, e -> {
            lblClock.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd | HH:mm:ss")));
        });
        clockTimer.start();

        JButton btnBack = new JButton("Return to Console");
        btnBack.setBounds(screen.width - 200, 20, 160, 38);
        btnBack.setMargin(new Insets(0, 0, 0, 0));
        btnBack.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusPainted(false);
        PhotoHelper.applyHoverEffect(btnBack, new Color(239, 68, 68), new Color(220, 38, 38));
        btnBack.addActionListener(e -> {
            clockTimer.stop();
            this.dispose();
        });
        add(btnBack);

        int centerX = (screen.width - 960) / 2;
        int centerY = (screen.height - 520) / 2 + 20;

        JPanel scannerPanel = new JPanel();
        scannerPanel.setBounds(centerX, centerY, 460, 480);
        scannerPanel.setBackground(new Color(30, 41, 59));
        scannerPanel.setBorder(BorderFactory.createLineBorder(new Color(56, 189, 248), 2));
        scannerPanel.setLayout(null);
        add(scannerPanel);

        JLabel lblScanHead = new JLabel("QR Code Verification Terminal", SwingConstants.CENTER);
        lblScanHead.setBounds(0, 25, 460, 30);
        lblScanHead.setFont(new Font("Segoe UI", Font.BOLD, 21));
        lblScanHead.setForeground(new Color(56, 189, 248));
        scannerPanel.add(lblScanHead);

        JLabel lblIcon = new JLabel("[ OPTICAL QR DECODER READY ]", SwingConstants.CENTER);
        lblIcon.setBounds(50, 75, 360, 150);
        lblIcon.setFont(new Font("Segoe UI", Font.BOLD, 19));
        lblIcon.setForeground(new Color(234, 179, 8));
        lblIcon.setBorder(BorderFactory.createDashedBorder(new Color(56, 189, 248), 2, 5, 5, false));
        scannerPanel.add(lblIcon);

        JButton btnScanQRFile = new JButton("1. Select & Decode QR Pass Image");
        btnScanQRFile.setBounds(50, 250, 360, 48);
        btnScanQRFile.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnScanQRFile.setForeground(Color.WHITE);
        btnScanQRFile.setFocusPainted(false);
        PhotoHelper.applyHoverEffect(btnScanQRFile, new Color(34, 197, 94), new Color(22, 163, 74));
        btnScanQRFile.addActionListener(e -> scanQRFromFile());
        scannerPanel.add(btnScanQRFile);

        JLabel lblOr = new JLabel("OR Enter Roll Number / Pass ID:", SwingConstants.CENTER);
        lblOr.setBounds(50, 320, 360, 25);
        lblOr.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblOr.setForeground(Color.LIGHT_GRAY);
        scannerPanel.add(lblOr);

        txtQuickScan = new JTextField();
        txtQuickScan.setBounds(50, 355, 230, 42);
        txtQuickScan.setFont(new Font("Segoe UI", Font.BOLD, 15));
        scannerPanel.add(txtQuickScan);

        JButton btnQuickMark = new JButton("Verify & Log");
        btnQuickMark.setBounds(290, 355, 120, 42);
        btnQuickMark.setMargin(new Insets(0, 0, 0, 0));
        btnQuickMark.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnQuickMark.setForeground(Color.WHITE);
        PhotoHelper.applyHoverEffect(btnQuickMark, new Color(59, 130, 246), new Color(37, 99, 235));
        btnQuickMark.addActionListener(e -> {
            String input = txtQuickScan.getText().trim();
            if (!input.isEmpty()) {
                processStudentAttendance(input);
                txtQuickScan.setText("");
            }
        });
        scannerPanel.add(btnQuickMark);

        JPanel verifyPanel = new JPanel();
        verifyPanel.setBounds(centerX + 500, centerY, 460, 480);
        verifyPanel.setBackground(new Color(30, 41, 59));
        verifyPanel.setBorder(BorderFactory.createLineBorder(new Color(56, 189, 248), 2));
        verifyPanel.setLayout(null);
        add(verifyPanel);

        JLabel lblVerHead = new JLabel("Identity Verification Telemetry", SwingConstants.CENTER);
        lblVerHead.setBounds(0, 25, 460, 30);
        lblVerHead.setFont(new Font("Segoe UI", Font.BOLD, 21));
        lblVerHead.setForeground(new Color(56, 189, 248));
        verifyPanel.add(lblVerHead);

        lblStudentPhoto = new JLabel("Awaiting Scan", SwingConstants.CENTER);
        lblStudentPhoto.setBounds(140, 75, 180, 170);
        lblStudentPhoto.setForeground(Color.LIGHT_GRAY);
        lblStudentPhoto.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblStudentPhoto.setBorder(BorderFactory.createLineBorder(new Color(56, 189, 248), 2));
        verifyPanel.add(lblStudentPhoto);

        lblStudentName = new JLabel("---", SwingConstants.CENTER);
        lblStudentName.setBounds(20, 265, 420, 32);
        lblStudentName.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblStudentName.setForeground(Color.WHITE);
        verifyPanel.add(lblStudentName);

        lblStudentBranch = new JLabel("---", SwingConstants.CENTER);
        lblStudentBranch.setBounds(20, 305, 420, 25);
        lblStudentBranch.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblStudentBranch.setForeground(Color.LIGHT_GRAY);
        verifyPanel.add(lblStudentBranch);

        lblStatusBadge = new JLabel("STANDBY - READY TO SCAN", SwingConstants.CENTER);
        lblStatusBadge.setBounds(40, 360, 380, 65);
        lblStatusBadge.setOpaque(true);
        lblStatusBadge.setBackground(new Color(51, 65, 85));
        lblStatusBadge.setForeground(Color.WHITE);
        lblStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 17));
        verifyPanel.add(lblStatusBadge);
    }

    private void scanQRFromFile() {
        File qrFile = PhotoHelper.showImageChooser(this);
        if (qrFile == null) return;

        try {
            BufferedImage bufferedImage = ImageIO.read(qrFile);
            BufferedImageLuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            Result result = new MultiFormatReader().decode(bitmap);

            String decodedText = result.getText();
            if (decodedText != null && decodedText.startsWith("SMARTPASS|")) {
                String[] parts = decodedText.split("\\|");
                if (parts.length >= 3) {
                    processStudentAttendance(parts[2]);
                    return;
                }
            }
            processStudentAttendance(decodedText);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to decode a valid QR payload from the selected image file.", "Invalid QR Code", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processStudentAttendance(String rollOrPassId) {
        try (Connection con = ConnectionProvider.getCon()) {
            PreparedStatement pst = con.prepareStatement("SELECT * FROM students WHERE roll_no = ? OR unique_reg_id = ? OR email = ?");
            pst.setString(1, rollOrPassId);
            pst.setString(2, rollOrPassId);
            pst.setString(3, rollOrPassId);
            ResultSet rs = pst.executeQuery();

            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "No matching student record found for the scanned credential.", "Unrecognized Pass", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int studentId = rs.getInt("id");
            String name = rs.getString("name");
            String roll = rs.getString("roll_no");
            String branch = rs.getString("branch") + " | " + rs.getString("semester");
            String imgName = rs.getString("image_name");

            lblStudentName.setText(name + " (" + roll + ")");
            lblStudentBranch.setText(branch);
            String imgPath = (imgName != null) ? "images/" + imgName : null;
            PhotoHelper.setPhotoOnLabel(lblStudentPhoto, imgPath, 180, 170, name.substring(0, 1).toUpperCase());

            LocalDate today = LocalDate.now();
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dtFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String nowStr = now.format(dtFormat);

            PreparedStatement attPst = con.prepareStatement("SELECT * FROM attendance WHERE student_id = ? AND att_date = ?");
            attPst.setInt(1, studentId);
            attPst.setString(2, today.toString());
            ResultSet attRs = attPst.executeQuery();

            if (!attRs.next()) {
                PreparedStatement ins = con.prepareStatement("INSERT INTO attendance (student_id, att_date, check_in) VALUES (?, ?, ?)");
                ins.setInt(1, studentId);
                ins.setString(2, today.toString());
                ins.setString(3, nowStr);
                ins.executeUpdate();

                lblStatusBadge.setBackground(new Color(34, 197, 94));
                lblStatusBadge.setText("CHECKED-IN AT " + now.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                JOptionPane.showMessageDialog(this, "Check-In recorded for " + name + " at " + nowStr, "Check-In Verified", JOptionPane.INFORMATION_MESSAGE);
            } else {
                String checkOutExisting = attRs.getString("check_out");
                if (checkOutExisting != null && !checkOutExisting.isEmpty()) {
                    lblStatusBadge.setBackground(new Color(239, 68, 68));
                    lblStatusBadge.setText("SESSION ALREADY COMPLETED TODAY");
                    JOptionPane.showMessageDialog(this, name + " has already completed Check-In and Check-Out for today.", "Session Complete", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                String checkInStr = attRs.getString("check_in");
                LocalDateTime checkInTime = LocalDateTime.parse(checkInStr, dtFormat);
                Duration dur = Duration.between(checkInTime, now);
                long hours = dur.toHours();
                long mins = dur.minusHours(hours).toMinutes();
                long secs = dur.minusHours(hours).minusMinutes(mins).getSeconds();

                String durationStr = hours + " hrs " + mins + " mins " + secs + " secs";

                PreparedStatement upd = con.prepareStatement("UPDATE attendance SET check_out = ?, work_duration = ? WHERE id = ?");
                upd.setString(1, nowStr);
                upd.setString(2, durationStr);
                upd.setInt(3, attRs.getInt("id"));
                upd.executeUpdate();

                lblStatusBadge.setBackground(new Color(249, 115, 22));
                lblStatusBadge.setText("CHECKED-OUT (" + hours + "h " + mins + "m)");
                JOptionPane.showMessageDialog(this, "Check-Out recorded successfully.\nTotal Session Duration: " + durationStr, "Check-Out Verified", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Verification Error: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}