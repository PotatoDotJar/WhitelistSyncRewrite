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

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import net.minecraft.server.MinecraftServer;
import pw.twpi.whitelistSync.WhitelistSync;
import pw.twpi.whitelistSync.util.ConfigHandler;

/**
 * @author PotatoSauceVFX <rj@potatosaucevfx.com>
 */
public class WhitelistSyncThread implements Runnable {

    private MinecraftServer server;
    private BaseService service;

    // Watch Listener
    private FileSystem fileSystem;
    private WatchService watcher;

    public WhitelistSyncThread(MinecraftServer server, BaseService service) {
        this.server = server;
        this.service = service;
    }

    @Override
    public void run() {
        if (service.getClass().equals(MYSQLService.class)) {
            while (server.isServerRunning()) {
                service.updateLocalFromDatabase(server);

                try {
                    Thread.sleep(ConfigHandler.mysqlServerSyncTimer * 1000);
                } catch (InterruptedException e) {
                }
            }
        } else if (service.getClass().equals(SQLITEService.class)) {

            if (ConfigHandler.sqliteMode.equalsIgnoreCase("INTERVAL")) {
                while (server.isServerRunning()) {
                    service.updateLocalFromDatabase(server);
                    try {
                        Thread.sleep(ConfigHandler.sqliteServerSyncTimer * 1000);
                    } catch (InterruptedException e) {
                    }
                }
            } else if (ConfigHandler.sqliteMode.equalsIgnoreCase("LISTENER")) {
                checkSQliteDB();
            }

        } else {
            WhitelistSync.logger.error("Error in the Sync Thread! "
                    + "Nothing will be synced! Please report to author!");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }
        }

    }

    private void checkSQliteDB() {
        try {
            this.fileSystem = FileSystems.getDefault();
            this.watcher = fileSystem.newWatchService();

            Path dataBasePath = fileSystem.getPath(ConfigHandler.sqliteDatabasePath.replace("whitelist.db", ""));
            dataBasePath.register(watcher, ENTRY_MODIFY);

        } catch (IOException e) {
            WhitelistSync.logger.error("Error finding whitelist database file. "
                    + "This should not happen, please report.\n" + e.getMessage());
        }

        while (server.isServerRunning()) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                // Test if whitelist is changed
                if (event.context().toString().equalsIgnoreCase("whitelist.db")) {
                    WhitelistSync.logger.debug("Remote Database Updated... Syncing...");
                    service.updateLocalFromDatabase(server);
                }
            }

            // Reset the key -- this step is critical if you want to
            // receive further watch events.  If the key is no longer valid,
            // the directory is inaccessible so exit the loop.
            boolean valid = key.reset();
            if (!valid) {
                break;
            }

            try {
                Thread.sleep(ConfigHandler.sqliteServerListenerTimer * 1000);
            } catch (InterruptedException e) {
            }

        }
    }
}
