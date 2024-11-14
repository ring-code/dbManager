package mySQL;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import utility.Messages;

/**
 * 
 * This class handles data operations for a MySQL database, it gets the Connection Object from the MySQLConnector Object as its 
 * parameter so there can be multiple instances of it handling different connections.
 * It is designed to function with a single current table because the MainFrame works that way.
 * Most of the GUI classes need an instance of this to work. 
 * Can probably manage other SQL dialects depending on syntax specifics.
 * 
 * 
 * <ul>
 *   <li><strong>Important for the GUI to work:</strong>
 *     <ul>
 *       <li>The table name list needs to be valid!</li>
 *       <li>SQL query for that may vary for different databases</li>
 *     </ul>
 *   </li>
 *   <li><strong>Limitations:</strong>
 *     <ul>    	 
 *       <li>Each relevant Table has a primary key and its always in the same column</li>
 *       <li>The primary key is an integer</li>
 *     </ul>
 *   </li>
 *   <li><strong>Possible Improvements:</strong>
 *     <ul>
 *       <li>Use a HashMap for metadata so where the primary key is does not matter</li>
 *       <li>Use an Object instead of an integer for the primary key, some of the methods already use a String</li>
 *     </ul>
 *   </li>
 * </ul>
 * 
 * @author ring-code
 */
public class MySQLDataHandler {

    private MySQLConnector connector; // Connector object for database operations
    
    private String dataBaseName;
    private List<String> relevantTableNames = new ArrayList<>(); 
    private List<String> primeKeyList = new ArrayList<>(); 
    private String currentTableName; 
    private List<String> currentColumnNames = new ArrayList<>(); // Careful: This can be immutable!
    private int primeKeyIndex = -1;
    private int currentPrimeKey; 
    private String history = "verlauf"; 
            							
    /**
     * Constructs a MySQLDataHandler using the provided MySQLConnector.
     *
     * @param connector The MySQLConnector object used to connect to the database.
     * @throws SQLException If a database access error occurs.
     */
    public MySQLDataHandler(MySQLConnector connector) throws SQLException {
        this.connector = connector;
        this.dataBaseName = connector.getDatabaseName();
        this.relevantTableNames = fetchAllTableNames();
        if (!relevantTableNames.isEmpty()) {
            this.currentTableName = relevantTableNames.get(0); // Set initial table if available
            fetchAndSetPrimeKeys(); // Fetch and set primary keys for the initial table
            fetchColumnNamesForTable(currentTableName); // Fetch column names for the initial table
            
        }
    }

    
    /**
     * Fetches all table names from the database.
     * Might need to adjust the query depending on Database Structure!
     * @return A list of table names excluding the Name of an optional history table
     * 
     * @throws SQLException If a database access error occurs.
     */
    public List<String> fetchAllTableNames() throws SQLException {
        List<String> tableNames = new ArrayList<>();
        
        if (dataBaseName == null || dataBaseName.isEmpty()) {
            throw new SQLException("Database name is not set in the connector.");
        }

        String useDbQuery = "USE " + dataBaseName;
        String showTablesQuery = "SHOW TABLES";

        try (Statement stmt = connector.getConnection().createStatement()) {
            stmt.execute(useDbQuery);

            try (ResultSet rs = stmt.executeQuery(showTablesQuery)) {
                while (rs.next()) {
                    String tableName = rs.getString(1);
                    
                    if (!tableName.equals(history)) { //filter out the history name
                        tableNames.add(tableName);
                    }
                }
            }
        }

       
        return tableNames;
    }

    
    /**
     * Fetches and sets primary keys for the current table.
     *
     * @throws SQLException If a database access error occurs.
     */
    private void fetchAndSetPrimeKeys() throws SQLException {
        if (currentTableName != null && !currentTableName.isEmpty()) {
            this.setPrimeKeyList(fetchPrimeKeysForTable(currentTableName));
        }
    }
    
    /**
     * Adds a new entry to a table with specified column names and values.
     *
     * @param tableName    The name of the table to add the entry to.
     * @param columnNames  The names of the columns to insert data into.
     * @param values       The values to insert into the columns.
     * @throws SQLException If a database access error occurs or column names and values do not match.
     */ 
    public void addEntryToTable(String tableName, List<String> columnNames, List<String> values) throws SQLException {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Values must not be null or empty.");
        }
        if (columnNames == null || columnNames.size() != values.size()) {
            throw new IllegalArgumentException("Column names and values must have the same size.");
        }

