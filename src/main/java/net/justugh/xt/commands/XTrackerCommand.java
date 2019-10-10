package net.justugh.xt.commands;

import net.justugh.xt.XTracker;
import net.justugh.xt.util.Format;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class XTrackerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(label.equalsIgnoreCase("xtracker")) {
            if(!sender.hasPermission(XTracker.getInstance().getConfig().getString("reload-config-permission"))) {
                sender.sendMessage(Format.format(XTracker.getInstance().getConfig().getString("no-permission")));
                return true;
            }

            XTracker.getInstance().reloadConfig();
            sender.sendMessage(Format.format(XTracker.getInstance().getConfig().getString("config-reloaded")));
        }
        return true;
    }

}
