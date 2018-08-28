package com.gianlu.internethacker;

import com.gianlu.internethacker.hackers.ProxyHacker;
import com.gianlu.internethacker.hackers.ProxyHttpHacker;
import com.gianlu.internethacker.hackers.ProxyHttpUrlHacker;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProxyModule implements Closeable, Module {
    private static final Logger logger = Logger.getLogger(ProxyModule.class.getName());
    private static final byte[] EOL = new byte[]{'\r', '\n'};
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final int port;
    private final ProxyHttpHacker[] hackers;
    private Runner runner;

    public ProxyModule(int port, ProxyHttpHacker[] hackers) {
        this.port = port;
        this.hackers = hackers;

        for (ProxyHttpHacker hacker : hackers)
            if (hacker instanceof ProxyHttpUrlHacker && hackers.length > 1)
                throw new IllegalStateException("ProxyHttpUrlHacker should be the only hacker.");
    }

    @Override
    public void close() throws IOException {
        if (runner != null) runner.close();
    }

    @Override
    public void start() throws IOException {
        this.runner = new Runner(port);
        new Thread(runner).start();

        logger.info("Proxy server started on port " + port + "!");
    }

    private class ServingClient implements Runnable {
        private final Socket client;
        private final InputStream clientIn;
        private final OutputStream clientOut;
        private Socket server;
        private InputStream serverIn;
        private OutputStream serverOut;

        ServingClient(Socket client) throws IOException {
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
            boolean modifiedUrl = false;

            List<ProxyHttpHacker> interestedHackers = new ArrayList<>();
            for (ProxyHacker hacker : hackers) {
                if (((ProxyHttpHacker) hacker).interceptRequest(method, url)) {
                    if (hacker instanceof ProxyHttpUrlHacker) {
                        url = ((ProxyHttpUrlHacker) hacker).getUrl(url);
                        modifiedUrl = true;
                        break;
                    }

                    if (((ProxyHttpHacker) hacker).interceptRequest(method, url))
                        interestedHackers.add((ProxyHttpHacker) hacker);
                }
            }

            int port = url.getPort();
            if (port == -1) port = url.getDefaultPort();
            createServerSocket(url.getHost(), port);

            String path = url.getPath();
            if (path.isEmpty()) path = "/";

            serverOut.write(method.getBytes());
            serverOut.write(' ');
            serverOut.write(path.getBytes());
            serverOut.write(' ');
            serverOut.write(httpVersion.getBytes());
            serverOut.write(EOL);

            String line;
            while ((line = Utils.readLine(clientIn)) != null && !line.isEmpty()) {
                String[] header = Utils.split(line, ':');
                if (header[0].equals("Host") && modifiedUrl) {
                    header[1] = url.getHost();
                }

                serverOut.write(header[0].getBytes());
                serverOut.write(':');
                serverOut.write(header[1].trim().getBytes());
                serverOut.write(EOL);
            }

            serverOut.write(EOL);

            byte[] buffer = new byte[8192];
            int read;
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

            logger.info("Handled HTTP request to " + url);
        }

        private void handleHttps(String address, String httpVersion) throws IOException {
            int colon = address.indexOf(':');

            try {
                createServerSocket(address.substring(0, colon),
                        Integer.parseInt(address.substring(colon + 1, address.length())));
            } catch (IOException ex) {
                clientOut.write(httpVersion.getBytes());
                clientOut.write(" 500 Internal Server Error\r\n\r\n".getBytes());
                clientOut.flush();
                throw ex;
            }

            String line;
            while ((line = Utils.readLine(clientIn)) != null) {
                if (line.isEmpty()) break;
            }

            clientOut.write(httpVersion.getBytes());
            clientOut.write(" 200 OK\r\n\r\n".getBytes());
            clientOut.flush();

            logger.info("Opening HTTPS tunnel to " + address);

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
                String connectLine = Utils.readLine(clientIn);
                String[] split = Utils.split(connectLine, ' ');
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

        Runner(int port) throws IOException {
            this.serverSocket = new ServerSocket(port);
        }

        @Override
        public void run() {
            while (!shouldStop) {
                try {
                    ServingClient client = new ServingClient(serverSocket.accept());
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
