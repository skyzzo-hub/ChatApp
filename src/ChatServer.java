import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(2000);
        System.out.println("Server Started. Waiting for clients...");

        while (true) {
            // Halting untill a client connects.
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected." + clientSocket);
            
            //Spawning a thread to handle each client
            ClientHandler clientThread = new ClientHandler(clientSocket , clients);
            clients.add(clientThread);
            new Thread(clientThread).start();
        }

    }
}

class ClientHandler implements Runnable{
    private Socket clientSocket;
    private List<ClientHandler> clients;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket, List<ClientHandler> clients) throws IOException {
        this.clientSocket = socket;
        this.clients = clients; 
        this.out = new PrintWriter(clientSocket.getOutputStream(),true);
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void run() {
        try{
            String inputLine; 
            while((inputLine = in.readLine()) != null){
                //Broadcast message to all clients
                for ( ClientHandler aClient : clients){
                    aClient.out.println(inputLine);
                }
            }
        } catch (IOException e) {
            System.out.println("An error occured: " + e.getMessage());
        } finally {
            try{
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
