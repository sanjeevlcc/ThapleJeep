package com.jeep;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class Login extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public Login() {
        setTitle("Hamro Kaligandaki Coridoor - Login");
        setSize(400, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
       
      

        // Main panel with form
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo or title
        
        
     // === Logo (row 0, col 0, top-left) ===
        ImageIcon icon = new ImageIcon(getClass().getResource("admin.png"));
        Image scaledImage = icon.getImage().getScaledInstance(200, 90, Image.SCALE_SMOOTH);
        ImageIcon smallIcon = new ImageIcon(scaledImage);

        JLabel lblLogo = new JLabel(smallIcon);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;  // align left
        formPanel.add(lblLogo, gbc);

        // === Title (row 1, span across 2 cols, centered) ===
        JLabel lblTitle = new JLabel("JEEP TRANSPORT SYSTEM", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;  // align center
        formPanel.add(lblTitle, gbc);

        // === Username (row 2) ===
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST; // align label right
        formPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST; // align field left
        txtUsername = new JTextField(15);
        formPanel.add(txtUsername, gbc);

        // === Password (row 3) ===
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        formPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        txtPassword = new JPasswordField(15);
        formPanel.add(txtPassword, gbc);

        

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnLogin = new JButton("Login");
        btnLogin.addActionListener(this::performLogin);
        buttonPanel.add(btnLogin);

        JButton btnSignUp = new JButton("Passenger Sign Up");
        btnSignUp.addActionListener(this::showSignUpForm);
        buttonPanel.add(btnSignUp);
        
        ///////////////--///////

        // Add components to frame
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void performLogin(ActionEvent e) {
    	
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password!");
            return;
        }

        try (Connection conn = DbCon.getConnection()) {
            String sql = "SELECT role FROM Users WHERE username = ? AND password = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role");
                switch (role) {
                    case "admin":
                        new AdminDashboard().setVisible(true);
                        break;
                    case "driver":
                        new DriverDashboard(username).setVisible(true);
                        break;
                    case "passenger":
                        new PassengerDashboard(username).setVisible(true);
                        break;
                }
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password!");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void showSignUpForm(ActionEvent e) {
        JDialog signUpDialog = new JDialog(this, "Passenger Registration", true);
        signUpDialog.setSize(400, 350);
        signUpDialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Form fields
        JLabel lblTitle = new JLabel("Create Passenger Account", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        signUpDialog.add(lblTitle, gbc);

        JTextField txtNewUsername = new JTextField(15);
        JPasswordField txtNewPassword = new JPasswordField(15);
        JTextField txtName = new JTextField(15);
        JTextField txtMobile = new JTextField(15);
        JTextField txtPickupLocation = new JTextField(15);

        // Username
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        signUpDialog.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        signUpDialog.add(txtNewUsername, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        signUpDialog.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        signUpDialog.add(txtNewPassword, gbc);

        // Name
        gbc.gridx = 0;
        gbc.gridy = 3;
        signUpDialog.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        signUpDialog.add(txtName, gbc);

        // Mobile
        gbc.gridx = 0;
        gbc.gridy = 4;
        signUpDialog.add(new JLabel("Mobile:"), gbc);
        gbc.gridx = 1;
        signUpDialog.add(txtMobile, gbc);

        // Pickup Location
        gbc.gridx = 0;
        gbc.gridy = 5;
        signUpDialog.add(new JLabel("Pickup Location:"), gbc);
        gbc.gridx = 1;
        signUpDialog.add(txtPickupLocation, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnCreate = new JButton("Create Account");
        btnCreate.addActionListener(ev -> createPassengerAccount(
            txtNewUsername.getText(),
            new String(txtNewPassword.getPassword()),
            txtName.getText(),
            txtMobile.getText(),
            txtPickupLocation.getText(),
            signUpDialog
        ));
        buttonPanel.add(btnCreate);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(ev -> signUpDialog.dispose());
        buttonPanel.add(btnCancel);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        signUpDialog.add(buttonPanel, gbc);

        signUpDialog.setVisible(true);
    }

    private void createPassengerAccount(String username, String password, String name, 
                                      String mobile, String pickupLocation, JDialog dialog) {
        if (username.isEmpty() || password.isEmpty() || name.isEmpty() || mobile.isEmpty() || pickupLocation.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Please fill all fields!");
            return;
        }

        if (!mobile.matches("\\d{10}")) {
            JOptionPane.showMessageDialog(dialog, "Please enter a valid 10-digit mobile number!");
            return;
        }

        try (Connection conn = DbCon.getConnection()) {
            // Check if username exists
            String checkSql = "SELECT id FROM Users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                JOptionPane.showMessageDialog(dialog, "Username already exists!");
                return;
            }

            // Start transaction
            conn.setAutoCommit(false);
            
            try {
                // Add to Users table
                String userSql = "INSERT INTO Users (username, password, role) VALUES (?, ?, 'passenger')";
                PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
                userStmt.setString(1, username);
                userStmt.setString(2, password);
                userStmt.executeUpdate();
                
                // Get generated user ID
                ResultSet generatedKeys = userStmt.getGeneratedKeys();
                if (!generatedKeys.next()) {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
                int userId = generatedKeys.getInt(1);
                
                // Add to Passengers table
                String passengerSql = "INSERT INTO Passengers (user_id, name, contact, pickup_location) VALUES (?, ?, ?, ?)";
                PreparedStatement passengerStmt = conn.prepareStatement(passengerSql);
                passengerStmt.setInt(1, userId);
                passengerStmt.setString(2, name);
                passengerStmt.setString(3, mobile);
                passengerStmt.setString(4, pickupLocation);
                passengerStmt.executeUpdate();
                
                conn.commit();
                JOptionPane.showMessageDialog(dialog, "Account created successfully! You can now login.");
                dialog.dispose();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(dialog, "Error creating account: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new Login().setVisible(true);
        });
    }
}