package com.geekbrains.cloud.jan.model;

import lombok.Data;

import java.nio.file.Path;

@Data
public class FileRequest implements CloudMessage {

    private final String fileName;

    public FileRequest(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public CommandType getType() {
        return CommandType.FILE_REQUEST;
    }
}
