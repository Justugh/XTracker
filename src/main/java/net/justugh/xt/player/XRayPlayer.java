package net.justugh.xt.player;

import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import net.justugh.xt.XTracker;
import net.justugh.xt.ore.OreData;
import net.justugh.xt.ore.OreType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Getter
public class XRayPlayer {

    private UUID uuid;
    private String world;

    private List<OreData> oreData = Lists.newArrayList();

    public XRayPlayer(UUID uuid, String world) {
        this.uuid = uuid;
        this.world = world;

        for (OreType oreType : OreType.values()) {
            oreData.add(new OreData(oreType));
        }
    }

    /**
     * Save data to player File.
     */
    public void saveToFile() {
        File playerFile = new File(XTracker.getInstance().getXRayManager().WORLD_FOLDER + File.separator + world + File.separator + uuid + ".json");

        try {
            FileUtils.writeStringToFile(playerFile, new GsonBuilder().setPrettyPrinting().create().toJson(this));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
