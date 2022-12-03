//Make it report an error if no file is present
public class Main {
    public static void main(String[] args) {
        TextCounter counter = new TextCounter();
        counter.countLines(args);
        counter.printCountedLines();
    }
}