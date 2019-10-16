package net.justugh.xt.commands;

import net.justugh.xt.XTracker;
import net.justugh.xt.ore.OreData;
import net.justugh.xt.player.XRayPlayer;
import net.justugh.xt.util.Format;
import net.justugh.xt.util.UUIDUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class XRayStatsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("xraycheck")) {
            if (sender instanceof Player) {
                if (!sender.hasPermission(XTracker.getInstance().getConfig().getString("command-permission"))) {
                    sender.sendMessage(Format.format(XTracker.getInstance().getConfig().getString("no-permission")));
                    return true;
                }

                if (args.length >= 1) {
                    if (args.length >= 2) {
                        displayCheck((Player) sender, args[0], args[1]);
                    } else {
                        displayCheck((Player) sender, args[0], ((Player) sender).getWorld().getName());
                    }
                } else {
                    sender.sendMessage(Format.format(XTracker.getInstance().getConfig().getString("command-usage")));
                }
            } else {
                sender.sendMessage(Format.format(XTracker.getInstance().getConfig().getString("player-only")));
            }
        }
        return true;
    }

    private void displayCheck(Player sender, String player, String world) {
        XTracker xTracker = XTracker.getInstance();

        // Get uuid async
        CompletableFuture<UUID> future = CompletableFuture.supplyAsync(() -> {
            return UUIDUtil.getUUID(player);
        });

        // Run code after async task has value (without blocking main thread)
        future.thenRun(() -> {
            UUID uuid = null;

            try {
                // Retrieve value from future
                uuid = future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            // Make sure it's not null, stop if it is.
            if (uuid == null) return;

            OfflinePlayer bukkitPlayer = Bukkit.getOfflinePlayer(uuid);

            if (bukkitPlayer != null && bukkitPlayer.hasPlayedBefore()) {
                XRayPlayer xRayPlayer = xTracker.getXRayManager().getXRayPlayer(bukkitPlayer.getUniqueId(), world);

                if (xRayPlayer == null) {
                    sender.sendMessage(Format.format(xTracker.getConfig().getString("no-data").replace("%player%", player).replace("%world%", world)));
                    return;
                }

                sender.sendMessage(Format.format(xTracker.getConfig().getString("data-title").replace("%player%", bukkitPlayer.getName())));

                for (OreData oreData : xRayPlayer.getOreData()) {
                    sender.sendMessage(Format.format(xTracker.getConfig().getString("data-item")
                            .replace("%ore_type%", oreData.getOreType().getFriendlyName())
                            .replace("%amount_mined%", oreData.getAmountMined() + "")
                            .replace("%veins_mined%", oreData.getOreVeinsMined() + "")
                            .replace("%percentage%", xTracker.getXRayManager().getPercentage(xRayPlayer, oreData.getOreType()) + "")));
                }
            } else {
                sender.sendMessage(Format.format(xTracker.getConfig().getString("no-data").replace("%player%", player).replace("%world%", world)));
            }
        });
    }

}
