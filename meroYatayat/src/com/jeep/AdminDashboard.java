package com.jeep;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;



import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import net.proteanit.sql.DbUtils;



// Adding backgraound

class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    public BackgroundPanel(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
        setLayout(new BorderLayout()); // so you can add child components
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            // Scale image to fit window
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}


///woring on construcotor

public class AdminDashboard extends JFrame {
    private static final long serialVersionUID = 1L;
	private JTable driversTable;
    private JTable passengersTable;
    private JTable bookingsTable;
    private JComboBox<String> locationComboBox;
    private List<String> pickupLocations;

    public AdminDashboard() {
        setTitle("Admin Dashboard - Jeep Transport System");
        setSize(1100, 700);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        ImageIcon bgIcon = new ImageIcon("background.jpg");  
        BackgroundPanel bgPanel = new BackgroundPanel(bgIcon.getImage());

        setContentPane(bgPanel); 
        
        
        pickupLocations = new ArrayList<>();
        loadPickupLocations();

        JTabbedPane tabbedPane = new JTabbedPane();

        // 1. Drivers Management Tab
        JPanel driversPanel = createDriversPanel();
        tabbedPane.addTab("Manage Drivers", driversPanel);

        // 2. Passengers Management Tab
        JPanel passengersPanel = createPassengersPanel();
        tabbedPane.addTab("Manage Passengers", passengersPanel);

        // 3. Bookings Management Tab
        JPanel bookingsPanel = createBookingsPanel();
        tabbedPane.addTab("Manage Bookings", bookingsPanel);

        // 4. Location Management Tab
        JPanel locationsPanel = createLocationsPanel();
        tabbedPane.addTab("Manage Locations", locationsPanel);

        add(tabbedPane);
        
        // Load initial data
        loadDrivers();
        loadPassengers();
        loadBookings();
    }

