import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class Client {
    private int port = 4999;
    private String serverAddress;
    private Socket clientSocket;

    /**
     * Client constructor that requires a server address and port number
     * @param serverAddress - IP address of the server
     * @param port - Port of the server
     * @throws IOException
     */
    public Client(String serverAddress, int port) throws IOException {
        this.port = port;
        this.serverAddress = serverAddress;
        clientSocket = new Socket(serverAddress,port);
        clientSocket.setSoTimeout(10000);
    }

    /**
     * Sends command to a server and receives a response from the server
     * @param command - String command to be sent to the server
     * @throws IOException
     */
    public void sendCommand(String command) throws IOException {
        try {
            //We take the command and we send it to the server
            Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());
            writer.write(command + "\n");
            writer.flush();
            //We open up a reader to receive a response from the server
            InputStreamReader input = new InputStreamReader(clientSocket.getInputStream());
            BufferedReader buffer = new BufferedReader(input);
            String serverInput;
            //Buffer all replies from the server
            while ((serverInput = buffer.readLine()) != null) {
                System.out.println(serverInput);
            }
            //Close connection when done
            clientSocket.close();
        } catch (SocketException e) { //Report the connection closed when the server stops
            System.out.println("Connection Closed");
        } catch (SocketTimeoutException e) {
            System.out.println("Connection Closed");
        }
    }
}
