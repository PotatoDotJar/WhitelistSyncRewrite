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
package pw.twpi.whitelistSync.service;

import com.mojang.authlib.GameProfile;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import net.minecraft.server.MinecraftServer;
import pw.twpi.whitelistSync.WhitelistSync;
import pw.twpi.whitelistSync.util.ConfigHandler;
import pw.twpi.whitelistSync.util.WhitelistRead;

/**
 * @author PotatoSauceVFX <rj@potatosaucevfx.com>
 */
public class MYSQLService implements BaseService {

    private Connection conn = null;
    private String SQL = "";
    private Statement statement = null;

    private String url;
    private String username;
    private String password;

    public MYSQLService() {
        this.url = "jdbc:mysql://" + ConfigHandler.mySQL_IP + ":" + ConfigHandler.mySQL_PORT + "/";
        this.username = ConfigHandler.mySQL_Username;
        this.password = ConfigHandler.mySQL_Password;

        try {
            conn = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            WhitelistSync.logger.error("Failed to connect to the mySQL database! Did you set one up in the config?\n" + e.getMessage());
            System.exit(0);
        }

        loadDatabase();
    }

    private Connection getConnection() {
        return conn;
    }

    private void loadDatabase() {
        // Create database
        try {
            // Create statement
            statement = conn.createStatement();

            // Create database
            SQL = "CREATE DATABASE IF NOT EXISTS WhitelistSync;";
            statement.execute(SQL);

            // Create table
            SQL = "CREATE TABLE IF NOT EXISTS WhitelistSync.whitelist ("
                    + "`uuid` VARCHAR(60) NOT NULL,"
                    + "`name` VARCHAR(20) NOT NULL,"
                    + "`whitelisted` TINYINT NOT NULL DEFAULT 1,"
                    + "PRIMARY KEY (`uuid`)"
                    + ");";
            statement.execute(SQL);
            statement.close();
            WhitelistSync.logger.info("Loaded mySQL database!");

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception ee) {
            ee.printStackTrace();
        }
    }

    @Override
    public void pushLocalToDatabase(MinecraftServer server) {
        // Load local whitelist to memory.
        ArrayList<String> uuids = WhitelistRead.getWhitelistUUIDs();
        ArrayList<String> names = WhitelistRead.getWhitelistNames();

        // Start job on thread to avoid lag.
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Keep track of records.
                int records = 0;

                try {
                    // Connect to database.
                    Connection conn = getConnection();
                    long startTime = System.currentTimeMillis();

                    // Loop through local whitelist and insert into database.
                    for (int i = 0; i < uuids.size() || i < names.size(); i++) {
                        if ((uuids.get(i) != null) && (names.get(i) != null)) {

                            PreparedStatement sql = conn.prepareStatement("INSERT IGNORE INTO WhitelistSync.whitelist(uuid, name, whitelisted) VALUES (?, ?, 1)");
                            sql.setString(1, uuids.get(i));
                            sql.setString(2, names.get(i));
                            sql.executeUpdate();
                            records++;
                        }
                    }
                    // Record time taken.
                    long timeTaken = System.currentTimeMillis() - startTime;

                    WhitelistSync.logger.debug("Database Updated | Took " + timeTaken + "ms | Wrote " + records + " records.");

                    stmt.close();
                } catch (SQLException e) {
                    WhitelistSync.logger.error("Failed to update database with local records.\n" + e.getMessage());
                }
            }
        }).start();
    }

    @Override
    public ArrayList<String> pullUuidsFromDatabase(MinecraftServer server) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ArrayList<String> pullNamesFromDatabase(MinecraftServer server) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addPlayerToDatabase(GameProfile player) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removePlayerFromDatabase(GameProfile player) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateLocalFromDatabase(MinecraftServer server) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
