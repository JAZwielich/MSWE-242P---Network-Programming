import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;

public class Client {
    private int port = 4999;
    private String serverAddress;
    private DatagramSocket clientSocket;
    private InetAddress server;

    /**
     * Client constructor that requires a server address and port number
     * @param serverAddress - IP address of the server
     * @param port - Port of the server
     * @throws IOException
     */
    public Client(String serverAddress, int port) throws IOException {
        this.port = port;
        this.serverAddress = serverAddress;
        this.clientSocket = new DatagramSocket(port);
        this.clientSocket.setSoTimeout(15000);
        this.server = InetAddress.getByName(serverAddress);
    }

    /**
     * Sends command to a server and receives a response from the server
     * @param command - String command to be sent to the server
     * @throws IOException
     */
    public void sendCommand(String command) throws IOException {
        try {
            //We take the command and we send it to the server
            convertAndSend(command);
            //We open up a reader to receive a response from the server
            String serverInput;
            int count = 1;
            //Buffer all replies from the server
            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket received = new DatagramPacket(buffer, buffer.length);
                clientSocket.receive(received);
                //Take the received Byte of data and convert it into a readable string
                String receivedString = new String(received.getData(), received.getOffset(), received.getLength());
                //Packets from the text are formated like this <packetOrder>&%^<textLength>&%^<Text> so we seperate it out
                String splitReceivedString[] = receivedString.split("&%#");
                //Close connection when done
                if (splitReceivedString.length == 1){ //Filters out one length responses as those are confirmations from server
                    System.out.println(splitReceivedString[0]);
                } else if (splitReceivedString.length == 2) { //Filers out two length responses which are new line
                    System.out.println("\n");
                    convertAndSend("ok");
                    count++;
                }else { //This is part of the received text
                    if (receivedString.contains("%&__&%")){ //indicates an end to the connection
                        clientSocket.close();
                        break;
                    } else if (splitReceivedString[2].length() == Integer.parseInt(splitReceivedString[1]) && Integer.parseInt(splitReceivedString[0]) == count) {
                        convertAndSend("ok"); //Ok if length in packet matches the length of the actual text and same with index
                        if(splitReceivedString[2].length() == 950){
                            System.out.print(splitReceivedString[2]);
                        } else{
                            System.out.print(splitReceivedString[2] + "\n");
                        }
                        count++;
                    } else { //Something was wrong with the packet we ask the server to resend
                        convertAndSend("resend");
                    }
                }
            }
        } catch (SocketException e) { //Report the connection closed when the server stops
            System.out.println("Connection Closed");
        } catch (SocketTimeoutException e) {
            System.out.println("Connection Closed");
        }
    }

    /**
     * Converts a string into a datagram and sends it out
     * @param dataString - The string to be sent out
     * @throws IOException
     */
    public void convertAndSend (String dataString) throws IOException {
        byte data[] = dataString.getBytes();
        DatagramPacket sent = new DatagramPacket(data, data.length, server, 5000);
        clientSocket.send(sent);
    }
}
