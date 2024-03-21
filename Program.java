import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import src.Simulator;

public class Program {
    public static void main(String[] args) throws IOException {
        start("TestProgramme/TPicSim1.LST");
    }

    private static void start(String file) throws IOException{
        File testFile = new File(file);
        BufferedReader reader = new BufferedReader(new FileReader(testFile));
        String input;
        String readFile = "";
        int[] instructions = new int[1024];
        int index = 0;
        while ((input = reader.readLine()) != null){
            readFile += input + "\n";
            if (input.charAt(0) != ' '){
                instructions[index++] = Integer.parseInt(input.substring(5, 9), 16);
            }
        }
        reader.close();

        Simulator simulator = new Simulator(instructions);
        while (true){
            simulator.nextInstruction();
        }
    }
}

