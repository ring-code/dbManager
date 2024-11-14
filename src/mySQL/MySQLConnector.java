package mySQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** 
 * This class is used to initially set up the connection to the mySQL database/server.
 * The User can create a custom instance via the Connection Menu.
 * @author ring-code
*/

public class MySQLConnector {

    private String url;          
    private String user;         
    private String password;     
    private String dbName;      
    private Connection connection;

    /**
     * Constructs a MySQLConnector with the specified database connection parameters.
     *
     * @param url      The URL of the database.
     * @param user     The username for database authentication.
     * @param password The password for database authentication.
     * @param dbName   The name of the database to use.
     * @throws SQLException If a database access error occurs or the URL is null.
     */
    public MySQLConnector(String url, String user, String password, String dbName) throws SQLException {
        this.url = url;
        this.user = user;
        this.password = password;
        this.dbName = dbName;
        connect(); // Establish the database connection
    }

    /**
     * Returns the current database connection.
     *
     * @return The Connection object representing the current connection to the database.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Establishes a connection to the database and sets the catalog to the specified database name.
     *
     * @throws SQLException If a database access error occurs or the URL is null.
     */
    private void connect() throws SQLException {
        connection = DriverManager.getConnection(url, user, password); // Establish connection to the database
        connection.setCatalog(dbName); // Set the default database to use
    }

    /**
     * Closes the current database connection if it is not already closed.
     *
     * @throws SQLException If a database access error occurs or the connection is closed.
     */
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close(); // Close the connection to the database
        }
    }

    public String getDatabaseName() {
        return dbName;
    }
}
