package src;

import java.util.Arrays;
import java.util.List;
/**
 * executes a specific command
 */
public class Execute {
    private final int[][] ram;
    private final int[] latchPortA = new int[8];
    private final int[] latchPortB = new int[8];
    public PICStack returnStack = new PICStack();
    public boolean isAsleep = false;

    public InterruptHandler interrupts;

    /**
     * Execute constructor
     * @param ram ram passed from Decoder
     */
    public Execute(int[][] ram) {
        Arrays.fill(latchPortA, -1);
        Arrays.fill(latchPortB, -1);
        this.ram = ram;
        interrupts = new InterruptHandler(this, ram);
    }

    /**
     * write into RAM
     * @param file port position in RAM
     * @param value content that should be written into RAM
     */
    private void write(int file, int value) {
        // file 7 is not implemented in the microcontroller
        if(file == 7 || CheckPortsForLatch(file, value)){
            return;
        }

        interrupts.CheckPrescalerReset(file, value);

        List<Integer> shared = Arrays.asList(2, 3, 4, 10, 11);
        testPCL(file, value);
        // write in both banks, if they share the same content
        if (shared.contains(file) || file >= 0x0C) {
            ram[0][file] = value;
            ram[1][file] = value;
        } else {
        // write in current bank using getRP0()
            ram[getRP0()][file] = value;
        }

        if((file == 0x05 || file == 0x06) && getRP0() != 0){
            UpdatePortsWithLatch(file);
        }
    }

    /**
     * check if PCLatch is active
     * @param file port that should be checked
     * @param value value that should be written into the port
     * @return true if successful
     */
    private boolean CheckPortsForLatch(int file, int value){
        // Write to PortA or PortB
        if((file == 0x05 || file == 0x06) && getRP0() == 0){
            ram[0][file] = (ram[0][file] & ram[1][file]) | (value & ~ram[1][file]);
            if(file == 0x05){
                for(int i = 0; i < 8; i++){
                    if((ram[1][5] & (1 << i)) != 0 && (value & (1 << i)) != (ram[0][5] & (1 << i))){
                        latchPortA[i] = value & (1 << i) >> i;
                    }
                }
            }else {
                for(int i = 0; i < 8; i++){
                    if((ram[1][6] & (1 << i)) != 0){
                        latchPortB[i] = value & (1 << i) >> i;
                    }
                }
            }
            return true;
        }

        return false;
    }

    /**
     * update ports depending on PCLatch
     * @param file port that should be updated
     */
    public void UpdatePortsWithLatch(int file){
        if (file == 0x05) {
            for(int i = 0; i < 8; i++){
                if((ram[1][file] & (1 << i)) == 0 && latchPortA[i] >= 0){
                    ram[0][file] = (ram[0][file] & ~(1 << i)) | (latchPortA[i] << i);
                    latchPortA[i] = -1;
                }
            }
        }
        if (file == 0x06) {
            for(int i = 0; i < 8; i++){
                if((ram[1][file] & (1 << i)) == 0 && latchPortB[i] >= 0){
                    ram[0][file] = (ram[0][file] & ~(1 << i)) | (latchPortB[i] << i);
                    latchPortB[i] = -1;
                }
            }
        }
    }

    /**
     * write into port with destinationbit
     * @param file port that should be written into
     * @param value value to write into port
     * @param destinationBit if 1: write in file-address, if 2: write in w-register
     */
    private void write(int file, int value, int destinationBit) {
        if (destinationBit == 1) {  // write in file-address if d-bit is 1
            write(file, value);
        } else {                    // write in w-register if d-bit is 0
            Simulator.wRegister = value;
        }
    }

    /**
     * test if PCL is active
     * @param file port
     * @param value value that should be written
     */
    private void testPCL(int file, int value) {
        if (file == 2) {
            Simulator.programCounter = (value + ((ram[getRP0()][10] & 0b0001_1111) << 8));
        }
    }

    /**
     * returns RP0 which is used often to switch banks
     * @return RP0 value
     */
    public int getRP0() {
        return (ram[0][3] & 0b0010_0000) >> 5;
    }

    /**
     * set a flag using the Flags-enumeration
     * @param flag flag-enumeration
     * @param value 0 or 1 to set
     */
    public void setFlag(Flags flag, int value) {
        if (value == 0) {   // set flag to 0
            write(flag.register, ram[flag.bank][flag.register] & ~(1 << flag.bit));
        } else if (value == 1) {    // set flag to 1
            write(flag.register, ram[flag.bank][flag.register] | (1 << flag.bit));
        }
    }

