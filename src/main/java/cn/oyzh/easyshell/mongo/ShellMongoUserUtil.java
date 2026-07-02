package cn.oyzh.easyshell.mongo;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.List;

/**
 *
 * @author oyzh
 * @since 2026-07-02
 */
public class ShellMongoUserUtil {

    public static Document getUsers(MongoDatabase database) {
        Document command = new Document("usersInfo", 1);
        return database.runCommand(command);
    }

    public static Document getUser(MongoDatabase database, String username) {
        Document command = new Document("usersInfo",
                new Document("user", username).append("db", database.getName())
        );
        return database.runCommand(command);
    }

    public static Document dropUser(MongoDatabase database, String username) {
        Document command = new Document("dropUser", username);
        return database.runCommand(command);
    }

    public static Document createUser(MongoDatabase database, String user, String password,
                                       List<Document> roles) {
        Document command = new Document("createUser", user)
                .append("pwd", password)
                .append("roles", roles);
        return database.runCommand(command);
    }
}
