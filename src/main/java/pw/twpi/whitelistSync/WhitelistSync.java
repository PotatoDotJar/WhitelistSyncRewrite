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
package pw.twpi.whitelistSync;

import java.io.File;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pw.twpi.whitelistSync.commands.CommandOp;
import pw.twpi.whitelistSync.commands.CommandWhitelist;
import pw.twpi.whitelistSync.service.BaseService;
import pw.twpi.whitelistSync.service.MYSQLService;
import pw.twpi.whitelistSync.service.SQLITEService;
import pw.twpi.whitelistSync.service.WhitelistSyncThread;
import pw.twpi.whitelistSync.util.ConfigErrorException;
import pw.twpi.whitelistSync.util.ConfigHandler;

/**
 * @author PotatoSauceVFX <rj@potatosaucevfx.com>
 */
@Mod(modid = WhitelistSync.MODID, version = WhitelistSync.VERSION, acceptableRemoteVersions = "*", serverSideOnly = true)
public class WhitelistSync {

    public static final String MODID = "whitelistsync";
    public static final String VERSION = "1.3-1.12.2"; // Change gradle build config too!
    public static String SERVER_FILEPATH;
    public static Configuration config;

    // Database Service
    BaseService service;

    // TODO: Use ScheduleExecutorServices
    public static final Logger logger = LogManager.getLogger(MODID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        logger.info("Hello Minecraft");
        updateConfig(e);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("Hello again Minecraft");
        logger.info("Setting up databases...");
        loadDatabase();
    }

    @SideOnly(Side.SERVER)
    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        //SERVER_FILEPATH = event.getServer().getDataDirectory().getAbsolutePath();

        File serverDir = event.getServer().getDataDirectory();

        SERVER_FILEPATH = serverDir.getPath();
        logger.info("--------------------------------------------");
        logger.info("---------------WHITELIST SYNC---------------");
        logger.info("--------------------------------------------");
        logger.info("Loading Commands");
        event.registerServerCommand(new CommandWhitelist(service));
        event.registerServerCommand(new CommandOp(service));

        logger.info("Starting Sync Thread...");
        startSyncThread(event.getServer());

        // Check if whitelisting is enabled.
        if (!event.getServer().getPlayerList().isWhiteListEnabled()) {
            logger.info("Oh no! I see whitelisting isn't enabled in the server properties. "
                    + "Is this intentional?");
        }

        logger.info("--------------------------------------------");
        logger.info("--------------------------------------------");
        logger.info("--------------------------------------------");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {
        if (config.hasChanged()) {
            config.save();
        }
    }

    // Mothod for loading database
    private void loadDatabase() {
        if (ConfigHandler.WHITELIST_MODE.equalsIgnoreCase(ConfigHandler.MODE_SQLITE)) {
            service = new SQLITEService();
        } else if (ConfigHandler.WHITELIST_MODE.equalsIgnoreCase(ConfigHandler.MODE_MYSQL)) {
            service = new MYSQLService();
        } else {
            throw new ConfigErrorException("Please check what WHITELIST_MODE is set in the config"
                    + "and make sure it is set to a supported mode.");
        }
    }

    // Method for loading config.
    private void updateConfig(FMLPreInitializationEvent e) {
        File directory = e.getModConfigurationDirectory();
        config = new Configuration(new File(directory.getPath(), MODID + ".cfg"));
        ConfigHandler.readConfig();
    }

    private void startSyncThread(MinecraftServer server) {
        Thread sync = new Thread(new WhitelistSyncThread(server, service));
        sync.start();
    }
}
