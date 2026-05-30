package com.icecloud.tab.utils;

import org.bukkit.Bukkit;
import org.bukkit.Server;

public final class VersionUtils {

    private static final String UNKNOWN = "unknown";
    private static String minecraftVersion = null;
    private static String nmsVersion = null;
    private static int majorVersion = -1;
    private static int minorVersion = -1;
    private static String serverSoftware = null;
    private static boolean versionInitialized = false;

    private VersionUtils() {
    }

    public static void init() {
        if (versionInitialized) return;
        detectMinecraftVersion();
        detectNMSVersion();
        detectServerSoftware();
        versionInitialized = true;
    }

    private static void detectMinecraftVersion() {
        try {
            String bukkitVersion = Bukkit.getBukkitVersion();
            String[] parts = bukkitVersion.split("-")[0].split("\\.");
            if (parts.length >= 2) {
                majorVersion = Integer.parseInt(parts[0]);
                minorVersion = Integer.parseInt(parts[1]);
            }
            minecraftVersion = majorVersion + "." + minorVersion;
        } catch (Exception e) {
            minecraftVersion = "1.8";
            majorVersion = 1;
            minorVersion = 8;
        }
    }

    private static void detectNMSVersion() {
        nmsVersion = tryDetectNMSFromPackage();
        if (nmsVersion == null || !nmsVersion.startsWith("v")) {
            nmsVersion = tryDetectNMSFromCraftBukkit();
        }
        if (nmsVersion == null || !nmsVersion.startsWith("v")) {
            nmsVersion = constructNMSFromGameVersion();
        }
        if (nmsVersion == null || !nmsVersion.startsWith("v")) {
            nmsVersion = "v1_8_R3";
        }
    }

    private static String tryDetectNMSFromPackage() {
        try {
            Server server = Bukkit.getServer();
            String packageName = server.getClass().getPackage().getName();
            String extracted = packageName.substring(packageName.lastIndexOf('.') + 1);
            if (extracted.startsWith("v")) {
                return extracted;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String tryDetectNMSFromCraftBukkit() {
        try {
            String serverClassName = Bukkit.getServer().getClass().getName();
            if (serverClassName.contains("craftbukkit")) {
                int idx = serverClassName.indexOf("craftbukkit.");
                if (idx != -1) {
                    String after = serverClassName.substring(idx + "craftbukkit.".length());
                    String potential = after.split("\\.")[0];
                    if (potential.startsWith("v")) {
                        return potential;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String constructNMSFromGameVersion() {
        if (majorVersion <= 0 || minorVersion < 0) return null;
        return "v" + majorVersion + "_" + minorVersion + "_R1";
    }

    private static void detectServerSoftware() {
        try {
            String name = Bukkit.getServer().getName();
            if (name != null) {
                serverSoftware = name;
            }
        } catch (Exception ignored) {
        }
    }

    public static String getMinecraftVersion() {
        if (!versionInitialized) init();
        return minecraftVersion;
    }

    public static String getNMSVersion() {
        if (!versionInitialized) init();
        return nmsVersion;
    }

    public static String getServerSoftware() {
        if (!versionInitialized) init();
        return serverSoftware;
    }

    public static int getMajorVersion() {
        if (!versionInitialized) init();
        return majorVersion;
    }

    public static int getMinorVersion() {
        if (!versionInitialized) init();
        return minorVersion;
    }

    public static boolean isVersionAtLeast(int major, int minor) {
        if (!versionInitialized) init();
        if (majorVersion > major) return true;
        if (majorVersion < major) return false;
        return minorVersion >= minor;
    }

    public static boolean isVersionAtLeast(int minor) {
        return isVersionAtLeast(1, minor);
    }

    public static boolean isPre13() {
        return !isVersionAtLeast(13);
    }

    public static boolean isPre16() {
        return !isVersionAtLeast(16);
    }

    public static boolean isLegacy() {
        return !isVersionAtLeast(13);
    }

    public static boolean isPurpur() {
        if (!versionInitialized) init();
        return serverSoftware != null && serverSoftware.equalsIgnoreCase("Purpur");
    }

    public static boolean isPaper() {
        if (!versionInitialized) init();
        return serverSoftware != null && serverSoftware.equalsIgnoreCase("Paper");
    }

    public static boolean isFolia() {
        if (!versionInitialized) init();
        return serverSoftware != null && serverSoftware.equalsIgnoreCase("Folia");
    }

    public static int getProtocolVersion() {
        if (isVersionAtLeast(21)) return 4;
        if (isVersionAtLeast(20, 5)) return 4;
        if (isVersionAtLeast(20)) return 4;
        if (isVersionAtLeast(16)) return 3;
        if (isVersionAtLeast(13)) return 2;
        if (isVersionAtLeast(11)) return 1;
        return 0;
    }

}