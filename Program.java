import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import src.Simulator;

public class Program {
    public static void main(String[] args) throws IOException {
        start("TestProgramme/TPicSim1.LST");
    }

    private static void start(String file) throws NumberFormatException, IOException{
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
        System.out.println(readFile);
        System.out.println(Arrays.toString(instructions));

        Simulator simulator = new Simulator(instructions);
    }
}

