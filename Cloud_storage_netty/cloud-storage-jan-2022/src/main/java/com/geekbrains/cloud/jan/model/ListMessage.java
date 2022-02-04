package com.geekbrains.cloud.jan.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;

@Data
public class ListMessage implements CloudMessage {

    private final List<String> files;
    private final String serverDir;

    public ListMessage(Path path) throws IOException {
        files = Files.list(path)
                .map(p -> p.getFileName().toString())
                .collect(Collectors.toList());
        serverDir = path.normalize().toString();
    }

    @Override
    public CommandType getType() {
        return CommandType.LIST;
    }
}
