# WhitelistSyncRewrite
Server Side Mod to sync all of your forge server whitelists to a single database (Currently supports SQLite or mySQL servers).

SEE https://minecraft.curseforge.com/projects/whitelist-sync

Are you a server owner who is sick of adding individual players to each of your 100 forge servers? Wait you only have 2 forge servers? Well this mod can help you still!

 

Introducing Whitelist Sync!

 

A mod that allows you to sync the whitelists from multiple forge servers together!

 

This mod allows you to run a single /wl add <player> on one of your servers and it will update all of your other forge servers running this mod!

 

Installation:

 

Wait! This mod requires 2 dependencies!

- json-simple-1.1.1.jar

- sqlite-jdbc-3.19.3.jar

Place both of those files in your server mods folder!

 

After you've gotten the dependencies in your mods folder download this mod and add it to your mods folder.
Run your server and setup the config (wlsync.cfg).
NOTE: Be sure to make the database path the same for all of the servers you want to link together!
When the server runs it is going to make a new database. If you want to push your current whitelist to the database use "/wl copyWhiteListToDatabase" then you can start all of your other server and they will sync to the database.
 

Commands:

 

/wl add <player> | Adds a specified player to whitelist. (Use this instead of /whitelist add)
/wl remove <player> | Removes a specified player from the whitelist. (Use this instead of /whitelist remove)
/wl sync | Pulls whitelist from the database and updates the server whitelist.
/wl reloadConfig | Reloads the config but a server restart is encouraged.
/wl copyServerToDatabase | Pushes local server whitelist to the database.
