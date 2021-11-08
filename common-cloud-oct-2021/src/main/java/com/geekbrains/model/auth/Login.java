package com.geekbrains.model.auth;

import com.geekbrains.model.AbstractMessage;
import com.geekbrains.model.CommandType;
import lombok.Getter;
import lombok.ToString;


@Getter
@ToString
public class Login extends AbstractMessage {


    private final String username;
    private final String password;

    public Login(String username, String password) {
        this.username = username;
        this.password = password;
        setType(CommandType.LOGIN);
    }
}


