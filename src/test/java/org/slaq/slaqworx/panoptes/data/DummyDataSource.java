package org.slaq.slaqworx.panoptes.data;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

/**
 * DummyDataSource is a DataSource that does absolutely nothing. It can be used only when a non-null
 * DataSource reference is needed, but is known to not actually be used.
 *
 * @author jeremy
 */
public class DummyDataSource implements DataSource {
    @Override
    public Connection getConnection() throws SQLException {
        return null;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return null;
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        // nothing to do
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        // nothing to do
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }
}
