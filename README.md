# 🎓 SmartPass: QR Campus Entry & Attendance Suite

A modern, role-based Desktop Application built with **Core Java (Swing GUI)**, **JDBC**, **MySQL**, and the **Google ZXing Library** for automated QR-based student entry and real-time attendance tracking.

---

## 🚀 Key Features

* **Role-Based Authentication:** Separate secure login & registration portals for **Administrators** and **Students**.
* **Dynamic QR Pass Generation:** Automatically generates unique, encrypted QR Code ID passes for registered students using the ZXing API.
* **Smart Attendance Terminal:** Scans student QR passes to log real-time **Check-In** and **Check-Out** timestamps and automatically calculates total campus/work duration.
* **Student Directory Management:** Full CRUD functionality (Search, Add, Update, Delete) for managing student profiles and photos.
* **Live Cloud Database Sync:** Configured via externalized `.properties` architecture to support both local (`localhost`) and live Cloud MySQL servers.
* **Modern Dark UI:** Custom-Engineered Swing dark theme with smooth hover transitions and statistical summary cards.

---

## 🛠️ Tech Stack

* **Language:** Java (JDK 8+)
* **Frontend GUI:** Java Swing & AWT (Custom Dark Theme)
* **Database:** MySQL (Cloud / Localhost via JDBC)
* **External Libraries:** 
  * `mysql-connector-j` (Database Connectivity)
  * `zxing-core` & `zxing-javase` (QR Code Generation & Decoding)

---

## ⚙️ How to Run Locally

1. **Clone the Repository:**
   ```bash
   git clone [https://github.com/masiha-2/SmartPass_Project.git]
             (https://github.com/masiha-2/SmartPass_Project.git)

(i) Database Setup:
   Execute the following SQL queries in your MySQL Workbench:

   SQL
   CREATE TABLE admins (
      id INT AUTO_INCREMENT PRIMARY KEY,
      full_name VARCHAR(100) NOT NULL,
      email VARCHAR(100) UNIQUE NOT NULL,
      password VARCHAR(100) NOT NULL,
      image_name VARCHAR(100)
   );

   CREATE TABLE students (
      id INT AUTO_INCREMENT PRIMARY KEY,
      name VARCHAR(255) NOT NULL,
      roll_no VARCHAR(50) UNIQUE NOT NULL,
      gender VARCHAR(20) NOT NULL,
      email VARCHAR(255) UNIQUE NOT NULL,
      contact VARCHAR(20) NOT NULL,
      branch VARCHAR(50) NOT NULL,
      semester VARCHAR(20) NOT NULL,
      unique_reg_id VARCHAR(100) NOT NULL,
      image_name VARCHAR(100),
      password VARCHAR(100) NOT NULL
   );

   CREATE TABLE attendance (
      id INT AUTO_INCREMENT PRIMARY KEY,
      student_id INT NOT NULL,
      att_date DATE NOT NULL,
      check_in DATETIME,
      check_out DATETIME,
      work_duration VARCHAR(100)
   );
   
(ii) Configure db.properties:
   Create a file named db.properties in the root folder and add your credentials:

   Properties
   
   db.host=localhost
   db.port=3306
   db.name=smartpass_db
   db.user=root
   db.password=your_mysql_password

   
(iii) Run the Application:
   Compile and run forms/Login.java from your IDE.




👨‍💻 Developed By
Mohd Masihullah

B.Tech in Computer Science & Engineering

   
