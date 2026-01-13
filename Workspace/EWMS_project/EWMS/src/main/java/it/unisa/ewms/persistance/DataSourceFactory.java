package it.unisa.ewms.persistance;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DataSourceFactory {

    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    static {
        //Cerco il file database.properties
        Properties prop = new Properties();

        try (InputStream in = DataSourceFactory.class.getClassLoader().getResourceAsStream("database.properties")) {

        if (in == null) {
            throw new FileNotFoundException("property file 'database.properties' not found in the classpath");
        }

        prop.load(in);

        // Configurazione JDBC
        config.setJdbcUrl("db.url");
        config.setUsername("db.username");
        config.setPassword("db.password");

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
        return ds.getConnection();
    }
}