package com.icecloud.tab.adapter;

import com.icecloud.tab.utils.ReflectionUtil;
import com.icecloud.tab.utils.VersionUtils;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class TabAdapterPost13 implements TabAdapter {

    private static Method setHeaderFooterStringMethod;
    private static Object adventureComponentEmpty;
    private static Method adventureSetHeaderFooterMethod;
    private static Class<?> adventureComponentClass;
    private static boolean adventureAvailable = false;

    static {
        initReflection();
    }

    private static void initReflection() {
        try {
            setHeaderFooterStringMethod = Player.class.getMethod(
                    "setPlayerListHeaderFooter", String.class, String.class);
        } catch (NoSuchMethodException ignored) {
        }
        try {
            adventureComponentClass = Class.forName("net.kyori.adventure.text.Component");
            Method emptyMethod = adventureComponentClass.getMethod("empty");
            adventureComponentEmpty = emptyMethod.invoke(null);
            Method playerMethod = Player.class.getMethod(
                    "setPlayerListHeaderFooter", adventureComponentClass, adventureComponentClass);
            adventureSetHeaderFooterMethod = playerMethod;
            adventureAvailable = true;
        } catch (Exception ignored) {
        }
    }

    @Override
    public void setHeaderFooter(Player player, String header, String footer) {
        if (setHeaderFooterStringMethod != null) {
            try {
                setHeaderFooterStringMethod.invoke(player, header, footer);
                return;
            } catch (Exception ignored) {
            }
        }
        if (adventureAvailable && adventureSetHeaderFooterMethod != null) {
            try {
                Object headerComponent = adventureComponentEmpty;
                Object footerComponent = adventureComponentEmpty;
                if (header != null && !header.isEmpty()) {
                    headerComponent = parseAdventureComponent(header);
                }
                if (footer != null && !footer.isEmpty()) {
                    footerComponent = parseAdventureComponent(footer);
                }
                adventureSetHeaderFooterMethod.invoke(player, headerComponent, footerComponent);
                return;
            } catch (Exception ignored) {
            }
        }
        try {
            player.setPlayerListHeaderFooter(header, footer);
        } catch (Exception ignored) {
        }
    }

    private Object parseAdventureComponent(String text) {
        try {
            Method serializerMethod = Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer")
                    .getMethod("legacySection");
            Object serializer = serializerMethod.invoke(null);
            Method deserialize = serializer.getClass().getMethod("deserialize", String.class);
            return deserialize.invoke(serializer, text);
        } catch (Exception ignored) {
        }
        try {
            Method textMethod = adventureComponentClass.getMethod("text", String.class);
            return textMethod.invoke(null, text);
        } catch (Exception ignored) {
        }
        return adventureComponentEmpty;
    }

    @Override
    public void clear(Player player) {
        if (adventureAvailable && adventureSetHeaderFooterMethod != null) {
            try {
                adventureSetHeaderFooterMethod.invoke(player,
                        adventureComponentEmpty, adventureComponentEmpty);
                return;
            } catch (Exception ignored) {
            }
        }
        try {
            player.setPlayerListHeaderFooter("", "");
        } catch (Exception ignored) {
        }
    }

    @Override
    public String getAdapterName() {
        return "1.13+ (Bukkit API / Adventure)";
    }

}