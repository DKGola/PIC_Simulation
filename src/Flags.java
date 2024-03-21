package src;

public enum Flags {
    Carry(0), DigitCarry(1), Zero(2);

    public final int value;
    private Flags(int value){
        this.value = value;
    }

}
