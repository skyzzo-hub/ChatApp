import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ChatClientGUI extends JFrame {

    private JButton sendFButton;
    private JTextArea messageArea;
    private JTextField textField;
    private ChatClient client;
    private JButton exitButton;
    private JButton confirmSendFileButton;
    //private File file;
    public ChatClientGUI() {
        super("Chat Application");
        setSize(400,500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Styling variables
        Color backgroundColor = new Color(241, 205, 176); // Light gray background
        Color buttonColor = new Color(75, 75, 75); // Darker gray for buttons
        Color FileButtonColor = new Color(75,75,75);
        Color textColor = new Color(50, 50, 50); // Almost black for text
        Font textFont = new Font("Arial", Font.PLAIN, 14);
        Font buttonFont = new Font("Arial", Font.BOLD, 12);
        Icon iconInputFile = new ImageIcon(getClass().getResource("add-document.png"));
        Icon iconOutputFile = new ImageIcon(getClass().getResource("file-export.png"));
        final File[] file = new File[1];

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setBackground(backgroundColor);
        messageArea.setForeground(textColor);
        messageArea.setFont(textFont);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        add(scrollPane, BorderLayout.CENTER);
        
        String name = JOptionPane.showInputDialog(this,"Enter your name: ","Name Entry",JOptionPane.PLAIN_MESSAGE);
        this.setTitle("ChatApp - " + name);

        textField = new JTextField();
        textField.setFont(textFont);
        textField.setForeground(textColor);
        textField.setBackground(backgroundColor);
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                String message = "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + name + ": " + textField.getText();
                client.sendMessage(message);
                textField.setText("");

            }
        });
        confirmSendFileButton = new JButton(iconOutputFile);
        confirmSendFileButton.setEnabled(false);


        sendFButton = new JButton(iconInputFile);
        sendFButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                JFileChooser jFileChooser = new JFileChooser();
                jFileChooser.setDialogTitle("Choose a file to send");

                if(jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                    sendFButton.setEnabled(false);
                    confirmSendFileButton.setEnabled(true);
                    file[0] = jFileChooser.getSelectedFile();
                }
            }
        });


        confirmSendFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                if (file[0] != null) {
                    sendFButton.setEnabled(true);
                    confirmSendFileButton.setEnabled(false);
                    String message = "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + name + ": " + "New file added to the Server !" + file[0].getName();
                    client.sendMessage(message);
                    client.sendFile(file[0]);
                    textField.setText("");
                }

            }
        });

        exitButton = new JButton("Exit");
        exitButton.setFont(buttonFont);
        exitButton.setBackground(buttonColor);
        exitButton.setForeground(Color.WHITE);
        exitButton.addActionListener(e ->{
            String departureMessage = name + " has left the chat.";
            client.sendMessage(departureMessage);
             // Delay to ensure the message is sent before exiting
             try{
                Thread.sleep(1000);

            }catch(InterruptedException ie){
                Thread.currentThread().interrupt();
            }
        
            System.exit(0);
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(backgroundColor);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        bottomPanel.add(exitButton);
        bottomPanel.add(textField);
        bottomPanel.add(sendFButton);
        bottomPanel.add(confirmSendFileButton);
        add(bottomPanel,BorderLayout.SOUTH);

    

        // Initialize and start the ChatClient
        try{
            this.client = new ChatClient("127.0.0.1",2000, this::onMessageReceived);
            client.startClient();
        } catch (IOException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,"Error connecting to the server", "Connection error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

        

        

    private void onMessageReceived(String message){
        SwingUtilities.invokeLater(() -> messageArea.append(message + "\n"));gg
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ChatClientGUI().setVisible(true);
        });
    }
}
