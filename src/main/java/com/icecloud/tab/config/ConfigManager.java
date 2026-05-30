package com.icecloud.tab.config;

import com.icecloud.tab.IceCloudTAB;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public final class ConfigManager {

    private final IceCloudTAB plugin;
    private FileConfiguration config;
    private boolean tabEnabled;
    private int tabUpdateInterval;
    private List<String> tabHeader;
    private List<String> tabFooter;
    private boolean scoreboardEnabled;
    private int scoreboardUpdateInterval;
    private String scoreboardTitle;
    private List<String> scoreboardLines;

    public ConfigManager(IceCloudTAB plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        loadTabConfig();
        loadScoreboardConfig();
    }

    private void loadTabConfig() {
        ConfigurationSection tabSection = config.getConfigurationSection("tab");
        if (tabSection == null) {
            tabEnabled = false;
            return;
        }
        this.tabEnabled = tabSection.getBoolean("enabled", true);
        this.tabUpdateInterval = tabSection.getInt("update-interval", 20);
        this.tabHeader = tabSection.getStringList("header");
        this.tabFooter = tabSection.getStringList("footer");
        if (this.tabHeader == null || this.tabHeader.isEmpty()) {
            List<String> def = new ArrayList<String>();
            def.add("&b&lIceCloud &f&lTAB");
            def.add("&7<gradient:#55ff55:#00aa00>Welcome %player%</gradient>");
            this.tabHeader = def;
        }
        if (this.tabFooter == null || this.tabFooter.isEmpty()) {
            List<String> defFooter = new ArrayList<String>();
            defFooter.add("&7Online: &a%online%&7/&a%max_players%");
            defFooter.add("&7Ping: &e%ping%ms &7| &7TPS: &6%tps%");
            this.tabFooter = defFooter;
        }
    }

    private void loadScoreboardConfig() {
        ConfigurationSection sbSection = config.getConfigurationSection("scoreboard");
        if (sbSection == null) {
            scoreboardEnabled = false;
            return;
        }
        this.scoreboardEnabled = sbSection.getBoolean("enabled", true);
        this.scoreboardUpdateInterval = sbSection.getInt("update-interval", 20);
        this.scoreboardTitle = sbSection.getString("title", "&b&lIceCloud &f&lServer");
        this.scoreboardLines = sbSection.getStringList("lines");
        if (this.scoreboardLines == null || this.scoreboardLines.isEmpty()) {
            this.scoreboardLines = new ArrayList<String>();
            this.scoreboardLines.add("&7&m------------------");
            this.scoreboardLines.add(" &fPlayer: &a%player%");
            this.scoreboardLines.add(" &fPing: &e%ping%ms");
            this.scoreboardLines.add("");
            this.scoreboardLines.add(" &fOnline: &a%online%&7/&a%max_players%");
            this.scoreboardLines.add(" &fTPS: &6%tps%");
            this.scoreboardLines.add(" &fWorld: &7%world%");
            this.scoreboardLines.add("&7&m------------------");
        }
    }

    public boolean isTabEnabled() {
        return tabEnabled;
    }

    public int getTabUpdateInterval() {
        return tabUpdateInterval;
    }

    public List<String> getTabHeader() {
        return tabHeader;
    }

    public List<String> getTabFooter() {
        return tabFooter;
    }

    public boolean isScoreboardEnabled() {
        return scoreboardEnabled;
    }

    public int getScoreboardUpdateInterval() {
        return scoreboardUpdateInterval;
    }

    public String getScoreboardTitle() {
        return scoreboardTitle;
    }

    public List<String> getScoreboardLines() {
        return scoreboardLines;
    }

}