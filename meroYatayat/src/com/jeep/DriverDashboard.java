package com.jeep;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import net.proteanit.sql.DbUtils;

import com.jeep.DbCon;

public class DriverDashboard extends JFrame {
    private static final long serialVersionUID = 1L;
	private String username;
    private int driverId;
    private JLabel lblSeats;
    private JLabel lblVehicleNo;
    private JLabel lblCurrentLocation;
    private JTable passengerTable;
    private JTable bookingsTable;

    public DriverDashboard(String username) {
        this.username = username;
        initializeUI();
        loadDriverInfo();
    }

    private void initializeUI() {
        setTitle("Driver Dashboard - " + username);
        setSize(900, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // 1. Driver Information Tab
        JPanel infoPanel = createInfoPanel();
        tabbedPane.addTab("My Information", infoPanel);

        // 2. Passenger Information Tab
        JPanel passengerPanel = createPassengerPanel();
        tabbedPane.addTab("My Passengers", passengerPanel);

        // 3. Bookings Management Tab
        JPanel bookingsPanel = createBookingsPanel();
        tabbedPane.addTab("Manage Bookings", bookingsPanel);

        add(tabbedPane);
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Driver Information
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Available Seats:"), gbc);
        gbc.gridx = 1;
        lblSeats = new JLabel("Loading...");
        panel.add(lblSeats, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Vehicle Number:"), gbc);
        gbc.gridx = 1;
        lblVehicleNo = new JLabel("Loading...");
        panel.add(lblVehicleNo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Current Location:"), gbc);
        gbc.gridx = 1;
        lblCurrentLocation = new JLabel("Loading...");
        panel.add(lblCurrentLocation, gbc);

        // Location Update
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("Update Location:"), gbc);
        gbc.gridx = 1;
        JTextField txtNewLocation = new JTextField(20);
        panel.add(txtNewLocation, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;
        JButton btnUpdateLocation = new JButton("Update Location");
        btnUpdateLocation.addActionListener(e -> {
            updateLocation(txtNewLocation.getText());
            txtNewLocation.setText("");
        });
        panel.add(btnUpdateLocation, gbc);

        // Sign Out Button
        gbc.gridy = 5;
        JButton btnSignOut = new JButton("Sign Out");
        btnSignOut.addActionListener(e -> {
            new Login().setVisible(true);
            dispose();
        });
        panel.add(btnSignOut, gbc);

        return panel;
    }

    private JPanel createPassengerPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Passenger Table
        passengerTable = new JTable();
        passengerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(passengerTable);

        // Refresh Button
        JButton btnRefresh = new JButton("Refresh Passengers");
        btnRefresh.addActionListener(e -> loadPassengers());

        panel.add(btnRefresh, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Bookings Table
        bookingsTable = new JTable();
        bookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(bookingsTable);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Refresh Bookings");
        btnRefresh.addActionListener(e -> loadBookings());

        JButton btnConfirm = new JButton("Confirm Booking");
        btnConfirm.addActionListener(this::confirmBooking);

        JButton btnComplete = new JButton("Mark as Completed");
        btnComplete.addActionListener(this::completeBooking);

        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnConfirm);
        buttonPanel.add(btnComplete);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadDriverInfo() {
        try (Connection conn = DbCon.getConnection()) {
            String sql = "SELECT d.id, d.available_seats, d.vehicle_no, d.current_location " +
                         "FROM Drivers d JOIN Users u ON d.user_id = u.id " +
                         "WHERE u.username = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                driverId = rs.getInt("id");
                lblSeats.setText(String.valueOf(rs.getInt("available_seats")));
                lblVehicleNo.setText(rs.getString("vehicle_no"));
                lblCurrentLocation.setText(rs.getString("current_location"));
                
                // Load related data
                loadPassengers();
                loadBookings();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading driver info: " + ex.getMessage());
        }
    }

    private void loadPassengers() {
        try (Connection conn = DbCon.getConnection()) {
            String sql = "SELECT p.name, p.contact, p.pickup_location, b.booking_time, b.status " +
                         "FROM Bookings b " +
                         "JOIN Passengers p ON b.passenger_id = p.id " +
                         "WHERE b.driver_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, driverId);
            ResultSet rs = pstmt.executeQuery();
            passengerTable.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading passengers: " + ex.getMessage());
        }
    }

    private void loadBookings() {
        try (Connection conn = DbCon.getConnection()) {
            String sql = "SELECT b.id, p.name as passenger, p.contact, b.pickup_location, " +
                         "b.booking_time, b.status " +
                         "FROM Bookings b " +
                         "JOIN Passengers p ON b.passenger_id = p.id " +
                         "WHERE b.driver_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, driverId);
            ResultSet rs = pstmt.executeQuery();
            bookingsTable.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading bookings: " + ex.getMessage());
        }
    }

    private void updateLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a location");
            return;
        }

        try (Connection conn = DbCon.getConnection()) {
            String sql = "UPDATE Drivers SET current_location = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, location);
            pstmt.setInt(2, driverId);
            pstmt.executeUpdate();

            lblCurrentLocation.setText(location);
            JOptionPane.showMessageDialog(this, "Location updated successfully!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating location: " + ex.getMessage());
        }
    }

    private void confirmBooking(ActionEvent e) {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to confirm");
            return;
        }

        int bookingId = (int) bookingsTable.getValueAt(selectedRow, 0);
        String currentStatus = (String) bookingsTable.getValueAt(selectedRow, 5);

        if ("confirmed".equals(currentStatus)) {
            JOptionPane.showMessageDialog(this, "Booking is already confirmed");
            return;
        }

        if ("completed".equals(currentStatus)) {
            JOptionPane.showMessageDialog(this, "Cannot confirm a completed booking");
            return;
        }

        try (Connection conn = DbCon.getConnection()) {
            String sql = "UPDATE Bookings SET status = 'confirmed' WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookingId);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Booking confirmed successfully!");
            loadBookings();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error confirming booking: " + ex.getMessage());
        }
    }

    private void completeBooking(ActionEvent e) {
        int selectedRow = bookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to complete");
            return;
        }

        int bookingId = (int) bookingsTable.getValueAt(selectedRow, 0);
        String currentStatus = (String) bookingsTable.getValueAt(selectedRow, 5);

        if ("completed".equals(currentStatus)) {
            JOptionPane.showMessageDialog(this, "Booking is already completed");
            return;
        }

        if (!"confirmed".equals(currentStatus)) {
            JOptionPane.showMessageDialog(this, "Please confirm the booking first");
            return;
        }

        try (Connection conn = DbCon.getConnection()) {
            // Start transaction
            conn.setAutoCommit(false);

            try {
                // Update booking status
                String bookingSql = "UPDATE Bookings SET status = 'completed' WHERE id = ?";
                PreparedStatement bookingStmt = conn.prepareStatement(bookingSql);
                bookingStmt.setInt(1, bookingId);
                bookingStmt.executeUpdate();

                // Increment available seats
                String seatsSql = "UPDATE Drivers SET available_seats = available_seats + 1 WHERE id = ?";
                PreparedStatement seatsStmt = conn.prepareStatement(seatsSql);
                seatsStmt.setInt(1, driverId);
                seatsStmt.executeUpdate();

                conn.commit();
                JOptionPane.showMessageDialog(this, "Booking marked as completed!");
                loadBookings();
                loadDriverInfo(); // Refresh seat count
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error completing booking: " + ex.getMessage());
        }
    }
}