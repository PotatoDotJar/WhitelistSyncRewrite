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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import pw.twpi.whitelistSync.WhitelistSync;
import pw.twpi.whitelistSync.util.ConfigHandler;
import pw.twpi.whitelistSync.util.MYsqlBDError;
import pw.twpi.whitelistSync.util.WhitelistRead;

/**
 * @author PotatoSauceVFX <rj@potatosaucevfx.com>
 */
public class MYSQLService implements BaseService {

    private Connection conn = null;
    private String S_SQL = "";
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
            throw new MYsqlBDError("Failed to connect to the mySQL database! Did you set one up in the config?\n" + e.getMessage());
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
            S_SQL = "CREATE DATABASE IF NOT EXISTS WhitelistSync;";
            statement.execute(S_SQL);

            // Create table
            S_SQL = "CREATE TABLE IF NOT EXISTS WhitelistSync.whitelist ("
                    + "`uuid` VARCHAR(60) NOT NULL,"
                    + "`name` VARCHAR(20) NOT NULL,"
                    + "`whitelisted` TINYINT NOT NULL DEFAULT 1,"
                    + "PRIMARY KEY (`uuid`)"
                    + ")";
            statement.execute(S_SQL);
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
                            try {
                                PreparedStatement sql = conn.prepareStatement("INSERT IGNORE INTO WhitelistSync.whitelist(uuid, name, whitelisted) VALUES (?, ?, 1)");
                                sql.setString(1, uuids.get(i));
                                sql.setString(2, names.get(i));
                                sql.executeUpdate();
                                records++;
                            } catch (ClassCastException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    // Record time taken.
                    long timeTaken = System.currentTimeMillis() - startTime;

                    WhitelistSync.logger.info("Wrote " + records + " to database in " + timeTaken + "ms.");
                    WhitelistSync.logger.debug("Database Updated | Took " + timeTaken + "ms | Wrote " + records + " records.");
                } catch (SQLException e) {
                    WhitelistSync.logger.error("Failed to update database with local records.\n" + e.getMessage());
                }
            }
        }).start();
    }

    @Override
    public ArrayList<String> pullUuidsFromDatabase(MinecraftServer server) {
        // ArrayList for uuids.
        ArrayList<String> uuids = new ArrayList<String>();

        try {
            // Keep track of records.
            int records = 0;

            // Connect to database.
            Connection conn = getConnection();
            long startTime = System.currentTimeMillis();

            String sql = "SELECT uuid, whitelisted FROM WhitelistSync.whitelist";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            // Add querried results to arraylist.
            while (rs.next()) {
                if (rs.getInt("whitelisted") == 1) {
                    uuids.add(rs.getString("uuid"));
                }
                records++;
            }

            // Time taken
            long timeTaken = System.currentTimeMillis() - startTime;

            WhitelistSync.logger.debug("Database Pulled | Took " + timeTaken + "ms | Read " + records + " records.");
        } catch (SQLException e) {
            WhitelistSync.logger.error("Error querrying uuids from database!\n" + e.getMessage());
        }
        return uuids;
    }

    @Override
    public ArrayList<String> pullNamesFromDatabase(MinecraftServer server) {
        // ArrayList for names.
        ArrayList<String> names = new ArrayList<String>();

        try {

            // Keep track of records.
            int records = 0;

            // Connect to database.
            Connection conn = getConnection();
            long startTime = System.currentTimeMillis();

            String sql = "SELECT name, whitelisted FROM WhitelistSync.whitelist";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            // Save querried return to names list.
            while (rs.next()) {
                if (rs.getInt("whitelisted") == 1) {
                    names.add(rs.getString("name"));
                }
                records++;
            }

            // Total time taken.
            long timeTaken = System.currentTimeMillis() - startTime;

            WhitelistSync.logger.debug("Database Pulled | Took " + timeTaken + "ms | Read " + records + " records.");
        } catch (SQLException e) {
            WhitelistSync.logger.error("Error querrying names from database!\n" + e.getMessage());
        }
        return names;
    }

    // TODO: Add boolean feedback.
    @Override
    public void addPlayerToDatabase(GameProfile player) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    // Start time.
                    long startTime = System.currentTimeMillis();

                    // Open connection
                    Connection conn = getConnection();
                    String sql = "REPLACE INTO WhitelistSync.whitelist(uuid, name, whitelisted) VALUES (?, ?, 1)";

                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, String.valueOf(player.getId()));
                    stmt.setString(2, player.getName());

                    // Execute statement.
                    stmt.execute();

                    // Time taken.
                    long timeTaken = System.currentTimeMillis() - startTime;

                    WhitelistSync.logger.debug("Database Added " + player.getName() + " | Took " + timeTaken + "ms");
                } catch (SQLException e) {
                    WhitelistSync.logger.error("Error adding " + player.getName() + " to database!\n" + e.getMessage());
                }
            }
        }).start();
    }

    // TODO: Add boolean feedback.
    @Override
    public void removePlayerFromDatabase(GameProfile player) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    // Start time.
                    long startTime = System.currentTimeMillis();

                    // Open connection
                    Connection conn = getConnection();
                    String sql = "REPLACE INTO WhitelistSync.whitelist(uuid, name, whitelisted) VALUES (?, ?, 0)";

                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, String.valueOf(player.getId()));
                    stmt.setString(2, player.getName());

                    // Execute statement.
                    stmt.execute();

                    // Time taken.
                    long timeTaken = System.currentTimeMillis() - startTime;

                    WhitelistSync.logger.debug("Database Removed " + player.getName() + " | Took " + timeTaken + "ms");
                } catch (SQLException e) {
                    WhitelistSync.logger.error("Error removing " + player.getName() + " to database!\n" + e.getMessage());
                }
            }
        }).start();
    }

    @Override
    public void updateLocalFromDatabase(MinecraftServer server) {
        new Thread(() -> {
            try {
                int records = 0;

                // Start time
                long startTime = System.currentTimeMillis();

                // Open connection
                Connection conn = getConnection();
                String sql = "SELECT name, uuid, whitelisted FROM WhitelistSync.whitelist";
                PreparedStatement stmt = conn.prepareStatement(sql);

                ResultSet rs = stmt.executeQuery();
                ArrayList<String> localUuids = WhitelistRead.getWhitelistUUIDs();
                while (rs.next()) {
                    int whitelisted = rs.getInt("whitelisted");
                    String uuid = rs.getString("uuid");
                    String name = rs.getString("name");
                    GameProfile player = new GameProfile(UUID.fromString(uuid), name);

                    if (whitelisted == 1) {
                        if (!localUuids.contains(uuid)) {
                            try {
                                server.getPlayerList().addWhitelistedPlayer(player);
                            } catch (NullPointerException e) {
                                WhitelistSync.logger.error("Player is null?\n" + e.getMessage());
                            }
                        }
                    } else {
                        WhitelistSync.logger.debug(uuid + " is NOT whitelisted.");
                        if (localUuids.contains(uuid)) {
                            server.getPlayerList().removePlayerFromWhitelist(player);
                            WhitelistSync.logger.debug("Removed player " + name);
                        }
                    }
                    records++;
                }
                long timeTaken = System.currentTimeMillis() - startTime;
                WhitelistSync.logger.debug("Database Pulled | Took " + timeTaken + "ms | Wrote " + records + " records.");
                WhitelistSync.logger.debug("Local whitelist.json up to date!");
            } catch (SQLException e) {
                WhitelistSync.logger.error("Error querying whitelisted players from database!\n" + e.getMessage());
            }
        }).start();
    }

}
