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
package pw.twpi.whitelistSync.commands;

import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import pw.twpi.whitelistSync.WhitelistSync;
import pw.twpi.whitelistSync.service.BaseService;
import pw.twpi.whitelistSync.util.ConfigHandler;

/**
 * @author PotatoSauceVFX <rj@potatosaucevfx.com>
 */
public class CommandWhitelist implements ICommand {

    private final ArrayList aliases;

    BaseService service;

    public CommandWhitelist(BaseService service) {
        this.service = service;
        aliases = new ArrayList();
        aliases.add("wl");
        aliases.add("whitelistsync");
    }

    @Override
    public String getName() {
        return "wl";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/wl <list|add|remove|sync|copyServerToDatabase>";
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        World world = sender.getEntityWorld();
        if (world.isRemote) {
            WhitelistSync.logger.error("I don't process on client-side!");
        } else {
            if (args.length > 0) {
                if (args.length > 0) {
                    //Action for showing list
                    if (args[0].equalsIgnoreCase("list")) {
                        service.pullNamesFromWhitelistDatabase(server).forEach(user -> sender.sendMessage(new TextComponentString(user.toString()))); // TODO: Format output in table and add feedback.

                    } // Actions for adding a player to whitelist
                    else if (args[0].equalsIgnoreCase("add")) {
                        if (args.length > 1) {

                            // Check if user exists on mojang's side.
                            GameProfile whitelistPlayer = server.getPlayerProfileCache().getGameProfileForUsername(args[1]);
                            if (whitelistPlayer != null) {
                                server.getPlayerList().addWhitelistedPlayer(whitelistPlayer);
                                service.addPlayerToWhitelistDatabase(whitelistPlayer);
                                sender.sendMessage(new TextComponentString(args[1] + " added to the whitelist."));
                            } else {
                                sender.sendMessage(new TextComponentString("User, " + args[1] + ", not found in mojang's database!"));
                            }
                            ;
                        } else {
                            sender.sendMessage(new TextComponentString("You must specify a name to add to the whitelist!"));
                        }
                    } // Actions for removing player from whitelist
                    else if (args[0].equalsIgnoreCase("remove")) {
                        if (args.length > 1) {
                            GameProfile gameprofile = server.getPlayerList().getWhitelistedPlayers().getByName(args[1]);
                            if (gameprofile != null) {
                                server.getPlayerList().removePlayerFromWhitelist(gameprofile);
                                service.removePlayerFromWhitelistDatabase(gameprofile);
                                sender.sendMessage(new TextComponentString(args[1] + " removed from the whitelist."));
                            } else {
                                sender.sendMessage(new TextComponentString("You must specify a valid name to remove from the whitelist!"));
                            }
                        }
                    } // Reloads the config
                    else if (args[0].equalsIgnoreCase("reloadConfig")) {
                        ConfigHandler.readConfig();
                    } // Sync Database to server
                    else if (args[0].equalsIgnoreCase("sync")) {
                        service.updateLocalWhitelistFromDatabase(server);
                        sender.sendMessage(new TextComponentString("Loaded data from database to local whitelist."));
                    } // Sync server to database
                    else if (args[0].equalsIgnoreCase("copyservertodatabase")) {
                        service.pushLocalWhitelistToDatabase(server);
                        sender.sendMessage(new TextComponentString("Pushed local whitelist data to database."));
                    }
                } else {
                    sender.sendMessage(new TextComponentString("/wl <list|add|remove|sync|copyServerToDatabase>"));
                }
            } else {
                sender.sendMessage(new TextComponentString("/wl <list|add|remove|sync|copyServerToDatabase>"));
            }
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender
    ) {
        if (sender.canUseCommand(4, "wl")) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender,
            String[] args,
            @Nullable BlockPos pos
    ) {
        if (args.length == 1) {
            return CommandBase.getListOfStringsMatchingLastWord(args, "list", "add", "remove", "reloadConfig", "sync", "copyServerToDatabase");
        } else {
            if (args.length == 2) {
                if (args[0].equals("remove")) {
                    return CommandBase.getListOfStringsMatchingLastWord(args, server.getPlayerList().getWhitelistedPlayerNames());
                }

                if (args[0].equals("add")) {
                    return CommandBase.getListOfStringsMatchingLastWord(args, server.getPlayerProfileCache().getUsernames());
                }
            }
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index
    ) {
        return false;
    }

    @Override
    public int compareTo(ICommand o
    ) {
        return 0;
    }
}
