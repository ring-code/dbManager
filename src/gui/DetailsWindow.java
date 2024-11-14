package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import mySQL.MySQLDataHandler;
import utility.Formatter;
import utility.Messages;
import utility.StringInputValidation;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**  
 * The other GUI classes have an instance of this to display, update and/or delete data for a single entry and switch to the next 
 * or previous one based on a list of primary keys. It will update itself and not create a new window every time due to instance behavior.
 *
 *
 * <ul>
 * <li><b>Information:</b>
 *   <ul>
 *     <li>Current table and the data for the currently selected entry.</li>
 *   </ul>
 * </li>
 * <li><b>Buttons and Functionality:</b>
 *   <ul>
 *     <li><code>previousButton</code>: Switches to the previous entry in the list. Listens for the left and up arrow keys. 
 *       Wraps around if there is no previous entry and turns off if only one entry is present.</li>
 *     <li><code>nextButton</code>: Switches to the next entry in the list. Listens for the right and down arrow keys. 
 *       Wraps around if there is no next entry and turns off if only one entry is present.</li>
 *     <li><code>deleteButton</code>: Calls the handler to delete the current entry after confirmation. Also listens for the delete key.</li>
 *     <li><code>cancelButton</code>: Closes the window. Also listens for ESC and Enter keys.</li>
 *     <li><code>F2 / doubleclick: Enables editing for the currently selected row</code></li>
 *   </ul>
 * </li>
 * </ul>
 * 
 * @author ring-code
 *
 */  		 



public class DetailsWindow extends JDialog {

    private static final long serialVersionUID = 1L;
    
    private static DetailsWindow instance; // Static instance to ensure singleton behavior
    private MySQLDataHandler mySQLHandler;
    private List<String> primeKeyList;
    private int currentIndex;
    private int deleteIndex; // Index to track the entry to be deleted

    private String[] data;
    
    // Public fields
    public List<String> columnNames;
    public Color backgroundColor;

    // UI Components
    private JTable detailsTable;
    private JButton previousButton;
    private JButton deleteButton;
    private JButton nextButton;
    private JButton closeButton;
    private JLabel titleLabel; 

    // Private constructor to prevent direct instantiation
    private DetailsWindow(Frame parent, MySQLDataHandler dataHandler, List<String> columnNames, Color backgroundColor) {
        super(parent, "Details", true); // Initialize as a modal dialog

        this.mySQLHandler = dataHandler;
        this.columnNames = columnNames;
        this.backgroundColor = backgroundColor;

        // Initialize components
        initializeUI();
        setupEventListeners();
        setupKeyBindings();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Create and configure the custom title label
        titleLabel = new JLabel("Details", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18)); // Set font size and style
        titleLabel.setPreferredSize(new Dimension(getWidth(), 40)); // Set the height of the title label

        // Create and configure the details table
        detailsTable = new JTable();
        detailsTable.setFont(new Font("SansSerif", Font.PLAIN, 16)); // Larger font size
        detailsTable.setRowHeight(30); // Increase row height for better visibility
        detailsTable.setFillsViewportHeight(true);

        JScrollPane tableScrollPane = new JScrollPane(detailsTable);
        tableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        // Remove default border from the JScrollPane to avoid extra spacing
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Create buttons and configure them
        previousButton = new JButton("\u2190"); // Left arrow character (←)
        deleteButton = new JButton("Löschen");
        closeButton = new JButton("Abbrechen");
        nextButton = new JButton("\u2192"); // Right arrow character (→)

        // Create and configure button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Center alignment with spacing
        buttonPanel.add(previousButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(closeButton);
        buttonPanel.add(nextButton);

        // Add components to the dialog
        add(titleLabel, BorderLayout.NORTH); // Add title label at the top
        add(tableScrollPane, BorderLayout.CENTER); // Add table scroll pane in the center
        add(buttonPanel, BorderLayout.SOUTH); // Add button panel at the bottom

        // Set the initial size of the dialog
        setSize(600, 402); // Set size as desired

        // Position the dialog relative to its parent
        setLocationRelativeTo(getOwner()); // Center relative to parent window

        // Set initial focus to the previousButton after UI is created
        SwingUtilities.invokeLater(() -> previousButton.requestFocusInWindow());
    }

    // Static method to get the singleton instance of DetailsWindow
    public static DetailsWindow getInstance(Frame owner, MySQLDataHandler dataHandler, List<String> columnNames, Color backgroundColor) {
        if (instance == null || !instance.isVisible()) {
            instance = new DetailsWindow(owner, dataHandler, columnNames, backgroundColor);
        }
        return instance;
    }

