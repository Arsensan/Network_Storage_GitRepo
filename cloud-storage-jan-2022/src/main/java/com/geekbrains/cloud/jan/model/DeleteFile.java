package com.geekbrains.cloud.jan.model;

import lombok.Data;

import java.nio.file.Path;

@Data
public class DeleteFile implements CloudMessage {

        private final String fileToDelete;

    public DeleteFile(Path path) {
        fileToDelete = path.getFileName().toString();
    }

    @Override
        public CommandType getType() {
            return CommandType.DELETE;
        }
    }

