package com.geekbrains.cloud.jan.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public class NioServer {

    private final ServerSocketChannel serverSocketChannel;
    private final Selector selector;
    private Path current;
    private ByteBuffer buf;

    public NioServer() throws IOException {

        current = Paths.get(System.getProperty("user.home"));
        serverSocketChannel = ServerSocketChannel.open();
        buf = ByteBuffer.allocate(256);
        selector = Selector.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(8189));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (serverSocketChannel.isOpen()) {
            selector.select(); // blocking operation
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = keys.iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                if (key.isAcceptable()) {
                    processAccept();
                }
                if (key.isReadable()) {
                    processRead(key);
                }
                keyIterator.remove();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new NioServer();
    }

    private void processRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        StringBuilder s = new StringBuilder();
        while (true) {
            int readCount = channel.read(buf); // non blocking
            if (readCount == 0) {
                break;
            }
            if (readCount < 0) {
                channel.close();
                return;
            }
            buf.flip();
            while (buf.hasRemaining()) {
                s.append((char) buf.get());
            }
            buf.clear();
        }

        System.out.println("Received: " + s);
        String message = s.toString().trim();

        // transfer layer
        if (message.equals("ls")) {
            String response = getFilesInCurrentDir();
            channel.write(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
        } else {
            String response = getPrelude() + "command " + message + " not supported.\n\r";
            channel.write(ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8)));
        }
        printPrelude(channel);
    }

    private String getFilesInCurrentDir() throws IOException {
        return Files.list(current)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.joining("\n")) + "\n\r";
    }

    private String getPrelude() {
        return current.getFileName().toString() + "> ";
    }

    private void processAccept() throws IOException {
        SocketChannel channel = serverSocketChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        channel.write(ByteBuffer.wrap(
                "Welcome on Mike terminal\n\r".getBytes(StandardCharsets.UTF_8))
        );
        printPrelude(channel);
    }

    private void printPrelude(SocketChannel channel) throws IOException {
        channel.write(ByteBuffer.wrap(getPrelude().getBytes(StandardCharsets.UTF_8)));
    }
}
