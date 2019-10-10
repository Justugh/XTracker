package net.justugh.xt.util;

import net.justugh.xt.XTracker;
import org.bukkit.ChatColor;

public class Format {

    /**
     * Format a message with color codes & add the prefix.
     *
     * @param message The message being formatted.
     * @return The formmated message.
     */
    public static String format(String message) {
        return ChatColor.translateAlternateColorCodes('&', message.replace("%prefix%", XTracker.getInstance().getConfig().getString("prefix")));
    }

}
