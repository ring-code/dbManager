package gui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import mySQL.MySQLDataHandler;
import utility.Formatter;
import utility.Messages;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** GUI Constructor enabling the user to search partially or exact among multiple columns in a given table
 *  using the according Button. Empty input is not possible, can be closed with ESC or the cancel Button.
 * 		
 * @author ring-code
 * 
 *  
 */

public class SearchWindow extends JDialog {

    private static final long serialVersionUID = 1L;

    private DetailsWindow detailsWindow;
    private final MySQLDataHandler queryHandler;

    private final List<JTextField> textFields;
    private final JButton exactSearchButton; 
    private final JButton partialSearchButton; 
    private final JButton cancelButton;
    @SuppressWarnings("unused")
	private final JFrame parentFrame;
    
    private final List<String> columnNames;
    private int primaryKeyIndex; 

    public SearchWindow(JFrame parentFrame, List<String> columnNames, MySQLDataHandler queryHandler) {
        super(parentFrame, "Suche", true); // Initially modal

       
        this.textFields = new ArrayList<>();
        this.queryHandler = queryHandler;
        this.parentFrame = parentFrame;
        this.columnNames = columnNames;
        this.primaryKeyIndex = queryHandler.getPrimeKeyIndex(); // Dynamically fetch primary key index

       
        String currentTableName = queryHandler.getCurrentTableName();

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel otherFieldsPanel = new JPanel(new GridBagLayout());
        JPanel buttonPanel = new JPanel();

        
        JLabel tableNameLabel = new JLabel("Suche in: " + Formatter.formatName(currentTableName));
        tableNameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        tableNameLabel.setHorizontalAlignment(SwingConstants.CENTER);

        
        mainPanel.add(tableNameLabel, BorderLayout.NORTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); 

        
        Font labelFont = new Font("SansSerif", Font.BOLD, 14);
        int yPos = 0; 

        for (int i = 0; i < columnNames.size(); i++) {
            if (i == primaryKeyIndex) {
                continue; // Skip the primary key column
            }

            String columnName = columnNames.get(i);

            JLabel label = new JLabel(columnName + ":");
            label.setFont(labelFont);
            gbc.gridx = 0;
            gbc.gridy = yPos;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.EAST;
            gbc.weightx = 0.1;
            gbc.weighty = 0.1;
            otherFieldsPanel.add(label, gbc);

            JTextField textField = new JTextField();
            textField.setName(columnName); // Set the name of the text field to the column name
            gbc.gridx = 1;
            gbc.gridy = yPos;
            gbc.fill = GridBagConstraints.BOTH;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.9;
            gbc.weighty = 0.1;
            otherFieldsPanel.add(textField, gbc);

            textFields.add(textField);

            // Add DocumentListener to each non-primary key text field
            textField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    updateButtonStates();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    updateButtonStates();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    updateButtonStates();
                }
            });

            yPos++; // Move to the next row for the next field
        }

        
        exactSearchButton = new JButton("Exakte Suche"); 
        partialSearchButton = new JButton("Partielle Suche"); 
        cancelButton = new JButton("Abbrechen");

        exactSearchButton.setEnabled(false);
        partialSearchButton.setEnabled(false);
        cancelButton.setEnabled(true);

        buttonPanel.add(exactSearchButton);
        buttonPanel.add(partialSearchButton);
        buttonPanel.add(cancelButton);

        buttonPanel.revalidate();
        buttonPanel.repaint();

        exactSearchButton.setEnabled(false); 
        partialSearchButton.setEnabled(false); 

        exactSearchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performExactSearch();
            }
        });

        partialSearchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performPartialSearch();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false); 
            }
        });

        getRootPane().setDefaultButton(exactSearchButton);
        getRootPane().registerKeyboardAction(
            e -> performPartialSearch(),
            KeyStroke.getKeyStroke("ENTER"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        getRootPane().registerKeyboardAction(
            e -> setVisible(false),
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        mainPanel.add(otherFieldsPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel); 

        pack();
        setMinimumSize(new Dimension(400, 600));
        setLocationRelativeTo(parentFrame);
    }

    private void updateButtonStates() {
        boolean anyFieldHasText = textFields.stream()
                .anyMatch(tf -> !tf.getText().trim().isEmpty());

        partialSearchButton.setEnabled(anyFieldHasText);
        exactSearchButton.setEnabled(anyFieldHasText); 
    }

    private void performExactSearch() {
        if (!exactSearchButton.isEnabled()) {
            return;
        }

        try {
            String currentTableName = queryHandler.getCurrentTableName();

            List<String> selectedColumnNames = new ArrayList<>();
            List<String> searchValues = new ArrayList<>();

            for (JTextField textField : textFields) {
                String value = textField.getText().trim();
                if (!value.isEmpty() && !textField.getName().equals("Nummer")) { // Skip the primary key field
                    selectedColumnNames.add(textField.getName()); // The name of the text field is the column name
                    searchValues.add(value);
                }
            }

            List<Integer> primaryKeyList = queryHandler.exactDataSearch(currentTableName, selectedColumnNames, searchValues);

            if (!primaryKeyList.isEmpty()) {
                // Convert List<Integer> to List<String>
                List<String> primaryKeyListAsString = new ArrayList<>();
                for (Integer pk : primaryKeyList) {
                    primaryKeyListAsString.add(pk.toString());
                }

                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(SearchWindow.this);

                detailsWindow = DetailsWindow.getInstance(
                    parentFrame,
                    queryHandler,
                    columnNames,
                    Color.LIGHT_GRAY
                );

                detailsWindow.showDetails(primaryKeyListAsString, 0);
            } else {
            	JOptionPane.showMessageDialog(SearchWindow.this, Messages.NOTHING_FOUND_ERROR, "Keine Ergebnisse", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(SearchWindow.this, "Error executing search: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void performPartialSearch() {
        if (!partialSearchButton.isEnabled()) {
            return; 
        }

        try {
            String currentTableName = queryHandler.getCurrentTableName();

            List<String> selectedColumnNames = new ArrayList<>();
            List<String> searchValues = new ArrayList<>();

            for (JTextField textField : textFields) {
                String value = textField.getText().trim();
                if (!value.isEmpty() && !textField.getName().equals("Nummer")) { // Skip the primary key field
                    selectedColumnNames.add(textField.getName()); // The name of the text field is the column name
                    searchValues.add(value);
                }
            }

            List<Integer> primaryKeyList = queryHandler.partialDataSearch(currentTableName, selectedColumnNames, searchValues);

            if (!primaryKeyList.isEmpty()) {
                // Convert List<Integer> to List<String>
                List<String> primaryKeyListAsString = new ArrayList<>();
                for (Integer pk : primaryKeyList) {
                    primaryKeyListAsString.add(pk.toString());
                }

                Frame parentFrame = (Frame) SwingUtilities.getWindowAncestor(SearchWindow.this);

                detailsWindow = DetailsWindow.getInstance(
                    parentFrame,
                    queryHandler,
                    columnNames,
                    Color.LIGHT_GRAY
                );

                detailsWindow.showDetails(primaryKeyListAsString, 0);
            } else {
                JOptionPane.showMessageDialog(SearchWindow.this, Messages.NOTHING_FOUND_ERROR, "Keine Ergebnisse", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(SearchWindow.this, "Error executing search: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public List<String> getInputValues() {
        List<String> values = new ArrayList<>();
        for (JTextField textField : textFields) {
            values.add(textField.getText());
        }
        return values;
    }
}
