package src;

public class Simulator {
    private int[] rom;
    private int[][] ram;
    public static int programCounter;
    public static int wRegister;
    private Decoder decoder;
    private Execute execute;


    public Simulator(int[] instructions) {
        rom = instructions;
        ram = new int[2][128];
        powerOnReset();
        execute = new Execute(ram);
        decoder = new Decoder(ram, execute);
    }

    /**
     * reads next instruction and gives it to the decoder
     */
    public void nextInstruction() {
        System.out.println("\n");
        System.out.printf("W %x\n", wRegister);
        for(int i = 0; i < ram[0].length; i++){
            if(i % 8 == 0){
                System.out.println();
            }
            System.out.printf("%x, ", ram[0][i]);
        }
        if (execute.isAsleep == false)
        {
            programCounter++;
            ram[0][2] = programCounter & 0b1111_1111;
            ram[1][2] = programCounter & 0b1111_1111;
            decoder.decode(rom[programCounter - 1]);
        }
        execute.CheckInterrupt();
    }

    public void powerOnReset(){
        int[][] values = {
            {0, 0, 0b0001_1000, 0, 0, 0, 0, 0, 0, 0, 0},
            {0b1111_1111, 0, 0b0001_1000, 0, 0b001_1111, 0b1111_1111, 0, 0, 0, 0, 0}
        };
        for (int i = 0; i < values.length; i++){
            for(int j = 0; j < values[i].length; j++){
                ram[i][j + 1] = values[i][j];
            }
        }
        wRegister = 0;
        programCounter = 0;
    }
}
