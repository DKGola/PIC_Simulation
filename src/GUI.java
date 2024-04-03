package src;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class GUI extends JFrame {

    private JSlider slider1;
    private JButton runButton;
    private JButton stopButton;
    private JButton stepButton;
    private JButton resetButton;
    private JTextPane LSTTextPane;
    private JTabbedPane tabbedPane1;
    private JTextArea consoleTextArea;
    private JButton fileButton;
    private JButton helpButton;
    private JTable table1;
    private JTable table2;
    private JPanel mainPanel;
    private JTable table3;

    public GUI() {
        setSize(1200, 800);
        setVisible(true);
        setContentPane(mainPanel);

        fileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select LST File");
                fileChooser.setFileFilter(new FileNameExtensionFilter("LST Files", "lst"));
                int userSelection = fileChooser.showOpenDialog(GUI.this);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    // Datei auswählen
                    java.io.File selectedFile = fileChooser.getSelectedFile();
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
                        // Tabelle füllen
                        List<String> lines = new ArrayList<>();
                        // Zeilen einzeln einlesen
                        String line;
                        while ((line = reader.readLine()) != null) {
                            lines.add(line);
                        }

                        // Tabelle mit zwei Spalten erstellen
                        DefaultTableModel model = new DefaultTableModel() {
                            @Override
                            public Class<?> getColumnClass(int columnIndex) {
                                if (columnIndex == 0) {
                                    return Boolean.class; // Erste Spalte ist vom Typ Boolean für Checkboxen
                                } else {
                                    return super.getColumnClass(columnIndex);
                                }
                            }
                        };
                        model.addColumn("BP");
                        model.addColumn("Commands");

                        // Tabellenzeilen füllen
                        for (String l : lines) {
                            model.addRow(new Object[]{false, l}); // Breakpoint standardmäßig auf false setzen
                        }

                        table3.setModel(model);
                        // Spaltenbreite Breakpoint
                        TableColumnModel columnModel = table3.getColumnModel();
                        columnModel.getColumn(0).setMaxWidth(30);

                        columnModel.getColumn(0).setCellRenderer(new CheckBoxRenderer());
                        columnModel.getColumn(0).setCellEditor(new DefaultCellEditor(new JCheckBox()));


                        reader.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(GUI.this, "Error reading file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    class CheckBoxRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
        JCheckBox checkBox = new JCheckBox();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            checkBox.setSelected((Boolean) value); // Checkbox entsprechend des Werts in der Zelle setzen
            return checkBox;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GUI().setVisible(true);
            }
        });
    }
}