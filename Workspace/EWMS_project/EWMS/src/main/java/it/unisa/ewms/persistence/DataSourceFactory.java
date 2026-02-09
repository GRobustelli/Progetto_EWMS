package it.unisa.ewms.PersistenceManagement;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DataSourceFactory {

    private static HikariDataSource ds;
    private static String propertiesFile = "database.properties";


    public static void setPropertiesFile(String newFileName) {
        propertiesFile = newFileName;
        if (ds != null) {
            ds.close(); // Chiude il pool attuale
            ds = null;  // Forza la ricarica alla prossima chiamata
        }
    }

    private static void initDataSource() {

        //Cerco il file database.properties
        Properties prop = new Properties();

        try (InputStream in = DataSourceFactory.class.getClassLoader().getResourceAsStream(propertiesFile)) {

        if (in == null) {
            throw new FileNotFoundException("property file 'database.properties' not found in the classpath");
        }

        prop.load(in);

        // Configurazione JDBC



        HikariConfig config = new HikariConfig();
        // -----------------------------
            String driverClass = prop.getProperty("db.driver");
            if (driverClass != null) {
                try {
                    Class.forName(driverClass);
                    config.setDriverClassName(driverClass);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Driver non trovato: " + driverClass, e);
                }
            }
            // -----------------------------
        config.setJdbcUrl(prop.getProperty("db.url"));
        config.setUsername(prop.getProperty("db.username"));
        config.setPassword(prop.getProperty("db.password"));

        // Configurazione specifica del Connection Pool (opzionale ma consigliata)
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        // Inizializzazione del DataSource
        ds = new HikariDataSource(config);
    } catch (IOException e) {
            throw new RuntimeException("Errore durante il caricamento della configurazione", e);
        }


    }

    // Costruttore privato per prevenire istanziazioni
    private DataSourceFactory() {}

    public static Connection getConnection() throws SQLException {
        if (ds == null) {
            initDataSource();
        }
        return ds.getConnection();
    }
}