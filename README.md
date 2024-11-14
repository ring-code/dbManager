#dbManager
  
  This Java application enables the user to manage a mySQL database. The GUI is written in german. It can be used for any MySQL database;
  however, input validation (StringInputValidation Class) for new or updated entries is based on data for persons.
  Its impractical for databases with many tables as the user can only switch between tables and not search for them directly.
  After selecting the server and database via a menu, the user can:
  
  - Switch between tables in the database
  - Open another window to search for an entry in the currently selected table using a primary key or column data (exact or partial)
  - Open another window to show details for a single entry, edit row data for that entry or delete it.
    This window will also be used to display the entries found via the search function and can switch to next/previous entry in the table or search results
  - Open another window to add an entry to the currently selected table (inputs will be validated)
  - Delete the selected entry from the current table (without opening another window)
  - View history table of added and deleted entries if the according tables have the necessary triggers
  - Disconnect from the database to set up a new connection
  
  
  
  Attention: The password/user data is not encrypted. Be cautious when using this for sensitive data!
