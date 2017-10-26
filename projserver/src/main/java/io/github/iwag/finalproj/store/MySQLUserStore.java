package io.github.iwag.finalproj.store;

import io.github.iwag.finalproj.models.entities.ExUserEntity;
import io.github.iwag.finalproj.models.entities.ProfileEntity;
import io.github.iwag.finalproj.models.entities.UserEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jvnet.hk2.annotations.Service;

import java.sql.*;
import java.time.LocalDate;

@Service
public class MySQLUserStore implements UserStore {
    private Connection connection;
    final Logger logger = LogManager.getLogger(getClass());

    public MySQLUserStore() throws SQLException {
        String port = System.getenv("MYSQL_PORT");
        port = port == null ? "53306" : port;
        String database = System.getenv("MYSQL_DATABASE");
        database = database == null ? "test" : database;
        String user = System.getenv("MYSQL_USER");
        user = user == null ? "test" : user;
        String password = System.getenv("MYSQL_PASSWORD");
        password = password == null ? "test" : password;

        String host = "jdbc:mysql://" + "localhost:" + port + "/" + database;

         connection = DriverManager.getConnection(host, user, password);

        Statement stmt = connection.createStatement();
//        stmt.executeUpdate("DROP TABLE IF NOT EXISTS User");
        stmt.executeUpdate("CREATE TABLE  IF NOT EXISTS User (`id` int not null auto_increment, `first_name` text," +
                " `last_name` text, `country` text, `username` text, `password` text, PRIMARY KEY (`id`));");
    }

    @Override
    public ExUserEntity addUser(UserEntity ue) {

        ExUserEntity ex = null;
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate("INSERT INTO User (first_name,last_name,country,username,password) VALUES(" + ue.getInsertValues() + ")");
            ResultSet rs = stmt.executeQuery("SELECT * from User where id=LAST_INSERT_ID();");
            if (rs.next()) {
                ex = new ExUserEntity(rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("country"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getInt("id"));
            }
        } catch (SQLException e) {
            logger.info(e);
            return null;
        }
        return ex;
    }

    @Override
    public ProfileEntity loginUser(String username, String password, LocalDate date, String auth) {
        ProfileEntity pe = null;
        try {
        Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * from User where username='" + username+"';");

            if (rs.next()) {
                if ( !rs.getString("password").equals(password) ) {
                    logger.info("password invalid " + username);
                    return null;
                }
                UserEntity ue = new UserEntity(rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("country"),
                        rs.getString("username"),
                        rs.getString("password"));

                pe =  Stores.userStore.newAuth(ue, rs.getInt("id"));

            } else {
                logger.info("not found" + rs);
                return null;
            }
        } catch (SQLException e) {
            logger.info("exp:" + e);

            return null;
        }
        return pe;
    }


}