package com.icecloud.tab.tab;

import com.icecloud.tab.IceCloudTAB;
import com.icecloud.tab.adapter.AdapterFactory;
import com.icecloud.tab.adapter.TabAdapter;
import com.icecloud.tab.utils.ColorUtils;
import com.icecloud.tab.utils.VariableUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public final class TabManager {

    private final IceCloudTAB plugin;
    private final TabAdapter adapter;

    public TabManager(IceCloudTAB plugin) {
        this.plugin = plugin;
        this.adapter = AdapterFactory.createAdapter();
    }

    public void updateTab() {
        if (!plugin.getConfigManager().isTabEnabled()) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateTabForPlayer(player);
        }
    }

    public void updateTabForPlayer(Player player) {
        try {
            String header = buildText(player, plugin.getConfigManager().getTabHeader());
            String footer = buildText(player, plugin.getConfigManager().getTabFooter());
            adapter.setHeaderFooter(player, header, footer);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to update tab for " + player.getName() + ": " + e.getMessage());
        }
    }

    private String buildText(Player player, List<String> lines) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            line = VariableUtils.replace(player, line);
            line = ColorUtils.translate(line);
            builder.append(line);
            if (i < lines.size() - 1) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    public void sendOnJoin(Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    updateTabForPlayer(player);
                }
            }
        }, 5L);
    }

    public TabAdapter getAdapter() {
        return adapter;
    }

}