        // Construct the SQL query with column names
        StringBuilder sql = new StringBuilder("INSERT INTO " + tableName + " (");
        for (int i = 0; i < columnNames.size(); i++) {
            sql.append(columnNames.get(i)).append(",");
        }
        sql.setLength(sql.length() - 1); // Remove last comma
        sql.append(") VALUES (");
        for (int i = 0; i < values.size(); i++) {
            sql.append("?,");
        }
        sql.setLength(sql.length() - 1); // Remove last comma
        sql.append(")");

        try (PreparedStatement pstmt = connector.getConnection().prepareStatement(sql.toString())) {
            // Set the values in the prepared statement
            for (int i = 0; i < values.size(); i++) {
                pstmt.setString(i + 1, values.get(i));
            }

            // Execute the insert
            pstmt.executeUpdate();
            Messages.showInfoMessage("Datensatz erfolgreich hinzugefügt", "Information");
        } catch (SQLException e) {
            e.printStackTrace();
            Messages.showErrorMessage("Fehler beim Hinzufügen", "Fehler");
        }
    }
   
    
    /**
     * Searches for entries in a specified table based on provided column names and search values using exact matches.
     * 
     * @param tableName The name of the table to search within. Must not be null or empty.
     * @param columnNames A list of column names to search. Must not be null. The size of this list should match the size of searchValues.
     * @param searchValues A list of search values corresponding to the column names. Must not be null. Each value is used for exact matching.
     * @return A list of primary key values for rows that match the search criteria. The list may be empty if no matching rows are found.
     * @throws SQLException If a database access error occurs or if there is an issue executing the query.
     * @throws IllegalArgumentException If tableName, columnNames, or searchValues are invalid, or if columnNames and searchValues sizes do not match.
     */
    public List<Integer> exactDataSearch(String tableName, List<String> columnNames, List<String> searchValues) throws SQLException {
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("Table name must not be null or empty.");
        }

        if (columnNames == null || searchValues == null) {
            throw new IllegalArgumentException("Column names and search values must not be null.");
        }

        // Ensure columnNames and searchValues have the same size or handle mismatch
        if (columnNames.size() != searchValues.size()) {
            throw new IllegalArgumentException("Column names and search values size mismatch.");
        }

        // Build the query dynamically based on non-empty search values
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM " + tableName + " WHERE ");
        List<String> nonEmptyValues = new ArrayList<>();
        List<Integer> primaryKeyList = new ArrayList<>();

        boolean firstCondition = true;

        for (int i = 0; i < columnNames.size(); i++) {
            String value = searchValues.get(i);
            if (!value.trim().isEmpty()) {
                if (!firstCondition) {
                    queryBuilder.append(" AND ");
                }
                queryBuilder.append(columnNames.get(i)).append(" = ?");
                nonEmptyValues.add(value); // Use exact matches
                firstCondition = false;
            }
        }

        if (nonEmptyValues.isEmpty()) {
            throw new IllegalArgumentException("At least one non-empty search value must be provided.");
        }

        String query = queryBuilder.toString();

        try (PreparedStatement pstmt = connector.getConnection().prepareStatement(query)) {
            // Set the non-empty search values in the PreparedStatement
            for (int i = 0; i < nonEmptyValues.size(); i++) {
                pstmt.setString(i + 1, nonEmptyValues.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                if (columnCount < 1) {
                    throw new SQLException("No columns found for the table: " + tableName);
                }

                while (rs.next()) {
                    int primaryKey = rs.getInt(1); 
                    primaryKeyList.add(primaryKey);
                }

                return primaryKeyList;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                null,
                "Error executing query: " + e.getMessage(),
                "Query Error",
                JOptionPane.ERROR_MESSAGE
            );
            throw e; // Rethrow the exception after logging
        }
    }

    
    
    /**
     * Searches for entries in a specified table based on provided column names and search values using partial matches.
     * 
     * @param tableName The name of the table to search within. Must not be null or empty.
     * @param columnNames A list of column names to search. Must not be null. The size of this list should match the size of searchValues.
     * @param searchValues A list of search values corresponding to the column names. Must not be null. Each value is used for partial matching (LIKE).
     * @return A list of primary key values for rows that match the search criteria. The list may be empty if no matching rows are found.
     * @throws SQLException If a database access error occurs or if there is an issue executing the query.
     * @throws IllegalArgumentException If tableName, columnNames, or searchValues are invalid, or if columnNames and searchValues sizes do not match.
     */
    public List<Integer> partialDataSearch(String tableName, List<String> columnNames, List<String> searchValues) throws SQLException {
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("Table name must not be null or empty.");
        }

        if (columnNames == null || searchValues == null) {
            throw new IllegalArgumentException("Column names and search values must not be null.");
        }

        // Ensure columnNames and searchValues have the same size or handle mismatch
        if (columnNames.size() != searchValues.size()) {
            throw new IllegalArgumentException("Column names and search values size mismatch.");
        }

        // Build the query dynamically based on non-empty search values
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM " + tableName + " WHERE ");
        List<String> nonEmptyValues = new ArrayList<>();
        List<Integer> primaryKeyList = new ArrayList<>();

        boolean firstCondition = true;

        for (int i = 0; i < columnNames.size(); i++) {
            String value = searchValues.get(i);
            if (!value.trim().isEmpty()) {
                if (!firstCondition) {
                    queryBuilder.append(" AND ");
                }
                queryBuilder.append(columnNames.get(i)).append(" LIKE ?");
                nonEmptyValues.add("%" + value + "%"); // Use LIKE for partial matches
                firstCondition = false;
            }
        }

        if (nonEmptyValues.isEmpty()) {
            throw new IllegalArgumentException("At least one non-empty search value must be provided.");
        }

        String query = queryBuilder.toString();

        try (PreparedStatement pstmt = connector.getConnection().prepareStatement(query)) {
            // Set the non-empty search values in the PreparedStatement
            for (int i = 0; i < nonEmptyValues.size(); i++) {
                pstmt.setString(i + 1, nonEmptyValues.get(i));
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                if (columnCount < 1) {
                    throw new SQLException("No columns found for the table: " + tableName);
                }

                while (rs.next()) {
                    int primaryKey = rs.getInt(1); 
                    primaryKeyList.add(primaryKey);
                }

                return primaryKeyList;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                null,
                "Error executing query: " + e.getMessage(),
                "Query Error",
                JOptionPane.ERROR_MESSAGE
            );
            throw e; // Rethrow the exception after logging
        }
    }

    /**
     * Fetches primary keys for a specified table.
     *
     * @param tableName The name of the table for which to fetch primary keys.
     * @return A list of primary key values as Objects.
     * @throws SQLException If a database access error occurs or no columns are found.
     */
    public List<String> fetchPrimeKeysForTable(String tableName) throws SQLException {
    List<String> primaryKeyList = new ArrayList<>();
    String query = "SELECT * FROM " + tableName;

    try (Statement stmt = connector.getConnection().createStatement();
         ResultSet rs = stmt.executeQuery(query)) {

        ResultSetMetaData metaData = rs.getMetaData();
        // Fetch primary key index
        int primeKeyIndex = getPrimeKeyColumnIndex(tableName);
        if (primeKeyIndex == -1) {
            throw new SQLException("No primary key found for table: " + tableName);
        }

        // Fetch primary keys
        query = "SELECT " + metaData.getColumnName(primeKeyIndex + 1) + " FROM " + tableName;
        try (Statement pkStmt = connector.getConnection().createStatement();
             ResultSet pkRs = pkStmt.executeQuery(query)) {

            while (pkRs.next()) {
                // Convert the primary key to a String, regardless of its original type
                Object primaryKeyObject = pkRs.getObject(1); // Use getObject to handle different types
                if (primaryKeyObject != null) {
                    primaryKeyList.add(primaryKeyObject.toString());
                }
            }
        	}
    	}

    	return primaryKeyList;
    }

    
    /**
     * Deletes an entry from a table based on its primary Key. No Confirmation!
     *
     * @param tableName  The name of the table from which to delete the entry.
     * @param primaryKey The primary key of the entry to delete.
     * @throws SQLException If a database access error occurs or no columns are found.
     */
    public void deleteEntryByPrimeKey(String tableName, int primaryKey) throws SQLException {
        if (tableName == null || tableName.isEmpty()) {
            throw new IllegalArgumentException("Table name must not be null or empty.");
        }
        
        
        // Retrieve the primary key column name from the handler
        String primaryKeyColumn = getPrimeKeyColumnName(tableName);
        if (primaryKeyColumn == null) {
            throw new SQLException("Primary key column not found for table: " + tableName);
        }
        

        String query = "DELETE FROM " + tableName + " WHERE " + primaryKeyColumn + " = ?";
        try (PreparedStatement pstmt = connector.getConnection().prepareStatement(query)) {
        	
        	pstmt.setInt(1, primaryKey);
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected == 0) {
                throw new SQLException("No entry found with primary key: " + primaryKey);
            }
        }

        // Refresh the prime key list after deletion
        refreshPrimeKeyList();
    }
       
         
    /**
     * Retrieves the primary key column name for the specified table.
     * 
     * @param tableName the name of the table for which to retrieve the primary key column name.
     * @return the name of the primary key column, or null if not found.
     * @throws SQLException if an SQL error occurs while retrieving metadata.
     */
    public String getPrimeKeyColumnName(String tableName) throws SQLException {
        // Use the existing connection from the connector
        Connection connection = connector.getConnection();
        DatabaseMetaData metaData = connection.getMetaData();
        
        try (ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, tableName)) {
            if (primaryKeys.next()) {
            	
            	// Retrieve the column name of the primary key
                return primaryKeys.getString("COLUMN_NAME");
            }
        }

        // Return null if no primary key column is found
        return null;
    }

               
    /**
     * Refreshes the list of column names for the currently selected table.
     *
     * @throws SQLException If a database access error occurs or if no table is set.
     */
    public void refreshColumnNames() throws SQLException {
        if (currentTableName == null || currentTableName.isEmpty()) {
            throw new SQLException("No table is currently selected.");
        }
        
        fetchColumnNamesForTable(currentTableName);
    }

    
    /**
     * Refreshes the list of primary keys for the current table.
     *
     * @throws SQLException If a database access error occurs or if no table is set.
     */
    public void refreshPrimeKeyList() throws SQLException {
        if (currentTableName == null || currentTableName.isEmpty()) {
            throw new SQLException("No table is currently selected.");
        }

        // Fetch and update the primary key list for the current table
        this.primeKeyList = fetchPrimeKeysForTable(currentTableName);
    }

    
    /**
     * Retrieves the index of the primary key column for a specified table.
     *
     * @param tableName The name of the table for which the primary key index is to be retrieved.
     * @return The index of the primary key column in the list of columns. If no primary key is found, returns -1.
     * @throws SQLException If there is an error accessing the database metadata.
     */
    public int getPrimeKeyColumnIndex(String tableName) throws SQLException {
        List<String> columnNames = new ArrayList<>();
        int primaryKeyIndex = -1; // Default if no primary key is found

        Connection connection = connector.getConnection();
        DatabaseMetaData metaData = connection.getMetaData();

        // Fetch column names in order
        try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                columnNames.add(columnName);
            }
        }

        // Fetch primary key info
        try (ResultSet pkRs = metaData.getPrimaryKeys(null, null, tableName)) {
            while (pkRs.next()) {
                String pkColumnName = pkRs.getString("COLUMN_NAME");
                primaryKeyIndex = columnNames.indexOf(pkColumnName);
                if (primaryKeyIndex != -1) {
                    break; // Assuming one primary key for simplicity
                }
            }
        }

        return primaryKeyIndex;
    }

    
    /**
     * Fetches all data from a specified table, including column names as the first row.
     *
     * @param tableName The name of the table from which to fetch data.
     * @return A list of string arrays representing rows of data from the table.
     * @throws SQLException If a database access error occurs.
     */
    public List<String[]> fetchAllDataForTable(String tableName) throws SQLException {
        List<String[]> data = new ArrayList<>();
        String query = "SELECT * FROM " + tableName;

        try (Statement stmt = connector.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Fetch column names dynamically
            if (currentColumnNames.isEmpty()) {
                for (int i = 1; i <= columnCount; i++) {
                    currentColumnNames.add(metaData.getColumnName(i));
                }
            }

            // Add column names as the first row
            data.add(currentColumnNames.toArray(new String[0]));

            // Determine the primary key index
            primeKeyIndex = getPrimeKeyColumnIndex(tableName);

            // Add rows
            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int i = 1; i <= columnCount; i++) {
                    row[i - 1] = rs.getString(i);
                }
                data.add(row);
            }
        }
        return data;
    }

    /**
     * Fetches data for a specific row identified by its primary key.
     *
     * @param tableName The name of the table.
     * @param primeKey  The primary key of the row to fetch.
     * @return An array of strings representing the row data, or null if no row is found.
     * @throws SQLException If a database access error occurs.
     */
    public String[] fetchDataByPrimeKey(String tableName, String primeKey) throws SQLException {
        if (primeKeyIndex == -1) {
            primeKeyIndex = getPrimeKeyColumnIndex(tableName);
        }

        if (primeKeyIndex == -1) {
            throw new SQLException("No primary key found for table: " + tableName);
        }

        String primaryKeyColumn = currentColumnNames.get(primeKeyIndex);
        String query = "SELECT * FROM " + tableName + " WHERE " + primaryKeyColumn + " = ?";

        try (PreparedStatement pstmt = connector.getConnection().prepareStatement(query)) {
            pstmt.setString(1, primeKey); // Use setString for the primary key
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String[] row = new String[currentColumnNames.size()];
                    for (int i = 1; i <= currentColumnNames.size(); i++) {
                        row[i - 1] = rs.getString(i);
                    }
                    return row;
                }
            }
        }
        return null;
    }
    
        
    /**
     * Fetches column names for a specified table.
     *
     * @param tableName The name of the table to fetch column names for.
     * @throws SQLException If a database access error occurs.
     */
    private void fetchColumnNamesForTable(String tableName) throws SQLException {
        if (tableName != null && !tableName.isEmpty()) {
            currentColumnNames.clear(); // Clear existing column names
            String query = "SELECT * FROM " + tableName + " LIMIT 1"; // Fetch data from the specified table

            try (Statement stmt = connector.getConnection().createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {

                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    currentColumnNames.add(metaData.getColumnName(i));
                }
            }
        } else {
            throw new IllegalArgumentException("Table name cannot be null or empty.");
        }
    }

    
    public void updateEntryByPrimeKey(String tableName, String primeKey, String columnName, String newValue) throws SQLException {
        String query = "UPDATE " + tableName + " SET " + columnName + " = ? WHERE " + getPrimeKeyColumnName(tableName) + " = ?"; 
        try (PreparedStatement stmt = connector.getConnection().prepareStatement(query)) {
            stmt.setString(1, newValue);
            stmt.setString(2, primeKey);
            stmt.executeUpdate();
        }
    }
    
    
    
    
    
    
    
    
    
    // Getters
    
    public String getCurrentTableName() {
        return currentTableName;
    }

    public List<String> getCurrentColumnNames() {
        return currentColumnNames;
    }

    public List<String> getPrimeKeyList() {
        return primeKeyList;
    }

    public int getCurrentPrimeKey() {
        return currentPrimeKey;
    }

    public MySQLConnector getConnector() {
        return connector;
    }

    public String getDataBaseName() {
        return dataBaseName;
    }

    public List<String> getTableNames() {
        return relevantTableNames.stream()
                .filter(tableName -> !tableName.equals(history)) // filter out the history table name again if necessary
                .collect(Collectors.toList());
    }

    public String getHistory() {
        return history;
    }

    public int getPrimeKeyIndex() {
        return primeKeyIndex;
    }

 // Setters
    
    /**
     * Updates the current table and fetches related data.
     *
     * @param newTableName The name of the new table to set as the current table.
     * @throws SQLException If a database access error occurs.
     */
    public void setCurrentTableName(String newTableName) throws SQLException {
        if (newTableName != null && !newTableName.equals(this.currentTableName)) {
            this.currentTableName = newTableName;
            fetchAndSetPrimeKeys(); // Fetch and set primary keys for the new table
            fetchColumnNamesForTable(newTableName); // Fetch column names for the new table
        }
    }

    public void setPrimeKeyList(List<String> primeKeyList) {
        this.primeKeyList = primeKeyList;
    }

    public void setCurrentPrimeKey(int currentPrimeKey) {
        this.currentPrimeKey = currentPrimeKey;
    }

    public void setConnector(MySQLConnector connector) {
        this.connector = connector;
    }

    public void setTableNames(List<String> tableNames) {
        this.relevantTableNames = tableNames;
    }

    public void setCurrentColumnNames(List<String> currentColumnNames) {
        this.currentColumnNames = currentColumnNames;
    }

    public void setHistory(String logName) {
        this.history = logName;
    }

    public void setPrimeKeyIndex(int primeKeyIndex) {
        this.primeKeyIndex = primeKeyIndex;
    }

}
