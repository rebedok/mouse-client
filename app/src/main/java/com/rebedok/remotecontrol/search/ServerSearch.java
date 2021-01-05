package com.rebedok.remotecontrol.search;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.ArrayAdapter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by rebed on 22.02.2018.
 */

public class ServerSearch extends AppCompatActivity {
    private int port = 7000;
    private InetAddress multicastAddress = null;
    private int sizeBuffer = 1024;
    byte[] buffer = new byte[sizeBuffer];
    AtomicBoolean stop = new AtomicBoolean(false);
    AtomicBoolean close = new AtomicBoolean(false);
    ArrayList<String> addressesList;
    private ArrayAdapter<String> addressesAdapter;

    public ServerSearch(ArrayAdapter<String> adapter, ArrayList<String> addresses) {
        addressesList = addresses;
        addressesAdapter = adapter;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    multicastAddress = InetAddress.getByName("228.5.6.7");
                    MulticastSocket socket = new MulticastSocket(port);
                    socket.joinGroup(multicastAddress);
                    receiveString(socket);
                    sendString(socket);
                    close(socket);
                } catch (IOException e) {
                    System.err.println("IOException " + e);
                }
            }
        }).start();
    }

    public void sendString(MulticastSocket socket) throws IOException {
        while (!stop.get()) {
            byte[] b = "who is here".getBytes();
            DatagramPacket dp = new DatagramPacket(
                    b, b.length, multicastAddress, port);
            socket.send(dp);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void receiveString(final MulticastSocket socket) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stop.get()) {
                    DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                    try {
                        socket.receive(reply);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String line = getMyString(reply);
                    System.out.println(line);
                    printAddress(line);
                }
                close.set(true);
            }
        }).start();
    }

    public String getMyString(DatagramPacket packet) {
        return new String(packet.getData(), 0, packet.getLength());
    }

    public void stopSearch() {
        stop.set(true);
    }

    private void close(MulticastSocket socket) throws IOException {
        while (!close.get()) ;
        socket.leaveGroup(multicastAddress);
        socket.close();
    }

    public void printAddress(String address) {
        if(address.equals("who is here") || addressesList.contains(address)) {
            return;
        }
        addressesList.add(address);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                addressesAdapter.notifyDataSetChanged();
            }
        });
    }

}

