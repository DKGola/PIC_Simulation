package src;

/**
 * decodes a command using bitmasks
 */
public class Decoder {
    private Execute execute;
    public Decoder(int[][] ram) {
        execute =  new Execute(ram);
    }

    private int readBit(int commandCode, int k) {
        if ((k < 0) || (k > 13)) {
            System.out.println("k should be 0 - 13");
            return -1;
        } else {
            int bitMask = 1 << k;
            return (commandCode & bitMask) >> k;
        }
    }

    /**
     * read a 'substring', first and last bit are inclusive
     * @param commandCode
     * @param firstBit
     * @param lastBit
     * @return
     */
    private int readBitSegment(int commandCode, int firstBit, int lastBit) {
        if ((firstBit < 0) || (firstBit > 13) || (lastBit < 0) || (lastBit > 13)) {
            System.out.println("firstBit and lastBit should be 0 - 13");
            return -1;
        } else {
            // Bitmask: All Bits between firstBit and lastBit are set to 1
            int bitMask = 0;
            for (int i = firstBit; i <= lastBit; i++) {
                bitMask |= (1 << i);
            }
            return (commandCode & bitMask) >> firstBit;
        }
    }

    public void decode(int commandCode) {
        int commandBits = (commandCode & 0b11_1000_0000_0000);
        switch (commandBits) {
            case 0b10_1000_0000_0000:   // GOTO(k)
                execute.GOTO(readBitSegment(commandCode, 0, 10));
                return;
            case 0b10_0000_0000_0000:   // CALL(k)
                execute.CALL(readBitSegment(commandCode, 0, 10));
                return;
        }

        commandBits = (commandCode & 0b11_1100_0000_0000);
        switch (commandBits) {
            case 0b01_0000_0000_0000:   // BCF(f,b)
                execute.BCF(readBitSegment(commandCode, 0, 6), readBitSegment(commandCode, 7, 9));
                return;
            case 0b01_0100_0000_0000:   // BSF(f,b)
                execute.BSF(readBitSegment(commandCode, 0, 6), readBitSegment(commandCode, 7, 9));
                return;
            case 0b01_1000_0000_0000:   // BTFSC(f,b)
                execute.BTFSC(readBitSegment(commandCode, 0, 6), readBitSegment(commandCode, 7, 9));
                return;
            case 0b01_1100_0000_0000:   // BTFSS(f,b)
                execute.BTFSS(readBitSegment(commandCode, 0, 6), readBitSegment(commandCode, 7, 9));
                return;
            case 0b11_0000_0000_0000:   // MOVLW(k)
                execute.MOVLW(readBitSegment(commandCode, 0, 7));
                return;
            case 0b11_0100_0000_0000:   // RETLW(k)
                execute.RETLW(readBitSegment(commandCode, 0, 7));
                return;
        }

        commandBits = (commandCode & 0b11_1110_0000_0000);
        switch (commandBits) {
            case 0b11_1110_0000_0000:   // ADDLW(k)
                execute.ADDLW(readBitSegment(commandCode, 0, 7));
                return;
            case 0b11_1100_0000_0000:   // SUBLW(k)
                execute.SUBLW(readBitSegment(commandCode, 0, 7));
                return;
        }

        commandBits = (commandCode & 0b11_1111_0000_0000);
        switch (commandBits) {
            case 0b00_0111_0000_0000:   // ADDWF(f,d)
                execute.ADDWF(readBitSegment(commandCode, 0, 6), readBit(commandBits, 7));
                return;
            case 0b00_0101_0000_0000:   // ANDWF(f,d)
                execute.ANDWF(readBitSegment(commandCode, 0, 6), readBit(commandBits, 7));
                return;
            case 0b00_1001_0000_0000:   // COMF(f,d)
                execute.COMF(readBitSegment(commandCode, 0, 6), readBit(commandBits, 7));
                return;
            case 0b00_0011_0000_0000:   // DECF(f,d)
                execute.DECF(readBitSegment(commandCode, 0, 6), readBit(commandBits, 7));
                return;
            case 0b00_1011_0000_0000:   // DECFSZ(f,d)
                execute.DECFSZ(readBitSegment(commandCode, 0, 6), readBit(commandBits, 7));
                return;
            case 0b00_1010_0000_0000:   // INCF(f,d)
                execute.INCF(readBitSegment(commandCode, 0, 6), readBit(commandBits, 7));
                return;
            case 0b00_1111_0000_0000:   // INCFSZ(f,d)
                execute.INCFSZ(readBitSegment(commandCode, 0, 6), readBit(commandBits, 7));
                return;
            case 0b00_0100_0000_0000:   // IORWF(f,d)
                execute.IORWF(readBitSegment(commandCode, 0, 6), readBit(commandBits, 7));
                return;
            case 0b00_1000_0000_0000:   // MOVF(f,d)
                execute.MOVF(readBitSegment(commandCode, 0, 6), readBit(commandBits, 7));
                return;
            case 0b00_1101_0000_0000:   // RLF(f,d)
                execute.RLF(readBitSegment(commandCode, 0, 6), readBit(commandBits, 7));
                return;
            case 0b00_1100_0000_0000:   // RRF(f,d)
                execute.RRF(readBitSegment(commandCode, 0, 6), readBit(commandBits, 7));
                return;
            case 0b00_0010_0000_0000:   // SUBWF(f,d)
                execute.SUBWF(readBitSegment(commandCode, 0, 6), readBit(commandBits, 7));
                return;
            case 0b00_1110_0000_0000:   // SWAPF(f,d)
                execute.SWAPF(readBitSegment(commandCode, 0, 6), readBit(commandBits, 7));
                return;
            case 0b00_0110_0000_0000:   // XORWF(f,d)
                execute.XORWF(readBitSegment(commandCode, 0, 6), readBit(commandBits, 7));
                return;
            case 0b11_1001_0000_0000:   // ANDLW(k)
                execute.ANDLW(readBitSegment(commandCode, 0, 7));
                return;
            case 0b11_1000_0000_0000:   // IORLW(k)
                execute.IORLW(readBitSegment(commandCode, 0, 7));
                return;
            case 0b11_1010_0000_0000:   // XORLW(k)
                execute.XORLW(readBitSegment(commandCode, 0, 7));
                return;
        }

        commandBits = (commandCode & 0b11_1111_1000_0000);
        switch (commandBits) {
            case 0b00_0001_1000_0000:   // CLRF(f)
                execute.CLRF(readBitSegment(commandCode, 0, 6));
                return;
            case 0b00_0001_0000_0000:   // CLRW()
                execute.CLRW();
                return;
            case 0b00_0000_1000_0000:   // MOVWF(f)
                execute.MOVWF(readBitSegment(commandCode, 0, 6));
                return;
        }

        switch (commandCode) {
            case 0b00_0000_0110_0100:   // CLRWDT()
                execute.CLRWDT();
                return;
            case 0b00_0000_0000_1001:   // RETFIE()
                execute.RETFIE();
                return;
            case 0b00_0000_0000_1000:   // RETURN()
                execute.RETURN();
                return;
            case 0b00_0000_0110_0011:   // SLEEP()
                execute.SLEEP();
                return;
        }

        if ((commandCode & 0b11_1111_1001_1111) == 0b00_0000_0000_0000) {
            execute.NOP();
            return;

        System.out.println("ERROR: Wrong Command Code given to the decoder");
        return;
        }
    }
}


/*
if (commandCode ^ 11111100000000 == 00011100000000) {
    Execute.ADDWF();
 */


/*
call        11_1000_0000_0000
goto

bcf         11_1100_0000_0000
bsf
btfsc
btfss
movlw
retlw

addlw       11_1110_0000_0000
sublw

addwf(f,d)  11_1111_0000_0000
andwf(f,d)
comf
decf
decfsz
incf
incfsz
iorwf
movf
rlf
rrf
subwf
swapf
xorwf
andlw
iorlw
xorlw

clrf(f)     11_1111_1000_0000
clrw()
movwf

clrwdt      11_1111_1111_1111
retfie
return
sleep

nop         11_1111_1001_1111

 */