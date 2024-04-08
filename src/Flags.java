package src;

public enum Flags {
    // Option register 3
    Carry(0, 3, 0), DigitCarry(1, 3, 0), Zero(2, 3, 0), 
    // IntCon register 10
    TImerOverflow(2,11, 0), TimerInterrupt(5, 10, 0), GlobalInterruptEnable(7, 10, 0),
    // Option register 1 bank 1
    TimerClockSource(5, 1,1), TimerSourceEdge(4,1,1), PrescalerAssignment(3, 1, 1);

    public final int bit;
    public final int register;
    public final int bank; 
    private Flags(int bit, int register, int bank){
        this.bit = bit;
        this.register = register;
        this.bank = bank;
    }

}
