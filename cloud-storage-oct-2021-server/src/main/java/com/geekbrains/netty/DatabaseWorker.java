package com.geekbrains.netty;

import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class DatabaseWorker {
    private Connection connection;

    private final String url = "jdbc:postgresql://localhost:5432/cloud-db";
    private final String username = "postgres";
    private final String password = "root";
    private StringBuilder sb;
    private String userID = null;

    public DatabaseWorker() {
        initConnection();
        sb = new StringBuilder();

    }

    private void initConnection() {
        try {
            connection = DriverManager.getConnection(url, username, password);
            log.debug("Connection succeed");
        } catch (SQLException e) {
            log.debug("Unable to establish connection to DB");
        }
    }

    public String registration(String username, String password) throws SQLException {
        ResultSet rs = findUser(username, password);
        if (!rs.next()) {
            PreparedStatement registration = connection.prepareStatement("INSERT INTO users.log_table (user_name, password) VALUES (?, ?);");
            registration.setString(1, username);
            registration.setString(2, password);
            registration.executeUpdate();
            return String.valueOf(getUserId(username, password));
        } else {
            return userID;
        }
    }

    public String login(String username, String password) throws SQLException {
        ResultSet rs = findUser(username, password);
        if (rs.next()) {
            if (rs.getString(2).equals(username) && rs.getString(3).equals(password)) {
                return String.valueOf(rs.getInt(1));
            }
        }return userID;
    }

    private ResultSet findUser(String username, String password) throws SQLException {
        PreparedStatement findUser = connection.prepareStatement("select * from users.log_table where user_name LIKE ? and password LIKE ?;");
        findUser.setString(1, username);
        findUser.setString(2, password);
        return findUser.executeQuery();

    }

    private int getUserId(String username, String password) throws SQLException {
        PreparedStatement findId = connection.prepareStatement("select log_table.user_id from users.log_table where user_name LIKE ? and password LIKE ?");
        findId.setString(1, username);
        findId.setString(2, password);
        ResultSet rs = findId.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        } else {
            return 0;
        }
    }


}



