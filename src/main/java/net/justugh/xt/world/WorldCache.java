package net.justugh.xt.world;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import lombok.Getter;
import net.justugh.xt.XTracker;
import net.justugh.xt.player.XRayPlayer;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

@Getter
public class WorldCache {

    private String world;
    private HashMap<UUID, XRayPlayer> playerCache = Maps.newHashMap();

    public WorldCache(String world) {
        this.world = world;
        loadCache();
    }

    /**
     * Load all player files into the player cache.
     */
    private void loadCache() {
        File worldFile = new File(XTracker.getInstance().getXRayManager().WORLD_FOLDER + File.separator + world);

        if (!worldFile.exists()) {
            worldFile.mkdir();
        }

        for (File file : Objects.requireNonNull(worldFile.listFiles())) {
            if (!file.getName().endsWith(".json")) {
                Bukkit.getLogger().log(Level.WARNING, "[XTracker]: Invalid X-Ray Player file: " + file.getName());
                return;
            }

            try {
                XRayPlayer player = new Gson().fromJson(new FileReader(file), XRayPlayer.class);

                playerCache.put(player.getUuid(), player);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Save all player instances to file.
     */
    public void saveAll() {
        playerCache.values().forEach(XRayPlayer::saveToFile);
    }

}
