package gui;

import javax.swing.*;

import mySQL.MySQLDataHandler;
import utility.Messages;

import static utility.StringInputValidation.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

/**
 * The MainFrame opens this for the user to add an entry in every column for a given table except for the primary Key. 
 * It uses the StringInputValidation class to validate each field and will inform the user via color changes, 
 * information messages, and disabling the button about invalid inputs.
 * 
 * @author ring-code
 */		

public class InputWindow extends JDialog {

    private static final long serialVersionUID = 1L;

    private final List<JTextField> textFields;
    private final JButton addButton;
    private final JButton cancelButton;
    @SuppressWarnings("unused")
    private final MySQLDataHandler queryHandler;
    @SuppressWarnings("unused")
    private final String tableName;
    private int primeKeyIndex; 

    public InputWindow(JFrame parentFrame, List<String> columnNames, String tableName, MySQLDataHandler queryHandler) {
        super(parentFrame, "Eintrag hinzufügen: " + tableName, true);

        // Save the table name and query handler
        this.tableName = tableName;
        this.queryHandler = queryHandler;

        this.primeKeyIndex = queryHandler.getPrimeKeyIndex(); // Fetch primary key index

        // Create a list of relevant column names, excluding the primary key column
        List<String> relevantColumnNames = new ArrayList<>(columnNames);
        if (primeKeyIndex >= 0 && primeKeyIndex < relevantColumnNames.size()) {
            relevantColumnNames.remove(primeKeyIndex);
        }
        this.textFields = new ArrayList<>();

        // Create the panel with GridBagLayout
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Reduced padding for closer alignment

        // Create larger font for labels
        Font labelFont = new Font("SansSerif", Font.BOLD, 14);

        // Create labels and input fields for each relevant column
        for (int i = 0; i < relevantColumnNames.size(); i++) {
            String columnName = relevantColumnNames.get(i);

            // Label
            JLabel label = new JLabel(columnName + ":");
            label.setFont(labelFont); // Set the larger font
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.fill = GridBagConstraints.BOTH; 
            gbc.anchor = GridBagConstraints.EAST; 
            gbc.weightx = 0.1; 
            gbc.weighty = 0.1;
            panel.add(label, gbc);

            // TextField
            JTextField textField = new JTextField();
            textField.setName(columnName); // Set the name of the text field to the column name
            gbc.gridx = 1;
            gbc.gridy = i;
            gbc.fill = GridBagConstraints.BOTH; // Allow vertical and horizontal expansion
            gbc.anchor = GridBagConstraints.WEST; // Align text field to the west
            gbc.weightx = 0.9; // Larger weight for the text field
            gbc.weighty = 0.1; // Consistent weight for vertical scaling
            panel.add(textField, gbc);

            textFields.add(textField);

            // Add a key listener to validate input on the fly
            textField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyReleased(KeyEvent e) {
                    validateSingleInput(textFields.indexOf(textField)); // Validate the input for each field individually
                }
            });
        }

        // Create OK and Cancel buttons
        addButton = new JButton("Hinzufügen");
        addButton.setEnabled(false); // Initially disabled

        cancelButton = new JButton("Abbrechen");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);

        // Add action listener for OK button
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<String> values = getInputValues();
                try {
                    queryHandler.addEntryToTable(tableName, relevantColumnNames, values);
                    setVisible(false); // Close the dialog on successful addition
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(InputWindow.this, 
                            "Error adding entry to table: " + ex.getMessage(), 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Add action listener for cancel button
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        // Add key listeners for ESC and Enter keys
        this.getRootPane().registerKeyboardAction(
            e -> cancelButton.doClick(), // Simulate cancel button click
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        this.getRootPane().registerKeyboardAction(
            e -> addButton.doClick(), // Simulate OK button click
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        // Add components to dialog
        add(panel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Set dialog properties
        pack();
        setMinimumSize(new Dimension(400, 600)); // Set a minimum size for the dialog
        setLocationRelativeTo(parentFrame);

        // Add component listeners to handle window resizing
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                panel.revalidate(); // Revalidate panel on resize
            }
        });
    }

    // Method to validate a single input field by its index
    private void validateSingleInput(int index) {
        JTextField textField = textFields.get(index);
        String input = textField.getText();
        boolean isValid;
        String errorMessage;

        // Validate input based on the field index
        switch (index) {
            case 0:            
            case 1:
                isValid = validName(input);
                errorMessage = Messages.INVALID_NACHNAME_ERROR;
                break;
            case 2:
                isValid = validAddress(input);
                errorMessage = Messages.INVALID_ADDRESS_ERROR;
                break;
            case 3:
                isValid = validZipCode(input);
                errorMessage = Messages.INVALID_ZIP_CODE_ERROR;
                break;
            case 4:
                isValid = validName(input);
                errorMessage = Messages.INVALID_NACHNAME_ERROR;
                break;
            case 5:
                isValid = validPhoneNumber(input);
                errorMessage = Messages.INVALID_PHONE_ERROR;
                break;
            case 6:
                isValid = validEmail(input);
                errorMessage = Messages.INVALID_EMAIL_ERROR;
                break;
            default:
                isValid = validName(input); // Default validation
                errorMessage = Messages.INVALID_INPUT_ERROR;
                break;
        }

        // Set the tooltip for the input field based on validation
        textField.setToolTipText(isValid ? null : errorMessage);

        // If the input is invalid, set the background color to red; otherwise, set it to white
        textField.setBackground(isValid ? Color.WHITE : Color.PINK);

        // Check if all inputs are valid
        boolean allValid = true;
        for (int i = 0; i < textFields.size(); i++) {
            JTextField tf = textFields.get(i);
            String inputField = tf.getText();
            boolean isFieldValid;

            // Validate input based on the field index
            switch (i) {
                case 0:
                case 4:
                    isFieldValid = validName(inputField);
                    break;
                case 1:
                    isFieldValid = validName(inputField);
                    break;
                case 2:
                    isFieldValid = validAddress(inputField);
                    break;
                case 3:
                    isFieldValid = validZipCode(inputField);
                    break;
                case 5:
                    isFieldValid = inputField.isEmpty() || validPhoneNumber(inputField);
                    break;
                case 6:
                    isFieldValid = inputField.isEmpty() || validEmail(inputField);
                    break;
                default:
                    isFieldValid = validName(inputField); // Default validation
                    break;
            }

            if (!isFieldValid) {
                allValid = false;
                break; // Stop checking as soon as one field is invalid
            }
        }

        // Enable or disable the OK button based on overall validity
        addButton.setEnabled(allValid);
    }

    
    
    
    
    
    
    
    // Method to get the text input values
    public List<String> getInputValues() {
        List<String> values = new ArrayList<>();
        for (JTextField textField : textFields) {
            values.add(textField.getText());
        }
        return values;
    }
}
