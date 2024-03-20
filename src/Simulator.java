package src;

public class Simulator {
    private int[] rom;
    private int[][] ram;
    public static int programCounter;
    private Decoder decoder;


    public Simulator(int[] instructions) {
        rom = instructions;
        ram = new int[2][128];
        programCounter = 0;
        powerOnReset();
        decoder = new Decoder(ram);
    }

    /**
     * reads next instruction and gives it to the decoder
     */
    public void nextInstruction() {
        decoder.decode(rom[programCounter]);
        programCounter++;
    }

    public void powerOnReset(){
        int[][] values = {
            {0, 0, 0b0001_1000, 0, 0, 0, 0, 0, 0, 0, 0},
            {1, 0, 0b0001_1000, 0, 0b001_1111, 0b1111_1111, 0, 0, 0, 0, 0}
        };
        for (int i = 0; i < values.length; i++){
            for(int j = 0; j < values[i].length; j++){
                ram[i][j + 1] = values[i][j];
            }
        }
    }
}
