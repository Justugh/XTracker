package net.justugh.xt.manager;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.Getter;
import net.justugh.xt.XTracker;
import net.justugh.xt.ore.OreData;
import net.justugh.xt.ore.OreType;
import net.justugh.xt.player.XRayPlayer;
import net.justugh.xt.util.Format;
import net.justugh.xt.world.WorldCache;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Getter
public class XRayManager implements Listener {

    private List<WorldCache> worldCache = Lists.newArrayList();
    @Getter(AccessLevel.NONE)
    public final File WORLD_FOLDER;

    public XRayManager() {
        WORLD_FOLDER = new File(XTracker.getInstance().getDataFolder() + File.separator + "X-Ray Worlds");

        if(!WORLD_FOLDER.exists()) {
            WORLD_FOLDER.mkdirs();
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(XTracker.getInstance(), () ->
                worldCache.forEach(WorldCache::saveAll), 0, XTracker.getInstance().getConfig().getInt("save-time") * 20);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (getXRayPlayer(event.getPlayer().getUniqueId(), event.getPlayer().getWorld().getName()) == null) {
            createNewXRayPlayer(event.getPlayer().getUniqueId(), event.getPlayer().getWorld().getName());
        }
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        if (getXRayPlayer(event.getPlayer().getUniqueId(), event.getPlayer().getWorld().getName()) == null) {
            createNewXRayPlayer(event.getPlayer().getUniqueId(), event.getPlayer().getWorld().getName());
        }
    }

    @EventHandler
    public void onOreMine(BlockBreakEvent event) {
        if (OreType.match(event.getBlock().getType()) == null) {
            return;
        }

        XRayPlayer player = getXRayPlayer(event.getPlayer().getUniqueId(), event.getPlayer().getWorld().getName());
        OreType oreType = OreType.match(event.getBlock().getType());
        OreData oreData = getOreData(player, oreType);

        if (!isInVein(event.getBlock())) {
            oreData.setOreVeinsMined(oreData.getOreVeinsMined() + 1);

            if(XTracker.getInstance().getConfig().getInt("alerts." + oreType.name()) != -1) {
                if(getPercentage(player, oreType) >= XTracker.getInstance().getConfig().getInt("alerts." + oreType.name())) {
                    Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                        if(onlinePlayer.hasPermission(XTracker.getInstance().getConfig().getString("alert-permission"))) {
                            onlinePlayer.sendMessage(Format.format(XTracker.getInstance().getConfig().getString("alert-message")
                                    .replace("%player%", event.getPlayer().getName())
                                    .replace("%ore_type%", oreData.getOreType().getFriendlyName())
                                    .replace("%amount_mined%", oreData.getAmountMined() + 1 + "")
                                    .replace("%veins_mined%", oreData.getOreVeinsMined() + "")
                                    .replace("%percentage%", getPercentage(player, oreData.getOreType()) + "")));
                        }
                    });
                }
            }
        }

        oreData.setAmountMined(oreData.getAmountMined() + 1);
    }

    /**
     * Create new X-Ray Player instance and add to cache.
     *
     * @param player The new UUID of the player.
     * @param world  The world of the player.
     * @return The newly created X-Ray Player instance.
     */
    private XRayPlayer createNewXRayPlayer(UUID player, String world) {
        WorldCache cache = getWorldCache(world, true);
        XRayPlayer xRayPlayer = new XRayPlayer(player, world);
        xRayPlayer.saveToFile();

        cache.getPlayerCache().put(player, xRayPlayer);
        return xRayPlayer;
    }

    /**
     * Get the percentage of ore mined.
     *
     * @param player The player who we're retrieving the data from.
     * @param type   The OreType that we're searching for.
     * @return The percentage of this mined OreType.
     */
    public float getPercentage(XRayPlayer player, OreType type) {
        if (getOreData(player, type).getAmountMined() == 0) {
            return 0;
        }

        int total = 0;

        for (OreData oreData : player.getOreData()) {
            total += oreData.getAmountMined();
        }

        return (getOreData(player, type).getAmountMined() * 100) / total;
    }

    /**
     * Get ore data from an X-Ray Player instance from the OreType
     *
     * @param player The player whose OreData instance is being collected.
     * @param type   The OreType that we're searching for.
     * @return An instance of the OreData.
     */
    public OreData getOreData(XRayPlayer player, OreType type) {
        return player.getOreData().stream().filter(oreData -> oreData.getOreType().equals(type)).findFirst().orElse(null);
    }

    /**
     * Get X-Ray Player instance from player cache.
     *
     * @param uuid  The cached UUID.
     * @param world The cached world.
     * @return The X-Ray Player instance.
     */
    public XRayPlayer getXRayPlayer(UUID uuid, String world) {
        WorldCache cache = getWorldCache(world, true);
        return cache == null ? null : cache.getPlayerCache().get(uuid);
    }

    /**
     * Get X-Ray Player instance from file.
     *
     * @param uuid  The player's UUID.
     * @param world The world we want the instance from.
     * @return The X-Ray Player instance.
     */
    public XRayPlayer getXRayPlayerFromFile(UUID uuid, String world) {
        File playerFile = new File(WORLD_FOLDER + File.separator + world + File.separator + uuid + ".json");

        if (playerFile.exists()) {
            try {
                return new Gson().fromJson(new FileReader(playerFile), XRayPlayer.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Get WorldCache instance.
     *
     * @param world    The world name.
     * @param generate Whether or not to generate a new WorldCache if null.
     * @return The WorldCache instance.
     */
    public WorldCache getWorldCache(String world, boolean generate) {
        WorldCache cache = worldCache.stream().filter(instance -> instance.getWorld().equals(world)).findFirst().orElse(null);

        if (cache == null && generate) {
            cache = new WorldCache(world);

            worldCache.add(cache);
        }

        return cache;
    }

    /**
     * Check if the current block is part of an ore vein.
     *
     * @param block The block being checked.
     * @return Whether or not the location is part of an ore vein.
     */
    public boolean isInVein(Block block) {
        if (block == null) {
            return false;
        }

        for (BlockFace blockFace : BlockFace.values()) {
            if (blockFace.equals(BlockFace.SELF)) {
                continue;
            }

            if (block.getRelative(blockFace).getType().equals(block.getType())) {
                return true;
            }

            Block topBlock = block.getRelative(blockFace.getModX(), 1, blockFace.getModZ());
            Block bottomBlock = block.getRelative(blockFace.getModX(), -1, blockFace.getModZ());

            if (topBlock != block && topBlock.getType().equals(block.getType())) {
                return true;
            }

            if (bottomBlock != block && bottomBlock.getType().equals(block.getType())) {
                return true;
            }
        }

        return false;
    }

}
