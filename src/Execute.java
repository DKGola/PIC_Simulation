package src;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * executes a specific command
 */
public class Execute {
    private int [][] ram;
    private Stack<Integer> returnStack = new Stack<Integer>();
    public Execute(int[][] ram){
        this.ram = ram;
    }

    private void write(int file, int value){
        List<Integer> shared = Arrays.asList(2, 3, 4, 10, 11);
        testPCL(file, value);
        if(shared.contains(file) || file >= 0x0C){
            ram[0][file] = value;
            ram[1][file] = value;
        }else{
            ram[getRb0()][file] = value;
        }
    }

    private int getRb0() {
        return (ram[0][3] & 0b0010_0000) >> 5;
    }

    private void setFlag(Flags flag, int value){
        if(value == 0){
            write(3, ram[0][3] & ~(1 << flag.value));
        }else if(value == 1){
            write(3, ram[0][3] | (1 << flag.value));
        }
    }

    // Byte instructions
    public void ADDWF(int file, int destinationBit){
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        // ADD
        int result = Simulator.wRegister + ram[getRb0()][file];


        // check DC and set if necessary
        int digitCarryResult = (Simulator.wRegister & 0xF) + (ram[getRb0()][file] & 0xF);
        if (digitCarryResult > 15) {
            setFlag(Flags.DigitCarry, 1);
        } else {
            setFlag(Flags.DigitCarry, 0);
        }

        // check Carry
        if (result > 255) {
            setFlag(Flags.Carry, 1);
        } else {
            setFlag(Flags.Carry, 0);
        }

        // check Zero
        if (result == 0) {
            setFlag(Flags.Zero, 1);
        } else {
            setFlag(Flags.Zero, 0);
        }

        // store result in f if dest is 1, in w if dest is 0
        result = result & 0xFF;

        if (destinationBit == 1) {
            write(file, result);
        } else {
            Simulator.wRegister = result;
        }
    }

    public void ANDWF(int file, int destinationBit){
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        // AND
        int result = Simulator.wRegister & ram[getRb0()][file];


        // check Zero
        if (result == 0) {
            setFlag(Flags.Zero, 1);
        } else {
            setFlag(Flags.Zero, 0);
        }

        // store result in f if dest is 1, in w if dest is 0
        result = result & 0xFF;

        if (destinationBit == 1) {
            write(file, result);
        } else {
            Simulator.wRegister = result;
        }
    }

    public void CLRF(int file){
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        write(file, 0);
        setFlag(Flags.Zero, 1);
    }

    public void CLRW(){
        Simulator.wRegister = 0;
        setFlag(Flags.Zero, 1);
    }

    public void COMF(int file, int destinationBit){
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ~ram[getRb0()][file];
        if(result == 0){
            setFlag(Flags.Zero, 1);
        }else{
            setFlag(Flags.Zero, 0);
        }

        if(destinationBit == 1){
            write(file, result);
        }else{
            Simulator.wRegister = result;
        }
    }

    public void DECF(int file, int destinationBit){
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ram[getRb0()][file] - 1;
        result = result & 0xFF;
        if(result == 0){
            setFlag(Flags.Zero, 1);
        }else{
            setFlag(Flags.Zero, 0);
        }
        write(file, result);
    }

    public void DECFSZ(int file, int destinationBit){
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ram[getRb0()][file] - 1;
        result = result & 0xFF;
        if(result == 0){
            setFlag(Flags.Zero, 1);
            Simulator.programCounter++;
        }else{
            setFlag(Flags.Zero, 0);
        }
        write(file, result);
    }

    public void INCF(int file, int destinationBit){
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ram[getRb0()][file] + 1;
        result = result & 0xFF;
        if(result == 0){
            setFlag(Flags.Zero, 1);
        }else{
            setFlag(Flags.Zero, 0);
        }
        write(file, result);
    }

    public void INCFSZ(int file, int destinationBit){
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ram[getRb0()][file] + 1;
        result = result & 0xFF;
        if(result == 0){
            setFlag(Flags.Zero, 1);
            Simulator.programCounter++;
        }else{
            setFlag(Flags.Zero, 0);
        }
        write(file, result);
    }

    public void IORWF(int file, int destinationBit){
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = Simulator.wRegister | ram[getRb0()][file];
        if(result == 0){
            setFlag(Flags.Zero, 1);
        }else{
            setFlag(Flags.Zero, 0);
        }
        write(file, result);
    }   

    public void MOVF(int file, int destinationBit){
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        if(ram[getRb0()][file] == 0){
            setFlag(Flags.Zero, 1);
        }else{
            setFlag(Flags.Zero, 0);
        }

        if(destinationBit == 0){
            Simulator.wRegister = ram[getRb0()][file];
        }
    }

