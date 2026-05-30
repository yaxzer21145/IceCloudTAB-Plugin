package com.icecloud.tab.adapter;

import com.icecloud.tab.utils.ReflectionUtil;
import com.icecloud.tab.utils.VersionUtils;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class TabAdapterPre13 implements TabAdapter {

    private static Class<?> packetClass;
    private static Class<?> headerFooterClass;
    private static Class<?> iChatBaseComponentClass;
    private static Class<?> chatComponentTextClass;
    private static Method getHandleMethod;
    private static Method sendPacketMethod;
    private static Object playerConnectionField;

    static {
        try {
            packetClass = ReflectionUtil.getNMSClass("PacketPlayOutPlayerListHeaderFooter");
            iChatBaseComponentClass = ReflectionUtil.getNMSClass("IChatBaseComponent");
            chatComponentTextClass = ReflectionUtil.getNMSClass("ChatComponentText");

            if (packetClass == null) {
                String nmsVersion = VersionUtils.getNMSVersion();
                if (nmsVersion != null && nmsVersion.startsWith("v1_7")) {
                    packetClass = ReflectionUtil.getClass(
                            "net.minecraft.server." + nmsVersion + ".PacketPlayOutPlayerListHeaderFooter");
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void setHeaderFooter(Player player, String header, String footer) {
        try {
            Object headerComponent = createChatComponent(header);
            Object footerComponent = createChatComponent(footer);
            if (headerComponent == null || footerComponent == null) return;

            Object packet = null;
            if (VersionUtils.isVersionAtLeast(8)) {
                try {
                    packet = ReflectionUtil.newInstance(packetClass, headerComponent, footerComponent);
                } catch (Exception ignored) {
                }
            }

            if (packet == null) {
                packet = ReflectionUtil.newInstance(packetClass);
                if (packet != null) {
                    try {
                        java.lang.reflect.Field headerField = packetClass.getDeclaredField("a");
                        headerField.setAccessible(true);
                        headerField.set(packet, headerComponent);

                        java.lang.reflect.Field footerField = packetClass.getDeclaredField("b");
                        footerField.setAccessible(true);
                        footerField.set(packet, footerComponent);
                    } catch (Exception ignored) {
                    }
                }
            }

            if (packet != null) {
                sendPacket(player, packet);
            }
        } catch (Exception ignored) {
        }
    }

    private Object createChatComponent(String text) {
        try {
            if (chatComponentTextClass != null) {
                return ReflectionUtil.newInstance(chatComponentTextClass, text);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private void sendPacket(Player player, Object packet) {
        try {
            if (getHandleMethod == null) {
                Object craftPlayer = ReflectionUtil.getCraftBukkitClass("entity.CraftPlayer");
                if (craftPlayer != null) {
                    getHandleMethod = ReflectionUtil.getMethod((Class<?>) craftPlayer, "getHandle");
                }
            }
            Object entityPlayer = ReflectionUtil.invokeMethod(player, "getHandle");
            if (entityPlayer == null && getHandleMethod != null) {
                entityPlayer = getHandleMethod.invoke(player);
            }
            if (entityPlayer == null) return;

            if (playerConnectionField == null) {
                playerConnectionField = ReflectionUtil.getField(entityPlayer.getClass(), "playerConnection");
            }
            Object playerConnection = ((java.lang.reflect.Field) playerConnectionField).get(entityPlayer);

            if (sendPacketMethod == null) {
                sendPacketMethod = ReflectionUtil.getMethod(playerConnection.getClass(), "sendPacket", ReflectionUtil.getNMSClass("Packet"));
            }
            if (sendPacketMethod != null) {
                sendPacketMethod.invoke(playerConnection, packet);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void clear(Player player) {
        setHeaderFooter(player, "", "");
    }

    @Override
    public String getAdapterName() {
        return "Pre-1.13 (Packet-Based)";
    }

}