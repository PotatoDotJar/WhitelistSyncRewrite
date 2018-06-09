package pw.twpi.whitelistSync.commands;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import pw.twpi.whitelistSync.WhitelistSync;
import pw.twpi.whitelistSync.service.BaseService;

/**
 * @author PotatoSauceVFX <rj@potatosaucevfx.com>
 */
public class CommandOp implements ICommand {

    private final ArrayList aliases;

    BaseService service;

    public CommandOp(BaseService service) {
        this.service = service;
        aliases = new ArrayList();
        aliases.add("wlop");
        aliases.add("whitelistsyncOP");
    }

    @Override
    public String getName() {
        return "wlop";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/wlop <list|add|remove|sync|copyServerToDatabase>";
    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        World world = sender.getEntityWorld();
        if (world.isRemote) {
            WhitelistSync.logger.error("I don't run on client-side!");
        } else {
            if (args.length > 0) {
                if (args.length > 0) {
                    //Action for showing list
                    if (args[0].equalsIgnoreCase("list")) {
                        service.pullOpNamesFromDatabase(server).forEach(user -> sender.sendMessage(new TextComponentString(user))); // TODO: Format output in table and add feedback.

                    } // Actions for adding a player to whitelist
                    else if (args[0].equalsIgnoreCase("add")) {
                        if (args.length > 1) {

                            GameProfile player = server.getPlayerProfileCache().getGameProfileForUsername(args[1]);

                            if (player != null) {
                                server.getPlayerList().addOp(player);
                                service.addOpPlayerToDatabase(server.getPlayerProfileCache().getGameProfileForUsername(args[1]));
                                sender.sendMessage(new TextComponentString(args[1] + " opped!"));
                            } else {
                                sender.sendMessage(new TextComponentString("User " + args[1] + " not found!"));
                            }

                        } else {
                            sender.sendMessage(new TextComponentString("You must specify a name to add to the whitelist!"));
                        }
                    } // Actions for removing player from whitelist
                    else if (args[0].equalsIgnoreCase("remove")) {
                        if (args.length > 1) {
                            GameProfile gameprofile = server.getPlayerList().getOppedPlayers().getGameProfileFromName(args[1]);
                            if (gameprofile != null) {
                                server.getPlayerList().removeOp(gameprofile);
                                service.removeOpPlayerFromDatabase(gameprofile);
                                sender.sendMessage(new TextComponentString(args[1] + " de-opped!"));
                            } else {
                                sender.sendMessage(new TextComponentString("You must specify a valid name to remove from the whitelist!"));
                            }
                        }
                    }
                } else {
                    sender.sendMessage(new TextComponentString("/wlop <list|add|remove>"));
                }
            } else {
                sender.sendMessage(new TextComponentString("/wlop <list|add|remove>"));
            }
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender
    ) {
        if (sender.canUseCommand(4, "wlop")) {
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
            return CommandBase.getListOfStringsMatchingLastWord(args, "list", "add", "remove");
        } else {
            if (args.length == 2) {
                if (args[0].equals("remove")) {
                    return CommandBase.getListOfStringsMatchingLastWord(args, server.getPlayerList().getOppedPlayerNames());
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
