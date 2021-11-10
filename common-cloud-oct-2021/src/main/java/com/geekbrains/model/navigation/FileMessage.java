package com.geekbrains.model.navigation;

import com.geekbrains.model.AbstractMessage;
import com.geekbrains.model.CommandType;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;



@Getter
@Builder
@ToString
public class FileMessage extends AbstractMessage {

    private static final int BATCH_SIZE = 8192;

    private final String name;
    private final long size;
    private final byte[] bytes;
    private final boolean isFirstBatch;
    private final int endByteNum;
    private final boolean isFinishBatch;



    public FileMessage(String name,
                       long size,
                       byte[] bytes,
                       boolean isFirstBatch,
                       int endByteNum,
                       boolean isFinishBatch) {
        this.name = name;
        this.size = size;
        this.bytes = bytes;
        this.isFirstBatch = isFirstBatch;
        this.endByteNum = endByteNum;
        this.isFinishBatch = isFinishBatch;
        setType(CommandType.FILE_MESSAGE);
    }
}

