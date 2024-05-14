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
    private JButton fileButton;
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
    private JRadioButton a32768HzRadioButton;
    private JRadioButton a500KHzRadioButton;
    private JRadioButton a1MHzRadioButton;
    private JRadioButton a20MHzRadioButton;
    private JRadioButton a4MHzRadioButton;
    private File selectedFile;
    private int[] lines;
    private int line;
    public int[][] ioData;


    public GUI() {
        // Get the screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        // Get the toolbar dimensions and subtract it from the height
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        int availableHeight = screenSize.height - screenInsets.bottom;
        // set screen size
        setSize(screenSize.width, availableHeight);
        setVisible(true);
        setContentPane(mainPanel);
        ioData = new int[16][2];

        /**
         * File-Button reads an LST file
         */
        fileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select LST File");
                fileChooser.setFileFilter(new FileNameExtensionFilter("LST Files", "lst"));
                int userSelection = fileChooser.showOpenDialog(GUI.this);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    // set selected file
                    java.io.File selectedFile = fileChooser.getSelectedFile();
                    setSelectedFile(selectedFile);
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
                        List<String> lines = new ArrayList<>();
                        // read lines
                        String line;
                        while ((line = reader.readLine()) != null) {
                            lines.add(line);
                        }

                        // create table with two columns
                        DefaultTableModel model = new DefaultTableModel() {
                            @Override
                            public Class<?> getColumnClass(int columnIndex) {
                                if (columnIndex == 0) {
                                    return Boolean.class; // column 0 is for breakpoints
                                } else {
                                    return super.getColumnClass(columnIndex);
                                }
                            }
                        };
                        model.addColumn("BP");
                        model.addColumn("Commands");

                        // fill table
                        for (String l : lines) {
                            model.addRow(new Object[]{false, l}); // Breakpoints are initially false
                        }

                        table3.setModel(model);
                        // breakpoint column width
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


        /**
         * Run-Button starts the program
         */
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

        /**
         * Stop-Button stops the program
         */
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Program.running = false;
            }
        });

        /**
         * Reset-Button triggers a Power-On-Reset
         */
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Program.running = false;
                Program.simulator.powerOnReset();
                Program.gui.updateGUI(Program.simulator);
            }
        });

        /**
         * Step-Button executes the marked line of the program
         */
        stepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Program.simulator.nextInstruction();
            }
        });

        /**
         * I/O-Table-Ports can be set to 0 or 1 with a mouseclick
         */
        ioTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    JTable target = (JTable)e.getSource();
                    int row = target.getSelectedRow();
                    int column = target.getSelectedColumn();
                    if (row == 2 && column != 0) {  // Port A cell is clicked
                        togglePortBitInRam(0, column);  // toggle this cell
                    }
                    if (row == 6 && column != 0) {  // Port B cell is clicked
                        togglePortBitInRam(1, column);  // toggle this cell
                    }
                    Program.gui.updateGUI(Program.simulator);
                }
            }
        });

        /**
         * sets minimum and maximum frequency
         */
        frequencySlider.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                frequencySlider.setMinimum(32_768);     // minimum frequency
                frequencySlider.setMaximum((20_000_000));   // maximum frequency
            }
        });

        /**
         * updates the frequency when slider is used
         */
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

        /**
         * SFR-Reset-Button triggers a soft-reset
         */
        sfrResetButtonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Program.simulator.softReset();
                Program.gui.updateGUI(Program.simulator);
            }
        });

        /**
         * Radio-Buttons for the most relevant frequencies
         */
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

    /**
     * updates the General Purpose Register Table
     */
    public void updateGprTable()
    {
        int[][] ram = Program.simulator.getRam();
        String[] columnNames = {"File Address", "Bank 0", "Bank 1"};
        Object[][] data = new Object[36][3];    // 36 rows, 3 columns
        // file address
        for (int i = 12; i < 48; i++) {     // gpr goes from 0Ch (12) to 2Fh (47)
            data[i - 12][0] = String.format("%02Xh", i);
        }
        // bank 0
        for (int i = 12; i < 48; i++) {
            data[i - 12][1] = String.format("%02Xh", ram[0][i]);
        }
        // bank 1
        for (int i = 12; i < 48; i++) {
            data[i - 12][2] = String.format("%02Xh", ram[1][i]);
        }

        CustomTableModel gprModel = new CustomTableModel(data, columnNames);
        gprTable.setModel(gprModel);
    }

    /**
     * updates the Special Function Register Table
     */
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

    /**
     * updates the Stack-Table
     */
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

    /**
     * updates I/O-Table
     */
    public void updateIOTable() {
        updateIoData();

        Object[][] data = new Object[7][9]; // 7 rows, 9 columns
        String[] columnNames = {"", "7", "6", "5", "4", "3", "2", "1", "0"};

        // RA
        data[0][0] = "RA";
        System.arraycopy(columnNames, 0, data[0], 0, columnNames.length);
        // Tris A
        data[1][0] = "Tris A";
        for (int i = 1; i < 9; i++) {
            if (ioData[i - 1][0] == 1) {
                data[1][i] = "i";
            } else {
                data[1][i] = "o";
            }
        }
        // Port A
        data[2][0] = "Port A";
        for (int i = 1; i < 9; i++) {
            data[2][i] = ioData[i - 1][1];
        }
        // empty line
        data[3][0] = "";

        // RB
        data[4][0] = "RB";
        System.arraycopy(columnNames, 0, data[4], 0, columnNames.length);
        // Tris B
        data[5][0] = "Tris B";
        for (int i = 1; i < 9; i++) {
            if (ioData[i + 7][0] == 1) {
                data[5][i] = "i";
            } else {
                data[5][i] = "o";
            }
        }
        // Port B
        data[6][0] = "Port B";
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

    /**
     * sets ioData
     * First bracket = PIN
     * Second bracket = Bank 0 / 1
     * initially: TRISA oooiiiii   TRISB iiiiiiii  PORTA 00000000 PORTB 00000000
     */
    public void updateIoData() {
        int[][] ram = Program.simulator.getRam();
        // convert iodata to string
        String trisAString = String.format("%8s", Integer.toBinaryString(ram[1][5])).replace(' ', '0');
        String trisBString = String.format("%8s", Integer.toBinaryString(ram[1][6])).replace(' ', '0');
        String portAString = String.format("%8s", Integer.toBinaryString(ram[0][5])).replace(' ', '0');
        String portBString = String.format("%8s", Integer.toBinaryString(ram[0][6])).replace(' ', '0');

        // fill io-data array with content of ram
        for (int i = 0; i < 8; i++) {
            ioData[i][0] = Character.getNumericValue(trisAString.charAt(i));   // Tris A
            ioData[i][1] = Character.getNumericValue(portAString.charAt(i));   // Port A
        }

        for (int i = 8, j = 0; i < 16; i++, j++) {
            ioData[i][0] = Character.getNumericValue(trisBString.charAt(j));   // Tris B
            ioData[i][1] = Character.getNumericValue(portBString.charAt(j));   // Port B
        }
    }

    /**
     * called if Port-Cell is clicked. Toggles from 1 -> 0 or from 0 -> 1
     * @param ab 0 if RA, 1 if RB
     * @param column column of the clicked cell
     */
    private void togglePortBitInRam(int ab, int column) {
        int[][] ram = Program.simulator.getRam();
        int pinValue = (ab == 0) ? ram[0][5] : ram[0][6];     // get portvalue depending on A or B
        int mask = 1 << (8 - column); // select bit depending on cell
        ram[0][5 + ab] = pinValue ^ mask; // toggle port (0 -> 1; 1 -> 0)
        updateIOTable();
    }

    /**
     * returns the selected file
     * @return selected file
     */
    public File getSelectedFile() {
        if (selectedFile != null) {
        }
        return selectedFile;
    }

    /**
     * wait until a file is selected.
     * @return file selected by user
     */
    public File waitForSelectedFile() {
        File selectedFile = null;
        while (selectedFile == null) {
            selectedFile = getSelectedFile();
            try {
                Thread.sleep(100);  // sleep 0.1 seconds to save performance
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return selectedFile;
    }

    /**
     * set the selected file
     * @param file file that is selected by user
     */
    public void setSelectedFile(File file) {
        this.selectedFile = file;
    }

    /**
     * set lines and current line for the lst-table
     * @param lines array with all rows for the table
     */
    public void setLines(int[] lines) {
        this.lines = lines;
        this.line = lines[Simulator.programCounter];
    }

    /**
     * set current line in the lst-table
     */
    public void setLine() {
        this.line = lines[Simulator.programCounter];
    }

    /**
     * info about wether a breakpoint is set for the current line
     * @return true if breakpoint is set
     */
    public boolean hasBreakpoint() {
        DefaultTableModel model = (DefaultTableModel) table3.getModel();
        Boolean breakpoint = (Boolean) model.getValueAt(line-1, 0);
        return breakpoint != null && breakpoint;
    }

    /**
     * highlights the next line in the GUI
     * @param line line to be highlighted
     */
    public void highlightCommand(int line) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (row == line - 1) {
                    c.setBackground(Color.PINK);    // next line is pink
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
     * render checkboxes for breakpoints
     */
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

    /**
     * main method
     * @param args .
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUI().setVisible(true));
    }
}

/**
 *  used by io-table to make Port-Cells editable but other cells not
 */
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