package src;

public class InterruHandler {
    private int prescalerCount = 0;
    private float watchdogCount;
    private int previousInput;
    private int previousRB7ToRB4;
    private int previousRB0;
    private Execute execute;
    private int[][] ram;
    private boolean watchdogEnable;
    public Simulator simulator;

    public InterruHandler(Execute execute, int[][] ram){
        this.ram = ram;
        previousInput = ram[0][5] & 0b0001_0000;
        previousRB0 = ram[0][6] & 1;
        previousRB7ToRB4 = ram[0][6] & 0b1111_0000;
        watchdogEnable = false;
    }

    public void updateTMR0() {
        updateWatchdog();
        // Option reg T0CS
        if (execute.getFlag(Flags.TimerClockSource) == 0) {
            incrementTRM0();
        } else {
            int RA4 = ram[0][5] & 0b0001_0000;
            if (execute.getFlag(Flags.TimerSourceEdge) == 0) {
                // low-to-high
                if (previousInput < RA4) {
                    incrementTRM0();
                }
            } else {
                // high-to-low
                if (previousInput > RA4) {
                    incrementTRM0();
                }
            }
            previousInput = RA4;
        }
    }

    private void incrementTRM0() {
        if (execute.getFlag(Flags.PrescalerAssignment) == 0) {
            if (handlePrescaler() == true) {
                ram[0][1]++;
            }
        } else {
            ram[0][1]++;
        }

        // Timer Overflow
        if (ram[0][1] > 255) {
            execute.setFlag(Flags.TImerOverflow, 1);
            execute.setFlag(Flags.Zero, 1);
            ram[0][1] = 0;
        }

        if (execute.getFlag(Flags.TImerOverflow) == 1 && execute.getFlag(Flags.TimerInterrupt) == 1 && execute.getFlag(Flags.GlobalInterruptEnable) == 0) {
            // Timer Interrupt
            execute.setFlag(Flags.GlobalInterruptEnable, 1);
            execute.returnStack.push(Simulator.programCounter);
            Simulator.programCounter = 4;
        }
    }

    private boolean handlePrescaler(){
        prescalerCount++;
        // prescaler rate determaned by Prescaler assignment bit
        if (prescalerCount == (execute.getFlag(Flags.PrescalerAssignment) == 0 ? (int) Math.pow(2, (ram[1][1] & 0b0000_0111) + 1) : (int) Math.pow(2, (ram[1][1] & 0b0000_0111)))) {
            prescalerCount = 0;
            return true;
        }
        return false;
    }

    public void CheckInterrupt(){
        // check timer interrupt
        updateTMR0();

        // RB0 bit
        int RB0 = ram[0][6] & 1;
        if(execute.getFlag(Flags.InterruptEdgeSelect) == 1){
            // low to high
            if(RB0 > previousRB0){
                RB0Interrupt();
            }
        }else{
            // Hight to low
            if (RB0 > previousRB0){
                RB0Interrupt();
            }
        }

        // Interrupt when enable
        if(execute.getFlag(Flags.RB0Interrupt) == 1 && execute.getFlag(Flags.GlobalInterruptEnable) == 1 && execute.getFlag(Flags.RB0InterruptEnable) == 1){
            execute.setFlag(Flags.GlobalInterruptEnable, 0);
            execute.returnStack.push(Simulator.programCounter);
            Simulator.programCounter = 4;
        }
        previousRB0 = RB0;

        
        // RB Port Change Interrupt
        int RB7ToRB4 = ram[0][6] & 0b11110000; 
        if(RB7ToRB4 != previousRB7ToRB4){
            execute.setFlag(Flags.RBInterrupt, 1);
        }

        if(execute.getFlag(Flags.RBInterrupt) == 1 && execute.getFlag(Flags.RBPortChangeEnable) == 1 && execute.getFlag(Flags.GlobalInterruptEnable) == 1){
            execute.setFlag(Flags.GlobalInterruptEnable, 0);
            execute.returnStack.push(Simulator.programCounter);
            Simulator.programCounter = 4;
        }
        previousRB7ToRB4 = RB7ToRB4;
    }

    private void updateWatchdog() {
        if(watchdogEnable == false){
            return;
        }

        // Update WatchdogTimer
        if (execute.getFlag(Flags.PrescalerAssignment) == 1) {
            if (handlePrescaler() == true) {
                watchdogCount += 0.001;
            }
        } else {
            watchdogCount += 0.001;
        }

        // WatchdogTimer Interrupt
        if(watchdogCount >= 18){
            if(execute.isAsleep == true){
                execute.isAsleep = false;
            }else{
                simulator.powerOnReset();
            }
            watchdogCount = 0;
        }
    }
    public void clearWatchdog(){
        watchdogCount = 0;
    }

    private void RB0Interrupt(){
        execute.isAsleep = false;
        execute.setFlag(Flags.RB0Interrupt, 1);
    }

    public void SetPrescaler(int num){
        prescalerCount = num;
    }

    public void CheckPrescalerReset(int file, int value){
        if (file == 1) {
            if(execute.getRP0() == 0){
                // Reset prescaler if user writes to timer
                prescalerCount = 0;
            }else{
                if((value & 0b0000_1111) != 0){
                    // Reset prescaler if prescaler flag bits get changed
                    prescalerCount = 0;
                }
            }
        }      
    }
}