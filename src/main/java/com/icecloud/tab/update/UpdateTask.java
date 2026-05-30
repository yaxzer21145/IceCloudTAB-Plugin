package com.icecloud.tab.update;

import com.icecloud.tab.IceCloudTAB;
import org.bukkit.Bukkit;

public final class UpdateTask {

    private final IceCloudTAB plugin;
    private int taskId = -1;

    public UpdateTask(IceCloudTAB plugin) {
        this.plugin = plugin;
    }

    public void start() {
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getTabManager().updateTab();
                plugin.getScoreboardManager().updateScoreboards();
            }
        }, 20L, 20L).getTaskId();
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }

    public void restart() {
        stop();
        start();
    }

}