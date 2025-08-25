package com.jeep;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import net.proteanit.sql.DbUtils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class PassengerDashboard extends JFrame {
    private static final long serialVersionUID = 1L;
	private String username;
    private int passengerId;
    private JTable availableJeepsTable;
    private JTable myBookingsTable;

    public PassengerDashboard(String username) {
        this.username = username;
        initializeUI();
        loadPassengerId();
        loadAvailableJeeps();
        loadMyBookings();
    }

    private void initializeUI() {
        setTitle("Passenger Dashboard - " + username);
        setSize(900, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        // 1. Available Jeeps Tab
        JPanel availableJeepsPanel = createAvailableJeepsPanel();
        tabbedPane.addTab("Available Jeeps", availableJeepsPanel);

        // 2. My Bookings Tab
        JPanel myBookingsPanel = createMyBookingsPanel();
        tabbedPane.addTab("My Bookings", myBookingsPanel);

        add(tabbedPane);
    }

    private JPanel createAvailableJeepsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Available Jeeps Table
        availableJeepsTable = new JTable();
        availableJeepsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(availableJeepsTable);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Refresh Available Jeeps");
        btnRefresh.addActionListener(e -> loadAvailableJeeps());

        JButton btnBook = new JButton("Book Selected Jeep");
        btnBook.addActionListener(this::bookJeep);

        JButton btnSignOut = new JButton("Sign Out");
        btnSignOut.addActionListener(e -> {
            new Login().setVisible(true);
            dispose();
        });

        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnBook);
        buttonPanel.add(btnSignOut);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createMyBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // My Bookings Table
        myBookingsTable = new JTable();
        myBookingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(myBookingsTable);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Refresh My Bookings");
        btnRefresh.addActionListener(e -> loadMyBookings());

        JButton btnCancel = new JButton("Cancel Booking");
        btnCancel.addActionListener(this::cancelBooking);

        buttonPanel.add(btnRefresh);
        buttonPanel.add(btnCancel);

        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadPassengerId() {
        try (Connection conn = DbCon.getConnection()) {
            String sql = "SELECT p.id FROM Passengers p JOIN Users u ON p.user_id = u.id WHERE u.username = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                passengerId = rs.getInt("id");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading passenger info: " + ex.getMessage());
        }
    }

    private void loadAvailableJeeps() {
        try (Connection conn = DbCon.getConnection()) {
            String sql = "SELECT d.id, d.name, d.vehicle_no, d.contact, d.current_location, d.available_seats " +
                         "FROM Drivers d WHERE d.available_seats > 0";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            availableJeepsTable.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading available jeeps: " + ex.getMessage());
        }
    }

    private void loadMyBookings() {
        try (Connection conn = DbCon.getConnection()) {
            String sql = "SELECT b.id, d.name as driver, d.vehicle_no, b.pickup_location, " +
                         "b.booking_time, b.status FROM Bookings b " +
                         "JOIN Drivers d ON b.driver_id = d.id " +
                         "WHERE b.passenger_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, passengerId);
            ResultSet rs = pstmt.executeQuery();
            myBookingsTable.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading your bookings: " + ex.getMessage());
        }
    }

    private void bookJeep(ActionEvent e) {
        int selectedRow = availableJeepsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a jeep to book");
            return;
        }

        int driverId = (int) availableJeepsTable.getValueAt(selectedRow, 0);
        int availableSeats = (int) availableJeepsTable.getValueAt(selectedRow, 5);

        if (availableSeats <= 0) {
            JOptionPane.showMessageDialog(this, "No available seats in this jeep");
            return;
        }

        String pickupLocation = JOptionPane.showInputDialog(this, "Enter your pickup location:");
        if (pickupLocation == null || pickupLocation.trim().isEmpty()) {
            return;
        }

        try (Connection conn = DbCon.getConnection()) {
            // Start transaction
            conn.setAutoCommit(false);

            try {
                // Create booking
                String bookingSql = "INSERT INTO Bookings (passenger_id, driver_id, pickup_location) VALUES (?, ?, ?)";
                PreparedStatement bookingStmt = conn.prepareStatement(bookingSql);
                bookingStmt.setInt(1, passengerId);
                bookingStmt.setInt(2, driverId);
                bookingStmt.setString(3, pickupLocation);
                bookingStmt.executeUpdate();

                // Update available seats
                String seatsSql = "UPDATE Drivers SET available_seats = available_seats - 1 WHERE id = ?";
                PreparedStatement seatsStmt = conn.prepareStatement(seatsSql);
                seatsStmt.setInt(1, driverId);
                seatsStmt.executeUpdate();

                conn.commit();
                JOptionPane.showMessageDialog(this, "Booking created successfully!");
                loadAvailableJeeps();
                loadMyBookings();
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error creating booking: " + ex.getMessage());
        }
    }

    private void cancelBooking(ActionEvent e) {
        int selectedRow = myBookingsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a booking to cancel");
            return;
        }

        int bookingId = (int) myBookingsTable.getValueAt(selectedRow, 0);
        String status = (String) myBookingsTable.getValueAt(selectedRow, 5);

        if ("completed".equals(status)) {
            JOptionPane.showMessageDialog(this, "Cannot cancel a completed booking");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to cancel this booking?", "Confirm", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DbCon.getConnection()) {
                // Start transaction
                conn.setAutoCommit(false);

                try {
                    // First get driver_id
                    String getDriverSql = "SELECT driver_id FROM Bookings WHERE id = ?";
                    PreparedStatement getDriverStmt = conn.prepareStatement(getDriverSql);
                    getDriverStmt.setInt(1, bookingId);
                    ResultSet rs = getDriverStmt.executeQuery();

                    if (rs.next()) {
                        int driverId = rs.getInt("driver_id");

                        // Delete booking
                        String deleteSql = "DELETE FROM Bookings WHERE id = ?";
                        PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                        deleteStmt.setInt(1, bookingId);
                        deleteStmt.executeUpdate();

                        // Increment available seats
                        String seatsSql = "UPDATE Drivers SET available_seats = available_seats + 1 WHERE id = ?";
                        PreparedStatement seatsStmt = conn.prepareStatement(seatsSql);
                        seatsStmt.setInt(1, driverId);
                        seatsStmt.executeUpdate();

                        conn.commit();
                        JOptionPane.showMessageDialog(this, "Booking cancelled successfully!");
                        loadMyBookings();
                        loadAvailableJeeps();
                    }
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error cancelling booking: " + ex.getMessage());
            }
        }
    }
}