package it.unisa.ewms.persistance;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceFactory {

    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    static {
        // Configurazione JDBC
        config.setJdbcUrl("jdbc:mysql://localhost:3306/nome_tuo_db");
        config.setUsername("tuo_username");
        config.setPassword("tua_password");

        // Configurazione specifica del Connection Pool (opzionale ma consigliata)
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        // Inizializzazione del DataSource
        ds = new HikariDataSource(config);
    }

    // Costruttore privato per prevenire istanziazioni
    private DataSourceFactory() {}

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}