package com.geekbrains.model.auth;

import com.geekbrains.model.AbstractMessage;
import com.geekbrains.model.CommandType;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Registration extends AbstractMessage {
    private final String username;
    private final String password;
    private  final String userId;

    public Registration(String username, String password,String userId) {
        this.username = username;
        this.password = password;
        this.userId=userId;

        setType(CommandType.REGISTRATION);
    }
}
