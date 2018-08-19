package com.gianlu.internethacker;

import com.sun.istack.internal.NotNull;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProxyModule implements Closeable {
    private static final Logger logger = Logger.getLogger(ProxyModule.class.getName());
    private final Runner runner;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public ProxyModule(int port) throws IOException {
        this.runner = new Runner(port);
        new Thread(runner).start();
    }

    /**
     * Reads a line until '\r\n', the EOF bytes aren't included.
     *
     * @param in the {@link InputStream} to read from.
     * @return a line from the input stream until '\r\n'
     * @throws IOException if an I/O error occurs.
     */
    @NotNull
    private static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        boolean lastWasR = false;
        int read;
        while ((read = in.read()) != -1) {
            if (read == '\r') {
                lastWasR = true;
                continue;
            } else if (read == '\n' && lastWasR) {
                break;
            }

            buffer.write(read);
        }

        return buffer.toString();
    }

    /**
     * Split a string without using Regex
     */
    @NotNull
    private static String[] split(@NotNull String str, char c) {
        int size = 1;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c) size++;
        }

        String tmp = str;
        String[] split = new String[size];
        for (int j = size - 1; j >= 0; j--) {
            int i = tmp.lastIndexOf(c);
            if (i == -1) {
                split[j] = tmp;
            } else {
                split[j] = tmp.substring(i + 1, tmp.length());
                tmp = tmp.substring(0, i);
            }
        }

        return split;
    }

    @Override
    public void close() throws IOException {
        runner.close();
    }

    private class ServingClient implements Runnable {
        private final Socket client;
        private final InputStream clientIn;
        private final OutputStream clientOut;
        private Socket server;
        private InputStream serverIn;
        private OutputStream serverOut;

        public ServingClient(Socket client) throws IOException {
            this.client = client;
            this.clientIn = client.getInputStream();
            this.clientOut = client.getOutputStream();
        }

        @Override
        public String toString() {
            return "ServingClient{" +
                    "client=" + client +
                    '}';
        }

        private void createServerSocket(String host, int port) throws IOException {
            server = new Socket(host, port);
            serverIn = server.getInputStream();
            serverOut = server.getOutputStream();
        }

        private void handleHttp(String method, String address, String httpVersion) throws IOException {
            URL url = new URL(address);
            int port = url.getPort();
            if (port == -1) port = url.getDefaultPort();
            createServerSocket(url.getHost(), port);

            byte[] buffer = new byte[8192];
            int read;
            serverOut.write((method + ' ' + url.getPath() + ' ' + httpVersion).getBytes());
            while (clientIn.available() > 0 && (read = clientIn.read(buffer)) != -1) {
                serverOut.write(buffer, 0, read);
            }

            while ((read = serverIn.read(buffer)) != -1) {
                clientOut.write(buffer, 0, read);
            }

            clientOut.close();
            serverOut.close();
            clientIn.close();
            serverIn.close();
        }

        private void handleHttps(String address, String httpVersion) throws IOException {
            int colon = address.indexOf(':');
            createServerSocket(address.substring(0, colon),
                    Integer.parseInt(address.substring(colon + 1, address.length())));

            String line;
            while ((line = readLine(clientIn)) != null) {
                if (line.isEmpty()) break;
            }

            clientOut.write((httpVersion + " 200 OK\r\n\r\n").getBytes());
            clientOut.flush();

            executorService.execute(new ClientToServer());

            byte[] buffer = new byte[8192];
            int read;
            while ((read = serverIn.read(buffer)) >= 0) {
                if (read > 0) clientOut.write(buffer, 0, read);
            }
        }

        @Override
        public void run() {
            try {
                String connectLine = readLine(clientIn);
                logger.info("New connection: " + connectLine);

                String[] split = split(connectLine, ' ');
                String method = split[0];
                if (method.equals("CONNECT")) handleHttps(split[1], split[2]);
                else handleHttp(method, split[1], split[2]);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Failed reading request.", ex);
            }
        }

        private class ClientToServer implements Runnable {

            @Override
            public void run() {
                try {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = clientIn.read(buffer)) >= 0) {
                        if (read > 0) serverOut.write(buffer, 0, read);
                    }
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Failed forwarding client data to server.", ex);
                }
            }
        }
    }

    private class Runner implements Runnable, Closeable {
        private final ServerSocket serverSocket;
        private volatile boolean shouldStop = false;

        public Runner(int port) throws IOException {
            this.serverSocket = new ServerSocket(port);
        }

        @Override
        public void run() {
            while (!shouldStop) {
                try {
                    ServingClient client = new ServingClient(serverSocket.accept());
                    logger.info("New client " + client);
                    executorService.execute(client);
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, "Failed accepting socket.", ex);
                }
            }
        }

        @Override
        public void close() throws IOException {
            shouldStop = true;
            serverSocket.close();
        }
    }
}
