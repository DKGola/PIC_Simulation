package src;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;

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

    public GUI() {
        setSize(1200, 800);
        setVisible(true);
        setContentPane(mainPanel);

        // ActionListener für den File-Button hinzufügen
        fileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Dateiauswahldialog anzeigen
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select LST File");
                fileChooser.setFileFilter(new FileNameExtensionFilter("LST Files", "lst"));
                int userSelection = fileChooser.showOpenDialog(GUI.this);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    // Datei auswählen und einlesen
                    java.io.File selectedFile = fileChooser.getSelectedFile();
                    try {
                        BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
                        String line;
                        StringBuilder content = new StringBuilder();
                        while ((line = reader.readLine()) != null) {
                            content.append(line).append("\n");
                        }
                        reader.close();

                        // Inhalt im LST-Text-Fenster anzeigen
                        LSTTextPane.setText(content.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(GUI.this, "Error reading file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
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
