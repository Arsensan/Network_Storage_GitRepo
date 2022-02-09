package com.geekbrains.cloud.jan.model;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
@Data
public class MoveBack implements CloudMessage {

        private final String serverDir;

    public MoveBack(String serverDir) {
        this.serverDir = serverDir;
    }

    @Override
        public CommandType getType() {
            return CommandType.MOVE_BACK;
        }
    }

