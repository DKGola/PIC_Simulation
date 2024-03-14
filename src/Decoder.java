package src;

/**
 * decodes a command using bitmasks
 */
public class Decoder {
    private Execute execute;
    public Decoder(int[][] ram) {
        execute =  new Execute(ram);
    }
    public void decode(int commandCode) {

    }
}
