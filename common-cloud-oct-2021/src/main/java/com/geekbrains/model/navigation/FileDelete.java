package com.geekbrains.model.navigation;

import com.geekbrains.model.AbstractMessage;
import com.geekbrains.model.CommandType;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class FileDelete extends AbstractMessage {
    private final String name;

    public FileDelete(String name) {
        this.name = name;
        setType(CommandType.FILE_DELETE);
    }
}
