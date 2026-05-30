package com.icecloud.tab.adapter;

import com.icecloud.tab.utils.VersionUtils;
import org.bukkit.Bukkit;

public final class AdapterFactory {

    private static TabAdapter adapter = null;

    private AdapterFactory() {
    }

    public static TabAdapter createAdapter() {
        if (adapter != null) return adapter;

        VersionUtils.init();

        if (VersionUtils.isPre13()) {
            try {
                TabAdapterPre13 pre13Adapter = new TabAdapterPre13();
                Bukkit.getLogger().info("[IceCloudTAB] Loaded tab adapter: " + pre13Adapter.getAdapterName());
                adapter = pre13Adapter;
            } catch (Exception e) {
                Bukkit.getLogger().warning("[IceCloudTAB] Failed to load Pre-13 adapter, falling back to Post-13: " + e.getMessage());
                adapter = new TabAdapterPost13();
            }
        } else {
            try {
                TabAdapterPost13 post13Adapter = new TabAdapterPost13();
                Bukkit.getLogger().info("[IceCloudTAB] Loaded tab adapter: " + post13Adapter.getAdapterName());
                adapter = post13Adapter;
            } catch (Exception e) {
                Bukkit.getLogger().warning("[IceCloudTAB] Failed to load Post-13 adapter: " + e.getMessage());
                adapter = new TabAdapterPost13();
            }
        }

        return adapter;
    }

    public static TabAdapter getAdapter() {
        if (adapter == null) {
            return createAdapter();
        }
        return adapter;
    }

}