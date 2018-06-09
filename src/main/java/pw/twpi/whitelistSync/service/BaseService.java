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
import java.util.ArrayList;
import net.minecraft.server.MinecraftServer;

/**
 * @author PotatoSauceVFX <rj@potatosaucevfx.com>
 */
public interface BaseService {

    // Merges database with local whitelist.
    public void pushLocalToDatabase(MinecraftServer server);

    // Gets ArrayList of uuids whitelisted in database.
    public ArrayList<String> pullUuidsFromDatabase(MinecraftServer server);

    // Gets ArrayList of uuids ops in database.
    public ArrayList<String> pullOpUuidsFromDatabase(MinecraftServer server);

    // Gets ArrayList of names whitelisted in database.
    public ArrayList<String> pullNamesFromDatabase(MinecraftServer server);

    // Gets ArrayList of names ops in database.
    public ArrayList<String> pullOpNamesFromDatabase(MinecraftServer server);

    // Adds player to database.
    public void addPlayerToDatabase(GameProfile player);

    // Adds op player to database.
    public void addOpPlayerToDatabase(GameProfile player);

    // Removes player from database.
    public void removePlayerFromDatabase(GameProfile player);

    // Removes op player from database.
    public void removeOpPlayerFromDatabase(GameProfile player);

    // Copies whitelist from database to server.
    public void updateLocalWhitelistFromDatabase(MinecraftServer server);

    // Copies op list from database to server.
    public void updateLocalOpListFromDatabase(MinecraftServer server);

}
