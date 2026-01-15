package org.jarApiAutomation.dbConfiguration;

import java.sql.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PostgresUtils {
    private final String url;
    private final String user;
    private final String pass;
    private volatile Connection connection;

    /**
     * Creates PostgresUtils object with DB connection details.
     *
     * @param url database URL
     * @param user database username
     * @param pass database password
     */
    public PostgresUtils(String url, String user, String pass) {
        this.url = url;
        this.user = user;
        this.pass = pass;
    }

    /**
     * Gets PostgresSQL connection. Creates new connection only if not already created or closed.
     *
     * @return active DB connection
     * @throws SQLException if connection fails
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                synchronized (this) {
                    if (connection == null || connection.isClosed()) {
                        connection = DriverManager.getConnection(url, user, pass);
                    }
                }
            }
            return connection;
        } catch (SQLException e) {
            log.error("Error establishing Postgres connection", e);
            throw new RuntimeException(
                    "Failed to connect to Postgres database : Check VPN connection", e);
        }
    }

    /**
     * Executes a SELECT SQL query.
     *
     * @param sql SQL query to execute
     * @return ResultSet containing query result
     */
    public ResultSet query(String sql, Object... params) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(sql);

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            return ps.executeQuery();
        } catch (SQLException e) {
            log.error("Error executing prepared query: {}", sql, e);
            throw new RuntimeException("DB query failed", e);
        }
    }

    /** Closes the database connection if it is open. */
    public void disconnect() {
        try {
            if (connection != null) {
                connection.close();
                connection = null;
                log.info("Postgres connection closed.");
            }
        } catch (Exception e) {
            log.error("Error closing Postgres connection", e);
        }
    }
}
