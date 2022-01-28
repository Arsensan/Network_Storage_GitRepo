package com.geekbrains.cloud.jan;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Handler implements Runnable {

    private final DataInputStream is;
    private final DataOutputStream os;
    private final Path path;
    private static final int SIZE = 256;
    private final byte[] buff;

    public Handler(Socket socket) throws IOException {
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        path = Paths.get("C:\\Users\\Arsen\\Desktop\\Java\\cloud-storage-jan-2022-1-master\\data\\serverData");
        buff = new byte[SIZE];
        sendServerFiles();
    }

    public void sendServerFiles() throws IOException {
        List<String> files = Files.list(path)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        os.writeUTF("#list#");
        os.writeInt(files.size());
        for (String file : files) {
            os.writeUTF(file);
        }
os.flush();
    }


    @Override
    public void run() {
        try {
            while (true) {
                String command = is.readUTF();
                System.out.println("received: " + command);
                if (command.equals("#file#")) {
                    String fileName = is.readUTF();
                    System.out.println("received: " + fileName);
                    Long size = is.readLong();
                    try (OutputStream fileOS = new FileOutputStream((path.resolve(fileName).toFile()))) {
                        for (int i = 0; i < (size + SIZE - 1)/ SIZE; i++) {
                            int readBytes = is.read(buff);
                            fileOS.write(buff, 0, readBytes);
                        }
                    }
                    sendServerFiles();
                } else if (command.equals("#get_file#")) {
                    String fileName = is.readUTF();
                    os.writeUTF("#file#");
                    Path file = path.resolve(fileName);
                    long size = Files.size(file);
                    byte[] bytes = Files.readAllBytes(file);
                    os.writeLong(size);
                    os.write(bytes);
                    os.writeUTF(fileName);
                    os.flush();
                }
                os.writeUTF(command);
                os.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
