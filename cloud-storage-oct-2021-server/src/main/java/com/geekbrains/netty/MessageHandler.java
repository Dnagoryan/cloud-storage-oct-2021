package com.geekbrains.netty;

import com.geekbrains.model.AbstractMessage;
import com.geekbrains.model.auth.Login;
import com.geekbrains.model.auth.Registration;
import com.geekbrains.model.navigation.FileDelete;
import com.geekbrains.model.navigation.FileMessage;
import com.geekbrains.model.navigation.FileRequest;
import com.geekbrains.model.navigation.ListMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    private Path serverClientDir;
    private byte[] buffer;
    private String userId;
    private final String ROOT_DIR = "server";
    DatabaseWorker dbWorker;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        buffer = new byte[8192];
        dbWorker=new DatabaseWorker();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage msg) throws Exception {
        log.debug("Start processing {}", msg);
        switch (msg.getType()) {
            case FILE_MESSAGE:
                processFile((FileMessage) msg, ctx);
                break;
            case FILE_REQUEST:
                sendFile((FileRequest) msg, ctx);
                break;
            case LOGIN:
                authentication((Login) msg, ctx);
                break;
            case REGISTRATION:
                registration((Registration) msg, ctx);
                break;
            case FILE_DELETE:
                fileDelete((FileDelete)msg,ctx);
        }
    }

    private void fileDelete(FileDelete msg, ChannelHandlerContext ctx) throws Exception {
        File delete = new File(serverClientDir + "\\" + msg.getName());
        delete.delete();
        ctx.writeAndFlush(new ListMessage(serverClientDir));

    }

    private void registration(Registration msg, ChannelHandlerContext ctx) throws Exception {
        userId = dbWorker.registration(msg.getUsername(), msg.getPassword());
        log.debug(userId);
        if (userId != null){
            ctx.writeAndFlush(new Registration(msg.getUsername(), msg.getPassword(), userId));
            createClientDir(msg.getUsername());
            ctx.writeAndFlush(new ListMessage(serverClientDir));
        }
    }

    private void authentication(Login msg, ChannelHandlerContext ctx) throws Exception {
            userId=dbWorker.login(msg.getUsername(), msg.getPassword());
        if (userId!=null) {
            ctx.writeAndFlush(new Login(msg.getUsername(), msg.getPassword(), userId));
            createClientDir(msg.getUsername());
            ctx.writeAndFlush(new ListMessage(serverClientDir));
        } else ctx.writeAndFlush(new Login());
    }

    private void createClientDir(String username) {

        serverClientDir = Paths.get(ROOT_DIR + "\\" + username);
        if (!Files.exists(serverClientDir)) {
            try {
                Files.createDirectories(serverClientDir);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void sendFile(FileRequest msg, ChannelHandlerContext ctx) throws IOException {
        boolean isFirstBatch = true;

        Path filePath = serverClientDir.resolve(msg.getName());
        long size = Files.size(filePath);
        try (FileInputStream is = new FileInputStream(filePath.toFile())) {
            int read;
            while ((read = is.read(buffer)) != -1) {
                FileMessage message = FileMessage.builder()
                        .bytes(buffer)
                        .name(filePath.getFileName().toString())
                        .size(size)
                        .isFirstBatch(isFirstBatch)
                        .isFinishBatch(is.available() <= 0)
                        .endByteNum(read)
                        .build();
                ctx.writeAndFlush(message);
                isFirstBatch = false;
            }
            log.debug("package sent!!!");
        } catch (Exception e) {
            log.error("e:", e);
        }


    }

    private void processFile(FileMessage msg, ChannelHandlerContext ctx) throws Exception {
        Path file = serverClientDir.resolve(msg.getName());

        if (msg.isFirstBatch()) {
            Files.deleteIfExists(file);
        }

        try (FileOutputStream os = new FileOutputStream(file.toFile(), true)) {
            os.write(msg.getBytes(), 0, msg.getEndByteNum());
        }

        if (msg.isFinishBatch()) {
            ctx.writeAndFlush(new ListMessage(serverClientDir));
        }
    }
}

