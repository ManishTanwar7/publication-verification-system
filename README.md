# Research Publication Verification & Management System

A desktop application built with **JavaFX**, **IntelliJ IDEA**, **Scene Builder**, and **MySQL**, designed to verify academic publications (Paper Title, DOI, Authors, Year, Journal) against official scholarly registries (Crossref REST API) and manage publication records.

---

## 🛠 Tech Stack
- **IDE**: IntelliJ IDEA (Community or Ultimate)
- **UI Framework**: JavaFX 21 (Controls + FXML)
- **UI Designer**: Gluon Scene Builder
- **Database**: MySQL Server 8.0+ / MySQL Workbench
- **Data Access**: JDBC with PreparedStatement and Connection Pooling
- **External API**: Crossref REST API (`https://api.crossref.org/works/{doi}`)
- **Build Tool**: Apache Maven (JDK 17 or 21)

---

## 📂 Project Structure

```text
publication-verification-system/
├── pom.xml                                   # Maven dependencies & build configuration
├── database/
│   └── schema.sql                            # Database DDL, indexes, and initial seed data
├── src/
│   └── main/
│       ├── java/
│       │   └── com/research/pubmanager/
│       │       ├── Launcher.java             # Main runner (bypasses JavaFX module errors)
│       │       ├── MainApp.java              # JavaFX Application stage setup
│       │       ├── config/
│       │       │   └── DatabaseConnection.java# JDBC connection manager
│       │       ├── model/
│       │       │   └── Publication.java      # JavaFX Property-bound Data Model
│       │       ├── dao/
│       │       │   ├── PublicationDAO.java   # Data Access Interface
│       │       │   └── PublicationDAOImpl.java # JDBC Implementation
│       │       ├── service/
│       │       │   └── CrossrefVerificationService.java # Crossref API fetch & compare
│       │       └── controller/
│       │           ├── DashboardController.java # Main UI event handler
│       │           └── PublicationDialogController.java # Add/Edit form controller
│       └── resources/
│           └── com/research/pubmanager/view/
│               ├── Dashboard.fxml            # Main dashboard FXML (Scene Builder ready)
│               ├── PublicationDialog.fxml    # Modal form FXML (Scene Builder ready)
│               └── styles.css                # Modern CSS styling
└── README.md
```

---

## 🚀 Step-by-Step Setup Guide

### 1. Database Setup (MySQL Workbench)
1. Open **MySQL Workbench** and connect to your local MySQL Server.
2. Open the file `database/schema.sql` (`File -> Open SQL Script...`).
3. Click the **Execute (⚡ Lightning Bolt)** button to execute the script.
4. This creates the database `research_pub_db`, the `publications` table, and initial seed records.

### 2. Configure Database Credentials
Open `src/main/java/com/research/pubmanager/config/DatabaseConnection.java` and adjust your MySQL password:
```java
private static final String PASSWORD = "your_mysql_password"; // Set your MySQL password here
```

### 3. Open in IntelliJ IDEA
1. Open **IntelliJ IDEA**.
2. Select **File → Open...** and choose the `publication-verification-system` folder.
3. IntelliJ will detect it as a Maven project and automatically download dependencies (JavaFX, MySQL Driver, JSON parser).

### 4. Link Scene Builder
1. Install [Gluon Scene Builder](https://gluonhq.com/products/scene-builder/) if you haven't already.
2. In IntelliJ: **Settings (`Ctrl+Alt+S`) → Languages & Frameworks → JavaFX**.
3. Set the **Path to SceneBuilder** (e.g. `C:\Program Files\SceneBuilder\SceneBuilder.exe`).
4. You can now right-click `Dashboard.fxml` or `PublicationDialog.fxml` in IntelliJ and select **"Open In SceneBuilder"** to visually customize the layout.

### 5. Run the Application
You can run the app in **two ways**:

- **Method A (Easiest)**:  
  Right-click `Launcher.java` in `src/main/java/com/research/pubmanager/Launcher.java` and click **Run 'Launcher.main()'**.

- **Method B (Maven CLI / Maven Tool Window)**:  
  In the IntelliJ terminal or Maven tool window:
  ```bash
  mvn javafx:run
  ```

---

## 🌟 Key Features

1. **⚡ Fast DOI Auto-Import**:
   - Click **"Fast DOI Import"** from the dashboard.
   - Enter a DOI (e.g. `10.1109/CVPR.2016.90`).
   - The system automatically contacts the Crossref registry, retrieves Title, Authors, Year, and Journal, validates it, and saves it into MySQL.

2. **🌐 Automated Online Verification**:
   - Select any publication and click **"Verify Selected via Crossref"**.
   - It performs automated fuzzy title matching and year verification against official publisher records.
   - Displays discrepancy alerts if the submitted data doesn't match the registry.

3. **📊 Live Metric Cards**:
   - Real-time tracking of Total Publications, Verified, Pending, and Rejected / Discrepancy counts.

4. **🔍 Real-Time Filter & Search**:
   - Instant search across Title, Author, DOI, and Journal.
   - Dropdown filter by verification status (`ALL`, `VERIFIED`, `PENDING`, `REJECTED`, `DISCREPANCY`).
