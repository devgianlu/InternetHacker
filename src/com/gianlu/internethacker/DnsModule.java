package com.gianlu.internethacker;

import com.gianlu.internethacker.models.Message;

import java.io.Closeable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DnsModule implements Closeable {
    private final Runner runner;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public DnsModule(int port) throws IOException {
        this.runner = new Runner(port);
        new Thread(runner).start();
    }

    public static String toBitString(final byte[] b) {
        final char[] bits = new char[8 * b.length];
        for (int i = 0; i < b.length; i++) {
            final byte byteval = b[i];
            int bytei = i << 3;
            int mask = 0x1;
            for (int j = 7; j >= 0; j--) {
                final int bitval = byteval & mask;
                if (bitval == 0) {
                    bits[bytei + j] = '0';
                } else {
                    bits[bytei + j] = '1';
                }
                mask <<= 1;
            }
        }
        return String.valueOf(bits);
    }

    @Override
    public void close() throws IOException {
        runner.close();
    }

    private class ServingClient implements Runnable {
        private final DatagramPacket packet;

        public ServingClient(DatagramPacket packet) {
            this.packet = packet;
        }

        @Override
        public void run() {
            byte[] data = packet.getData();
            System.out.println(toBitString(data));

            Message message = new Message(data);
            System.out.println(message);
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
                    executorService.execute(new ServingClient(packet));
                } catch (IOException ex) {
                    ex.printStackTrace();  // TODO
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
