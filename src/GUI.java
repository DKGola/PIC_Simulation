package src;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private JTable ioTable;
    private JLabel RuntimeLabel;
    private Simulator simulator;
    private File selectedFile;
    private int[] lines;
    private int line;
    public int[][] ioData;


    public GUI() {
        setSize(1200, 800);
        setVisible(true);
        setContentPane(mainPanel);
        ioData = new int[16][2];

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
                            model.addRow(new Object[]{false, l}); // Breakpoints are initially false
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
        // change iotable Port with mouseclick
        ioTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    JTable target = (JTable)e.getSource();
                    int row = target.getSelectedRow();
                    int column = target.getSelectedColumn();
                    if (row == 2 && column != 0) {  // Port A cell is clicked
                        togglePortBitInRam(0, column);
                    }
                    if (row == 6 && column != 0) {  // Port B cell is clicked
                        togglePortBitInRam(1, column);
                    }
                    Program.gui.updateGUI(Program.simulator);
                }
            }
        });
    }

    private void togglePortBitInRam(int port, int column) {
        int[][] ram = Program.simulator.getRam();
        int pinValue = (port == 0) ? ram[0][5] : ram[0][6];
        int mask = 1 << (column - 1); // select bit depending on cell
        ram[0][5 + port] = pinValue ^ mask; // toggle port (0 -> 1; 1 -> 0)
    }

    public void updateIOTable() {
        System.out.println("UPDATEIOTABLE AUFGERUFEN");
        updateIoData();

        Object[][] data = new Object[7][9]; // 7 Zeilen, 9 Spalten
        String[] columnNames = {"7", "6", "5", "4", "3", "2", "1", "0"};

        // RA
        data[0][0] = "RA";
        System.arraycopy(columnNames, 0, data[0], 1, columnNames.length);
        data[1][0] = "Tris A";
        for (int i = 1; i < 9; i++) {
            data[1][i] = ioData[i - 1][0];
        }
        data[2][0] = "Pin A";
        for (int i = 1; i < 9; i++) {
            data[2][i] = ioData[i - 1][1];
        }
        // empty line
        data[3][0] = "";

        // RB
        data[4][0] = "RB";
        System.arraycopy(columnNames, 0, data[4], 1, columnNames.length);
        data[5][0] = "Tris B";
        for (int i = 1; i < 9; i++) {
            data[5][i] = ioData[i + 7][0];
        }
        data[6][0] = "Pin B";
        for (int i = 1; i < 9; i++) {
            data[6][i] = ioData[i + 7][1];
        }

        CustomTableModel ioModel = new CustomTableModel(data, columnNames);
        ioTable.setModel(ioModel);
        ioTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < ioTable.getColumnCount(); i++) {
            ioTable.getColumnModel().getColumn(i).setPreferredWidth(50);
        }
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
            checkBox.setSelected(value.equals("1"));
            return checkBox;
        }
    }

    /**
     * Updates the GUI after each instruction called by the simulator
     * @param simulator instance of Simulator
     */
    public void updateGUI(Simulator simulator) {
        System.out.println("UPDATEGUI AUFGERUFEN");
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
        // update ioTable
        updateIOTable();
        // update runtime label
        RuntimeLabel.setText("Runtime: " + String.format("%.2f µs", Program.simulator.getRuntime()));
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

    public boolean hasBreakpoint() {
        DefaultTableModel model = (DefaultTableModel) table3.getModel();
        Boolean breakpoint = (Boolean) model.getValueAt(line-1, 0);
        return breakpoint != null && breakpoint;
    }
    /*
        sets ioData
        First bracket = PIN
        Second bracket = Bank 0 / 1
        initially: TRISA 00011111   TRISB 11111111  PORTA
         */
    public void updateIoData() {
        System.out.println("UPDATEIODATA AUFGERUFEN");
        int[][] ram = Program.simulator.getRam();
        String trisAString = String.format("%8s", Integer.toBinaryString(ram[1][5])).replace(' ', '0');
        String trisBString = String.format("%8s", Integer.toBinaryString(ram[1][6])).replace(' ', '0');
        String portAString = String.format("%8s", Integer.toBinaryString(ram[0][5])).replace(' ', '0');
        String portBString = String.format("%8s", Integer.toBinaryString(ram[0][6])).replace(' ', '0');


        for (int i = 0; i < 8; i++) {
            ioData[i][0] = Character.getNumericValue(trisAString.charAt(i));   // Tris A
            ioData[i][1] = Character.getNumericValue(portAString.charAt(i));   // Port A
        }

        for (int i = 8, j = 0; i < 16; i++, j++) {
            ioData[i][0] = Character.getNumericValue(trisBString.charAt(j));   // Tris B
            ioData[i][1] = Character.getNumericValue(portBString.charAt(j));   // Port B
        }

        System.out.println("\n\nTris A:");
        for (int i = 0; i < 8; i++) {
            System.out.print(ioData[i][0]);
        }
        System.out.println("\n\nTris B:");
        for (int i = 8; i < 16; i++) {
            System.out.print(ioData[i][0]);
        }
        System.out.println("\n\nPort A:");
        for (int i = 0; i < 8; i++) {
            System.out.print(ioData[i][1]);
        }
        System.out.println("\n\nPort B:");
        for (int i = 8; i < 16; i++) {
            System.out.print(ioData[i][1]);
        }
    }
}

// tablemodel for ioTable
class CustomTableModel extends DefaultTableModel {
    public CustomTableModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
    }

    // only pins are editable
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}

/**
 * make iotable editable
 */
class PinCellEditor extends DefaultCellEditor {
    public PinCellEditor(JCheckBox checkBox) {
        super(checkBox);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        Component editor = super.getTableCellEditorComponent(table, value, isSelected, row, column);

        // only if pin cell
        if (row == 2 || row == 6) {
            JCheckBox checkBox = (JCheckBox) editor;
            checkBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // change value in ram
                    int bit = checkBox.isSelected() ? 1 : 0;

                }
            });
        }
        return editor;
    }
}
