# PIC-16F8X Simulator

A simulator of the PIC-16F8X microcontroller written in Java.
It simulates all the functionalities of a real microcontroller, including the Fetch - Decode - Execute cycle, registers, calculating, interrupts, etc.

## Description

In the following, the general functionality of the simulator will be displayed in a little more detail.

![image](image.png)

The interface consists of the following control elements:

- Red: Menu ribbon, via which an LST file containing the assembler code can be selected using the file picker. There is also a checkbox for the Watchdog Enable.
- Blue: Start button, Stop button, Step button and Reset button. These can be used to navigate through the commands.
- Orange: The frequency can be freely selected between 32,768 hertz and 20 megahertz using a slider. There are also five radio buttons which can be used to select common frequencies directly. On the right is a display for the current runtime in microseconds.
- Green: The large table on the left-hand side displays the assembler program that has been read in, including colored marking of the next command. Each line contains the option to set a breakpoint on the left.
- Purple: To the right of this table, the most important values such as the content of the W register, the program counter or the most important flags can be seen at a glance.
- Brown: On the right-hand side, the general purpose register, the special function register, the stack and the I/O pins are displayed in table form. The latter can be switched between 1 and 0 by clicking on a port. You can switch between these tables using tabs.

## Used Technologies

* Java for the functionality
* Java Swing for the User Interface

Run the program using these tools.
In the top left of the GUI, click the "File"-button and select one of the given .lst test files, then run the code.

## Authors

* Damian Gola
* Maik Schindler
