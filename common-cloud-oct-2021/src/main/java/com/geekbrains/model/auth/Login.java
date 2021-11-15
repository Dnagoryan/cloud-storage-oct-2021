package com.geekbrains.model.auth;

import com.geekbrains.model.AbstractMessage;
import com.geekbrains.model.CommandType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Getter
@ToString
public class Login extends AbstractMessage {


    private  String username;
    private  String password;
    private   String userId;

    public Login(String username,
                 String password,
                 String userId) {
        this.username = username;
        this.password = password;
        this.userId=userId;
        setType(CommandType.LOGIN);
    }

    public Login(){
        setType(CommandType.LOGIN);
}
}


