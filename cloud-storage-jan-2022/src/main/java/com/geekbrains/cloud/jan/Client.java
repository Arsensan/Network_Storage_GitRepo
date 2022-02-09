package com.geekbrains.cloud.jan;

import com.geekbrains.cloud.jan.model.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

@Slf4j
public class Client implements Initializable {

    public TextArea clientCurrentDir;
    public TextArea serverCurrentDir;
    public ListView<String> clientView;
    public ListView<String> serverView;
    private Path clientDir;
    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;


    // read from network
    private void readLoop() {
        try {
            while (true) {
                CloudMessage message = (CloudMessage) is.readObject();
                log.info("received: {}", message);
                switch (message.getType()) {
                    case FILE:
                        processFileMessage((FileMessage) message);
                        break;
                    case LIST:
                        processListMessage((ListMessage) message);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processListMessage(ListMessage message) {
        Platform.runLater(() -> {
            serverView.getItems().clear();
            serverView.getItems().addAll(message.getFiles());
            serverCurrentDir.setText(message.getServerDir());

        });
    }


    private void processFileMessage(FileMessage message) throws IOException {
        Files.write(clientDir.resolve(message.getFileName()), message.getBytes());
        Platform.runLater(this::updateClientView);
    }

    private void updateClientView() {
        try {
            clientView.getItems().clear();
            Files.list(clientDir)
                    .map(p -> p.getFileName().toString())
                    .forEach(f -> clientView.getItems().add(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
        showDir();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            clientDir = Paths.get("C:\\Users\\Arsen");
            clientCurrentDir.setWrapText(true);
            serverCurrentDir.setWrapText(true);
            updateClientView();
            Socket socket = new Socket("localhost", 8189);
            System.out.println("Network created...");
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            initMouseListeners();
            Thread readThread = new Thread(this::readLoop);
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initMouseListeners() {

        clientView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Path current = clientDir.resolve(getItem());
                if (Files.isDirectory(current)) {
                    clientDir = current;
                    Platform.runLater(this::updateClientView);

                }
            }
        });


        serverView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String dir = serverView.getSelectionModel().getSelectedItem();
                if (dir != null) {
                    try {
                        os.writeObject(new FileRequest(dir));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }


    public void showDir() {
        Path curr = clientDir.normalize();
        clientCurrentDir.setText(curr.toString());
    }


    public void clientFolderReturn(ActionEvent actionEvent) {
        Path current = clientDir.resolve("..");
        if (Files.isDirectory(current)) {
            clientDir = current;
            Platform.runLater(this::updateClientView);

        }

    }

    public void serverFolderReturn(ActionEvent actionEvent) throws IOException {
        os.writeObject(new MoveBack(".."));
    }


    private String getItem() {
        return clientView.getSelectionModel().getSelectedItem();
    }

    public void upload(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        os.writeObject(new FileMessage(clientDir.resolve(fileName)));
    }


    public void download(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        os.writeObject(new FileRequest(fileName));
    }

    public void deleteClientFile(ActionEvent actionEvent) throws IOException {
        String server = serverView.getSelectionModel().getSelectedItem();
        if(server != null) {
            os.writeObject(new DeleteFile(Paths.get(server)));
        }
        Path client = clientDir.resolve(getItem());
        if (Files.isDirectory((client)) | Files.isRegularFile((client))) {
            Files.deleteIfExists((client));
            Platform.runLater(this::updateClientView);
    }
        }

    }




