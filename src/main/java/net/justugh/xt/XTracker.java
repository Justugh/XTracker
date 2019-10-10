package net.justugh.xt;

import lombok.Getter;
import net.justugh.xt.commands.XRayStatsCommand;
import net.justugh.xt.commands.XTrackerCommand;
import net.justugh.xt.manager.XRayManager;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class XTracker extends JavaPlugin {

    @Getter
    private static XTracker instance;

    private XRayManager xRayManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        xRayManager = new XRayManager();
        getServer().getPluginManager().registerEvents(xRayManager, this);

        getCommand("xraycheck").setExecutor(new XRayStatsCommand());
        getCommand("xtracker").setExecutor(new XTrackerCommand());
    }

    @Override
    public void onDisable() {

    }
}
