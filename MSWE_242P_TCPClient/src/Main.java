import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class Main {
    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost",4999);
        client.sendCommand(args[0]);
    }
}