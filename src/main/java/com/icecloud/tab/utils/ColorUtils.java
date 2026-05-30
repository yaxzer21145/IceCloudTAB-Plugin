package com.icecloud.tab.utils;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtils {

    private static final Pattern HEX_PATTERN = Pattern.compile("#([a-fA-F0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile(
            "<gradient:([^>]+)>([^<]*)</gradient>"
    );

    private static boolean hexSupported = false;
    private static boolean colorInitialized = false;

    private ColorUtils() {
    }

    public static void init() {
        if (colorInitialized) return;
        hexSupported = VersionUtils.isVersionAtLeast(16);
        colorInitialized = true;
    }

    public static String translate(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String parsed = text;
        parsed = parseGradients(parsed);
        if (hexSupported) {
            parsed = parseHexColors(parsed);
        } else {
            parsed = stripHexColors(parsed);
        }
        parsed = ChatColor.translateAlternateColorCodes('&', parsed);
        return parsed;
    }

    public static List<String> translate(List<String> texts) {
        List<String> result = new ArrayList<>();
        for (String text : texts) {
            result.add(translate(text));
        }
        return result;
    }

    private static String parseHexColors(String text) {
        try {
            Class<?> chatColorClass = ChatColor.class;
            java.lang.reflect.Method ofMethod = chatColorClass.getMethod("of", String.class);
            Matcher matcher = HEX_PATTERN.matcher(text);
            StringBuilder buffer = new StringBuilder();
            while (matcher.find()) {
                String hex = matcher.group();
                try {
                    Object chatColor = ofMethod.invoke(null, hex);
                    matcher.appendReplacement(buffer,
                            Matcher.quoteReplacement(chatColor.toString()));
                } catch (Exception e) {
                    matcher.appendReplacement(buffer, hex);
                }
            }
            matcher.appendTail(buffer);
            return buffer.toString();
        } catch (Exception e) {
            return stripHexColors(text);
        }
    }

    private static String stripHexColors(String text) {
        return HEX_PATTERN.matcher(text).replaceAll("");
    }

    private static String parseGradients(String text) {
        Matcher matcher = GRADIENT_PATTERN.matcher(text);
        StringBuilder buffer = new StringBuilder();
        int lastEnd = 0;
        while (matcher.find()) {
            buffer.append(text, lastEnd, matcher.start());
            String colorsPart = matcher.group(1);
            String content = matcher.group(2);
            String[] colorStrs = colorsPart.split(":");
            List<java.awt.Color> colors = new ArrayList<>();
            for (String cs : colorStrs) {
                cs = cs.trim();
                if (cs.startsWith("#")) {
                    try {
                        colors.add(java.awt.Color.decode(cs));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            if (colors.size() >= 2 && !content.isEmpty()) {
                buffer.append(applyGradientFallback(content, colors));
            } else {
                buffer.append(content);
            }
            lastEnd = matcher.end();
        }
        buffer.append(text.substring(lastEnd));
        return buffer.toString();
    }

    private static String applyGradientFallback(String text, List<java.awt.Color> colors) {
        if (text.isEmpty() || colors == null || colors.size() < 2) {
            return text;
        }
        if (hexSupported) {
            return applyGradientWithHex(text, colors);
        }
        return applyGradientWithoutHex(text, colors);
    }

    private static String applyGradientWithHex(String text, List<java.awt.Color> colors) {
        try {
            Class<?> chatColorClass = ChatColor.class;
            java.lang.reflect.Method ofMethod = chatColorClass.getMethod("of", String.class);
            int length = text.length();
            int segments = colors.size() - 1;
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < length; i++) {
                float ratio = (float) i / Math.max(length - 1, 1);
                int segmentIndex = Math.min((int) (ratio * segments), segments - 1);
                float segmentRatio = (ratio * segments) - segmentIndex;
                java.awt.Color from = colors.get(segmentIndex);
                java.awt.Color to = colors.get(Math.min(segmentIndex + 1, colors.size() - 1));
                int r = clamp(from.getRed() + (int) ((to.getRed() - from.getRed()) * segmentRatio));
                int g = clamp(from.getGreen() + (int) ((to.getGreen() - from.getGreen()) * segmentRatio));
                int b = clamp(from.getBlue() + (int) ((to.getBlue() - from.getBlue()) * segmentRatio));
                String hex = String.format("#%02x%02x%02x", r, g, b);
                try {
                    Object color = ofMethod.invoke(null, hex);
                    result.append(color.toString());
                } catch (Exception e) {
                    result.append(text.charAt(i));
                }
                result.append(text.charAt(i));
            }
            return result.toString();
        } catch (Exception e) {
            return applyGradientWithoutHex(text, colors);
        }
    }

    private static String applyGradientWithoutHex(String text, List<java.awt.Color> colors) {
        StringBuilder result = new StringBuilder();
        ChatColor[] chatColors = {
                ChatColor.BLACK, ChatColor.DARK_BLUE, ChatColor.DARK_GREEN,
                ChatColor.DARK_AQUA, ChatColor.DARK_RED, ChatColor.DARK_PURPLE,
                ChatColor.GOLD, ChatColor.GRAY, ChatColor.DARK_GRAY,
                ChatColor.BLUE, ChatColor.GREEN, ChatColor.AQUA,
                ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, ChatColor.WHITE
        };
        int length = text.length();
        for (int i = 0; i < length; i++) {
            float ratio = (float) i / Math.max(length - 1, 1);
            int colorIndex = Math.min((int) (ratio * (chatColors.length - 1)), chatColors.length - 1);
            result.append(chatColors[colorIndex]).append(text.charAt(i));
        }
        return result.toString();
    }

    private static int clamp(int value) {
        return Math.min(255, Math.max(0, value));
    }

}