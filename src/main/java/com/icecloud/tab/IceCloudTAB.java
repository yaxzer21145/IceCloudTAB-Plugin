package com.icecloud.tab;

import com.icecloud.tab.adapter.AdapterFactory;
import com.icecloud.tab.adapter.TabAdapter;
import com.icecloud.tab.config.ConfigManager;
import com.icecloud.tab.scoreboard.ScoreboardManager;
import com.icecloud.tab.tab.TabManager;
import com.icecloud.tab.update.UpdateTask;
import com.icecloud.tab.utils.ColorUtils;
import com.icecloud.tab.utils.VariableUtils;
import com.icecloud.tab.utils.VersionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public final class IceCloudTAB extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    private ConfigManager configManager;
    private TabManager tabManager;
    private ScoreboardManager scoreboardManager;
    private UpdateTask updateTask;

    @Override
    public void onEnable() {
        VersionUtils.init();
        ColorUtils.init();

        saveDefaultConfig();
        this.configManager = new ConfigManager(this);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            VariableUtils.setPapiEnabled(true);
            getLogger().info("PlaceholderAPI detected - PlaceholderAPI variable support enabled");
        }

        this.tabManager = new TabManager(this);
        this.scoreboardManager = new ScoreboardManager(this);

        TabAdapter adapter = AdapterFactory.getAdapter();
        getLogger().info("Server version: " + VersionUtils.getMinecraftVersion()
                + " (NMS: " + VersionUtils.getNMSVersion() + ")");
        getLogger().info("Tab adapter: " + adapter.getAdapterName());

        getServer().getPluginManager().registerEvents(this, this);

        getCommand("icecloudtab").setExecutor(this);
        getCommand("icecloudtab").setTabCompleter(this);

        this.updateTask = new UpdateTask(this);
        this.updateTask.start();

        getLogger().info("IceCloudTAB v" + getDescription().getVersion() + " enabled successfully");
        getLogger().info("Tab: " + (configManager.isTabEnabled() ? "ON" : "OFF")
                + " | Scoreboard: " + (configManager.isScoreboardEnabled() ? "ON" : "OFF"));
    }

    @Override
    public void onDisable() {
        if (updateTask != null) {
            updateTask.stop();
        }

        TabAdapter adapter = AdapterFactory.getAdapter();
        for (Player player : getOnlinePlayers()) {
            try {
                if (configManager != null && configManager.isTabEnabled()) {
                    adapter.clear(player);
                }
            } catch (Exception ignored) {
            }
            try {
                player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            } catch (Exception ignored) {
            }
        }
        getLogger().info("IceCloudTAB disabled successfully");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if (configManager.isTabEnabled()) {
            tabManager.sendOnJoin(player);
        }
        if (configManager.isScoreboardEnabled()) {
            scoreboardManager.sendOnJoin(player);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        scoreboardManager.removeScoreboard(event.getPlayer());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("icecloudtab.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command");
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            configManager.reload();
            updateTask.restart();
            TabAdapter adapter = AdapterFactory.getAdapter();
            for (Player player : getOnlinePlayers()) {
                if (configManager.isTabEnabled()) {
                    tabManager.sendOnJoin(player);
                } else {
                    adapter.clear(player);
                }
                if (configManager.isScoreboardEnabled()) {
                    scoreboardManager.sendOnJoin(player);
                } else {
                    player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
                }
            }
            sender.sendMessage(ChatColor.GREEN + "IceCloudTAB configuration reloaded successfully");
            return true;
        }
        if (args[0].equalsIgnoreCase("toggle")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                boolean enabled = !scoreboardManager.isScoreboardEnabled(player);
                scoreboardManager.setScoreboardEnabled(player, enabled);
                sender.sendMessage(ChatColor.GREEN + "Scoreboard: "
                        + (enabled ? ChatColor.GREEN + "ON" : ChatColor.RED + "OFF"));
            } else {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players");
            }
            return true;
        }
        sendHelp(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<String>();
        if (args.length == 1) {
            completions.add("reload");
            completions.add("toggle");
            List<String> result = new ArrayList<String>();
            String prefix = args[0].toLowerCase();
            for (String s : completions) {
                if (s.startsWith(prefix)) {
                    result.add(s);
                }
            }
            return result;
        }
        return completions;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.AQUA + "=== IceCloudTAB Help ===");
        sender.sendMessage(ChatColor.GOLD + "/ictab reload " + ChatColor.GRAY + "Reload configuration");
        sender.sendMessage(ChatColor.GOLD + "/ictab toggle " + ChatColor.GRAY + "Toggle scoreboard visibility");
    }

    private Player[] getOnlinePlayers() {
        try {
            Method method = Bukkit.class.getMethod("getOnlinePlayers");
            Object result = method.invoke(null);
            if (result instanceof Player[]) {
                return (Player[]) result;
            } else if (result instanceof java.util.Collection) {
                java.util.Collection<?> collection = (java.util.Collection<?>) result;
                return collection.toArray(new Player[0]);
            }
        } catch (Exception ignored) {
        }
        return new Player[0];
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public TabManager getTabManager() {
        return tabManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

}