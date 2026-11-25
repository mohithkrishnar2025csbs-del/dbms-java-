package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages JDBC Database Connections using externalized configuration.
 */
public class DBConnection {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/smart_lost_found?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";

    private static String url = DEFAULT_URL;
    private static String user = DEFAULT_USER;
    private static String password = DEFAULT_PASSWORD;
    private static String driver = DEFAULT_DRIVER;
    private static boolean initialized = false;

    static {
        loadConfiguration();
    }

    public static synchronized void loadConfiguration() {
        Properties props = new Properties();
        File configFile = new File("config/db.properties");

        if (configFile.exists()) {
            try (InputStream in = new FileInputStream(configFile)) {
                props.load(in);
                url = props.getProperty("db.url", DEFAULT_URL).trim();
                user = props.getProperty("db.user", DEFAULT_USER).trim();
                password = props.getProperty("db.password", DEFAULT_PASSWORD).trim();
                driver = props.getProperty("db.driver", DEFAULT_DRIVER).trim();
            } catch (Exception e) {
                System.err.println("Warning: Could not read config/db.properties, using defaults: " + e.getMessage());
            }
        } else {
            // Try loading from classpath
            try (InputStream in = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
                if (in != null) {
                    props.load(in);
                    url = props.getProperty("db.url", DEFAULT_URL).trim();
                    user = props.getProperty("db.user", DEFAULT_USER).trim();
                    password = props.getProperty("db.password", DEFAULT_PASSWORD).trim();
                    driver = props.getProperty("db.driver", DEFAULT_DRIVER).trim();
                }
            } catch (Exception ignored) {
            }
        }

        try {
            Class.forName(driver);
            initialized = true;
        } catch (ClassNotFoundException e) {
            System.err.println("Error: MySQL JDBC Driver class not found: " + driver);
        }
    }

    /**
     * Obtains a fresh database connection.
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            loadConfiguration();
        }
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Checks whether the database connection can be established.
     * @return true if successful, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connectivity test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Safely closes resources without throwing checked exceptions.
     */
    public static void closeQuietly(AutoCloseable... closeables) {
        if (closeables == null) return;
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static String getDatabaseName() {
        return "smart_lost_found";
    }

    public static String getUrl() {
        return url;
    }

    public static String getUser() {
        return user;
    }
}

