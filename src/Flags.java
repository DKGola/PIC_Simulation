package src;

public enum Flags {
    // Status register 3
    Carry(0, 3, 0), DigitCarry(1, 3, 0), Zero(2, 3, 0), 
    // IntCon register 10
    TImerOverflow(2,10, 0), TimerInterrupt(5, 10, 0), GlobalInterruptEnable(7, 10, 0),
    RB0InterruptEnable(4, 10, 0), RB0Interrupt(1, 10, 0),
    RBPortChangeEnable(3, 10, 0), RBInterrupt(0, 10, 0),
    // Option register 1 bank 1
    TimerClockSource(5, 1,1), TimerSourceEdge(4,1,1), PrescalerAssignment(3, 1, 1),
    InterruptEdgeSelect(6, 1, 1);

    public final int bit;
    public final int register;
    public final int bank; 
    private Flags(int bit, int register, int bank){
        this.bit = bit;
        this.register = register;
        this.bank = bank;
    }

}
