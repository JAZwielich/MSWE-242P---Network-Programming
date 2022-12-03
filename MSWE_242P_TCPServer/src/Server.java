import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Scanner;

//handle if a text isn't there
public class Server {
    private int port;
    ServerSocket serverSocket;
    LinkedList<String> fileRepository = new LinkedList<>();

    /**
     * Creates the socket and generates a timeout.
     * @param port - Port open for the incoming client connection
     * @param fileStrings - Strings of the file locations
     * @throws IOException - Throws unknown host exception if no host is available
     */
    public Server (int port, String[] fileStrings) throws IOException {
        try{
            this.port = port;
            serverSocket = new ServerSocket(port);
            //Adds all the file names from the fileStrings instance variable
            try {
                for (int i = 0; i < fileStrings.length; i ++) {fileRepository.add((fileStrings[i]));}
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (UnknownHostException e){
            System.out.println(e
            );
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
            Socket clientSocket = serverSocket.accept();
            //Reports when the client is connected
            System.out.println("Client Connected");
            //Create streams and a buffer for receiving the initial command from the client
            InputStreamReader input = new InputStreamReader(clientSocket.getInputStream());
            BufferedReader buffer = new BufferedReader(input);
            //Reads the command send from the user
            String clientInput = buffer.readLine();
            //Seperates the different words based on the space
            String[] parseString = clientInput.split(" ");
            Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());
            //If we received 'close' from the client we just clost the connection
            if (parseString[0].equals("close")) {
                System.out.println("Shutting Down Server");
                writer.write("Shutting Down Server\n");
                writer.flush();
                closeSocket();
                break;
            } else if (parseString[0].equals("get")) { //We look for the first word to say 'get'
                String parseStringOne = parseString[1].toString();
                //check if our file repository has the mentioned file. File name should be located one space after get
                if (fileRepository.contains(parseString[1])) {
                    //Acknoledge back to the client 'ok' and open the file and create a scanner
                    writer.write("Ok\n");
                    File readFile;
                    Scanner textScanner = null;
                    try {
                        readFile = new File(parseString[1]);
                        textScanner = new Scanner(readFile);
                    } catch (FileNotFoundException e) {
                        writer.write("Specified File not found");
                        writer.write("\n");
                        writer.flush();
                        continue;
                    }
                    //Read the text file till the EOF
                    while (textScanner.hasNextLine()) {
                        writer.write(textScanner.nextLine());
                        writer.write("\n");
                    }
                    //Flush writer back to client and close the scanner
                    textScanner.close();
                    writer.flush();
                } else { //Report an error if we do not have the file specified after get
                    writer.write("error\n");
                    writer.flush();
                }
            } else if (parseString[0].equals("index")) {
                if (fileRepository.size() == 0) {
                    writer.write("File Server is Empty");
                } //If we have no files specified tell the client
                else { //Else we return all files specified in our file repository
                    for (int i = 0; i < fileRepository.size() - 1; i++) {
                        writer.write(fileRepository.get(i) + ", ");
                    }
                    writer.write(fileRepository.get(fileRepository.size() - 1) + "\n");
                }
                writer.flush(); //Flush the buffer back to the user
            } else { //Tell the client if we don't recognize the command at all
                writer.write("Command not recognized");
                writer.flush();
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
}
