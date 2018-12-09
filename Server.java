import java.io.*;
import java.net.Socket;

import java.net.ServerSocket;
import java.security.Key;
import java.util.Arrays;

public class Server {
    private ServerSocket serverSocket;

    public Server() {
        try {
            serverSocket = new ServerSocket(8088);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ClientHandler implements Runnable {

        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

//              generate a random secret key;

                String sk = Utils.getRandomString();

                System.out.println("Generating a random secret key: " + sk + "\n");

                byte[] skBytes = sk.getBytes();
                byte[] geoLock = Utils.generateGeoLock(Utils.e, Utils.n, Utils.l);

//              encrypt the secret key by Geolock and send it
                byte[] encryptedSk = new byte[Utils.keyLength];
                for (int i = 0; i < Utils.keyLength; i++) {
                    encryptedSk[i] = (byte) (skBytes[i] ^ geoLock[i]);
                }

                System.out.println("Sending sk XOR GeoLock: " + new String(encryptedSk));

                dos.write(encryptedSk);

//                verify the secret key from client
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                Key key = Utils.createKey(skBytes);
                byte[] buf = new byte[512];
                int len = dis.read(buf);
                byte[] verify = Utils.copyArray(buf, len);
                byte[] receiveSk = Utils.decryptAES(verify, key);

                System.out.println("\nGetting reply from the client: " + new String(verify));
                System.out.println("Decrypting the message by sk (check if content equals sk): " + new String(receiveSk));

                if (receiveSk == null) {
                    System.out.println("Authentication failed! \n");
                    socket.close();
                } else {
                    if (Arrays.equals(skBytes, receiveSk)) {
                        System.out.println("Authentication success! \n");
                    } else {
                        System.out.println("Authentication failed! \n");
                        socket.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

//            start providing services......

        }
    }


    public void start() {
        try {
            while (true) {
                Thread.sleep(2000);
                System.out.println("Waiting for new connection.....");
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                new Thread(handler).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }


}
