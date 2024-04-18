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
import java.io.File;
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
    private JLabel wRegisterLabel;
    private JLabel PCLabel;
    private JLabel PCLLabel;
    private JLabel PCLATHLabel;
    private JLabel carryLabel;
    private JLabel digitCarryLabel;
    private JLabel zeroLabel;
    private Simulator simulator;
    private File selectedFile;
    private int[] lines;
    private int line;


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
                    setSelectedFile(selectedFile); // Speichern Sie das ausgewählte Dateiobjekt
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


        // Run Button
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Program.running) {
                    return;
                }
                Program.running = true;
                Thread thread = Thread.startVirtualThread(Program::runProgram);
            }
        });
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Program.running = false;
            }
        });
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Program.simulator.powerOnReset();
                Program.gui.updateGUI(Program.simulator);
            }
        });
        stepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Program.simulator.nextInstruction();
            }
        });
    }

    public File getSelectedFile() {
        if (selectedFile != null) {
            System.out.println("get selected file: " + selectedFile.getAbsolutePath());
        }
        return selectedFile;
    }

    public File waitForSelectedFile() {
        File selectedFile = null;
        while (selectedFile == null) {
            selectedFile = getSelectedFile();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return selectedFile;
    }


    public void setSelectedFile(File file) {
        this.selectedFile = file;
    }

    public void highlightCommand(int line) {
        System.out.println("\nLINE: " + line);
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                System.out.println("LINE IN COMPONENT: " + line);
                if (row == line - 1) {
                    c.setBackground(Color.PINK);
                } else {
                    c.setBackground(Color.WHITE);
                }

                return c;
            }
        };

        table3.getColumnModel().getColumn(1).setCellRenderer(renderer);
        table3.repaint();
    }


    /**
     * highlights the current line in the GUI
     * @param line line to be highlighted
     */
/**    public void highlightCommand(int line) {
        cell.setBackground(Color.PINK);
        return cell;
    } **/

    // Checkboxen
    class CheckBoxRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
        JCheckBox checkBox = new JCheckBox();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            checkBox.setSelected((Boolean) value);
            return checkBox;
        }
    }

    /**
     * Updates the GUI after each instruction called by the simulator
     * @param simulator instance of Simulator
     */
    public void updateGUI(Simulator simulator) {
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> updateGUI(simulator));
            return;
        }
        setLine();
        // highlight line
        highlightCommand(line);

        // update labels
        wRegisterLabel.setText("W-Register: " + String.format("0x%X", Simulator.wRegister));
        PCLabel.setText("PC: " + String.format("0x%X", Simulator.programCounter));
        PCLLabel.setText("PCL: " + String.format("0x%X", simulator.getPCL()));
        PCLATHLabel.setText("PCLATH: " + String.format("0x%X", simulator.getPCLath()));
        carryLabel.setText("Carry: " + String.format("%d", simulator.getCarry()));
        digitCarryLabel.setText("Digit Carry: " + String.format("%d", simulator.getDigitCarry()));
        zeroLabel.setText("Zero: " + String.format("%d", simulator.getZero()));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUI().setVisible(true));
    }

    public void setLines(int[] lines) {
        this.lines = lines;
        this.line = lines[Simulator.programCounter];
    }
    public void setLine() {
        this.line = lines[Simulator.programCounter];
    }
}