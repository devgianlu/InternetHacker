package com.gianlu.internethacker;

import com.gianlu.internethacker.hackers.DnsHacker;
import com.gianlu.internethacker.io.DnsOutputStream;
import com.gianlu.internethacker.models.DnsHeader;
import com.gianlu.internethacker.models.DnsMessage;

import java.io.Closeable;
import java.io.IOException;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DnsModule implements Closeable, Module {
    private static final Logger logger = Logger.getLogger(DnsModule.class.getName());
    private static final SocketAddress DEFAULT_SERVER;
    private static final long MAX_TIMEOUT = TimeUnit.SECONDS.toMillis(5);

    static {
        try {
            DEFAULT_SERVER = new InetSocketAddress(InetAddress.getByName("8.8.8.8"), 53);
        } catch (UnknownHostException ex) {
            throw new RuntimeException("Default DNS server not found.", ex);
        }
    }

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Map<Short, RecipientHolder> ownIdToRecipient = new ConcurrentHashMap<>();
    private final AtomicReference<Short> ownIdCounter = new AtomicReference<>(Short.MIN_VALUE);
    private final int port;
    private final DnsHacker[] hackers;
    private Runner runner = null;

    public DnsModule(int port, DnsHacker[] hackers) {
        this.port = port;
        this.hackers = hackers;
    }

    @Override
    public void close() {
        if (runner != null)
            runner.close();
    }

    @Override
    public void start() throws IOException {
        this.runner = new Runner(port);
        new Thread(runner).start();

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() ->
                        ownIdToRecipient.values().removeIf(recipientHolder ->
                                System.currentTimeMillis() - recipientHolder.timestamp > MAX_TIMEOUT),
                0, MAX_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    public static class RecipientHolder {
        private final SocketAddress address;
        private final short originalId;
        private final long timestamp;

        RecipientHolder(DnsHeader header, DatagramPacket packet) {
            this.originalId = header.id;
            this.address = packet.getSocketAddress();
            this.timestamp = System.currentTimeMillis();
        }

        void sendBack(DnsMessage message, DatagramSocket socket) throws IOException {
            message = message.buildUpon()
                    .setHeader(message.header.buildUpon()
                            .setID(originalId)
                            .build())
                    .build();

            DnsOutputStream out = message.write();
            DatagramPacket packet = new DatagramPacket(out.toByteArray(), out.size(), address);
            socket.send(packet);
        }
    }

    private class ServingClient implements Runnable {
        private final DatagramSocket socket;
        private final DatagramPacket packet;

        ServingClient(DatagramSocket socket, DatagramPacket packet) {
            this.socket = socket;
            this.packet = packet;
        }

        @Override
        public void run() {
            DnsMessage message = new DnsMessage(packet.getData());
            RecipientHolder recipient = ownIdToRecipient.remove(message.header.id);

            if (recipient != null) {
                for (DnsHacker hacker : hackers) {
                    if (hacker.interceptAnswerMessage(message))
                        message = hacker.hackDnsAnswerMessage(message);
                }

                try {
                    recipient.sendBack(message, socket);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Failed sending packet to client.", ex);
                }
            } else {
                short ownId;
                synchronized (ownIdCounter) {
                    ownId = ownIdCounter.getAndUpdate(aShort -> {
                        if (aShort == Short.MAX_VALUE) return Short.MIN_VALUE;
                        else return ++aShort;
                    });
                }

                recipient = new RecipientHolder(message.header, packet);
                ownIdToRecipient.put(ownId, recipient);

                message = message.buildUpon()
                        .setHeader(message.header.buildUpon()
                                .setID(ownId)
                                .build())
                        .build();

                try {
                    DnsOutputStream out = message.write();
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

        Runner(int port) throws IOException {
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
