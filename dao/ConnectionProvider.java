package dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class ConnectionProvider {

    public static Connection getCon() {
        try {
            Properties props = new Properties();
            
            try (InputStream is = ConnectionProvider.class.getResourceAsStream("/dao/db.properties")) {
                if (is == null) {
                    System.err.println("[Config Error] Embedded db.properties not found.");
                    return null;
                }
                props.load(is);
            }

            String host = props.getProperty("db.host");
            String port = props.getProperty("db.port", "3306");
            String dbName = props.getProperty("db.name");
            String user = props.getProperty("db.user");
            String pass = props.getProperty("db.password");

            String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                    + "?useSSL=false&allowPublicKeyRetrieval=true";

            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(jdbcUrl, user, pass);

        } catch (Exception ex) {
            System.err.println("[JDBC Exception] Database connection failed: " + ex.getMessage());
            return null;
        }
    }
}