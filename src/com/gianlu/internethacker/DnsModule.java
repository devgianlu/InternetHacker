package com.gianlu.internethacker;

import com.gianlu.internethacker.models.Message;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DnsModule implements Closeable {
    private static final Logger logger = Logger.getLogger(DnsModule.class.getName());
    private final Runner runner;
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final AtomicInteger waitingFor = new AtomicInteger(-1);

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
            Message message = new Message(packet.getData());
            if (message.header.id == waitingFor.get()) {
                System.out.println("GOT RESPONSE FROM SERVER: " + message);
            } else {
                synchronized (waitingFor) {
                    waitingFor.set(message.header.id);
                }

                System.out.println("SENDING OUT: " + message);

                try {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    message.write(out);
                    DatagramPacket outgoing = new DatagramPacket(out.toByteArray(), out.size(), InetAddress.getByName("8.8.8.8"), 53);
                    socket.send(outgoing);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Failed sending packet.", ex);
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
