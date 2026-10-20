package com.ngtukien.demo.dao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Each DAO owns a JDBC connection; create it with try-with-resources. */
public abstract class DAO implements AutoCloseable {
    protected final Connection con;

    protected DAO() throws SQLException {
        Properties properties = new Properties();
        try {
            try (var input = DAO.class.getResourceAsStream("/database.properties")) {
                if (input != null) properties.load(input);
            }
            Path local = Path.of(".local/database.properties");
            if (Files.exists(local)) {
                try (var reader = Files.newBufferedReader(local, StandardCharsets.UTF_8)) {
                    properties.load(reader);
                }
            }
        } catch (IOException exception) {
            throw new SQLException("Unable to read database configuration", exception);
        }
        String url = setting("db.url", "DB_URL", properties);
        try {
            Class.forName(url.startsWith("jdbc:h2:") ? "org.h2.Driver" : "org.postgresql.Driver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("JDBC driver was not found", exception);
        }
        con = DriverManager.getConnection(url, setting("db.username", "DB_USERNAME", properties),
                setting("db.password", "DB_PASSWORD", properties));
    }

    private static String setting(String key, String environment, Properties properties) {
        return System.getProperty(key, System.getenv().getOrDefault(environment, properties.getProperty(key, "")));
    }

    protected <T> T transaction(SqlWork<T> work) throws SQLException {
        con.setAutoCommit(false);
        try {
            T result = work.execute();
            con.commit();
            return result;
        } catch (SQLException | RuntimeException exception) {
            try { con.rollback(); } catch (SQLException rollback) { exception.addSuppressed(rollback); }
            throw exception;
        } finally {
            con.setAutoCommit(true);
        }
    }

    @Override
    public void close() throws SQLException {
        con.close();
    }

    @FunctionalInterface
    protected interface SqlWork<T> {
        T execute() throws SQLException;
    }
}
