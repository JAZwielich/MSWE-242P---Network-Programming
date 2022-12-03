import javax.xml.crypto.Data;
import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

//handle if a text isn't there
public class Server {
    private int port;
    DatagramSocket serverSocket;
    private InetAddress client;
    LinkedList<String> fileRepository = new LinkedList<>();

    /**
     * Creates the socket and generates a timeout.
     * @param port - Port open for the incoming client connection
     * @param fileStrings - Strings of the file locations
     * @throws IOException - Throws unknown host exception if no host is available
     */
    public Server (int port, String[] fileStrings) throws IOException {
        this.port = port;
        this.serverSocket = new DatagramSocket(port);
        client = InetAddress.getByName("localhost");
        //Adds all the file names from the fileStrings instance variable
        try {
            for (int i = 0; i < fileStrings.length; i ++) {fileRepository.add((fileStrings[i]));}
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This is the main method for the server. It allows the user to send the following commands with these effects
     * index - Returns all file index names
     * close - closes connection
     * get <FILE> - Where <FILE> is the file index name then it returns the text of the file to the client
     * @throws IOException
     */
    public void start() throws IOException {
        while (true) {
            byte[] buffer = new byte[1024];
            DatagramPacket received = new DatagramPacket(buffer, buffer.length);
            serverSocket.receive(received);
            //Reports when the client is connected
            System.out.println("Client Connected");
            //Reads the command send from the user
            String clientInput =  new String(received.getData(), received.getOffset(), received.getLength());
            //Separates the different words based on the space
            String[] parseString = clientInput.split(" ");
            byte data[];
            String dataString;
            //If we received 'close' from the client we just close the connection
            if (parseString[0].equals("close")) {
                System.out.println("Shutting Down Server");
                convertAndSend("Server Shutting Down");
                convertAndSend("%&__&%");
                closeSocket();
                break;
            } else if (parseString[0].equals("get")) { //We look for the first word to say 'get'
                String parseStringOne = parseString[1].toString();
                //check if our file repository has the mentioned file. File name should be located one space after get
                if (fileRepository.contains(parseString[1])) {
                    //Acknowledge back to the client 'ok' and open the file and create a scanner
                    convertAndSend("Ok");
                    File readFile;
                    Scanner textScanner = null;
                    try {
                        readFile = new File(parseString[1]);
                        textScanner = new Scanner(readFile);
                    } catch (FileNotFoundException e) {
                        convertAndSend("Specified File not found");
                        convertAndSend("%&__&%");
                        continue;
                    }
                    LinkedList<String> allText = new LinkedList<>();
                    //Read the text file and store in a linked list with an encoded number
                    int count = 1;
                    while (textScanner.hasNextLine()) {
                        String text = textScanner.nextLine();
                        //We have to reduce all text to a size of 950 so it fits in the packet
                        while (text.length() > 950){
                            int size = text.length();
                            String subtext = text.substring(0, 950);
                            String line =  count + "&%#" + subtext.length() + "&%#" + subtext;
                            allText.add(line);
                            count++;
                            text = text.substring(950, size);
                        }
                        //Add the remaining text
                        String line =  count + "&%#" + text.length() + "&%#" + text;
                        allText.add(line);
                        count++;
                    }
                    //Send all text lines with encoded number
                    count = 0;
                    while (count != allText.size()){
                        try {
                            //We set a timeout incase we don't get a response from the client. Then we resend
                            serverSocket.setSoTimeout(5000);
                            convertAndSend(allText.get(count));
                            serverSocket.receive(received);
                            clientInput = new String(received.getData(), received.getOffset(), received.getLength());
                            //If we receive ok from the client we received our packet!
                            if (clientInput.equals("ok")) {
                                count++;
                            } else if (clientInput.equals("resend")) { //Otherwise we resend the packet
                                continue;
                            } else {
                                continue;
                            }
                        }catch (SocketTimeoutException e) {
                            continue;
                        }
                    }
                    //close the scanner and remove the timeout
                    serverSocket.setSoTimeout(0);
                    textScanner.close();
                } else { //Report an error if we do not have the file specified after get
                    convertAndSend("error");
                }
            } else if (parseString[0].equals("index")) {
                if (fileRepository.size() == 0) {//If we have no files specified tell the client
                    convertAndSend("File Server is Empty");
                }
                else { //Else we return all files specified in our file repository
                    convertAndSend("Files available are:");
                    for (int i = 0; i < fileRepository.size() - 1; i++) {
                        convertAndSend(fileRepository.get(i));
                    }
                    convertAndSend(fileRepository.get(fileRepository.size() - 1));
                }
            } else { //Tell the client if we don't recognize the command at all
                convertAndSend("Command not recognized");
            }
        }
    }

    /**
     * closes the socket connection
     * @throws IOException
     */
    public void closeSocket() throws IOException {
        serverSocket.close();
    }

    /**
     * Converts a string into a datagram and sends it out
     * @param dataString - The string to be sent out
     * @throws IOException
     */
    public void convertAndSend (String dataString) throws IOException {
        byte data[] = dataString.getBytes();
        DatagramPacket sent = new DatagramPacket(data, data.length, client, 4999);
        serverSocket.send(sent);
    }
}
