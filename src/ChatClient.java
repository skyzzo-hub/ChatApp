import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.function.Consumer;

public class ChatClient {
    private Socket socket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private Consumer<String> onMessageReceived;


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

            this.out.println("FILE: " + fileName);
            this.out.flush();
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
