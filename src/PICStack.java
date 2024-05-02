package src;

import java.util.Arrays;

public class PICStack {
    private int[] stack = new int[8];
    private int stackPointer = 0;

    public void push(int num){
        stack[stackPointer] = num;
        stackPointer++;
        stackPointer = stackPointer % 8;
    }

    public int pop(){
        stackPointer--;
        if(stackPointer < 0){
            stackPointer = 7;
        }
        return stack[stackPointer];
    }

    public int[] getStack() {
        return stack;
    }

    public void resetStack() {
        Arrays.fill(stack, 0);
    }
}
