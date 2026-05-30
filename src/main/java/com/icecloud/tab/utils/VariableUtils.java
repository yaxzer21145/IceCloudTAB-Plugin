package com.icecloud.tab.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VariableUtils {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("%([^%]+)%");
    private static boolean papiEnabled = false;
    private static final DecimalFormat TPS_FORMAT = new DecimalFormat("#.##");
    private static Method getOnlinePlayersMethod;

    private VariableUtils() {
    }

    public static void setPapiEnabled(boolean enabled) {
        papiEnabled = enabled;
    }

    public static String replace(Player player, String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String result = replaceBuiltIn(player, text);
        if (papiEnabled) {
            try {
                Class<?> papiClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
                Method setPlaceholders = papiClass.getMethod("setPlaceholders", Player.class, String.class);
                result = (String) setPlaceholders.invoke(null, player, result);
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    private static String replaceBuiltIn(Player player, String text) {
        Matcher matcher = VARIABLE_PATTERN.matcher(text);
        StringBuilder buffer = new StringBuilder();
        int lastEnd = 0;
        while (matcher.find()) {
            buffer.append(text, lastEnd, matcher.start());
            String varName = matcher.group(1).toLowerCase();
            buffer.append(getVariableValue(player, varName));
            lastEnd = matcher.end();
        }
        buffer.append(text.substring(lastEnd));
        return buffer.toString();
    }

    private static String getVariableValue(Player player, String varName) {
        switch (varName) {
            case "player":
            case "name":
                return player.getName();
            case "displayname":
            case "display_name":
                return player.getDisplayName();
            case "uuid":
                return player.getUniqueId().toString();
            case "world":
                return player.getWorld().getName();
            case "online":
            case "online_players":
                return String.valueOf(getOnlinePlayerCount());
            case "max_players":
            case "maxplayers":
                return String.valueOf(Bukkit.getMaxPlayers());
            case "ping":
                return String.valueOf(getPlayerPing(player));
            case "x":
                return String.valueOf(player.getLocation().getBlockX());
            case "y":
                return String.valueOf(player.getLocation().getBlockY());
            case "z":
                return String.valueOf(player.getLocation().getBlockZ());
            case "health":
            case "hp":
                return String.valueOf((int) player.getHealth());
            case "max_health":
            case "maxhealth":
                return String.valueOf((int) player.getMaxHealth());
            case "food":
            case "food_level":
                return String.valueOf(player.getFoodLevel());
            case "level":
            case "xp_level":
                return String.valueOf(player.getLevel());
            case "gamemode":
            case "game_mode":
                return player.getGameMode().name();
            case "ip":
            case "address":
                try {
                    if (player.getAddress() != null) {
                        return player.getAddress().getAddress().getHostAddress();
                    }
                } catch (Exception ignored) {
                }
                return "unknown";
            case "tps":
                try {
                    Method tpsMethod = Bukkit.class.getMethod("getTPS");
                    double[] tps = (double[]) tpsMethod.invoke(null);
                    return TPS_FORMAT.format(tps[0]);
                } catch (Exception e) {
                    return "20.00";
                }
            case "time":
                try {
                    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                } catch (Exception e) {
                    return "00:00:00";
                }
            case "date":
                try {
                    return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                } catch (Exception e) {
                    return "2024/01/01";
                }
            case "online_players_list":
            case "onlineplayers":
                StringBuilder sb = new StringBuilder();
                int count = 0;
                for (Player p : getOnlinePlayers()) {
                    if (count > 0) {
                        sb.append("§7, ");
                    }
                    sb.append("§a").append(p.getName());
                    count++;
                }
                return sb.toString();
            default:
                return "%" + varName + "%";
        }
    }

    private static int getOnlinePlayerCount() {
        try {
            Method getOnlinePlayersMethod = Bukkit.class.getMethod("getOnlinePlayers");
            Object result = getOnlinePlayersMethod.invoke(null);
            if (result instanceof Player[]) {
                return ((Player[]) result).length;
            } else if (result instanceof java.util.Collection) {
                return ((java.util.Collection<?>) result).size();
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    private static Player[] getOnlinePlayers() {
        try {
            if (getOnlinePlayersMethod == null) {
                try {
                    getOnlinePlayersMethod = Bukkit.class.getMethod("getOnlinePlayers");
                } catch (NoSuchMethodException e) {
                    return new Player[0];
                }
            }
            Object result = getOnlinePlayersMethod.invoke(null);
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

    private static int getPlayerPing(Player player) {
        try {
            Method pingMethod = Player.class.getMethod("getPing");
            return (int) pingMethod.invoke(player);
        } catch (Exception e) {
            return 0;
        }
    }

}