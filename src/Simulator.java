package src;
import java.io.*;
import java.util.Arrays;

/**
 * contains important aspects like rom, ram, etc.
 * represents the fetch cycle and passes the commands to the decoder
 */
public class Simulator {
    private int[] rom;
    private final int[][] ram;
    public int frequency;      // in Hz, 4000000 Hz = 1 µs
    private double runtime;        // in microseconds
    private final int[] EEPRom;
    public static int programCounter;
    public static int wRegister;
    private final Decoder decoder;

    public Execute getExecute() {
        return execute;
    }

    private final Execute execute;


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
     * reads next instruction and passes it to the decoder
     */
    public void nextInstruction() {
        checkEEPRomReadWrite();
        if (!execute.isAsleep)
        {
            programCounter++;
            ram[0][2] = programCounter & 0b1111_1111;
            ram[1][2] = programCounter & 0b1111_1111;
            decoder.decode(rom[programCounter - 1]);
        }
        execute.interrupts.CheckInterrupt();

        incrementRuntime();
        // update GUI after instruction was executed
        Program.gui.updateGUI(Program.simulator);
        Program.gui.setLine();
    }

    /**
     * write or read into/from EEPROM - a ROM which still saves its data even after shutdown
     */
    private void checkEEPRomReadWrite(){
        // Write to EEPRom
        if(execute.getFlag(Flags.WriteEnableBit) == 1 && execute.getFlag(Flags.WriteControlBit) == 1){
            EEPRom[ram[0][9]] = ram[0][8];
            execute.setFlag(Flags.WriteControlBit, 0);
            execute.setFlag(Flags.WriteInterrupt, 1);
        }
        // Read from EEPRom
        if(execute.getFlag(Flags.ReadControlBit) == 1){
            ram[0][8] = EEPRom[ram[0][9]];
            execute.setFlag(Flags.ReadControlBit, 0);
        }
    }

    /**
     * reset all registers as written in the original PIC-documentation
     */
    public void powerOnReset(){
        Arrays.fill(ram[0], 0);
        Arrays.fill(ram[1], 0);
        Arrays.fill(EEPRom, 0);

        int[][] values = {
            {0, 0, 0b0001_1000, 0, 0, 0, 0, 0, 0, 0, 0},
            {0b1111_1111, 0, 0b0001_1000, 0, 0b001_1111, 0b1111_1111, 0, 0, 0, 0, 0}
        };
        for (int i = 0; i < values.length; i++){
            System.arraycopy(values[i], 0, ram[i], 1, values[i].length);
        }
        wRegister = 0;
        programCounter = 0;
        runtime = 0;
        if (execute != null) {
            execute.returnStack.resetStack();
            execute.interrupts.clearWatchdog();
            execute.interrupts.SetPrescaler(0);
            execute.isAsleep = false;
        }
    }

    /**
     * used when instructions are loaded and shall be replaced by new ones
     * @param file newly selected file using filechooser
     * @throws IOException in case of an IO-Exception
     */
    public void newInstructions(File file) throws IOException {
        powerOnReset();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String input;
        int[] instructions = new int[1024];
        int[] lines = new int[1024];
        while ((input = reader.readLine()) != null) {
            if (!input.startsWith(" ")) {
                instructions[Integer.parseInt(input.substring(0,4), 16)] = Integer.parseInt(input.substring(5, 9), 16);
                lines[Integer.parseInt(input.substring(0,4), 16)] = Integer.parseInt(input.substring(20,25));
            }
        }
        reader.close();

        Program.gui.setLines(lines);
        rom = instructions;
    }

    /**
     * soft reset used by InterruptHandler according to original PIC-documentation
     */
    public void softReset(){
        int[][] and = {
                {0b1111_1111, 0, 0b0000_0111, 0b1111_1111, 0b1111_1111, 0b1111_1111, 0, 0b1111_1111, 0b1111_1111, 0, 0b0000_0001},
                {0, 0, 0b0000_0111, 0b1111_1111, 0, 0, 0, 0, 0, 0b0000_0001}
        };
        for(int i = 0; i < and.length; i++){
            for(int j = 0; j < and[i].length; j++){
                ram[i][j + 1] = ram[i][j + 1] & and[i][j];
            }
        }
        ram[1][1] = 0b1111_1111;
        ram[1][5] = 0b0001_1111;
        ram[1][6] = 0b1111_1111;
        programCounter = 0;
    }

    /**
     * increment total runtime
     */
    public void incrementRuntime() {
        runtime += ((double)4_000_000 / frequency);
    }

    /**
     * get PCL content
     * @return PCL content (= RAM[0][2] and RAM[1][2])
     */
    public int getPCL() {
        return ram[0][2];       // PCL is in RAM[0][2] and RAM[1][2]
    }

    /**
     * get PCLath content
     * @return PCLath content ( = RAM[0][A] and RAM[1][A])
     */
    public int getPCLath() {
        return ram[0][10];      // PCLATH is in RAM[0][A] and RAM[1][A]
    }

    /**
     * get carry-flag
     * @return carry-flag
     */
    public int getCarry() {
        return execute.getFlag(Flags.Carry);
    }

    /**
     * get digit-carry-flag
     * @return digit-carry-flag
     */
    public int getDigitCarry() {
        return execute.getFlag(Flags.DigitCarry);
    }

    /**
     * get zero-flag
     * @return zero-flag
     */
    public int getZero() {
        return execute.getFlag(Flags.Zero);
    }

    /**
     * get ram
     * @return ram as double int[][]-array
     */
    public int[][] getRam() {return ram;}

    /**
     * get total runtime
     * @return total runtime
     */
    public double getRuntime() {
        return runtime;
    }

    /**
     * get current set frequency
     * @return frequency
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * set frequency
     * @param frequency frequency which controls how many milliseconds one command takes
     */
    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

}
