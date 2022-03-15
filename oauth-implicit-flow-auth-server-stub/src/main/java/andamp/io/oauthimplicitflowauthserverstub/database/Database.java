package andamp.io.oauthimplicitflowauthserverstub.database;

import java.util.LinkedList;
import java.util.List;

public class Database {

    private static List<String> users = new LinkedList<>();


    private static boolean storeUser(String user){
        return users.add(user);
    }

}
