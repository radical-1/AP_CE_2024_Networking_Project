import java.io.*;
import java.net.Socket;

public class Client {
    public void start() {
        try {
            Socket socket = new Socket("localhost", 8080);
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            String input;
            while (true) {
                System.out.println("Enter a command: ");
                input = bufferedReader.readLine();
                dataOutputStream.writeUTF(input);
                dataOutputStream.flush();
                System.out.println(dataInputStream.readUTF());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
     }
    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }
}
