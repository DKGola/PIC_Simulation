package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class Program {
    public static boolean running;
    public static Simulator simulator;
    public static GUI gui;
    private static boolean bp;

    public static void main(String[] args) throws IOException {
        Program program = new Program();
        program.start();
    }

    public void start() throws IOException {
        gui = new GUI();
        bp = true;
        simulator = new Simulator(new int[1024]);

        File selectedFile = gui.waitForSelectedFile();

        // Eine Datei wurde ausgewählt
        System.out.println("Ausgewählte Datei: " + selectedFile.getAbsolutePath());
        loadInstructions(selectedFile);
        running = false; // Set running to false when starting a new program
        runProgram();
    }

    private void loadInstructions(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String input;
        StringBuilder readFile = new StringBuilder();
        int[] instructions = new int[1024];
        int[] lines = new int[1024];
        int index = 0;
        while ((input = reader.readLine()) != null) {
            readFile.append(input).append("\n");
            if (!input.startsWith(" ")) {
                instructions[index] = Integer.parseInt(input.substring(5, 9), 16);
                lines[index] = Integer.parseInt(input.substring(20,25));
                index++;
            }
        }
        reader.close();

        // load instructions in simulator
        simulator = new Simulator(instructions);
        gui.setLines(lines);
        Program.gui.updateGUI(Program.simulator);
    }

    public static void runProgram() {
        // don't run if no file is selected
        if (gui.getSelectedFile() == null) {
            return;
        }

        // HIER RESET EINBAUEN

        while (running) {
            // stops program if breakpoint is set
            if (gui.hasBreakpoint() && bp) {
                running = false;
                bp = false;
                continue;
            } else {
                bp = true;
            }
            simulator.nextInstruction();
            // 1000 milliseconds delay after every instruction
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
