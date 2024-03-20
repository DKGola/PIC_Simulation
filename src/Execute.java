package src;

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


    // Byte instructions
    public void ADDWF(int file, int destinationBit){

    }

    public void ANDWF(int file, int destinationBit){

    }

    public void CLRF(int file){

    }

    public void CLRW(){

    }

    public void COMF(int file, int destinationBit){

    }

    public void DECF(int file, int destinationBit){

    }

    public void DECFSZ(int file, int destinationBit){

    }

    public void INCF(int file, int destinationBit){

    }

    public void INCFSZ(int file, int destinationBit){

    }

    public void IORWF(int file, int destinationBit){

    }

    public void MOVF(int file, int destinationBit){

    }

    public void MOVWF(int file){

    }

    public void NOP(){

    }

    public void RLF(int file, int destinationBit){

    }

    public void RRF(int file, int destinationBit){

    }

    public void SUBWF(int file, int destinationBit){

    }

    public void SWAPF(int file, int destinationBit){

    }

    public void XORWF(int file, int destinationBit){

    }


    // Bit Instructions
    public void BCF(int file, int bit){

    }

    public void BSF(int file, int bit){

    }

    public void BTFSC(int file, int bit){

    }

    public void BTFSS(int file, int bit){

    }


    // Literal and Control Instructions
    public void ADDLW(int literal){

    }

    public void ANDLW(int literal){

    }

    public void CALL(int literal){

    }

    public void CLRWDT(){

    }

    public void GOTO(int literal){

    }

    public void IORLW(int literal){

    }

    public void MOVLW(int literal){

    }

    public void RETFIE(){

    }

    public void RETLW(int literal){

    }

    public void RETURN(){

    }

    public void SLEEP(){

    }

    public void SUBLW(int literal){

    }

    public void XORLW(int literal){

    }
}