    //for a single prime key
    public void showDetails(String primeKey) throws SQLException {
        List<String> singleKeyList = new ArrayList<>(List.of(primeKey)); 
        showDetails(singleKeyList, 0); 
    }
    
       
    //for multiple prime keys  
    public void showDetails(List<String> primeKeyList, int initialIndex) throws SQLException {
    if (primeKeyList == null || primeKeyList.isEmpty()) {
        throw new IllegalStateException("Primary key list is not set.");
    }

    this.primeKeyList = new ArrayList<>(primeKeyList);
    this.currentIndex = Math.max(0, Math.min(initialIndex, primeKeyList.size() - 1)); // Ensure index is within bounds

    // Update the title and details
    String tableName = mySQLHandler.getCurrentTableName();
    if (tableName != null) {
        titleLabel.setText(Formatter.formatName(tableName));
    }

    updateDetails();
    setVisible(true);
    SwingUtilities.invokeLater(() -> {
        toFront();
        previousButton.requestFocusInWindow();
    });
}
    
    private void updateDetails() throws SQLException {
        if (mySQLHandler == null) {
            throw new IllegalStateException("DataHandler is not set.");
        }

        if (primeKeyList == null || primeKeyList.isEmpty()) {
            throw new IllegalStateException("Primary key list is not set.");
        }

        if (currentIndex < 0 || currentIndex >= primeKeyList.size()) {
            throw new IllegalStateException("Current index is out of bounds.");
        }

        String currentPrimeKey = primeKeyList.get(currentIndex);
        
        try {
            data = mySQLHandler.fetchDataByPrimeKey(mySQLHandler.getCurrentTableName(), currentPrimeKey);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching data: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1 && row != 0; // Not editable for the first row
            }
        };

        model.addColumn("");
        model.addColumn("");

        if (columnNames != null && columnNames.size() == data.length) {
            for (int i = 0; i < columnNames.size(); i++) {
                model.addRow(new Object[]{columnNames.get(i), data[i]});
            }
        }

        detailsTable.setModel(model);
        
       
        
        detailsTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = detailsTable.rowAtPoint(e.getPoint());
                int column = detailsTable.columnAtPoint(e.getPoint());