    /**
     * get value of flag
     * @param flag flag-enumeration which should be checked
     * @return value of flag
     */
    public int getFlag(Flags flag) {
        return (ram[flag.bank][flag.register] & (1 << flag.bit)) >> flag.bit;
    }

    /**
     * set zero-flag depending on result (0->set)
     * @param result result from operation
     */
    private void testResultZero(int result) {
        if (result == 0) {
            setFlag(Flags.Zero, 1);
        } else {
            setFlag(Flags.Zero, 0);
        }
    }

    /**
     * set carry flag is result > 255
     * @param result result from operation
     */
    private void testResultCarry(int result) {
        if (result > 255) {
            setFlag(Flags.Carry, 1);
        } else {
            setFlag(Flags.Carry, 0);
        }
    }

    /**
     * set digit-carry-flag if result > 15
     * @param result result from operation
     */
    private void testResultDigitCarry(int result) {
        if (result > 15) {
            setFlag(Flags.DigitCarry, 1);
        } else {
            setFlag(Flags.DigitCarry, 0);
        }
    }

    // Byte instructions

    /**
     * Add W and f
     * @param file f
     * @param destinationBit 0->W-register; 1->register f
     */
    public void ADDWF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        // ADD
        int result = Simulator.wRegister + ram[getRP0()][file];

        // check DC and set if necessary
        int digitCarryResult = (Simulator.wRegister & 0xF) + (ram[getRP0()][file] & 0xF);
        testResultDigitCarry(digitCarryResult);

        // check Carry
        testResultCarry(result);

        // check Zero
        testResultZero(result);

        // store result in f if dest is 1, in w if dest is 0
        result = result & 0xFF;

