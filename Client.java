import java.io.*;
import java.net.Socket;
import java.security.Key;

public class Client {

    private Socket socket;

    public Client() {
        try {
            socket = new Socket(Utils.ip_addr, 8088);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ServerHandler implements Runnable {

        @Override
        public void run() {
            try {
                // start transferring message by using sk......
                while (true) {
                    Thread.sleep(1000000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {

        try {
            ServerHandler handler = new ServerHandler();
            Thread t = new Thread(handler);
            t.start();
//                generate geoLock
            byte[] geoLock = Utils.generateGeoLock(Utils.e, Utils.n, Utils.l);
            byte[] encryptedSk = new byte[Utils.keyLength];
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            dis.read(encryptedSk);

            System.out.println("Get ciphertext from the server: " + new String(encryptedSk));

//                decrypt to get the sk
            byte[] decryptedSk = new byte[Utils.keyLength];
            for (int i = 0; i < Utils.keyLength; i++) {
                decryptedSk[i] = (byte) (encryptedSk[i] ^ geoLock[i]);
            }

            System.out.println("Get the sk from ciphertext XOR Geolock: " + new String(decryptedSk));

//                send the Esk(sk) back to the server for verification
            Key key = Utils.createKey(decryptedSk);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            byte[] sendBack = Utils.encryptAES(decryptedSk, key);
            dos.write(sendBack);

            System.out.println("Send E(sk) back to the server: " + new String(sendBack));

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
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Client client = new Client();
        client.start();
    }

}
