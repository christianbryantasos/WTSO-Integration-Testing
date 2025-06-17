package com.asos.ip.helper.JMS;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Datasource to enable Oracle connection using specific connection details
 */
public class OracleDataSource implements DataSource {

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public OracleDataSource(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return DriverManager.getConnection(jdbcUrl, username, password);
    }

    @Override
    public PrintWriter getLogWriter() {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) {
    }

    @Override
    public void setLoginTimeout(int seconds) {
    }

    @Override
    public int getLoginTimeout() {
        return 0;
    }

    @Override
    public Logger getParentLogger() {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return false;
    }

}