        write(file, result, destinationBit);
    }

    /**
     * AND W with f
     * @param file f
     * @param destinationBit 0->W-register; 1->register f
     */
    public void ANDWF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        // AND
        int result = Simulator.wRegister & ram[getRP0()][file];

        // check Zero
        testResultZero(result);

        // store result in f if dest is 1, in w if dest is 0
        result = result & 0xFF;

        write(file, result, destinationBit);
    }

    /**
     * Clear f
     * @param file f
     */
    public void CLRF(int file) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        write(file, 0);
        setFlag(Flags.Zero, 1);
    }

    /**
     * Clear W
     */
    public void CLRW() {
        Simulator.wRegister = 0;
        setFlag(Flags.Zero, 1);
    }

    /**
     * Complement f
     * @param file f
     * @param destinationBit 0->W-register; 1->register f
     */
    public void COMF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ~ram[getRP0()][file] & 0xFF;
        testResultZero(result);

        write(file, result, destinationBit);
    }

    /**
     * Decrement f
     * @param file f
     * @param destinationBit 0->W-register; 1->register f
     */
    public void DECF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ram[getRP0()][file] - 1;
        result = result & 0xFF;
        testResultZero(result);
        write(file, result, destinationBit);
    }

    /**
     * Decrement f, Skip if 0
     * @param file f
     * @param destinationBit 0->W-register; 1->register f
     */
    public void DECFSZ(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }
        // decrement file-content
        int result = ram[getRP0()][file] - 1;
        result = result & 0xFF; // make 8 bit
        testResultZero(result); // zero flag test
        // if 0, skip next command, simulate NOP
        if (result == 0) {
            Simulator.programCounter++;
            interrupts.updateTMR0();
            Program.simulator.incrementRuntime();
        }
        // write result in w if d == 0, in f if d == 1
        write(file, result, destinationBit);
    }

    /**
     * Increment f
     * @param file f
     * @param destinationBit 0->W-register; 1->register f
     */
    public void INCF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ram[getRP0()][file] + 1;
        result = result & 0xFF;
        testResultZero(result);
        write(file, result, destinationBit);
    }

    /**
     * Increment f, Skip if 0
     * @param file f
     * @param destinationBit 0->W-register; 1->register f
     */
    public void INCFSZ(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ram[getRP0()][file] + 1;
        result = result & 0xFF;
        testResultZero(result);
        if (result == 0) {
            Simulator.programCounter++;
            interrupts.updateTMR0();
            Program.simulator.incrementRuntime();
        }
        write(file, result, destinationBit);
    }

    /**
     * Inclusive OR W with f
     * @param file f
     * @param destinationBit 0->W-register; 1->register f
     */
    public void IORWF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = Simulator.wRegister | ram[getRP0()][file];
        testResultZero(result);
        write(file, result, destinationBit);
    }

    /**
     * Move f
     * @param file f
     * @param destinationBit 0->W-register; 1->register f
     */
    public void MOVF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }
        // set Zero-Flag is content of file-adress is 0
        int result = ram[getRP0()][file];
        testResultZero(result);
        // write content of file adress i w-register if it is 0
        if (destinationBit == 0) {
            Simulator.wRegister = ram[getRP0()][file];
        }
    }

    /**
     * Move W to f
     * @param file f
     */
    public void MOVWF(int file) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        write(file, Simulator.wRegister);
    }

    /**
     * No Operation
     */
    public void NOP() {
    }

    /**
     * Rotate Left f through Carry
     * @param file f
     * @param destinationBit 0->W-register; 1->register f
     */
    public void RLF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int carryFlag = ram[0][3] & 1;

        int result = (ram[getRP0()][file] << 1) + carryFlag;

        testResultCarry(result);

        result = result & 0xFF;

        write(file, result, destinationBit);
    }

    /**
     * Rotate Right f through Carry
     * @param file f
     * @param destinationBit 0->W-register; 1->register f
     */
    public void RRF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }
        // store carry flag in variable
        int carryFlag = ram[0][3] & 1;
        // store lowest bit of file-adress in variable
        int lowerBit = ram[getRP0()][file] & 1;
        // shift file-content 1 to the right, last bit is old carry flag
        int result = (ram[getRP0()][file] >> 1) + (carryFlag << 7);
        // cut top off result
        result = result & 0xFF;
        // carry-flag = lowest bit of file-adress
        setFlag(Flags.Carry, lowerBit);
        // write in w-reg. or f, depending on d-bit
        write(file, result, destinationBit);
    }

    /**
     * Subtract W from f
     * @param file f
     * @param destinationBit 0->W-register; 1->register f
     */
    public void SUBWF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }
        // subtract w-register from file adress, store result in variable
        int result = ram[getRP0()][file] - Simulator.wRegister;
        // subtract lowest 4 bits for DC
        int digitCarryResult = (ram[getRP0()][file] & 0xF) - (Simulator.wRegister & 0xF);

        // In SUBWF, Carry-Flag is set if result >= 0
        if (result < 0) {
            setFlag(Flags.Carry, 0);
        } else {
            setFlag(Flags.Carry, 1);
        }
        // In SUBWF, Digit-Carry-Flag is set if digitCarryResult >= 0
        if (digitCarryResult < 0) {
            setFlag(Flags.DigitCarry, 0);
        } else {
            setFlag(Flags.DigitCarry, 1);
        }

        result = result & 0xFF;
        // set Zero-Flag if result is 0
        testResultZero(result);
        // store result in f if dest is 1, in w if dest is 0
        write(file, result, destinationBit);
    }

    /**
     * Swap nibbles in f
     * @param file f
     * @param destinationBit 0->W-register; 1->register f
     */
    public void SWAPF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int upper = ram[getRP0()][file] & 0xF0;
        int lower = ram[getRP0()][file] & 0xF;
        int result = (lower << 4) + (upper >> 4);

        write(file, result, destinationBit);
    }

    /**
     * Exclusive OR W with f
     * @param file f
     * @param destinationBit 0->W-register; 1->register f
     */
    public void XORWF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = Simulator.wRegister ^ ram[getRP0()][file];
        testResultZero(result);

        write(file, result, destinationBit);
    }

    // Bit Instructions

    /**
     * Bit Clear f
     * @param file f
     * @param bit b
     */
    public void BCF(int file, int bit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ram[getRP0()][file] & ~(1 << bit);
        write(file, result);
    }

    /**
     * Bit Set f
     * @param file f
     * @param bit b
     */
    public void BSF(int file, int bit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ram[getRP0()][file] | (1 << bit);
        write(file, result);
    }

    /**
     * Bit Test f, Skip if Clear
     * @param file f
     * @param bit b
     */
    public void BTFSC(int file, int bit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];   // get address from fsr if indirect
        }
        // AND-mask bit with file-address -> 0 if the bit at this position is 0
        int result = ram[getRP0()][file] & (1 << bit);
        if (result == 0) {
            Simulator.programCounter++;     // skip next command
            Program.simulator.incrementRuntime();   // instead of NOP
            interrupts.updateTMR0();    // update timer
        }
    }

    /**
     * Bit Test f, Skip if Set
     * @param file f
     * @param bit b
     */
    public void BTFSS(int file, int bit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ram[getRP0()][file] & (1 << bit);
        if (result != 0) {
            Simulator.programCounter++;
            interrupts.updateTMR0();
            Program.simulator.incrementRuntime();
        }
    }

    // Literal and Control Instructions

    /**
     * Add literal and W
     * @param literal l
     */
    public void ADDLW(int literal) {
        int result = literal + Simulator.wRegister;
        int digitResult = (Simulator.wRegister & 0xF) + (literal & 0xF);

        testResultCarry(result);

        testResultDigitCarry(digitResult);

        result = result & 0xFF;

        testResultZero(result);

        Simulator.wRegister = result;
    }

    /**
     * AND literal with W
     * @param literal l
     */
    public void ANDLW(int literal) {
        int result = Simulator.wRegister & literal;
        testResultZero(result);
        Simulator.wRegister = result;
    }

    /**
     * Call subroutine
     * @param literal l
     */
    public void CALL(int literal) {
        returnStack.push(Simulator.programCounter); // push current pc on stack
        GOTO(literal);
    }

    /**
     * Clear Watchdog Timer
     */
    public void CLRWDT() {
        interrupts.clearWatchdog();
        setFlag(Flags.TimeOut, 1);
        setFlag(Flags.PowerDown, 1);
    }

    /**
     * Go to address
     * @param literal l
     */
    public void GOTO(int literal) {
        // set program counter to:
        // first 11 bits from literal k
        // 11th & 12th bit from PCLATH<4:3>
        Simulator.programCounter = literal + ((ram[0][10] & 0b0001_1000) << 8);
        interrupts.updateTMR0();    // update timer
        Program.simulator.incrementRuntime();   // 2-cycle-command
    }

    /**
     * Inclusive OR literal with W
     * @param literal l
     */
    public void IORLW(int literal) {
        int result = (Simulator.wRegister | literal);
        testResultZero(result);
        Simulator.wRegister = result;
    }

    /**
     * Move literal to W
     * @param literal l
     */
    public void MOVLW(int literal) {
        Simulator.wRegister = literal;
    }

    /**
     * Return from interrupt
     */
    public void RETFIE() {
        interrupts.updateTMR0();
        setFlag(Flags.GlobalInterruptEnable, 1);
        Simulator.programCounter = returnStack.pop();
        Program.simulator.incrementRuntime();
    }

    /**
     * Return with literal in W
     * @param literal l
     */
    public void RETLW(int literal) {
        Simulator.programCounter = returnStack.pop();
        Simulator.wRegister = literal;
        interrupts.updateTMR0();
        Program.simulator.incrementRuntime();
    }

    /**
     * Return from Subroutine
     */
    public void RETURN() {
        Simulator.programCounter = returnStack.pop();
        interrupts.updateTMR0();
        Program.simulator.incrementRuntime();
    }

    /**
     * Go into standby mode
     */
    public void SLEEP() {
        isAsleep = true;
        setFlag(Flags.PowerDown, 0);
        setFlag(Flags.TimeOut, 1);
    }

    /**
     * Subtract W from literal
     * @param literal l
     */
    public void SUBLW(int literal) {
        int result = literal - Simulator.wRegister;
        int digitResult = (literal & 0xF) - (Simulator.wRegister & 0xF);

        // In this PIC, flags are inversed for SUBLW
        if (result < 0) {
            setFlag(Flags.Carry, 0);
        } else {
            setFlag(Flags.Carry, 1);
        }

        if (digitResult < 0) {
            setFlag(Flags.DigitCarry, 0);
        } else {
            setFlag(Flags.DigitCarry, 1);
        }

        result = result & 0xFF;

        testResultZero(result);

        Simulator.wRegister = result;
    }

    /**
     * Exclusive OR literal with W
     * @param literal l
     */
    public void XORLW(int literal) {
        Simulator.wRegister = Simulator.wRegister ^ literal;
        testResultZero(Simulator.wRegister);
    }
}