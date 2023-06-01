package mini.tx.mk4;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by cameronfranz on 7/13/16.
 */
public class UDPSendServer implements Runnable {

    private byte[] messageToSend;
    private String destinationIP;
    private int destinationPort;
    private int updateRate;


    public UDPSendServer(byte[] commandInput) {
        messageToSend = commandInput;
           }

    public void setDestination(String ip, int port) {
        destinationIP = ip;
        destinationPort = port;
    }

    public void setUpdateRate(int rate) {
        updateRate = rate;
    }

    public void run() {
        DatagramSocket sendSocket;
        //Log.d("CommandServer", "Starting UDP send server with IP destination: " + destinationIP + " and port: " + Integer.toString(destinationPort));
        try {
            sendSocket = new DatagramSocket();
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    sendCommandPacket(sendSocket, messageToSend);
                    //Log.d("ControllerActivity", " send socket package= " + String.valueOf(messageToSend[1]));

                } catch (UnknownHostException e) {
                    //Log.d("CommandServer","Invalid destination IP");
                    //e.printStackTrace();
                } catch (IOException e) {
                    //Log.d("CommandServer","Error sending packet");
                    //e.printStackTrace();
                }
                try {
                    Thread.sleep(1000 / updateRate);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            //Log.d("CommandServer","Shutting down UDP send server...");
            sendSocket.close();
        } catch (Exception e) {
            //Log.d("CommandServer","Error binding send socket");
            //e.printStackTrace();
        }
    }

    private void sendCommandPacket(DatagramSocket socket, byte[] message) throws IOException {
        DatagramPacket sendPacket = new DatagramPacket(message, message.length, InetAddress.getByName(destinationIP), destinationPort);
        socket.send(sendPacket);
        //Log.d("CommandThread", "Sent message: " + message[5]);
    }


}
