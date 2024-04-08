package src;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * executes a specific command
 */
public class Execute {
    private int[][] ram;
    private Stack<Integer> returnStack = new Stack<Integer>();
    private int prescalerCount = 0;
    private int previasInput;

    public Execute(int[][] ram) {
        this.ram = ram;
        previasInput = ram[0][5] & 0b0001_0000;
    }

    private void write(int file, int value) {
        if (file == 1) {
            prescalerCount = 0;
        }
        List<Integer> shared = Arrays.asList(2, 3, 4, 10, 11);
        testPCL(file, value);
        if (shared.contains(file) || file >= 0x0C) {
            ram[0][file] = value;
            ram[1][file] = value;
        } else {
            ram[getRb0()][file] = value;
        }
    }

    private void write(int file, int value, int destinationBit) {
        if (destinationBit == 1) {
            write(file, value);
        } else {
            Simulator.wRegister = value;
        }
    }

    private void testPCL(int file, int value) {
        if (file == 2) {
            Simulator.programCounter = (value + ((ram[getRb0()][10] & 0b001_111) << 8));
        }
    }

    private int getRb0() {
        return (ram[0][3] & 0b0010_0000) >> 5;
    }

    public void setFlag(Flags flag, int value) {
        if (value == 0) {
            write(flag.register, ram[flag.bank][flag.register] & ~(1 << flag.bit));
        } else if (value == 1) {
            write(flag.register, ram[flag.bank][flag.register] | (1 << flag.bit));
        }
    }

    public int getFlag(Flags flag) {
        return (ram[flag.bank][flag.register] & (1 << flag.bit)) >> flag.bit;
    }

    private void testResultZero(int result) {
        if (result == 0) {
            setFlag(Flags.Zero, 1);
        } else {
            setFlag(Flags.Zero, 0);
        }
    }

    private void testResultCarry(int result) {
        if (result > 255) {
            setFlag(Flags.Carry, 1);
        } else {
            setFlag(Flags.Carry, 0);
        }
    }

    private void testResultDigitCarry(int result) {
        if (result > 15) {
            setFlag(Flags.DigitCarry, 1);
        } else {
            setFlag(Flags.DigitCarry, 0);
        }
    }

    public void updateTMR0() {
        // Option reg T0CS
        if (getFlag(Flags.TimerClockSource) == 0) {
            incrementTRM0();
        } else {
            int RA4 = ram[0][6] & 0b0001_0000;
            if (getFlag(Flags.TimerSourceEdge) == 0) {
                // low-to-high
                if (previasInput < RA4) {
                    incrementTRM0();
                }
            } else {
                // high-to-low
                if (previasInput > RA4) {
                    incrementTRM0();
                }
            }
            previasInput = RA4;
        }
    }

    private void incrementTRM0() {
        if (getFlag(Flags.PrescalerAssignment) == 0) {
            prescalerCount++;
            if (prescalerCount == (int) Math.pow(2, (ram[1][1] & 0b0000_0111) + 1)) {
                ram[0][1]++;
                prescalerCount = 0;
            }
        } else {
            ram[0][1]++;
            prescalerCount = 0;
        }

        // Timer Overflow
        if (ram[0][1] > 255) {
            setFlag(Flags.TImerOverflow, 1);
            setFlag(Flags.Zero, 1);
            if (getFlag(Flags.TimerInterrupt) == 1) {
                setFlag(Flags.GlobalInterruptEnable, 0);
                CALL(4);
            }
            ram[0][1] = 0;
        }
    }

    // Byte instructions
    public void ADDWF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        // ADD
        int result = Simulator.wRegister + ram[getRb0()][file];

        // check DC and set if necessary
        int digitCarryResult = (Simulator.wRegister & 0xF) + (ram[getRb0()][file] & 0xF);
        testResultDigitCarry(digitCarryResult);

        // check Carry
        testResultCarry(result);

        // check Zero
        testResultZero(result);

        // store result in f if dest is 1, in w if dest is 0
        result = result & 0xFF;

