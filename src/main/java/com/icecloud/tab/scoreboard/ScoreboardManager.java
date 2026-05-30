package com.icecloud.tab.scoreboard;

import com.icecloud.tab.IceCloudTAB;
import com.icecloud.tab.utils.ColorUtils;
import com.icecloud.tab.utils.VariableUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

public final class ScoreboardManager {

    private final IceCloudTAB plugin;
    private final List<Player> disabledPlayers;

    public ScoreboardManager(IceCloudTAB plugin) {
        this.plugin = plugin;
        this.disabledPlayers = new ArrayList<Player>();
    }

    public void updateScoreboards() {
        if (!plugin.getConfigManager().isScoreboardEnabled()) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (disabledPlayers.contains(player)) continue;
            updateScoreboardForPlayer(player);
        }
    }

    public void updateScoreboardForPlayer(Player player) {
        try {
            String title = plugin.getConfigManager().getScoreboardTitle();
            List<String> rawLines = plugin.getConfigManager().getScoreboardLines();

            title = VariableUtils.replace(player, title);
            title = ColorUtils.translate(title);

            List<String> processedLines = new ArrayList<String>();
            for (String line : rawLines) {
                line = VariableUtils.replace(player, line);
                line = ColorUtils.translate(line);
                processedLines.add(line);
            }

            Scoreboard scoreboard = player.getScoreboard();
            if (scoreboard == null || scoreboard == Bukkit.getScoreboardManager().getMainScoreboard()) {
                scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            }

            Objective objective = scoreboard.getObjective("ictab");
            if (objective == null) {
                objective = scoreboard.registerNewObjective("ictab", "dummy", title);
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            } else {
                objective.setDisplayName(title);
            }

            for (String entry : scoreboard.getEntries()) {
                scoreboard.resetScores(entry);
            }

            for (int i = 0; i < processedLines.size(); i++) {
                String line = processedLines.get(i);
                line = sanitizeLine(line, i);
                if (line.isEmpty()) {
                    line = "\u00A7" + (char) ('r' + (i % 10));
                }
                if (line.length() > 40) {
                    line = line.substring(0, 40);
                }
                int scoreValue = processedLines.size() - i;
                Score score = objective.getScore(line);
                score.setScore(scoreValue);
            }

            player.setScoreboard(scoreboard);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to update scoreboard for " + player.getName() + ": " + e.getMessage());
        }
    }

    public void setScoreboardEnabled(Player player, boolean enabled) {
        if (enabled) {
            disabledPlayers.remove(player);
            updateScoreboardForPlayer(player);
        } else {
            disabledPlayers.add(player);
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        }
    }

    public boolean isScoreboardEnabled(Player player) {
        return !disabledPlayers.contains(player);
    }

    public void removeScoreboard(Player player) {
        disabledPlayers.remove(player);
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    public void sendOnJoin(Player player) {
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    updateScoreboardForPlayer(player);
                }
            }
        }, 10L);
    }

    private String sanitizeLine(String line, int index) {
        if (line == null || line.isEmpty()) {
            return line;
        }
        String sanitized = line
                .replace("\u00A7k", "")
                .replace("\u00A7m", "")
                .replace("\u00A7n", "");
        if (sanitized.isEmpty()) {
            return "\u00A7" + (char) ('r' + (index % 10));
        }
        return sanitized;
    }

}