    private JPanel createDriversPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Toolbar with buttons
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadDrivers());
        
        JButton btnAddDriver = new JButton("Add Driver");
        btnAddDriver.addActionListener(this::showAddDriverDialog);
        
        JButton btnDeleteDriver = new JButton("Delete Driver");
        btnDeleteDriver.addActionListener(this::deleteDriver);
        
        JButton btnSignOut = new JButton("Sign Out");
        btnSignOut.addActionListener(e -> {
            new Login().setVisible(true);
            dispose();
        });
        
        toolbar.add(btnRefresh);
        toolbar.add(btnAddDriver);
        toolbar.add(btnDeleteDriver);
        toolbar.add(btnSignOut);
        
        // Drivers table
        driversTable = new JTable();
        driversTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(driversTable), BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createPassengersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Toolbar with buttons and filter
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadPassengers());
        
        JButton btnDeletePassenger = new JButton("Delete Passenger");
        btnDeletePassenger.addActionListener(this::deletePassenger);
        
        locationComboBox = new JComboBox<>();
        locationComboBox.addItem("All Locations");
        for (String location : pickupLocations) {
            locationComboBox.addItem(location);
        }
        locationComboBox.addActionListener(e -> filterPassengersByLocation());
        
        toolbar.add(btnRefresh);
        toolbar.add(btnDeletePassenger);
        toolbar.add(new JLabel("Filter by Location:"));
        toolbar.add(locationComboBox);
        
        // Passengers table
        passengersTable = new JTable();
        passengersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(passengersTable), BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JButton btnRefresh = new JButton("Refresh Bookings");
        btnRefresh.addActionListener(e -> loadBookings());
        
        // Bookings table
        bookingsTable = new JTable();
        
        panel.add(btnRefresh, BorderLayout.NORTH);
        panel.add(new JScrollPane(bookingsTable), BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createLocationsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Location management components
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField txtNewLocation = new JTextField(20);
        JButton btnAddLocation = new JButton("Add Location");
        JButton btnRemoveLocation = new JButton("Remove Selected");
        
        JList<String> locationsList = new JList<>(pickupLocations.toArray(new String[0]));
        locationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        btnAddLocation.addActionListener(e -> {
            String newLocation = txtNewLocation.getText().trim();
            if (!newLocation.isEmpty()) {
                addPickupLocation(newLocation);
                txtNewLocation.setText("");
            }
        });
        
        btnRemoveLocation.addActionListener(e -> {
            String selected = locationsList.getSelectedValue();
            if (selected != null) {
                removePickupLocation(selected);
            }
        });
        
        inputPanel.add(new JLabel("New Location:"));
        inputPanel.add(txtNewLocation);
        inputPanel.add(btnAddLocation);
        inputPanel.add(btnRemoveLocation);
        
        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(locationsList), BorderLayout.CENTER);
        
        return panel;
    }

    private void loadPickupLocations() {
        pickupLocations.clear();
        try (Connection conn = DbCon.getConnection()) {
            String sql = "SELECT DISTINCT pickup_location FROM Passengers WHERE pickup_location IS NOT NULL";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                pickupLocations.add(rs.getString("pickup_location"));
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading locations: " + ex.getMessage());
        }
    }

    private void loadDrivers() {
        try (Connection conn = DbCon.getConnection()) {
            String sql = "SELECT d.id, d.name, d.vehicle_no, d.contact, d.current_location, d.available_seats FROM Drivers d";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            driversTable.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading drivers: " + ex.getMessage());
        }
    }

    private void loadPassengers() {
        try (Connection conn = DbCon.getConnection()) {
            String sql = "SELECT p.id, p.name, p.contact, p.pickup_location FROM Passengers p";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            passengersTable.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading passengers: " + ex.getMessage());
        }
    }

    private void filterPassengersByLocation() {
        String selectedLocation = (String) locationComboBox.getSelectedItem();
        if (selectedLocation == null || "All Locations".equals(selectedLocation)) {
            loadPassengers();
            return;
        }

        try (Connection conn = DbCon.getConnection()) {
            String sql = "SELECT p.id, p.name, p.contact, p.pickup_location FROM Passengers p WHERE p.pickup_location = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, selectedLocation);
            ResultSet rs = pstmt.executeQuery();
            passengersTable.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error filtering passengers: " + ex.getMessage());
        }
    }

    private void loadBookings() {
        try (Connection conn = DbCon.getConnection()) {
            String sql = "SELECT b.id, p.name as passenger, d.name as driver, b.pickup_location, " +
                       "b.booking_time, b.status FROM Bookings b " +
                       "JOIN Passengers p ON b.passenger_id = p.id " +
                       "JOIN Drivers d ON b.driver_id = d.id";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            bookingsTable.setModel(DbUtils.resultSetToTableModel(rs));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading bookings: " + ex.getMessage());
        }
    }

    private void showAddDriverDialog(ActionEvent e) {
        JDialog dialog = new JDialog(this, "Add New Driver", true);
        dialog.setSize(400, 350);
        dialog.setLayout(new GridLayout(7, 2, 5, 5));
        
        JTextField txtUsername = new JTextField();
        JPasswordField txtPassword = new JPasswordField();
        JTextField txtName = new JTextField();
        JTextField txtVehicleNo = new JTextField();
        JTextField txtContact = new JTextField();
        JComboBox<String> cbLocation = new JComboBox<>(pickupLocations.toArray(new String[0]));
        
        dialog.add(new JLabel("Username:"));
        dialog.add(txtUsername);
        dialog.add(new JLabel("Password:"));
        dialog.add(txtPassword);
        dialog.add(new JLabel("Full Name:"));
        dialog.add(txtName);
        dialog.add(new JLabel("Vehicle No:"));
        dialog.add(txtVehicleNo);
        dialog.add(new JLabel("Contact:"));
        dialog.add(txtContact);
        dialog.add(new JLabel("Default Location:"));
        dialog.add(cbLocation);
        
        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(ev -> {
            try (Connection conn = DbCon.getConnection()) {
                // Add to Users table
                String userSql = "INSERT INTO Users (username, password, role) VALUES (?, ?, 'driver')";
                PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
                userStmt.setString(1, txtUsername.getText());
                userStmt.setString(2, new String(txtPassword.getPassword()));
                userStmt.executeUpdate();
                
                // Get generated user ID
                ResultSet rs = userStmt.getGeneratedKeys();
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    
                    // Add to Drivers table
                    String driverSql = "INSERT INTO Drivers (user_id, name, vehicle_no, contact, current_location) " +
                                     "VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement driverStmt = conn.prepareStatement(driverSql);
                    driverStmt.setInt(1, userId);
                    driverStmt.setString(2, txtName.getText());
                    driverStmt.setString(3, txtVehicleNo.getText());
                    driverStmt.setString(4, txtContact.getText());
                    driverStmt.setString(5, cbLocation.getSelectedItem().toString());
                    driverStmt.executeUpdate();
                    
                    JOptionPane.showMessageDialog(dialog, "Driver added successfully!");
                    dialog.dispose();
                    loadDrivers();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Error adding driver: " + ex.getMessage());
            }
        });
        
        dialog.add(btnSave);
        dialog.setVisible(true);
    }

    private void deleteDriver(ActionEvent e) {
        int selectedRow = driversTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a driver to delete");
            return;
        }
        
        int driverId = (int) driversTable.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this driver?", "Confirm", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DbCon.getConnection()) {
                // First get user_id
                String getUserIdSql = "SELECT user_id FROM Drivers WHERE id = ?";
                PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdSql);
                getUserIdStmt.setInt(1, driverId);
                ResultSet rs = getUserIdStmt.executeQuery();
                
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    
                    // Delete from Drivers
                    String deleteDriverSql = "DELETE FROM Drivers WHERE id = ?";
                    PreparedStatement deleteDriverStmt = conn.prepareStatement(deleteDriverSql);
                    deleteDriverStmt.setInt(1, driverId);
                    deleteDriverStmt.executeUpdate();
                    
                    // Delete from Users
                    String deleteUserSql = "DELETE FROM Users WHERE id = ?";
                    PreparedStatement deleteUserStmt = conn.prepareStatement(deleteUserSql);
                    deleteUserStmt.setInt(1, userId);
                    deleteUserStmt.executeUpdate();
                    
                    JOptionPane.showMessageDialog(this, "Driver deleted successfully!");
                    loadDrivers();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting driver: " + ex.getMessage());
            }
        }
    }

    private void deletePassenger(ActionEvent e) {
        int selectedRow = passengersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a passenger to delete");
            return;
        }
        
        int passengerId = (int) passengersTable.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this passenger?", "Confirm", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DbCon.getConnection()) {
                // First get user_id
                String getUserIdSql = "SELECT user_id FROM Passengers WHERE id = ?";
                PreparedStatement getUserIdStmt = conn.prepareStatement(getUserIdSql);
                getUserIdStmt.setInt(1, passengerId);
                ResultSet rs = getUserIdStmt.executeQuery();
                
                if (rs.next()) {
                    int userId = rs.getInt("user_id");
                    
                    // Delete from Passengers
                    String deletePassengerSql = "DELETE FROM Passengers WHERE id = ?";
                    PreparedStatement deletePassengerStmt = conn.prepareStatement(deletePassengerSql);
                    deletePassengerStmt.setInt(1, passengerId);
                    deletePassengerStmt.executeUpdate();
                    
                    // Delete from Users
                    String deleteUserSql = "DELETE FROM Users WHERE id = ?";
                    PreparedStatement deleteUserStmt = conn.prepareStatement(deleteUserSql);
                    deleteUserStmt.setInt(1, userId);
                    deleteUserStmt.executeUpdate();
                    
                    JOptionPane.showMessageDialog(this, "Passenger deleted successfully!");
                    loadPassengers();
                    loadPickupLocations();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting passenger: " + ex.getMessage());
            }
        }
    }

    private void addPickupLocation(String location) {
        // This just adds to the list for the admin interface
        // In a real application, you might want to store these in a separate table
        if (!pickupLocations.contains(location)) {
            pickupLocations.add(location);
            locationComboBox.addItem(location);
            JOptionPane.showMessageDialog(this, "Location added to filter list");
        } else {
            JOptionPane.showMessageDialog(this, "Location already exists");
        }
    }

    private void removePickupLocation(String location) {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Remove this location from the filter list?", "Confirm", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            pickupLocations.remove(location);
            locationComboBox.removeItem(location);
            JOptionPane.showMessageDialog(this, "Location removed from filter list");
        }
    }
}