        write(file, result, destinationBit);
    }

    public void ANDWF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        // AND
        int result = Simulator.wRegister & ram[getRb0()][file];

        // check Zero
        testResultZero(result);

        // store result in f if dest is 1, in w if dest is 0
        result = result & 0xFF;

        write(file, result, destinationBit);
    }

    public void CLRF(int file) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        write(file, 0);
        setFlag(Flags.Zero, 1);
    }

    public void CLRW() {
        Simulator.wRegister = 0;
        setFlag(Flags.Zero, 1);
    }

    public void COMF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ~ram[getRb0()][file] & 0xFF;
        testResultZero(result);

        write(file, result, destinationBit);
    }

    public void DECF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ram[getRb0()][file] - 1;
        result = result & 0xFF;
        testResultZero(result);
        write(file, result, destinationBit);
    }

    public void DECFSZ(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ram[getRb0()][file] - 1;
        result = result & 0xFF;
        testResultZero(result);
        if (result == 0) {
            Simulator.programCounter++;
            updateTMR0();
        }
        write(file, result);
    }

    public void INCF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ram[getRb0()][file] + 1;
        result = result & 0xFF;
        testResultZero(result);
        write(file, result, destinationBit);
    }

    public void INCFSZ(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ram[getRb0()][file] + 1;
        result = result & 0xFF;
        testResultZero(result);
        if (result == 0) {
            Simulator.programCounter++;
            updateTMR0();
        }
        write(file, result, destinationBit);
    }

    public void IORWF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = Simulator.wRegister | ram[getRb0()][file];
        testResultZero(result);
        write(file, result, destinationBit);
    }

    public void MOVF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ram[getRb0()][file];
        testResultZero(result);

        if (destinationBit == 0) {
            Simulator.wRegister = ram[getRb0()][file];
        }
    }

    public void MOVWF(int file) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        write(file, Simulator.wRegister);
    }

    public void NOP() {

    }

    public void RLF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int carryFlag = ram[0][3] & 1;

        int result = (ram[getRb0()][file] << 1) + carryFlag;

        testResultCarry(result);

        result = result & 0xFF;

        write(file, result, destinationBit);
    }

    public void RRF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int carryFlag = ram[0][3] & 1;

        int lowerBit = ram[getRb0()][file] & 1;

        int result = (ram[getRb0()][file] >> 1) + (carryFlag << 7);

        result = result & 0xFF;

        setFlag(Flags.Carry, lowerBit);

        write(file, result, destinationBit);
    }

    // Test needed
    public void SUBWF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ram[getRb0()][file] - Simulator.wRegister;

        // check DC and set if necessary
        int digitCarryResult = (ram[getRb0()][file] & 0xF) - (Simulator.wRegister & 0xF);

        if (result < 0) {
            setFlag(Flags.Carry, 0);
        } else {
            setFlag(Flags.Carry, 1);
        }

        if (digitCarryResult < 0) {
            setFlag(Flags.DigitCarry, 0);
        } else {
            setFlag(Flags.DigitCarry, 1);
        }

        // store result in f if dest is 1, in w if dest is 0
        result = result & 0xFF;

        // check Zero
        testResultZero(result);

        write(file, result, destinationBit);
    }

    public void SWAPF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int upper = ram[getRb0()][file] & 0xF0;
        int lower = ram[getRb0()][file] & 0xF;
        int result = (lower << 4) + (upper >> 4);

        write(file, result, destinationBit);
    }

    public void XORWF(int file, int destinationBit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = Simulator.wRegister ^ ram[getRb0()][file];
        testResultZero(result);

        write(file, result, destinationBit);
    }

    // Bit Instructions
    public void BCF(int file, int bit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ram[getRb0()][file] & ~(1 << bit);
        write(3, result);
    }

    public void BSF(int file, int bit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ram[getRb0()][file] | (1 << bit);
        write(file, result);
    }

    public void BTFSC(int file, int bit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ram[getRb0()][file] & (1 << bit);
        if (result == 0) {
            Simulator.programCounter++;
            updateTMR0();
        }
    }

    public void BTFSS(int file, int bit) {
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ram[getRb0()][file] & (1 << bit);
        if (result != 0) {
            Simulator.programCounter++;
            updateTMR0();
        }
    }

    // Literal and Control Instructions
    public void ADDLW(int literal) {
        int result = literal + Simulator.wRegister;
        int digitResult = (Simulator.wRegister & 0xF) + (literal & 0xF);

        testResultCarry(result);

        testResultDigitCarry(digitResult);

        result = result & 0xFF;

        testResultZero(result);

        Simulator.wRegister = result;
    }

    public void ANDLW(int literal) {
        int result = Simulator.wRegister & literal;
        testResultZero(result);
        Simulator.wRegister = result;
    }

    public void CALL(int literal) {
        returnStack.add(Simulator.programCounter);
        GOTO(literal);
    }

    public void CLRWDT() {

    }

    public void GOTO(int literal) {
        Simulator.programCounter = literal + ((ram[0][10] & 0b0001_1000) << 8);
        updateTMR0();
    }

    public void IORLW(int literal) {
        int result = (Simulator.wRegister | literal);
        testResultZero(result);
        Simulator.wRegister = result;
    }

    public void MOVLW(int literal) {
        Simulator.wRegister = literal;
    }

    public void RETFIE() {
        setFlag(Flags.GlobalInterruptEnable, 1);
        Simulator.programCounter = returnStack.pop();
    }

    public void RETLW(int literal) {
        Simulator.programCounter = returnStack.pop();
        Simulator.wRegister = literal;
        updateTMR0();
    }

    public void RETURN() {
        Simulator.programCounter = returnStack.pop();
        updateTMR0();
    }

    public void SLEEP() {

    }

    // Test
    public void SUBLW(int literal) {
        int result = literal - Simulator.wRegister;
        int digitResult = (literal & 0xF) - (Simulator.wRegister & 0xF);

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

    public void XORLW(int literal) {
        int result = Simulator.wRegister ^ literal;
        Simulator.wRegister = result;
    }
}