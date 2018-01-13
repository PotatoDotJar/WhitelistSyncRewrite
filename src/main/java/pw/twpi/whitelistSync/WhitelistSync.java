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
import pw.twpi.whitelistSync.commands.CommandWhitelist;

/**
 * @author PotatoSauceVFX <rj@potatosaucevfx.com>
 */
@Mod(modid = WhitelistSync.MODID, version = WhitelistSync.VERSION, acceptableRemoteVersions = "*", serverSideOnly = true)
public class WhitelistSync {

    public static final String MODID = "whitelistSync";
    public static final String VERSION = "1.0";
    public static String SERVER_FILEPATH;
    public static Configuration config;

    public static final Logger logger = LogManager.getLogger(MODID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        logger.info("Hello Minecraft");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("Hello again Minecraft");

    }

    @SideOnly(Side.SERVER)
    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        SERVER_FILEPATH = event.getServer().getDataDirectory().getAbsolutePath();
        logger.info("--------------------------------------------");
        logger.info("---------------WHITELIST SYNC---------------");
        logger.info("--------------------------------------------");
        logger.info("Loading Commands");
        event.registerServerCommand(new CommandWhitelist());

        logger.info("--------------------------------------------");
        logger.info("--------------------------------------------");
        logger.info("--------------------------------------------");
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent e) {

    }
}
