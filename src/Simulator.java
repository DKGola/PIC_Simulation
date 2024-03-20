package src;

import java.io.File;

public class Simulator {
    private int[] rom;
    private int[][] ram;
    public static int programCounter;
    private Decoder decoder;


    public Simulator(int[] instructions) {
        rom = instructions;
        ram = new int[2][128];
        programCounter = 0;
        decoder = new Decoder(ram);
    }

    /**
     * reads next command and returns it to the decoder
     */
    public void nextInstruction() {
        decoder.decode(rom[programCounter]);
        programCounter++;
    }


}
