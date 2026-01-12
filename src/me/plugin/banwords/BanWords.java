package me.plugin.banwords;

import org.bukkit.plugin.java.JavaPlugin;

public class BanWords extends JavaPlugin {

    private String pendingConfirm;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getCommand("banword").setExecutor(new BanWordCommand(this));
        getCommand("banword").setTabCompleter(new BanWordTabCompleter(this));
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
    }

    public void setPendingConfirm(String name) {
        pendingConfirm = name;
    }

    public boolean confirm(String name) {
        if (pendingConfirm != null && pendingConfirm.equals(name)) {
            pendingConfirm = null;
            return true;
        }
        return false;
    }
}
