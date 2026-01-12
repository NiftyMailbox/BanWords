package me.plugin.banwords;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

public class ChatListener implements Listener {

    private final BanWords plugin;

    public ChatListener(BanWords plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();

        if (p.hasPermission("banwords.bypass")) return;

        List<String> exempt = plugin.getConfig().getStringList("exempt");
        if (exempt.contains(p.getName())) return;

        ConfigurationSection words = plugin.getConfig().getConfigurationSection("words");
        if (words == null) return;

        String msg = e.getMessage().toLowerCase();

        for (String word : words.getKeys(false)) {
            if (!msg.contains(word.toLowerCase())) continue;

            e.setCancelled(true);

            String type = words.getString(word + ".type");
            String duration = words.getString(word + ".duration");

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (type.equalsIgnoreCase("mute")) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            "mute " + p.getName() + " " + duration + " BanWords");
                } else {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            "ban " + p.getName() + " BanWords");
                }
            });

            logHistory(p.getName(), word, type, duration);
            return;
        }
    }

    private void logHistory(String player, String word, String type, String duration) {
        long now = System.currentTimeMillis();
        String path = "history." + player + "." + now;

        plugin.getConfig().set(path + ".word", word);
        plugin.getConfig().set(path + ".type", type);
        plugin.getConfig().set(path + ".duration", duration);
        plugin.getConfig().set(path + ".time", now);
        plugin.saveConfig();
    }
}
