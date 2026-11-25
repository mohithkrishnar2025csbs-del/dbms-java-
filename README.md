# SMART LOST-AND-FOUND MANAGEMENT SYSTEM
## College Academic DBMS & Desktop Application Project

A centralized, professional desktop-based Lost-and-Found Management System designed to help students report, track, match, and claim campus belongings securely, administered through a role-based management dashboard.

---

## 1. TECHNOLOGY STACK

Strictly developed using core desktop technologies without web frameworks:
* **Frontend / GUI**: Java Swing, Java AWT
* **Backend**: Core Java (Java 17 LTS), Object-Oriented Programming
* **Database**: MySQL Server 8.4 (InnoDB, ACID transactions)
* **Connectivity**: JDBC (`mysql-connector-j.jar`)
* **Security**: PBKDF2 with HMAC-SHA256 (65,536 iterations, salted) password hashing

---

## 2. PROJECT ARCHITECTURE

```
javadbms/
│
├── src/
│   ├── app/                    # Application bootstrap and verification test suites
│   │   ├── Main.java                        # Primary application launcher (boots HomeFrame)
│   │   ├── SecureDatabaseSetupDialog.java   # Swing database setup dialog
│   │   ├── DatabaseSetupAndVerifier.java    # Schema execution & verification helper
│   │   ├── SystemIntegrationTest.java       # Core 17-test verification suite
│   │   ├── AdminModuleVerificationTest.java # 29-test Admin module suite
│   │   ├── UIFramesVerificationTest.java    # Swing frames layout verification suite
│   │   ├── ConfigureAdminAccount.java       # Admin account configuration utility
│   │   └── VerifyAdminAndStudentWorkflows.java # Full workflow verification
│   │
│   ├── model/                  # Domain entity models
│   │   ├── User.java                        # Student and Admin account entity
│   │   ├── LostItem.java                    # Lost item report entity
│   │   ├── FoundItem.java                   # Found item report entity
│   │   ├── Claim.java                       # Ownership claim request entity
│   │   └── ActivityLog.java                 # Administrator audit log entity
│   │
│   ├── dao/                    # Data Access Objects (JDBC PreparedStatement operations)
│   │   ├── UserDAO.java                     # User authentication, queries & profile updates
│   │   ├── LostItemDAO.java                 # Lost items CRUD & dynamic PreparedStatement search
│   │   ├── FoundItemDAO.java                # Found items CRUD & dynamic PreparedStatement search
│   │   ├── ClaimDAO.java                    # Transactional claim approvals & status handling
│   │   └── ActivityLogDAO.java              # Admin activity logging & audit trail
│   │
│   ├── service/                # Business logic & authorization layer
│   │   ├── AuthService.java                 # Authentication, registration & session handling
│   │   ├── AdminService.java                # Real-time statistics, moderation & audit logs
│   │   ├── ItemService.java                 # Item validation & reporting business logic
│   │   └── ClaimService.java                # Claim validation, submission & processing
│   │
│   ├── ui/                     # Java Swing graphical user interface
│   │   ├── HomeFrame.java                   # Main portal selection home page
│   │   ├── StudentLoginFrame.java           # Student portal sign-in
│   │   ├── AdminLoginFrame.java             # Admin portal sign-in
│   │   ├── LoginFrame.java                  # Dual login window
│   │   ├── RegistrationFrame.java           # New student account registration
│   │   ├── StudentDashboard.java            # Student control center & reports tracker
│   │   ├── AdminDashboard.java              # Admin KPI overview, moderation & management
│   │   ├── ClaimManagementFrame.java        # Claim verification & decision interface
│   │   ├── AdminActivityFrame.java          # Admin audit log viewer
│   │   ├── LostItemFrame.java               # Lost item submission form
│   │   ├── FoundItemFrame.java              # Found item submission form
│   │   ├── SearchItemsFrame.java            # Dynamic catalog search & claim launcher
│   │   ├── ClaimFrame.java                  # Claim request submission form
│   │   ├── MyReportsFrame.java              # Student's submitted reports table
│   │   ├── MyClaimsFrame.java               # Student's submitted claims tracker
│   │   └── ProfileFrame.java                # Student profile management
│   │
│   └── util/                   # Reusable utilities
│       ├── DBConnection.java                # Externalized JDBC connection management
│       ├── PasswordUtil.java                # PBKDF2 hashing & verification
│       ├── UITheme.java                     # Modern styling, colors, borders & tables
│       └── ValidationUtil.java              # Input formats, email & date validation
│
├── database/                   # SQL schemas and sample records
│   ├── schema.sql                           # Idempotent DDL (5 tables, 13+ indexes, FK cascades)
│   ├── sample_data.sql                      # Idempotent sample records (INSERT IGNORE)
│   ├── setup_report.txt                     # Automated database verification report
│   └── migrations/                          # Incremental database migrations
│       └── .gitkeep
│
├── config/                     # Externalized database credentials
│   ├── db.properties                        # Local credentials (git-ignored)
│   └── db.properties.example                # Configuration template
│
├── lib/                        # Runtime JDBC dependencies
│   └── mysql-connector-j.jar                # MySQL Connector/J 8.4.0
│
├── bin/                        # Compiled bytecode output directory
├── compile.bat                 # One-click project compilation
├── run.bat                     # One-click application launcher
├── setup_database.bat          # Database setup launcher
├── LAUNCH_SETUP.bat            # Direct setup wizard launcher
├── README.md                   # Complete project documentation
└── .gitignore                  # Git ignore rules for builds & credentials
```

---

## 3. DATABASE SCHEMA & CONSTRAINTS

The database `smart_lost_found` consists of 5 normalized InnoDB tables:
1. **`USERS`**: Stores student and admin credentials, roles (`STUDENT`, `ADMIN`), and account statuses (`ACTIVE`, `INACTIVE`, `SUSPENDED`).
2. **`LOST_ITEMS`**: Campus lost item reports linked to the reporting student via Foreign Key (`ON DELETE CASCADE`).
3. **`FOUND_ITEMS`**: Campus found item reports linked to the finder via Foreign Key (`ON DELETE CASCADE`).
4. **`CLAIMS`**: Student ownership claim requests. Enforces single-item reference using `CHECK ((lost_item_id IS NOT NULL AND found_item_id IS NULL) OR (lost_item_id IS NULL AND found_item_id IS NOT NULL))`.
5. **`ADMIN_ACTIVITY_LOG`**: Audit log recording all administrative actions.

---

## 4. DEFAULT CREDENTIALS

### Administrator Account:
* **Email**: `mohithkrishna.r.2025.csbs@rajalakshgmi.edu.in` *(or `mohithkrishna.r.2025.csbs@rajalakshmi.edu.in`)*
* **Password**: `Mohith@2007`

### Secondary Academic Admin:
* **Email**: `admin@college.edu`
* **Password**: `Admin@123`

### Sample Student Accounts:
* **Rahul Sharma**: `rahul.sharma@college.edu` / `Student@123`
* **Priya Patel**: `priya.patel@college.edu` / `Student@123`
* **Ananya Singh**: `ananya.singh@college.edu` / `Student@123`

---

## 5. LAUNCHING THE APPLICATION

### Compile:
Double-click `compile.bat` or run:
```powershell
javac -encoding UTF-8 -cp "lib\mysql-connector-j.jar;bin" -d bin src\model\*.java src\util\*.java src\dao\*.java src\service\*.java src\ui\*.java src\app\*.java
```

### Run:
Double-click `run.bat` or run:
```powershell
java -cp "bin;lib\mysql-connector-j.jar" app.Main
```
