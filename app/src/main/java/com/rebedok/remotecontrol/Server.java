package com.rebedok.remotecontrol;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by rebed on 31.08.2017.
 */

public class Server {
    private String address;
    private int port = 6789;
    private Socket socket = null;
    DataOutputStream out;

    public Server() {
    }

    public void inputAddress(String address) {
        this.address = new String(address);
    }

    public void openConnection(String address) throws Exception {
        closeConnection();
        try {
            socket = new Socket(address, port);
            out = new DataOutputStream( socket.getOutputStream());
        } catch (IOException e) {
            throw new Exception("failed connection" + e.getMessage());
        }
    }

    public void closeConnection() throws Exception {
        if (socket != null) {
            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    throw new Exception("failed close connection" + e.getMessage());
                } finally {
                    socket = null;
                }
            }
        }
    }

    public void Send(String data) throws Exception {
        if (socket != null || !socket.isClosed()) {
            try {
                out.writeUTF(data);
                out.flush();
            } catch (IOException e) {
                if (socket == null || socket.isClosed()) {
                   return;
                }
                throw new Exception("невозможно отправить данные " + e.getMessage());
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        closeConnection();
    }
}
