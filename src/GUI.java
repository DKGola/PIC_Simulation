package src;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class GUI extends JFrame {

    private JSlider frequencySlider;
    private JButton runButton;
    private JButton stopButton;
    private JButton stepButton;
    private JButton resetButton;
    public JTextPane LSTTextPane;
    private JTabbedPane tabbedPane1;
    private JTextArea consoleTextArea;
    private JButton fileButton;
    private JButton helpButton;
    private JTable gprTable;
    private JTable sfrTable;
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
    private JLabel runtimeLabel;
    private JLabel frequencyLabel;
    private JTable stackTable;
    private JButton sfrResetButtonButton;
    private JButton a4MHzButton;
    private JRadioButton a32768HzRadioButton;
    private JRadioButton a500KHzRadioButton;
    private JRadioButton a1MHzRadioButton;
    private JRadioButton a20MHzRadioButton;
    private JRadioButton a4MHzRadioButton;
    private Simulator simulator;
    private Execute execute;
    private File selectedFile;
    private int[] lines;
    private int line;
    public int[][] ioData;


    public GUI() {
        setSize(1200, 800);
        setVisible(true);
        setContentPane(mainPanel);
        ioData = new int[16][2];
        a4MHzRadioButton.setSelected(true);

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
                Program.running = false;
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
        frequencySlider.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                frequencySlider.setMinimum(32_768);     // minimum frequency
                frequencySlider.setMaximum((20_000_000));   // maximum frequency
            }
        });
        frequencySlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Program.simulator.setFrequency(frequencySlider.getValue());
                // update runtime and frequency label
                runtimeLabel.setText("Runtime: " + String.format("%.2f µs", Program.simulator.getRuntime()));
                NumberFormat numberFormat = NumberFormat.getInstance();
                numberFormat.setGroupingUsed(true);
                String formattedFrequency = numberFormat.format(Program.simulator.getFrequency());
                frequencyLabel.setText(String.format("%s Hz", formattedFrequency));
            }
        });
        sfrResetButtonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Program.simulator.softReset();
                Program.gui.updateGUI(Program.simulator);
            }
        });

        a32768HzRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frequencySlider.setValue(32_768);
            }
        });
        a500KHzRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frequencySlider.setValue(500_000);
            }
        });
        a1MHzRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frequencySlider.setValue(1_000_000);
            }
        });
        a4MHzRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frequencySlider.setValue(4_000_000);
            }
        });
        a20MHzRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frequencySlider.setValue(20_000_000);
            }
        });
    }

    private void togglePortBitInRam(int port, int column) {
        int[][] ram = Program.simulator.getRam();
        int pinValue = (port == 0) ? ram[0][5] : ram[0][6];
        int mask = 1 << (8 - column); // select bit depending on cell
        ram[0][5 + port] = pinValue ^ mask; // toggle port (0 -> 1; 1 -> 0)
        updateIOTable();
    }

    public void updateGprTable()
    {
        int[][] ram = Program.simulator.getRam();
        String[] columnNames = {"File Address", "Bank 0", "Bank 1"};
        Object[][] data = new Object[37][3];    // 12 rows, 3 columns
        // header
        for (int i = 0; i < 3; i++) {
            data[0][i] = columnNames[i];
        }
        // file address
        for (int i = 12; i < 48; i++) {     // gpr goes from 0Ch (12) to 2Fh (47)
            data[i - 11][0] = String.format("%02Xh", i);
        }
        // bank 0
        for (int i = 12; i < 48; i++) {
            data[i - 11][1] = String.format("%02Xh", ram[0][i]);
        }
        // bank 1
        for (int i = 12; i < 48; i++) {
            data[i - 11][2] = String.format("%02Xh", ram[1][i]);
        }

        CustomTableModel gprModel = new CustomTableModel(data, columnNames);
        gprTable.setModel(gprModel);
        gprTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < gprTable.getColumnCount(); i++) {
            gprTable.getColumnModel().getColumn(i).setPreferredWidth(50);
        }
    }

    public void updateSfrTable()
    {
        int[][] ram = Program.simulator.getRam();
        String[] columnNames = {"File Address", "Bank 0", "Bank 1"};
        Object[][] data = new Object[13][3];    // 12 rows, 3 columns
        // header
        for (int i = 0; i < 3; i++) {
            data[0][i] = columnNames[i];
        }
        // file address
        for (int i = 0; i < 12; i++) {
            data[i + 1][0] = String.format("%02Xh", i);
        }
        // bank 0
        for (int i = 0; i < 12; i++) {
            data[i + 1][1] = String.format("%02Xh", ram[0][i]);
        }
        // bank 1
        for (int i = 0; i < 12; i++) {
            data[i + 1][2] = String.format("%02Xh", ram[1][i]);
        }

        CustomTableModel sfrModel = new CustomTableModel(data, columnNames);
        sfrTable.setModel(sfrModel);
        sfrTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < sfrTable.getColumnCount(); i++) {
            sfrTable.getColumnModel().getColumn(i).setPreferredWidth(50);
        }
    }

    public void updateStackTable() {
        String[] columnNames = {"Index", "Value"};
        int[] stack = Program.simulator.getExecute().returnStack.getStack();
        Object[][] data = new Object[9][2];  // 8 rows, 2 columns
        // header
        for (int i = 0; i < 2; i++) {
            data[0][i] = columnNames[i];
        }
        for (int i = 0; i < 8; i++) {
            data[i + 1][0] = 7 - i;     // array index 0-7 from bottom to top
        }
        for (int i = 0; i < 8; i++) {
            data[i + 1][1] = stack[7 - i];
        }

        CustomTableModel ioModel = new CustomTableModel(data, columnNames);
        stackTable.setModel(ioModel);
        stackTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < stackTable.getColumnCount(); i++) {
            stackTable.getColumnModel().getColumn(i).setPreferredWidth(50);
        }
    }

    public void updateIOTable() {
        updateIoData();

        Object[][] data = new Object[7][9]; // 7 rows, 9 columns
        String[] columnNames = {"RB", "7", "6", "5", "4", "3", "2", "1", "0"};

        // RA
        data[0][0] = "RA";
        System.arraycopy(columnNames, 0, data[0], 0, columnNames.length);
        data[1][0] = "Tris A";
        for (int i = 1; i < 9; i++) {
            if (ioData[i - 1][0] == 1) {
                data[1][i] = "i";
            } else {
                data[1][i] = "o";
            }
        }
        data[2][0] = "Pin A";
        for (int i = 1; i < 9; i++) {
            data[2][i] = ioData[i - 1][1];
        }
        // empty line
        data[3][0] = "";

        // RB
        data[4][0] = "RB";
        System.arraycopy(columnNames, 0, data[4], 0, columnNames.length);
        data[5][0] = "Tris B";
        for (int i = 1; i < 9; i++) {
            if (ioData[i + 7][0] == 1) {
                data[5][i] = "i";
            } else {
                data[5][i] = "o";
            }
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

    /**
     * highlights the current line in the GUI
     * @param line line to be highlighted
     */
    public void highlightCommand(int line) {
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



    // Checkboxen
    class CheckBoxRenderer extends DefaultTableCellRenderer implements TableCellRenderer {
        JCheckBox checkBox = new JCheckBox();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            checkBox.setSelected((Boolean) value); // set checkbox depending on cell value
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
        // update general purpose register table
        updateGprTable();
        // update special function register table
        updateSfrTable();
        // update stackTable
        updateStackTable();
        // update ioTable
        updateIOTable();
        // update runtime and frequency label
        runtimeLabel.setText("Runtime: " + String.format("%.2f µs", Program.simulator.getRuntime()));
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setGroupingUsed(true);
        String formattedFrequency = numberFormat.format(Program.simulator.getFrequency());
        frequencyLabel.setText(String.format("%s Hz", formattedFrequency));
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
        int[][] ram = Program.simulator.getRam();
        // make iodata to string
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
