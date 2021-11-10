package com.geekbrains.netty;

import com.geekbrains.model.AbstractMessage;
import com.geekbrains.model.auth.Login;
import com.geekbrains.model.navigation.FileMessage;
import com.geekbrains.model.navigation.FileRequest;
import com.geekbrains.model.navigation.ListMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

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

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // создать для каждого клиента свою директорию!-=
        serverClientDir = Paths.get("server");
        ctx.writeAndFlush(new ListMessage(serverClientDir));
        buffer = new byte[8192];
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
        }
    }

    private void authentication(Login msg, ChannelHandlerContext ctx) {
        if (msg.getUsername().equals("admin") & msg.getPassword().equals("admin")) {
            userId="1";
            ctx.writeAndFlush(new FileRequest(userId));
        }else {
            ctx.writeAndFlush(new FileRequest(null));
        }
    }

    private void sendFile(FileRequest msg, ChannelHandlerContext ctx) throws IOException {
        boolean isFirstBatch = true;
        Path filePath = serverClientDir.resolve(msg.getName());
        System.out.println(serverClientDir.toAbsolutePath() + "\\" + msg.getName());
        System.out.println(serverClientDir.resolve(msg.getName()));
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

