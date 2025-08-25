# ThapleJeep
"ThapleJeep: Kaligandaki Corridor Transport Coordination System (KCTCS)"



# 🚙 Jeep Transport Database (MariaDB/MySQL)

This project contains the SQL schema for a **Jeep Transport Management System**, built using **MariaDB/MySQL**.  
It defines tables for managing **users, drivers, passengers, and bookings** with proper relationships and constraints.  

---

## 📂 Database Schema

### Database
```sql
CREATE DATABASE jeeptransport;
USE jeeptransport; ```


## Tables
### Users – Stores login and role info (admin, driver, passenger).
### Drivers – Information about jeep drivers and vehicles.
### Passengers – Passenger details and pickup locations.
### Bookings – Links passengers and drivers with booking details.















# 🚙 Jeep Transport Database (MariaDB/MySQL)

This project contains the SQL schema for a **Jeep Transport Management System**, built using **MariaDB/MySQL**.  
It defines tables for managing **users, drivers, passengers, and bookings** with proper relationships and constraints.  

---

## 📂 Database Schema

### Database
```sql
CREATE DATABASE jeeptransport;
USE jeeptransport;
```

### Tables
1. **Users** – Stores login and role info (admin, driver, passenger).  
2. **Drivers** – Information about jeep drivers and vehicles.  
3. **Passengers** – Passenger details and pickup locations.  
4. **Bookings** – Links passengers and drivers with booking details.  

---

## 🗄️ SQL Script

```sql
-- Users table
CREATE TABLE users (
    id INT(11) AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(50) NOT NULL,
    role ENUM('admin','driver','passenger') NOT NULL
);

-- Drivers table
CREATE TABLE drivers (
    id INT(11) AUTO_INCREMENT PRIMARY KEY,
    user_id INT(11) NOT NULL,
    name VARCHAR(100) NOT NULL,
    vehicle_no VARCHAR(20) NOT NULL,
    contact VARCHAR(15) NOT NULL,
    current_location VARCHAR(100),
    available_seats INT(11) DEFAULT 14,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Passengers table
CREATE TABLE passengers (
    id INT(11) AUTO_INCREMENT PRIMARY KEY,
    user_id INT(11) NOT NULL,
    name VARCHAR(100) NOT NULL,
    contact VARCHAR(15) NOT NULL,
    pickup_location VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Bookings table
CREATE TABLE bookings (
    id INT(11) AUTO_INCREMENT PRIMARY KEY,
    passenger_id INT(11) NOT NULL,
    driver_id INT(11) NOT NULL,
    booking_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    pickup_location VARCHAR(100) NOT NULL,
    status ENUM('pending','confirmed','completed') DEFAULT 'pending',
    FOREIGN KEY (passenger_id) REFERENCES passengers(id),
    FOREIGN KEY (driver_id) REFERENCES drivers(id)
);
```

---

## 🔑 Relationships

- A **user** can be either a **driver** or a **passenger**.  
- A **driver** can have multiple **bookings**.  
- A **passenger** can make multiple **bookings**.  
- Foreign keys enforce referential integrity between tables.  

---

## 🚀 How to Use

1. Clone this repository:
   ```bash
   git clone https://github.com/your-username/jeeptransport-db.git
   ```
2. Open **MariaDB/MySQL CLI**.  
3. Run the provided SQL script:
   ```bash
   source jeeptransport.sql;
   ```
4. Start inserting sample data and test queries.  

---

## 📌 Example Insert Queries

```sql
-- Add an admin user
INSERT INTO users (username, password, role) VALUES ('admin1', 'adminpass', 'admin');

-- Add a driver
INSERT INTO users (username, password, role) VALUES ('driver1', 'driverpass', 'driver');
INSERT INTO drivers (user_id, name, vehicle_no, contact, current_location) 
VALUES (2, 'Ram Bahadur', 'BA-2-CHA-1234', '9800000000', 'Kathmandu');

-- Add a passenger
INSERT INTO users (username, password, role) VALUES ('passenger1', 'passpass', 'passenger');
INSERT INTO passengers (user_id, name, contact, pickup_location) 
VALUES (3, 'Sita Thapa', '9811111111', 'Pokhara');

-- Add a booking
INSERT INTO bookings (passenger_id, driver_id, pickup_location, status) 
VALUES (1, 1, 'Pokhara Buspark', 'pending');
```

---

## 📖 License
This project is open-source. You can use, modify, and share under the MIT License.

---

👨‍💻 Developed for **database learning and transport management system prototyping**.










