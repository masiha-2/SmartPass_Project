package dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionProvider {

    public static Connection getCon() {
        try {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream("db.properties")) {
                props.load(fis);
            } catch (IOException e) {
                System.err.println("[Config Error] db.properties file not found in project root.");
                return null;
            }

            String host = props.getProperty("db.host", "localhost");
            String port = props.getProperty("db.port", "3306");
            String dbName = props.getProperty("db.name", "smartpass_db");
            String user = props.getProperty("db.user", "root");
            String pass = props.getProperty("db.password", "");

            String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName
                    + "?useSSL=false&allowPublicKeyRetrieval=true";

            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(jdbcUrl, user, pass);

        } catch (ClassNotFoundException | SQLException ex) {
            System.err.println("[JDBC Exception] Database connection failed: " + ex.getMessage());
            return null;
        }
    }
}