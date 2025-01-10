package src;

import java.util.Arrays;

/**
 * PIC-stack data structure. can contain up to 8 integers
 */
public class PICStack {
    private final int[] stack = new int[8];
    private int stackPointer = 0;

    /**
     * push a value on the stack
     * @param num value as integer
     */
    public void push(int num){
        stack[stackPointer] = num;
        stackPointer++;
        stackPointer = stackPointer % 8;
    }

    /**
     * pop a value from the stack
     * @return value as integer
     */
    public int pop(){
        stackPointer--;
        if(stackPointer < 0){
            stackPointer = 7;
        }
        return stack[stackPointer];
    }

    /**
     * stack getter function
     * @return stack
     */
    public int[] getStack() {
        return stack;
    }

    /**
     * reset stack:
     * fills stack with default value 0 and resets the pointer
     */
    public void resetStack() {
        Arrays.fill(stack, 0);
        stackPointer = 0;
    }
}
