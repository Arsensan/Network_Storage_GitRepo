package com.geekbrains.cloud.jan;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class Client implements Initializable {
    private static final int SIZE = 256;
    public final static int MOUSEEVENT_PRIMARY_BUTTON = 1;
    public Button upl;
    public Button downl;
    public Button del;
    public Button refr;
    public ListView<String> serverView;
    public ListView<String> clientView;
    private DataInputStream is;
    private DataOutputStream os;
    private Path path;
    private byte[] buff;



    private void readLoop() {
        try {
            while (true) {
                String command = is.readUTF(); // wait message
                System.out.println("received: " + command);
                if (command.equals("#list#")) {
                    Platform.runLater(() -> serverView.getItems().clear());
                    serverView.getItems().clear();
                    int fileCount = is.readInt();
                    for (int i = 0; i < fileCount; i++) {
                        String fileName = is.readUTF();
                        Platform.runLater(() -> serverView.getItems().add(fileName));
                    }
                } else if (command.equals("#file#")) {
                    String fileName = is.readUTF();
                    System.out.println("received: " + fileName);
                    Long size = is.readLong();
                    try (OutputStream fileOS = new FileOutputStream((path.resolve(fileName).toFile()))) {
                        for (int i = 0; i < (size + SIZE - 1); i++) {
                            int readBytes = is.read(buff);
                            fileOS.write(buff, 0, readBytes);
                        }
                    }
                    Platform.runLater(this::updateClientView);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateClientView() {
        try {
            clientView.getItems().clear();
            Files.list(path)
                    .map(p -> p.getFileName().toString())
                    .forEach(f -> clientView.getItems().add(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
            public void initialize (URL location, ResourceBundle resources){
                try {
                    buff = new byte[SIZE];
                    path = Paths.get("C:\\Users\\Arsen\\Desktop\\Java\\cloud-storage-jan-2022-1-master\\data\\clientData");
                    updateClientView();
                    Socket socket = new Socket("localhost", 8189);
                    System.out.println("Network created...");
                    is = new DataInputStream(socket.getInputStream());
                    os = new DataOutputStream(socket.getOutputStream());
                    Thread readThread = new Thread (this::readLoop);
                    readThread.setDaemon(true);
                    readThread.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public void upload (ActionEvent actionEvent) throws IOException {
                String fileName = clientView.getSelectionModel().getSelectedItem();
                os.writeUTF("#file#");
                os.writeUTF(fileName);
                Path file = path.resolve(fileName);
                long size = Files.size(file);
                byte[] bytes = Files.readAllBytes(file);
                os.writeLong(size);
                os.write(bytes);
                os.flush();

            }


            public void download (ActionEvent actionEvent) throws IOException {
                String fileName = serverView.getSelectionModel().getSelectedItem();
                os.writeUTF("#get_file#");
                os.writeUTF(fileName);
                os.flush();
            }
        }






