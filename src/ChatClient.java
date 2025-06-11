import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.Consumer;

public class ChatClient {
    private Socket socket = null;
    private BufferedReader inputConsole = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private Consumer<String> onMessageReceived;
    private File file;


/*     public ChatClient( String address, int port) {
        try{
            socket = new Socket(address, port);
            System.out.println("Connected to the chat server");

            inputConsole = new BufferedReader(new InputStreamReader(System.in));
            out = new PrintWriter(socket.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = "";
            while(!line.equals("exit")) {
                line = inputConsole.readLine();
                out.println(line);
                System.out.println(in.readLine());
            }
            socket.close();
            inputConsole.close();
            out.close();            
        } catch(UnknownHostException u) {
            System.out.println("Host unknown: " +u.getMessage());
        } catch (IOException i){
            System.out.println("Expected exception: " + i.getMessage());
        }
    } */

    public ChatClient(String serverAddress, int serverPort, Consumer<String> onMessageReceived) throws IOException {
        this.socket = new Socket(serverAddress, serverPort);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.onMessageReceived = onMessageReceived;
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }

    public void sendFile(File f){
        try {
            FileInputStream fileInputStream = new FileInputStream(f.getAbsolutePath());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            String fileName = f.getName();
            byte[] fileNameBytes = fileName.getBytes();

            byte[] fileContentsBytes = new byte[(int) f.length()];
            fileInputStream.read(fileContentsBytes);

            dataOutputStream.writeInt(fileNameBytes.length);
            dataOutputStream.write(fileNameBytes);

            dataOutputStream.writeInt(fileContentsBytes.length);
            dataOutputStream.write(fileContentsBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startClient(){
        new Thread(() -> {
            try{
                String line;
                while((line = in.readLine()) != null){
                    onMessageReceived.accept(line);
                }
            }catch (IOException e) {
                    e.printStackTrace();
            }
        }).start();
    }
}
