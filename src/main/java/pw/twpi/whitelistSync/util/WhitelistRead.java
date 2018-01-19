/*
 * Copyright 2018 TWPI.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pw.twpi.whitelistSync.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonParseException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import pw.twpi.whitelistSync.WhitelistSync;
import pw.twpi.whitelistSync.model.WhitelistUser;

/**
 * @author PotatoSauceVFX <rj@potatosaucevfx.com>
 */
public class WhitelistRead {

    private static JsonParser parser = new JsonParser();

    // Get whitelisted uuids as a string array list
    public static ArrayList getWhitelistUUIDs() {
        ArrayList<String> uuids = new ArrayList<String>();
        // OMG ITS A LAMBDA EXPRESSION!!! :D
        getWhitelistJson().forEach(emp -> parseToList(emp.getAsJsonObject(), uuids, "uuid"));
        return uuids;
    }

    // Get whitelisted usernames as a string array list
    public static ArrayList getWhitelistNames() {
        ArrayList<String> names = new ArrayList<String>();
        // WOAH ITS A LAMBDA EXPRESSION!!! CRAZY COMPLEX STUFF GOIN ON RIGHT HERE!!! :D
        getWhitelistJson().forEach(emp -> parseToList(emp.getAsJsonObject(), names, "name"));
        return names;
    }

    // Get Arraylist of whitelisted users on server.
    public static ArrayList<WhitelistUser> getWhitelistUsers() {
        ArrayList<WhitelistUser> users = new ArrayList<WhitelistUser>();
        // HOLY SHIT.. ANOTHER LAMBDA EXPRESSION!!!!
        getWhitelistJson().forEach((user) -> {
            String uuid = ((JsonObject) user).get("uuid").toString();
            String name = ((JsonObject) user).get("name").toString();
            users.add(new WhitelistUser(uuid, name));
        });
        return users;
    }

    private static void parseToList(JsonObject whitelist, List list, String key) {
        list.add(whitelist.get(key).getAsString());
    }

    private static JsonArray getWhitelistJson() {
        JsonArray whitelist = null;
        try {
            whitelist = (JsonArray) parser.parse(new FileReader(WhitelistSync.SERVER_FILEPATH + "/whitelist.json"));
        } catch (FileNotFoundException e) {
            WhitelistSync.logger.error("Whitelist.json file not found! :O\n" + e.getMessage());
        } catch (JsonParseException e) {
            WhitelistSync.logger.error("Whitelist.json parse error!! D:\n" + e.getMessage());
        }
        WhitelistSync.logger.debug("getWhitelistJson returned an array of " + whitelist.size() + " entries.");
        return whitelist;
    }

}
