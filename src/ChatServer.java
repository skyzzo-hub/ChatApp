import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;

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
    static ArrayList<MyFile> myFiles = new ArrayList<>();

    public ClientHandler(Socket socket, List<ClientHandler> clients) throws IOException {
        this.clientSocket = socket;
        this.clients = clients; 
        this.out = new PrintWriter(clientSocket.getOutputStream(),true);
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void run() {
        try{
            JFrame jFrame = new JFrame("ChatApp Server");
            jFrame.setSize(400,400);
            jFrame.setLayout(new BoxLayout(jFrame.getContentPane(),BoxLayout.Y_AXIS));
            jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel jPanel = new JPanel();
            jPanel.setLayout(new BoxLayout(jPanel,BoxLayout.Y_AXIS));

            JScrollPane jScrollPane = new JScrollPane(jPanel);
            jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

            JLabel jlTitle = new JLabel("ChatApp File Receiver");
            jlTitle.setFont(new Font("Arial", Font.BOLD ,25 ));
            jlTitle.setBorder(new EmptyBorder(20,0,10,0));
            jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

            jFrame.add(jlTitle);
            jFrame.add(jScrollPane);
            jFrame.setVisible(true);


            String inputLine; 
            while((inputLine = in.readLine()) != null){
                //Broadcast message to all clients
                for ( ClientHandler aClient : clients) {
                    aClient.out.println(inputLine);
                }

                int fileId = 0;

                DataInputStream dataInputStream = new DataInputStream(clientSocket.getInputStream());
                int fileNameLength = dataInputStream.readInt();
                if (fileNameLength > 0){
                    byte[] fileNameBytes = new byte[fileNameLength];
                    dataInputStream.readFully(fileNameBytes,0,fileNameLength);
                    String fileName = new String(fileNameBytes);

                    int fileContentLength = dataInputStream.readInt();
                    if (fileContentLength > 0){
                        byte[] fileContentBytes = new byte[fileContentLength];
                        dataInputStream.readFully(fileContentBytes,0,fileContentLength);


                        JPanel jpFileRow = new JPanel();
                        jpFileRow.setLayout(new BoxLayout(jpFileRow,BoxLayout.Y_AXIS));

                        JLabel jlFileName = new JLabel(fileName);
                        jlFileName.setFont(new Font("Arial", Font.BOLD ,20 ));
                        jlFileName.setBorder(new EmptyBorder(10,0,10,0));

                        if(getFileExtention(fileName).equalsIgnoreCase("txt")){

                            jpFileRow.setName(String.valueOf(fileId));
                            jpFileRow.addMouseListener(getMyMouseListener());
                            jpFileRow.add(jlFileName);

                            jPanel.add(jpFileRow);
                            jFrame.validate();

                        }else{
                            jpFileRow.setName(String.valueOf(fileId));
                            jpFileRow.addMouseListener(getMyMouseListener());
                            jpFileRow.add(jlFileName);
                            jPanel.add(jpFileRow);
                            jFrame.validate();

                        }
                        myFiles.add(new MyFile(fileId,fileName,fileContentBytes,getFileExtention(fileName)));
                    }

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

    public static MouseListener getMyMouseListener() {

        return new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JPanel jPanel =(JPanel) e.getSource();

                int fileId = Integer.parseInt(jPanel.getName());

                for ( MyFile myFile: myFiles ){

                    if(myFile.getId() == fileId){
                        JFrame jfPreview = createFrame(myFile.getName(),myFile.getData(),myFile.getFileExtension());
                        jfPreview.setVisible(true);
                    }

                }
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        };
    }

    public static JFrame createFrame(String fileName, byte[] fileData , String fileExtension) {
        JFrame jFrame = new JFrame("ChatApp File Downloader");
        jFrame.setSize(400,400);

        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel,BoxLayout.Y_AXIS));

        JLabel jlTitle = new JLabel("ChatApp File Downloader");
        jlTitle.setFont(new Font("Arial", Font.BOLD ,25 ));
        jlTitle.setBorder(new EmptyBorder(20,0,10,0));
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel jPrompt = new JLabel("Are you sure you want to download the file?");

        JButton jbYes = new JButton("YES");
        jbYes.setPreferredSize(new Dimension(150,75));
        jbYes.setFont(new Font("Arial", Font.BOLD ,20 ));

        JButton jbNo = new JButton("NO");
        jbNo.setPreferredSize(new Dimension(150,75));
        jbNo.setFont(new Font("Arial", Font.BOLD ,20 ));

        JPanel jpButtons = new JPanel();
        jpButtons.setBorder(new EmptyBorder(20,0,10,0));
        jpButtons.add(jbYes);
        jpButtons.add(jbNo);

        jbYes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                File fileToDownload = new File(fileName);
                try{
                    FileOutputStream fileOutputStream = new FileOutputStream(fileToDownload);

                    fileOutputStream.write(fileData);
                    fileOutputStream.close();

                    jFrame.dispose();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        });

        jbNo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                jFrame.dispose();
            }
        });

        jPanel.add(jlTitle);
        jPanel.add(jPrompt);
        jPanel.add(jpButtons);
        jFrame.add(jPanel);
        jFrame.setVisible(true);
        return jFrame;



    }

    public static String getFileExtention(String fileName){
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i + 1);
        }
        return "no Extension Found";
    }
}