                // Check if the cell is editable and not the first row
                if (row != 0 && column == 1) {
                    detailsTable.setToolTipText("Doppelklick oder F2 zum Editieren");
                } else {
                   
                    detailsTable.setToolTipText(null);
                }
            }
        });
        
        

        model.addTableModelListener(e -> {
            int row = e.getFirstRow();
            int column = e.getColumn();
            if (column == 1 && row != 0) { // Only track changes in the editable column
                handleInputChange(model, row, column);
            }
        });

        // Remove column headers
        detailsTable.getTableHeader().setVisible(false);

        // Set column widths and alignments
        TableColumnModel columnModel = detailsTable.getColumnModel();
        TableColumn leftColumn = columnModel.getColumn(0);
        TableColumn rightColumn = columnModel.getColumn(1);

        leftColumn.setPreferredWidth(200);
        rightColumn.setPreferredWidth(400);

        detailsTable.setCellSelectionEnabled(true);
        leftColumn.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER); // Center-align text in the left column
                return c;
            }
        });

        rightColumn.setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER); // Center-align text in the right column
                return c;
            }
        });

        // Update button states
        updateButtonState();
    }

    private void handleInputChange(DefaultTableModel model, int row, int column) {
        String updatedValue = (String) model.getValueAt(row, column);
        String columnName = (String) model.getValueAt(row, 0);
        String primeKey = primeKeyList.get(currentIndex); // Get the current prime key

        // Validate input
        if (StringInputValidation.validateInputByColumnName(columnName, updatedValue)) {
            try {
                mySQLHandler.updateEntryByPrimeKey(mySQLHandler.getCurrentTableName(), primeKey, columnName, updatedValue);
                updateDetails(); // Refresh the details after successful update
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating data: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            
            JOptionPane.showMessageDialog(this, Messages.INVALID_INPUT_ERROR, "Ungültige Eingabe", JOptionPane.ERROR_MESSAGE);
           
            model.setValueAt(data[row], row, column); // Revert to original value
        }
    }

    
    
 
    
    private void setupEventListeners() {
    	
    	previousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	
                try {
                    navigateEntry(-1); // Navigate to the previous entry
                    updateDetails(); // Refresh details after navigating
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(DetailsWindow.this, "Error navigating to previous entry: " + ex.getMessage(), "Navigation Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    navigateEntry(1); // Navigate to the next entry
                    updateDetails(); // Refresh details after navigating
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(DetailsWindow.this, "Error navigating to next entry: " + ex.getMessage(), "Navigation Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Close the dialog
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteIndex = currentIndex;
                deleteSelectedEntry(); // Perform the delete operation
            }
        });
    }

    
    
    @SuppressWarnings("serial")
	private void setupKeyBindings() {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        // Register keys to close the dialog
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "close");
        
        actionMap.put("close", new AbstractAction() {
            			
			@Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        
        // Register LEFT and RIGHT arrow keys for navigation
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "previous");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "next");

        // Register UP and DOWN arrow keys for navigation
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "previous");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "next");

        // Register DELETE key to perform delete operation
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        actionMap.put("delete", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteIndex = currentIndex;
                deleteSelectedEntry(); // Perform the delete operation
            }
        });

        actionMap.put("previous", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    navigateEntry(-1); // Navigate to the previous entry
                    updateDetails(); // Refresh details after navigating
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(DetailsWindow.this, "Error navigating to previous entry: " + ex.getMessage(), "Navigation Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        actionMap.put("next", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    navigateEntry(1); // Navigate to the next entry
                    updateDetails(); // Refresh details after navigating
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(DetailsWindow.this, "Error navigating to next entry: " + ex.getMessage(), "Navigation Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void deleteSelectedEntry() {
        try {
            if (primeKeyList != null && !primeKeyList.isEmpty()) {
                // Convert the primary key string to an integer
                int currentPrimeKey = Integer.parseInt(primeKeyList.get(deleteIndex));

                String currentTableName = mySQLHandler.getCurrentTableName();
                if (currentTableName != null) {
                    // Ask for confirmation before deleting the entry
                    int confirmation = JOptionPane.showConfirmDialog(
                        this, // Reference to the current window
                        Messages.DELETE_ENTRY_CONFIRM, // Confirmation message from Messages class
                        "Löschen bestätigen", // Title of the confirmation dialog
                        JOptionPane.YES_NO_OPTION, // Type of options available
                        JOptionPane.WARNING_MESSAGE // Type of message (warning)
                    );

                    if (confirmation == JOptionPane.YES_OPTION) {
                        // Proceed with deletion if the user confirmed
                        
                    	mySQLHandler.deleteEntryByPrimeKey(currentTableName, currentPrimeKey);
                    		
                        // Remove the entry from the list
                        primeKeyList.remove(deleteIndex);

                        // Check if the list is empty and close the window if it is
                        if (primeKeyList.isEmpty()) {
                            dispose(); // Close the window
                        } else {
                            // Adjust the index and update details
                            if (deleteIndex >= primeKeyList.size()) {
                                deleteIndex = primeKeyList.size() - 1;
                            }
                            currentIndex = deleteIndex; // Ensure currentIndex is updated correctly
                            updateDetails(); // Update the details view
                        }
                    } // User selected "No", do nothing.
                } else {
                    // Handle case where the table name is not found
                    Messages.showErrorMessage(Messages.TABLE_LOAD_ERROR, "Fehler");
                }
            } else {
                // Handle case where no entry is selected
                Messages.showErrorMessage(Messages.RECORD_SELECTION_ERROR, "Fehler");
            }
        } catch (NumberFormatException ex) {
            // Handle the case where the conversion fails
            ex.printStackTrace();
            Messages.showErrorMessage("Invalid primary key format.", "Error");
        } catch (SQLException ex) {
            // Print stack trace and show error message
            ex.printStackTrace();
            Messages.showErrorMessage(Messages.ENTRY_DELETION_ERROR + ": " + ex.getMessage(), "Löschfehler");
        }
    }

    
    
    private void navigateEntry(int direction) throws SQLException {
        if (primeKeyList != null && !primeKeyList.isEmpty()) {
            currentIndex = (currentIndex + direction + primeKeyList.size()) % primeKeyList.size();
            updateDetails(); // Refresh details after navigating
        }
    }

    private void updateButtonState() {
        // Disable both "Previous" and "Next" buttons if there's only one entry
        boolean hasMultipleEntries = primeKeyList != null && primeKeyList.size() > 1;
        
        previousButton.setEnabled(hasMultipleEntries);
        nextButton.setEnabled(hasMultipleEntries);
        deleteButton.setEnabled(primeKeyList != null && !primeKeyList.isEmpty());
    }

}
