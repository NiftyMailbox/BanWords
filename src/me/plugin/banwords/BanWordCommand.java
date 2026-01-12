package me.plugin.banwords;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

public class BanWordCommand implements CommandExecutor {

    private final BanWords plugin;

    public BanWordCommand(BanWords plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!sender.hasPermission("banwords.admin")) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§6BanWords");
            sender.sendMessage("§7Created by §dUncommonWindmill");
            sender.sendMessage("§7Use §e/banword help §7for uses");
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "help":
                sender.sendMessage("§eBanWords Commands:");
                sender.sendMessage("§7/banword add <word> <mute|ban> <duration>");
                sender.sendMessage("§7/banword remove <word>");
                sender.sendMessage("§7/banword edit <word> <mute|ban> <duration>");
                sender.sendMessage("§7/banword list");
                sender.sendMessage("§7/banword get <player>");
                sender.sendMessage("§7/banword history");
                sender.sendMessage("§7/banword historyclear");
                sender.sendMessage("§7/banword exempt <add|remove> <player>");
                sender.sendMessage("§7/banword exemptlist");
                return true;

            case "add":
                if (args.length != 4) {
                    sender.sendMessage("§cIncorrect use: /banword add <word> <mute/ban> <duration>");
                    return true;
                }
                plugin.getConfig().set("words." + args[1] + ".type", args[2].toUpperCase());
                plugin.getConfig().set("words." + args[1] + ".duration", args[3]);
                plugin.saveConfig();
                sender.sendMessage("§aAdded banned word §f" + args[1]);
                sender.sendMessage("§7Punishment: §e" + args[2].toUpperCase() + " §7Duration: §e" + args[3]);
                return true;

            case "remove":
                plugin.getConfig().set("words." + args[1], null);
                plugin.saveConfig();
                sender.sendMessage("§cRemoved banned word §f" + args[1]);
                return true;

            case "list":
                ConfigurationSection sec = plugin.getConfig().getConfigurationSection("words");
                sender.sendMessage("§eBanned Words:");
                if (sec != null)
                    for (String w : sec.getKeys(false))
                        sender.sendMessage("§7" + w + " → §e" + sec.getString(w + ".type") + " §7" + sec.getString(w + ".duration"));
                return true;

            case "historyclear":
                plugin.setPendingConfirm(sender.getName());
                sender.sendMessage("§cThis will permanently clear BanWords history.");
                sender.sendMessage("§eType /banword confirm to continue.");
                return true;

            case "confirm":
                if (plugin.confirm(sender.getName())) {
                    plugin.getConfig().set("history", null);
                    plugin.saveConfig();
                    sender.sendMessage("§aBanWords history has been cleared.");
                } else sender.sendMessage("§cNothing to confirm.");
                return true;

            default:
                sender.sendMessage("§cUnknown argument.");
                sender.sendMessage("§eUse /banword help for a list of commands.");
                return true;
        }
    }
}
