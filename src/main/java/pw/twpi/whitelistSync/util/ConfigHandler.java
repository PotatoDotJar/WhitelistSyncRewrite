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

import net.minecraftforge.common.config.Configuration;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;
import pw.twpi.whitelistSync.WhitelistSync;

/**
 * @author PotatoSauceVFX <rj@potatosaucevfx.com>
 */
public class ConfigHandler {

    // Custom Categories
    private final static String MYSQL_CATEGORY = "mySQL";
    private final static String SQLITE_CATEGORY = "sqlite";

    // Modes
    public static final String MODE_MYSQL = "MYSQL";
    public static final String MODE_SQLITE = "SQLITE";

    // General settings
    public static String WHITELIST_MODE = "SQLITE"; // SQLITE or MYSQL, use mode finals above.

    // sqlite config
    public static String sqliteDatabasePath = "./whitelist.db";
    public static String sqliteMode = "INTERVAL";
    public static int sqliteServerSyncTimer = 60;
    public static int sqliteServerListenerTimer = 10;

    // mySQL config
    public static String mySQL_IP = "localhost";
    public static String mySQL_PORT = "3306";
    public static String mySQL_Username = "root";
    public static String mySQL_Password = "password";

    public static void readConfig() {
        Configuration cfg = WhitelistSync.config;
        try {
            cfg.load();
            initGeneralConfig(cfg);
        } catch (Exception e1) {
            WhitelistSync.logger.error("Problem loading config file!\n" + e1.getMessage());
        } finally {
            if (cfg.hasChanged()) {
                cfg.save();
            }
        }
    }

    private static void initGeneralConfig(Configuration cfg) {
        // General Settings
        cfg.addCustomCategoryComment(CATEGORY_GENERAL, "General configuration");
        WHITELIST_MODE = cfg.getString("Whitelist Sync Mode", CATEGORY_GENERAL, MODE_SQLITE,
                "Mode for the database. Options are [MYSQL or SQLITE].");

        // Sqlite settings
        cfg.addCustomCategoryComment(SQLITE_CATEGORY, "Sqlite configuration (To enable "
                + "Sqlitee, refer to the mode setting in the general configuration).");
        sqliteDatabasePath = cfg.getString("Database Path", SQLITE_CATEGORY,
                sqliteDatabasePath, "Insert System Path for your Sqlite database file. "
                + "This will be the same for all your servers you want to sync!");

        sqliteMode = cfg.getString("Sqlite Sync Mode", SQLITE_CATEGORY, sqliteMode,
                "Mode for how the database updates."
                + " INTERVAL = Update Time Interval, LISTENER = Database Update Listener (Please let me know if there are problems).");

        sqliteServerSyncTimer = cfg.getInt("Sync Timer", SQLITE_CATEGORY,
                sqliteServerSyncTimer, 5, 1000, "Time Interval in seconds for when the server polls "
                + "the whitelist changes from the database. (Only used in Interval Sqlite Mode!)");

        sqliteServerListenerTimer = cfg.getInt("Server Listener Sync Time",
                SQLITE_CATEGORY, sqliteServerListenerTimer, 1, 1000,
                "Time Interval in seconds for when the server checks for"
                + " database changes (Only used in Database Update "
                + "Sqlite Mode!)");

        // MY_SQL settings
        cfg.addCustomCategoryComment(MYSQL_CATEGORY, "mySQL configuration (To enable "
                + "mySQL, refer to the mode setting in the general configuration).");

        mySQL_IP = cfg.getString("mySQL IP", MYSQL_CATEGORY, mySQL_IP,
                "IP for your mySQL server (Example: localhost) Note: Do not add schema.");

        mySQL_PORT = cfg.getString("mySQL Port", MYSQL_CATEGORY, mySQL_PORT, "Port for your mySQL server.");

        mySQL_Username = cfg.getString("mySQL Username", MYSQL_CATEGORY, mySQL_Username, "Username for your mySQL server.");

        mySQL_Password = cfg.getString("mySQL Password", MYSQL_CATEGORY, mySQL_Password, "Password for your mySQL server.");
    }

}
