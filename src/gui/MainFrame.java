package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import mySQL.MySQLDataHandler;
import utility.Formatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * The Main GUI element of this application. It shows a table to the user and enables him to switch to others
 * in the Database. He can choose an entry in the table to show Details for it or delete it directly. It can open 
 * up other Windows to let the user search for entries in the currently shown table or add an entry to it. 
 * 
 * <ul>
 * <li><b>Top Panel:</b>
 *   <ul>
 *     <li><b>Information:</b>
 *       <ul>
 *         <li>All Entries from the currently selected Table</li>
 *         <li>Displays the name of the database to inform the user.</li>
 *         <li>Shows the name of the currently displayed table.</li>
 *       </ul>
 *     </li>
 *     <li><b>Buttons:</b>
 *       <ul>
 *         <li><code>previousTableButton</code>: Allows the user to switch to the previous table in the list. Wrap around if there is none, turned off if only table is present. </li>
 *         <li><code>nextTableButton</code>: Allows the user to switch to the next table in the list. Wrap around if there is none, turned off if only table is present. </li>
 *         <li><code>disconnectButton</code>: Disconnects from the database and shows the connection menu again.</li>
 *       </ul>
 *     </li>
 *   </ul>
 * </li>
 * <li><b>Bottom Panel:</b>
 *   <ul>
 *     <li><b>Buttons:</b>
 *       <ul>
 *         <li><code>searchNumberButton</code>: Opens up <code>SimpleSearchWindow</code> to search for a primary key in the given table.</li>
 *         <li><code>searchDataButton</code>: Opens up <code>SearchWindow</code> to search data (exact or partial) across a chosen number of columns in the current table.</li>
 *         <li><code>detailsButton</code>: Opens up the <code>DetailsWindow</code> for the currently selected row in the table; also listens for the Enter key.</li>
 *         <li><code>addButton</code>: Opens up <code>InputWindow</code> to add an entry to the current table.</li>
 *         <li><code>deleteButton</code>: Allows the user to delete the currently selected entry after confirmation; also listens for the Delete key.</li>
 *         <li><code>historyButton</code>: Switches the current table to show previous user actions, will be inactive if the history field of the handler is empty.          
 *         </li>
 *       </ul>
 *     </li>
 *   </ul>
 * </li>
 * </ul>
 * 
 * <b>Note:</b> This approach might not be suitable for a huge database.
 * 
 * @author ring-code
 */


public class MainFrame {

	private MySQLDataHandler handler;
    
	private JTable infoTable;
    private DefaultTableModel tableModel;
    private int currentTableIndex = 0;
    private JButton detailsButton;
    private JButton disconnectButton;
    private JButton addButton;
    private JButton deleteButton;
    private JButton historyButton;
    private JButton searchDataButton;
    private JButton searchNumberButton;
    private JButton previousTableButton;
    private JButton nextTableButton;           
    private JFrame frame;
    
    private DetailsWindow detailsWindow;
    
    //these are needed because the Lists the handler has can be immutable
    private List<String> mFColumnNames = new ArrayList<>(); 
    private List<String> primeKeyList = new ArrayList<>();
    
    
      
    private boolean isOpen = true;
    
    private String previousTableName = null;
    
    
    @SuppressWarnings("unused")								
	private int currentPrimeKey = -1; // needed to keep track of the user selected row

