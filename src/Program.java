package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Program {
    public static boolean running;
    private static Simulator simulator; // Nicht statisch machen
    private static GUI gui;

    public static void main(String[] args) throws IOException {
        Program program = new Program(); // Neue Instanz von Program erstellen
        program.start(); // Programm starten
    }

    public void start() throws IOException {
        gui = new GUI();
        simulator = new Simulator(new int[1024]);

        File selectedFile = gui.waitForSelectedFile();

        // Eine Datei wurde ausgewählt
        System.out.println("Ausgewählte Datei: " + selectedFile.getAbsolutePath());
        loadInstructions(selectedFile);
        running = true; // Set running to true when starting a new program
        runProgram();
    }

    private void loadInstructions(File file) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String input;
        StringBuilder readFile = new StringBuilder();
        int[] instructions = new int[1024];
        int index = 0;
        while ((input = reader.readLine()) != null) {
            readFile.append(input).append("\n");
            if (!input.startsWith(" ")) {
                instructions[index++] = Integer.parseInt(input.substring(5, 9), 16);
            }
        }
        reader.close();

        // Laden der Anweisungen in den Simulator
        simulator = new Simulator(instructions);
    }

 //   private Simulator initSimulator(GUI gui) {
//        int[] instructions = {};
//        return new Simulator(instructions, gui);
//    }

    public static void runProgram() {
        while (running) {
            simulator.nextInstruction();
            // 500 milliseconds delay after every instruction
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
