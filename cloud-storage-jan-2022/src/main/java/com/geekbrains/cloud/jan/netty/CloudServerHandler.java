package com.geekbrains.cloud.jan.netty;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.geekbrains.cloud.jan.model.*;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.scene.control.TextArea;

public class CloudServerHandler extends SimpleChannelInboundHandler<CloudMessage> {

    private Path currentDir;


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // init client dir
        currentDir = Paths.get("data");
        sendList(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CloudMessage cloudMessage) throws Exception {
        switch (cloudMessage.getType()) {
            case FILE_REQUEST:
                processFileRequest((FileRequest) cloudMessage, ctx);
                break;
            case FILE:
                processFileMessage((FileMessage) cloudMessage);
                sendList(ctx);
                break;
            case MOVE_BACK:
                moveBackProcess((MoveBack) cloudMessage, ctx);
                break;
            case DELETE:
                deleteFileProcess((DeleteFile) cloudMessage, ctx);
                break;
        }
    }


    private void sendList(ChannelHandlerContext ctx) throws IOException {
        ctx.writeAndFlush(new ListMessage(currentDir));
    }

    private void processFileMessage(FileMessage cloudMessage) throws IOException {
        Files.write(currentDir.resolve(cloudMessage.getFileName()), cloudMessage.getBytes());
    }

    private void processFileRequest(FileRequest cloudMessage, ChannelHandlerContext ctx) throws IOException {
        Path path = currentDir.resolve(cloudMessage.getFileName());
        if (Files.isDirectory(path)) {
            currentDir = path;
            ctx.writeAndFlush(new ListMessage(currentDir));
        } else
            ctx.writeAndFlush(new FileMessage(path));
    }

    private void moveBackProcess(MoveBack cloudMessage, ChannelHandlerContext ctx) throws IOException {
        Path current = currentDir.resolve(cloudMessage.getServerDir());
        if (Files.isDirectory(current)) {
            currentDir = current;
            ctx.writeAndFlush(new ListMessage(currentDir));
        }
    }

    private void deleteFileProcess(DeleteFile cloudMessage, ChannelHandlerContext ctx) throws IOException {
        Path path = currentDir.resolve(cloudMessage.getFileToDelete());
        if (Files.isDirectory(path) | Files.isRegularFile(path)) {
            Files.deleteIfExists(path);
            ctx.writeAndFlush(new ListMessage(currentDir));
        }
    }
}