    public void MOVWF(int file){
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        write(file, Simulator.wRegister);
    }

    public void NOP(){

    }

    public void RLF(int file, int destinationBit){
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int carryFlag = ram[0][3] & ~(1 << Flags.Carry.value);

        int result = ram[getRb0()][file] << 1 + carryFlag;

        if(destinationBit == 1){
            write(file, result);
        }else{
            Simulator.wRegister = result;
        }
    }

    public void RRF(int file, int destinationBit){
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int carryFlag = ram[0][3] & ~(1 << Flags.Carry.value);
        
        int result = ram[getRb0()][file] >> 1 + (carryFlag << 7);

        if(destinationBit == 1){
            write(file, result);
        }else{
            Simulator.wRegister = result;
        }
    }

    // Test needed
    public void SUBWF(int file, int destinationBit){
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        write(file, (~ram[getRb0()][file] + 1) & 0xFF);
        ADDWF(file, destinationBit);
    }

    public void SWAPF(int file, int destinationBit){
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int upper = ram[getRb0()][file] & 0xF0;
        int lower = ram[getRb0()][file] & 0xF;
        int result = (lower << 4) + (upper >> 4);

        if(destinationBit == 1){
            write(file, result);
        }else{
            Simulator.wRegister = result;
        }
    }

    public void XORWF(int file, int destinationBit){
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = Simulator.wRegister ^ ram[getRb0()][file];
        if(result == 0){
            setFlag(Flags.Zero, 1);
        }else{
            setFlag(Flags.Zero, 0);
        }

        if(destinationBit == 1){
            write(file, result);
        }else{
            Simulator.wRegister = result;
        }
    }


    // Bit Instructions
    public void BCF(int file, int bit){
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ram[getRb0()][file] & ~(1 << bit);
        ram[getRb0()][file] = result;
    }

    public void BSF(int file, int bit){
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result =  ram[getRb0()][file] | (1 << bit);
        ram[getRb0()][file] = result;
    }

    public void BTFSC(int file, int bit){
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ram[getRb0()][file] & (1 << bit);
        if(result == 0){
            Simulator.programCounter++;
        }
    }

    public void BTFSS(int file, int bit){
        // test for indirect addressing
        if (file == 0) {
            file = ram[0][4];
        }

        int result = ram[getRb0()][file] & (1 << bit);
        if(result != 0){
            Simulator.programCounter++;
        }
    }


    // Literal and Control Instructions
    public void ADDLW(int literal){
        int result = Simulator.wRegister + literal;
        int digitResult = (Simulator.wRegister & 0xF) + (literal & 0xF);

        if(result > 255){
            setFlag(Flags.Carry, 1);
        }else{
            setFlag(Flags.Carry, 0);
        }

        if(digitResult > 15){
            setFlag(Flags.DigitCarry, 1);
        }else{
            setFlag(Flags.DigitCarry, 0);
        }

        result = result & 0xFF;

        if(result == 0){
            setFlag(Flags.Zero, 1);
        }else{
            setFlag(Flags.Zero, 0);
        }

        Simulator.wRegister = result;
    }

    public void ANDLW(int literal){
        int result = Simulator.wRegister & literal;
        if(result == 0){
            setFlag(Flags.Zero, 1);
        }else{
            setFlag(Flags.Zero, 0);
        }
        Simulator.wRegister = result;
    }

    public void CALL(int literal){
        returnStack.add(Simulator.programCounter);
        GOTO(literal);
    }

    public void CLRWDT(){

    }

    public void GOTO(int literal){
        Simulator.programCounter = literal + ((ram[0][10] & 0b0001_1000) << 8);
    }

    public void IORLW(int literal){
        int result = (Simulator.wRegister | literal);
        if (result == 0) {
            setFlag(Flags.Zero, 1);
        } else {
            setFlag(Flags.Zero, 0);
        }
        Simulator.wRegister = result;
    }

    public void MOVLW(int literal){
        Simulator.wRegister = literal;
    }

    public void RETFIE(){

    }

    public void RETLW(int literal){
        Simulator.programCounter = returnStack.pop();
        Simulator.wRegister = literal;
    }

    public void RETURN(){
        Simulator.programCounter = returnStack.pop();
    }

    public void SLEEP(){

    }

    // Test
    public void SUBLW(int literal){
        ADDLW(~literal + 1);
    }

    public void XORLW(int literal){
        int result = Simulator.wRegister ^ literal;
        if(result == 0){
            setFlag(Flags.Zero, 1);
        }else{
            setFlag(Flags.Zero, 0);
        }
        Simulator.wRegister = result;
    }

    private void testPCL(int file, int value){
        if(file == 2){
            Simulator.programCounter = (value + ((ram[getRb0()][10] & 0b001_111) << 8));
        }
    }
}