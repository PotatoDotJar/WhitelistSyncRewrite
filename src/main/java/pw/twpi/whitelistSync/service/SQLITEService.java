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
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import pw.twpi.whitelistSync.WhitelistSync;
import pw.twpi.whitelistSync.util.ConfigHandler;
import pw.twpi.whitelistSync.util.WhitelistRead;

/**
 * @author PotatoSauceVFX <rj@potatosaucevfx.com>
 */
public class SQLITEService implements BaseService {

    // TODO: Prepared statements.
    private File databaseFile;
    private Connection conn = null;

    public SQLITEService() {
        WhitelistSync.logger.info("Settign up the SQLITE service...");
        this.databaseFile = new File(ConfigHandler.sqliteDatabasePath);
        loadDatabase();
        WhitelistSync.logger.info("SQLITE service loaded!");
    }

    private Boolean loadDatabase() {
        // If database does not exist.
        if (!databaseFile.exists()) {
            createNewDatabase();
        }

        // Create whitelist table if it doesn't exist.
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + ConfigHandler.sqliteDatabasePath);
            WhitelistSync.logger.info("Connected to SQLite database successfully!");

            // SQL statement for creating a new table
            String sql = "CREATE TABLE IF NOT EXISTS whitelist (\n"
                    + "	uuid text NOT NULL PRIMARY KEY,\n"
                    + "	name text,\n"
                    + " whitelisted integer NOT NULL);";
            Statement stmt = conn.createStatement();
            stmt.execute(sql);

        } catch (SQLException e) {
            WhitelistSync.logger.error("Error creating whitelist table!\n" + e.getMessage());
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                return true;
            } catch (SQLException ex) {
                WhitelistSync.logger.error("Error closing connection for creating whitelist table!\n" + ex.getMessage());
            }
        }
        return true;
    }

    @Override
    public void pushLocalWhitelistToDatabase(MinecraftServer server) {
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
                    Connection conn = DriverManager.getConnection("jdbc:sqlite:" + ConfigHandler.sqliteDatabasePath);
                    Statement stmt = conn.createStatement();
                    long startTime = System.currentTimeMillis();

                    // Loop through local whitelist and insert into database.
                    for (int i = 0; i < uuids.size() || i < names.size(); i++) {
                        if ((uuids.get(i) != null) && (names.get(i) != null)) {
                            String sql = "INSERT OR REPLACE INTO whitelist(uuid, name, whitelisted) VALUES (\'" + uuids.get(i) + "\', \'" + names.get(i) + "\', 1);";
                            stmt.execute(sql);
                            records++;
                        }
                    }
                    // Record time taken.
                    long timeTaken = System.currentTimeMillis() - startTime;

                    WhitelistSync.logger.debug("Database Updated | Took " + timeTaken + "ms | Wrote " + records + " records.");

                    stmt.close();
                    conn.close();
                } catch (SQLException e) {
                    WhitelistSync.logger.error("Failed to update database with local records.\n" + e.getMessage());
                }
            }
        }).start();
    }

    @Override
    public ArrayList<String> pullUuidsFromWhitelistDatabase(MinecraftServer server) {
        // ArrayList for uuids.
        ArrayList<String> uuids = new ArrayList<String>();

        try {
            // Keep track of records.
            int records = 0;

            // Connect to database.
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + ConfigHandler.sqliteDatabasePath);
            Statement stmt = conn.createStatement();
            String sql = "SELECT uuid, whitelisted FROM whitelist;";

            // Start time of querry.
            long startTime = System.currentTimeMillis();

            stmt.execute(sql);
            ResultSet rs = stmt.executeQuery(sql);

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

            rs = null;
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            WhitelistSync.logger.error("Error querrying uuids from database!\n" + e.getMessage());
        }
        return uuids;
    }

    @Override
    public ArrayList<String> pullNamesFromWhitelistDatabase(MinecraftServer server) {
        // ArrayList for names.
        ArrayList<String> names = new ArrayList<String>();

        try {

            // Keep track of records.
            int records = 0;

            // Connect to database.
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + ConfigHandler.sqliteDatabasePath);
            Statement stmt = conn.createStatement();
            String sql = "SELECT name, whitelisted FROM whitelist;";

            // Start time of querry.
            long startTime = System.currentTimeMillis();

            stmt.execute(sql);
            ResultSet rs = stmt.executeQuery(sql);

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

            rs = null;
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            WhitelistSync.logger.error("Error querrying names from database!\n" + e.getMessage());
        }
        return names;
    }

    @Override
    public void addPlayerToWhitelistDatabase(GameProfile player) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Open connection
                    Connection conn = DriverManager.getConnection("jdbc:sqlite:" + ConfigHandler.sqliteDatabasePath);
                    Statement stmt = conn.createStatement();
                    String sql = "INSERT OR REPLACE INTO whitelist(uuid, name, whitelisted) VALUES (\'" + player.getId() + "\', \'" + player.getName() + "\', 1);";

                    // Start time.
                    long startTime = System.currentTimeMillis();

                    // Execute statement.
                    stmt.execute(sql);

                    // Time taken.
                    long timeTaken = System.currentTimeMillis() - startTime;

                    WhitelistSync.logger.debug("Database Added " + player.getName() + " | Took " + timeTaken + "ms");

                    stmt.close();
                    conn.close();
                } catch (SQLException e) {
                    WhitelistSync.logger.error("Error adding " + player.getName() + " to database!\n" + e.getMessage());
                }
            }
        }).start();
    }

    @Override
    public void removePlayerFromWhitelistDatabase(GameProfile player) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Open connection
                    Connection conn = DriverManager.getConnection("jdbc:sqlite:" + ConfigHandler.sqliteDatabasePath);
                    Statement stmt = conn.createStatement();
                    String sql = "INSERT OR REPLACE INTO whitelist(uuid, name, whitelisted) VALUES (\'" + player.getId() + "\', \'" + player.getName() + "\', 0);";

                    // Start time.
                    long startTime = System.currentTimeMillis();

                    // Execute SQL.
                    stmt.execute(sql);

                    // Time taken
                    long timeTaken = System.currentTimeMillis() - startTime;

                    WhitelistSync.logger.debug("Database Removed " + player.getName() + " | Took " + timeTaken + "ms");

                    stmt.close();
                    conn.close();
                } catch (SQLException e) {
                    WhitelistSync.logger.error("Error removing " + player.getName() + " from database!\n" + e.getMessage());
                }
            }
        }).start();
    }

    @Override
    public void updateLocalWhitelistFromDatabase(MinecraftServer server) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int records = 0;
                    Connection conn = DriverManager.getConnection("jdbc:sqlite:" + ConfigHandler.sqliteDatabasePath);
                    Statement stmt = conn.createStatement();
                    String sql = "SELECT name, uuid, whitelisted FROM whitelist;";

                    long startTime = System.currentTimeMillis();

                    stmt.execute(sql);
                    ResultSet rs = stmt.executeQuery(sql);

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
                            if (localUuids.contains(uuid)) {
                                server.getPlayerList().removePlayerFromWhitelist(player);
                                WhitelistSync.logger.info("Removed player " + name);
                            }
                        }
                        records++;
                    }
                    long timeTaken = System.currentTimeMillis() - startTime;
                    WhitelistSync.logger.debug("Database Pulled | Took " + timeTaken + "ms | Wrote " + records + " records.");
                    WhitelistSync.logger.debug("Local whitelist.json up to date!");

                    rs = null;
                    stmt.close();
                    conn.close();
                } catch (SQLException e) {
                    WhitelistSync.logger.error("Error querying whitelisted players from database!\n" + e.getMessage());
                }
            }
        }).start();
    }

    private void createNewDatabase() {
        String url = "jdbc:sqlite:" + ConfigHandler.sqliteDatabasePath;
        try {
            Connection conn = DriverManager.getConnection(url);
            if (conn != null) {
                WhitelistSync.logger.info("A new database \"" + ConfigHandler.sqliteDatabasePath + "\" has been created.");
            }
        } catch (SQLException e) {
            WhitelistSync.logger.error("Error creating non-existing database!\n" + e.getMessage());
        }
    }

}
