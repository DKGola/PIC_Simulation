package src;

import java.util.Arrays;

public class Simulator {
    private int[] rom;
    private int[][] ram;
    private int frequency;      // in Hz, 4000000 Hz = 1 µs
    private double runtime;        // in microseconds
    private int[] EEPRom;
    public static int programCounter;
    public static int wRegister;
    private Decoder decoder;

    public Execute getExecute() {
        return execute;
    }

    private Execute execute;


    public Simulator(int[] instructions) {
        rom = instructions;
        ram = new int[2][128];
        EEPRom = new int[64];
        frequency = 4_000_000;
        runtime = 0;
        powerOnReset();
        execute = new Execute(ram);
        decoder = new Decoder(ram, execute);
    }

    /**
     * reads next instruction and gives it to the decoder
     */
    public void nextInstruction() {
        if (execute.isAsleep == false)
        {
            programCounter++;
            ram[0][2] = programCounter & 0b1111_1111;
            ram[1][2] = programCounter & 0b1111_1111;
            decoder.decode(rom[programCounter - 1]);
        }
        execute.interrupts.CheckInterrupt();

        checkEEPRomReadWrite();

        execute.UpdatePortsWithLatch();

        updateRuntime();

        Program.gui.updateGUI(Program.simulator);
        Program.gui.setLine();
    }

    private void checkEEPRomReadWrite(){
        // Write to EEPRom
        if(execute.getFlag(Flags.WriteEnableBit) == 1 && execute.getFlag(Flags.WriteControlBit) == 1){
            EEPRom[ram[0][9]] = ram[0][8];
            execute.setFlag(Flags.WriteControlBit, 0);
        }
        // Read from EEPRom
        if(execute.getFlag(Flags.ReadControlBit) == 1){
            ram[0][8] = EEPRom[ram[0][9]];
            execute.setFlag(Flags.ReadControlBit, 0);
        }

        // update runtime
        updateRuntime();
        // update GUI after instruction was executed
        Program.gui.updateGUI(Program.simulator);
        Program.gui.setLine();
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
        runtime = 0;
        if (execute != null) {
            execute.returnStack.resetStack();
        }
    }

    public void updateRuntime() {
            runtime += ((double)4_000_000 / frequency);
    }

    public int getPCL() {
        return ram[0][2];       // PCL is in RAM[0][2] and RAM[1][2]
    }

    public int getPCLath() {
        return ram[0][10];      // PCLATH is in RAM[0][A] and RAM[1][A]
    }

    public int getCarry() {
        return execute.getFlag(Flags.Carry);
    }

    public int getDigitCarry() {
        return execute.getFlag(Flags.DigitCarry);
    }

    public int getZero() {
        return execute.getFlag(Flags.Zero);
    }
    public int[][] getRam() {return ram;}
    public double getRuntime() {
        return runtime;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

}
