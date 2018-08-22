package com.gianlu.internethacker;

import com.gianlu.internethacker.models.DnsMessage;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DnsModule implements Closeable {
    private static final Logger logger = Logger.getLogger(DnsModule.class.getName());
    private static final SocketAddress DEFAULT_SERVER;

    static {
        try {
            DEFAULT_SERVER = new InetSocketAddress(InetAddress.getByName("8.8.8.8"), 53);
        } catch (UnknownHostException ex) {
            throw new RuntimeException("Default DNS server not found.", ex);
        }
    }

    private final Runner runner;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Map<Short, SocketAddress> idToRecipient = new ConcurrentHashMap<>();

    public DnsModule(int port) throws IOException {
        this.runner = new Runner(port);
        new Thread(runner).start();
    }

    @Override
    public void close() {
        runner.close();
    }

    private class ServingClient implements Runnable {
        private final DatagramSocket socket;
        private final DatagramPacket packet;

        public ServingClient(DatagramSocket socket, DatagramPacket packet) {
            this.socket = socket;
            this.packet = packet;
        }

        @Override
        public void run() {
            DnsMessage message = new DnsMessage(packet.getData());
            SocketAddress recipient = idToRecipient.remove(message.header.id);

            ByteArrayOutputStream out;
            try {
                out = new ByteArrayOutputStream();
                message.write(out);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Failed writing packet to the stream.", ex);
                return;
            }

            if (recipient != null) {
                try {
                    DatagramPacket packet = new DatagramPacket(out.toByteArray(), out.size(), recipient);
                    socket.send(packet);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Failed sending packet to client.", ex);
                }
            } else {
                idToRecipient.put(message.header.id, packet.getSocketAddress());

                try {
                    DatagramPacket packet = new DatagramPacket(out.toByteArray(), out.size(), DEFAULT_SERVER);
                    socket.send(packet);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Failed sending packet to server.", ex);
                }
            }
        }
    }

    private class Runner implements Runnable, Closeable {
        private final DatagramSocket serverSocket;
        private volatile boolean shouldStop;

        public Runner(int port) throws IOException {
            serverSocket = new DatagramSocket(port);
        }

        @Override
        public void run() {
            while (!shouldStop) {
                try {
                    DatagramPacket packet = new DatagramPacket(new byte[512], 512);
                    serverSocket.receive(packet);
                    executorService.execute(new ServingClient(serverSocket, packet));
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Failed receiving packet.", ex);
                }
            }
        }

        @Override
        public void close() {
            shouldStop = true;
            serverSocket.close();
        }
    }
}
