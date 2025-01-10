package src;

/**
 * handles simulated interrupts and the watchdog
 */
public class InterruptHandler {
    private int prescalerCount = 0;
    private double watchdogCount;
    private int previousInput;
    private int previousRB7ToRB4;
    private int previousRB0;
    private final Execute execute;
    private final int[][] ram;
    private boolean watchdogEnable;

    /**
     * InterruptHandler constructor
     * @param execute executer instance
     * @param ram ram instance
     */
    public InterruptHandler(Execute execute, int[][] ram){
        this.ram = ram;
        previousInput = ram[0][5] & 0b0001_0000;
        previousRB0 = ram[0][6] & 1;
        previousRB7ToRB4 = ram[0][6] & 0b1111_0000;
        watchdogEnable = false;
        this.execute = execute;
    }

    /**
     * update timer0
     */
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

    /**
     * increment timer 0
     */
    private void incrementTRM0() {
        if (execute.getFlag(Flags.PrescalerAssignment) == 0) {
            if (handlePrescaler()) {
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

        if (execute.getFlag(Flags.TImerOverflow) == 1 && execute.getFlag(Flags.TimerInterrupt) == 1 && execute.getFlag(Flags.GlobalInterruptEnable) == 1) {
            // Timer Interrupt
            if(Program.simulator.getExecute().isAsleep){
                Program.simulator.getExecute().isAsleep = false;
            }else {
                execute.setFlag(Flags.GlobalInterruptEnable, 0);
                execute.returnStack.push(Simulator.programCounter);
                Simulator.programCounter = 4;
            }
        }
    }

    /**
     * prescaler handler
     * @return .
     */
    private boolean handlePrescaler(){
        prescalerCount++;
        // prescaler rate determined by Prescaler assignment bit
        if (prescalerCount == (execute.getFlag(Flags.PrescalerAssignment) == 0 ? (int) Math.pow(2, (ram[1][1] & 0b0000_0111) + 1) : (int) Math.pow(2, (ram[1][1] & 0b0000_0111)))) {
            prescalerCount = 0;
            return true;
        }
        return false;
    }

    /**
     * check for interrupt, interrupt when conditions are met
     */
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
            if (RB0 < previousRB0){
                RB0Interrupt();
            }
        }

        // Interrupt when enable
        if(execute.getFlag(Flags.RB0Interrupt) == 1 && execute.getFlag(Flags.GlobalInterruptEnable) == 1 && execute.getFlag(Flags.RB0InterruptEnable) == 1){
            if(Program.simulator.getExecute().isAsleep){
                Program.simulator.getExecute().isAsleep = false;
            }else {
                execute.setFlag(Flags.GlobalInterruptEnable, 0);
                execute.returnStack.push(Simulator.programCounter);
                Simulator.programCounter = 4;
            }
        }
        previousRB0 = RB0;

        
        // RB Port Change Interrupt
        int RB7ToRB4 = ram[0][6] & 0b11110000;
        if((RB7ToRB4 & ram[1][6]) != (previousRB7ToRB4 & ram[1][6])){
            execute.setFlag(Flags.RBInterrupt, 1);
        }

        if(execute.getFlag(Flags.RBInterrupt) == 1 && execute.getFlag(Flags.RBPortChangeEnable) == 1 && execute.getFlag(Flags.GlobalInterruptEnable) == 1){
            if(Program.simulator.getExecute().isAsleep){
                Program.simulator.getExecute().isAsleep = false;
            }else {
                execute.setFlag(Flags.GlobalInterruptEnable, 0);
                execute.returnStack.push(Simulator.programCounter);
                Simulator.programCounter = 4;
            }
        }
        previousRB7ToRB4 = RB7ToRB4;
    }

    /**
     * update watchdog timer
     */
    private void updateWatchdog() {
        if(!watchdogEnable){
            return;
        }

        // Update WatchdogTimer
        if (execute.getFlag(Flags.PrescalerAssignment) == 1) {
            if (handlePrescaler()) {
                watchdogCount += (4_000_000.0 / Program.simulator.frequency);
            }
        } else {
            watchdogCount += (4_000_000.0 / Program.simulator.frequency);
        }

        // WatchdogTimer Interrupt
        if(watchdogCount >= 18000){
            if(execute.isAsleep){
                execute.isAsleep = false;
                execute.setFlag(Flags.TimeOut, 0);
            }else{
                Program.simulator.softReset();
            }
            watchdogCount = 0;
        }
    }

    /**
     * switch watchdog on / off
     */
    public void ToggleWatchdog(){
        watchdogEnable = !watchdogEnable;
    }

    /**
     * reset watchdog to 0
     */
    public void clearWatchdog(){
        watchdogCount = 0;
    }

    /**
     * set rb0interrupt flag
     */
    private void RB0Interrupt(){
        execute.isAsleep = false;
        execute.setFlag(Flags.RB0Interrupt, 1);
    }

    /**
     * set prescaler on count
     * @param num count for prescaler
     */
    public void SetPrescaler(int num){
        prescalerCount = num;
    }

    /**
     * reset prescaler if user writes to timer or flag bit gets changed
     * @param file f
     * @param value value whose flag bit should be checked
     */
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
