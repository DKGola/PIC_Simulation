package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * contains main-method and starts the program
 */
public class Program {
    public static boolean running;
    public static Simulator simulator;
    public static GUI gui;
    private static boolean breakpoint;

    /**
     * main method to start the simulation
     * @param args .
     * @throws IOException in case of an IO-Exception
     */
    public static void main(String[] args) throws IOException {
        start();
    }

    /**
     * start program (gui, create simulator, wait for file select)
     * @throws IOException in case of an IO-Exception
     */
    public static void start() throws IOException {
        simulator = new Simulator(new int[1024]);
        gui = new GUI();
        breakpoint = true;
        // get file from GUI
        File selectedFile = gui.waitForSelectedFile();

        // a file was selected
        System.out.println("Selected File: " + selectedFile.getAbsolutePath());
        loadInstructions(selectedFile);
        running = false; // set running to false when starting a new program
        runProgram();
    }

    /**
     * parse input file line for line and load into instructions-array;
     * give instructions to simulator
     * @param file selected file using the filechooser
     * @throws IOException in case of an IO-Exception
     */
    public static void loadInstructions(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String input;
        // arrays to store commands read from file
        int[] instructions = new int[1024]; // for commands
        int[] lines = new int[1024];    // for line numbers
        while ((input = reader.readLine()) != null) {
            if (!input.startsWith(" ")) {   // only command-lines start with number -> can be ignored
                instructions[Integer.parseInt(input.substring(0,4), 16)] = Integer.parseInt(input.substring(5, 9), 16);
                lines[Integer.parseInt(input.substring(0,4), 16)] = Integer.parseInt(input.substring(20,25));
            }
        }
        reader.close();

        // pass instructions to simulator instance
        simulator = new Simulator(instructions);
        gui.setLines(lines);
        Program.gui.updateGUI(Program.simulator);
    }

    /**
     * run program if running == true and no breakpoint is set
     */
    public static void runProgram() {
        // don't run if no file is selected
        if (gui.getSelectedFile() == null) {
            return;
        }

        while (running) {
            // stops program if breakpoint is set
            if (gui.hasBreakpoint() && breakpoint) {
                running = false;
                breakpoint = false;
                continue;
            } else {
                breakpoint = true;
            }
            simulator.nextInstruction();
            // 100 milliseconds delay after every instruction
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
