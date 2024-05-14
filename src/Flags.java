package src;

public enum Flags {
    // Status register 3
    Carry(0, 3, 0), DigitCarry(1, 3, 0), Zero(2, 3, 0),
    PowerDown(3, 3, 0), TimeOut(4, 3, 0),
    // IntCon register 11
    TImerOverflow(2,11, 0), TimerInterrupt(5, 11, 0), GlobalInterruptEnable(7, 11, 0),
    RB0InterruptEnable(4, 11, 0), RB0Interrupt(1, 11, 0),
    RBPortChangeEnable(3, 11, 0), RBInterrupt(0, 11, 0),
    // Option register 1 bank 1
    TimerClockSource(5, 1,1), TimerSourceEdge(4,1,1), PrescalerAssignment(3, 1, 1),
    InterruptEdgeSelect(6, 1, 1),
    // EECON 1 register 8  bank 1
    WriteEnableBit(2, 8, 1), WriteControlBit(1, 8, 1), ReadControlBit(0, 8, 1),
    WriteInterrupt(4,8,1);

    public final int bit;
    public final int register;
    public final int bank; 
    private Flags(int bit, int register, int bank){
        this.bit = bit;
        this.register = register;
        this.bank = bank;
    }

}
