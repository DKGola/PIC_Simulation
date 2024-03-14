package src;

import java.io.File;

public class Simulator {
    private int[] rom;
    private int[][] ram;
    public static int programCounter;
    private Decoder decoder;


    public Simulator(File File) {
        rom = new int[1024];
        ram = new int[2][128];
        programCounter = 0;
        decoder = new Decoder(ram);
    }

    /**
     * reads next command and returns it to the decoder
     */
    public void nextCommand() {
        decoder.decode(rom[programCounter]);
        programCounter++;
    }


}
