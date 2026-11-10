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