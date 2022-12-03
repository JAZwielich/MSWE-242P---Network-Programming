import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Server myServer = new Server(4999, args);
        myServer.start();
    }
}