    public MainFrame(MySQLDataHandler sqlHandler) {
        this.handler = sqlHandler;
        initializeGUI();
        
    }

    
    @SuppressWarnings("serial")
    private void initializeGUI() {
    	
    	
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Datenbankverwaltung");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setLayout(new BorderLayout());

            JPanel disconnectPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(0, 10, 0, 10); 
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel databaseNameLabel = new JLabel("Datenbank: " + Formatter.formatName(handler.getDataBaseName()), SwingConstants.LEFT);
            gbc.gridx = 0;
            gbc.weightx = 0.0; 
            disconnectPanel.add(databaseNameLabel, gbc);

            JPanel tableControlPanel = new JPanel();
            tableControlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0)); 

            previousTableButton = new JButton("\u2190"); 
            nextTableButton = new JButton("\u2192"); 

            JLabel tableNameLabel = new JLabel("Tabelle: " + Formatter.formatName(handler.getCurrentTableName()), SwingConstants.CENTER);
            tableNameLabel.setFont(new Font("SansSerif", Font.PLAIN, 16)); 

            tableControlPanel.add(previousTableButton);
            tableControlPanel.add(tableNameLabel);
            tableControlPanel.add(nextTableButton);

            gbc.gridx = 1;
            gbc.weightx = 1.0; 
            disconnectPanel.add(tableControlPanel, gbc);

            disconnectButton = new JButton("Verbindung trennen");
            gbc.gridx = 2;
            gbc.weightx = 0.0; 
            disconnectPanel.add(disconnectButton, gbc);

            disconnectPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); 

            JPanel centerPanel = new JPanel(new BorderLayout());
            JPanel bottomPanel = new JPanel(new FlowLayout());

            tableModel = new DefaultTableModel();
            infoTable = new JTable(tableModel);
            centerPanel.add(new JScrollPane(infoTable), BorderLayout.CENTER);

            searchNumberButton = new JButton("Suche Nummer");
            searchDataButton = new JButton("Suche Daten");
            detailsButton = new JButton("Details / Update");
            addButton = new JButton("Hinzufügen");
            deleteButton = new JButton("Löschen");
            historyButton = new JButton("Verlauf");

            bottomPanel.add(searchNumberButton);
            bottomPanel.add(searchDataButton);
            bottomPanel.add(detailsButton);
            bottomPanel.add(addButton);
            bottomPanel.add(deleteButton);
            bottomPanel.add(historyButton);

            frame.add(disconnectPanel, BorderLayout.NORTH);
            frame.add(centerPanel, BorderLayout.CENTER);
            frame.add(bottomPanel, BorderLayout.SOUTH);

            frame.setSize(1000, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // Actions
            Action showDetailsAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showDetailsWindow();
                }
            };

            Action deleteAction = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteSelectedEntry();
                }
            };

            InputMap inputMap = infoTable.getInputMap(JComponent.WHEN_FOCUSED);
            inputMap.put(KeyStroke.getKeyStroke("ENTER"), "showDetails");
            inputMap.put(KeyStroke.getKeyStroke("DELETE"), "deleteEntry");

            ActionMap actionMap = infoTable.getActionMap();
            actionMap.put("showDetails", showDetailsAction);
            actionMap.put("deleteEntry", deleteAction);

            previousTableButton.addActionListener(e -> {
                try {
                    String currentTableName = handler.getCurrentTableName();
                    int currentIndex = getTableNames().indexOf(currentTableName);
                    currentIndex = (currentIndex - 1 + getTableNames().size()) % getTableNames().size();
                    String newTableName = getTableNames().get(currentIndex);
                    updateTable(newTableName);
                    tableNameLabel.setText("Tabelle: " + Formatter.formatName(newTableName));
                    handler.setCurrentTableName(newTableName);
                    updateButtonStates(); // Update button states after changing the table
                    
                
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            });

            nextTableButton.addActionListener(e -> {
                try {
                    String currentTableName = handler.getCurrentTableName();
                    int currentIndex = getTableNames().indexOf(currentTableName);
                    currentIndex = (currentIndex + 1) % getTableNames().size();
                    String newTableName = getTableNames().get(currentIndex);
                    updateTable(newTableName);
                    tableNameLabel.setText("Tabelle: " + Formatter.formatName(newTableName));
                    handler.setCurrentTableName(newTableName);
                    updateButtonStates(); // Update button states after changing the table
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            });

            detailsButton.addActionListener(e -> showDetailsWindow());

            searchDataButton.addActionListener(e -> {
                SearchWindow searchWindow = new SearchWindow(frame, mFColumnNames, handler);
                searchWindow.setVisible(true);
            });

            searchNumberButton.addActionListener(e -> {
                String currentTableName = handler.getCurrentTableName();
                SimpleSearchWindow simpleInput = new SimpleSearchWindow(frame, currentTableName);
                String userInput = simpleInput.showInputDialog();
                try {
                    String[] data = handler.fetchDataByPrimeKey(currentTableName, userInput);
                    if (data != null) {
                        if (detailsWindow == null || !detailsWindow.isVisible()) {
                            detailsWindow = DetailsWindow.getInstance(frame, handler, getColumnNames(), Color.LIGHT_GRAY);
                            detailsWindow.showDetails(userInput);
                        }
                    
                        
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });

            addButton.addActionListener(e -> {
                String currentTableName = handler.getCurrentTableName();
                InputWindow inputDialog = new InputWindow(frame, mFColumnNames, currentTableName, handler);
                inputDialog.setVisible(true);
            });

            deleteButton.addActionListener(e -> deleteSelectedEntry());

            disconnectButton.addActionListener(e -> {
                try {
                    handler.getConnector().close();
                    handler = null;
                    frame.dispose();
                    isOpen = false;
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });

            historyButton.addActionListener(e -> {
                try {
                    String currentTableName = handler.getCurrentTableName();
                    String logName = handler.getHistory();
                    
                    if (logName == null || logName.isEmpty()) {
                        historyButton.setEnabled(false); 
                        return;
                    }

                    if (logName.equals(currentTableName)) {
                        if (previousTableName != null) {
                            updateTable(previousTableName);
                            tableNameLabel.setText("Tabelle: " + Formatter.formatName(previousTableName));
                            handler.setCurrentTableName(previousTableName);
                            previousTableName = null;
                        } else {
                            JOptionPane.showMessageDialog(frame, "Keine vorherige Tabelle zum Zurückkehren gefunden.", "Fehler", JOptionPane.WARNING_MESSAGE);
                        }
                    } else {
                        previousTableName = currentTableName;
                        updateTable(logName);
                        tableNameLabel.setText("Tabelle: " + Formatter.formatName(logName));
                        handler.setCurrentTableName(logName);
                    }
                    updateButtonStates(); // Update button states after changing the table
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Fehler beim Anzeigen der Log-Tabelle.", "Fehler", JOptionPane.ERROR_MESSAGE);
                }
            });

            updateButtonStates(); // Initial button state update

            frame.addWindowFocusListener(new WindowAdapter() {
                @Override
                public void windowGainedFocus(WindowEvent e) {
                    try {
                        updateTable(handler.getCurrentTableName());
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(frame, "Fehler beim Aktualisieren der Tabelle.", "Fehler", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            infoTable.getSelectionModel().addListSelectionListener(e -> {
                int selectedRow = infoTable.getSelectedRow();
                if (selectedRow != -1) {
                    int primeKeyIndex = handler.getPrimeKeyIndex();
                    if (primeKeyIndex != -1 && primeKeyIndex < tableModel.getColumnCount()) {
                        Object idObject = tableModel.getValueAt(selectedRow, primeKeyIndex);
                        if (idObject instanceof Integer) {
                            currentPrimeKey = (Integer) idObject;
                        }
                    }
                }
            });
        }); 
    }

    
    
    
    private void updateButtonStates() {
        List<String> tableNames = getTableNames();
        boolean hasMultipleTables = tableNames.size() > 1;
        
        previousTableButton.setEnabled(hasMultipleTables);
        nextTableButton.setEnabled(hasMultipleTables);
    }

    
    private void deleteSelectedEntry() {
	    int selectedRow = infoTable.getSelectedRow();
	    if (selectedRow != -1) {
	        String currentTableName = handler.getCurrentTableName(); // Use handler to get current table name
	        if (currentTableName == null) {
	            JOptionPane.showMessageDialog(frame, "Aktuelle Tabelle nicht gefunden.", "Fehler", JOptionPane.ERROR_MESSAGE);
	            return;
	        }
	
	        int primeKeyIndex = handler.getPrimeKeyIndex();
	        if (primeKeyIndex < 0 || primeKeyIndex >= infoTable.getColumnCount()) {
	            JOptionPane.showMessageDialog(frame, "Primärschlüsselspalte ist ungültig.", "Fehler", JOptionPane.ERROR_MESSAGE);
	            return;
	        }
	
	        Object value = infoTable.getValueAt(selectedRow, primeKeyIndex);
	        if (value == null) {
	            JOptionPane.showMessageDialog(frame, "Primärschlüsselwert ist leer oder ungültig.", "Fehler", JOptionPane.ERROR_MESSAGE);
	            return;
	        }
	
	        int selectedPrimeKey;
	        try {
	            selectedPrimeKey = Integer.parseInt(value.toString()); // Convert to int
	        } catch (NumberFormatException e) {
	            JOptionPane.showMessageDialog(frame, "Primärschlüsselwert ist ungültig.", "Fehler", JOptionPane.ERROR_MESSAGE);
	            return;
	        }
	
	        // Show confirmation dialog
	        int confirmation = JOptionPane.showConfirmDialog(
	            frame,
	            "Möchten Sie den Eintrag wirklich löschen?",
	            "Bestätigung",
	            JOptionPane.YES_NO_OPTION,
	            JOptionPane.WARNING_MESSAGE
	        );
	
	        if (confirmation == JOptionPane.YES_OPTION) {
	            try {
	                // Delete the entry by primary key (int)
	                handler.deleteEntryByPrimeKey(currentTableName, selectedPrimeKey);
	                updateTable(currentTableName); // Refresh the table after deletion
	                
	            } catch (SQLException e) {
	                e.printStackTrace();
	                JOptionPane.showMessageDialog(frame, "Fehler beim Löschen des Eintrags.", "Fehler", JOptionPane.ERROR_MESSAGE);
	            }
	        }
	    } else {
	        JOptionPane.showMessageDialog(frame, "Bitte wählen Sie eine Zeile aus.", "Fehler", JOptionPane.ERROR_MESSAGE);
	    }
    }

    
    
    private String getCurrentTableName() {
        return handler.getTableNames().get(currentTableIndex);
    }

    private List<String> getTableNames() {
        return handler.getTableNames();
    }

    
    private void showDetailsWindow() {
        int selectedRow = infoTable.getSelectedRow();
        if (selectedRow != -1) {
            try {
                String currentTableName = getCurrentTableName();
                if (currentTableName == null) {
                    JOptionPane.showMessageDialog(frame, "Aktuelle Tabelle nicht gefunden.", "Fehler", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int primeKeyIndex = handler.getPrimeKeyIndex();
                if (primeKeyIndex < 0 || primeKeyIndex >= infoTable.getColumnCount()) {
                    JOptionPane.showMessageDialog(frame, "Primärschlüsselspalte ist ungültig.", "Fehler", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Object value = infoTable.getValueAt(selectedRow, primeKeyIndex);
                String selectedPrimeKey = value != null ? value.toString() : "";

                if (selectedPrimeKey.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Primärschlüsselwert ist leer oder ungültig.", "Fehler", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int initialIndex = primeKeyList.indexOf(selectedPrimeKey);
                if (initialIndex == -1) {
                    JOptionPane.showMessageDialog(frame, "Primärschlüssel nicht gefunden.", "Fehler", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                detailsWindow = DetailsWindow.getInstance(frame, handler, getColumnNames(), Color.LIGHT_GRAY);
                detailsWindow.showDetails(primeKeyList, initialIndex);

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Fehler beim Anzeigen der Details.", "Fehler", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Bitte wählen Sie eine Zeile aus.", "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(String tableName) throws SQLException {
        handler.setCurrentTableName(tableName);
        // Refresh column names before fetching data
        handler.refreshColumnNames();

        List<String[]> data = handler.fetchAllDataForTable(tableName);
        // Update local column names from the fetched data
        if (!data.isEmpty()) {
            mFColumnNames = List.of(data.get(0)); // Assuming the first row contains column names
            primeKeyList = handler.fetchPrimeKeysForTable(tableName); // Update the primary key list
        }

        currentPrimeKey = -1;
        updateTableModel(data);
    }

    
    private void updateTableModel(List<String[]> data) {
        tableModel.setRowCount(0);
        
        if (data.isEmpty()) {
            tableModel.setColumnCount(0);
            return;
        }

       
        tableModel.setColumnIdentifiers(mFColumnNames.toArray(new String[0]));

        for (int i = 1; i < data.size(); i++) {
            tableModel.addRow(data.get(i));
        }
    }
    
    
    public boolean isOpen() {
        return isOpen;
    }

    public List<String> getColumnNames() {
        return mFColumnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.mFColumnNames = columnNames;
    }
}
