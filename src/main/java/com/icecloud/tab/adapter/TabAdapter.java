package com.icecloud.tab.adapter;

import org.bukkit.entity.Player;

public interface TabAdapter {

    void setHeaderFooter(Player player, String header, String footer);

    void clear(Player player);

    String getAdapterName();

}