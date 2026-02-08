package me.plugin.banwords;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
            sender.sendMessage("§7Source code: §dhttps://github.com/NiftyMailbox/BanWords");
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

            case "edit":
                if (args.length != 4) {
                    sender.sendMessage("§cIncorrect use: /banword edit <word> <mute|ban> <duration>");
                    return true;
                }

                ConfigurationSection words =
                        plugin.getConfig().getConfigurationSection("words");

                if (words == null || !words.contains(args[1])) {
                    sender.sendMessage("§cThat word is not currently banned.");
                    return true;
                }

                plugin.getConfig().set("words." + args[1] + ".type", args[2].toUpperCase());
                plugin.getConfig().set("words." + args[1] + ".duration", args[3]);
                plugin.saveConfig();

                sender.sendMessage("§aUpdated banned word §f" + args[1]);
                sender.sendMessage("§7New punishment: §e" + args[2].toUpperCase()
                        + " §7Duration: §e" + args[3]);

                return true;

            case "list":
                ConfigurationSection sec = plugin.getConfig().getConfigurationSection("words");
                sender.sendMessage("§eBanned Words:");
                if (sec != null)
                    for (String w : sec.getKeys(false))
                        sender.sendMessage("§7" + w + " → §e" +
                                sec.getString(w + ".type") + " §7" +
                                sec.getString(w + ".duration"));
                return true;

            case "get":
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /banword get <player>");
                    return true;
                }

                String target = args[1];
                ConfigurationSection name =
                        plugin.getConfig().getConfigurationSection("history." + target);

                sender.sendMessage("§d" + target + "§f §7's history:");

                if (name == null || name.getKeys(false).isEmpty()) {
                    sender.sendMessage("§7- No history found.");
                    return true;
                }

                for (String w : name.getKeys(false)) {
                    String type = name.getString(w + ".type");
                    String duration = name.getString(w + ".duration");

                    sender.sendMessage("§7- §c" + type.toUpperCase()
                            + " §7for §f\"" + w + "\" §7(" + duration + ")");
                }
                return true;

            case "history":
                ConfigurationSection history =
                        plugin.getConfig().getConfigurationSection("history");

                int days = plugin.getConfig().getInt("history-visible-days", 14);
                long cutoff =
                        System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days);

                sender.sendMessage("§eBanWords History (§7last " + days + " days§e):");

                if (history == null || history.getKeys(false).isEmpty()) {
                    sender.sendMessage("§7- No history found.");
                    return true;
                }

                boolean shownAny = false;

                for (String player : history.getKeys(false)) {
                    ConfigurationSection playerSec =
                            history.getConfigurationSection(player);
                    if (playerSec == null) continue;

                    for (String word : playerSec.getKeys(false)) {
                        long time = playerSec.getLong(word + ".time", 0);
                        if (time < cutoff) continue;

                        String type = playerSec.getString(word + ".type");
                        String duration = playerSec.getString(word + ".duration");

                        long minutesAgo =
                                TimeUnit.MILLISECONDS.toMinutes(
                                        System.currentTimeMillis() - time);

                        sender.sendMessage(
                                "§7- §d" + player +
                                        " §7→ §c" + type +
                                        " §7for §f\"" + word +
                                        "\" §7(" + duration +
                                        ", " + minutesAgo + "m ago)"
                        );

                        shownAny = true;
                    }
                }

                if (!shownAny) {
                    sender.sendMessage("§7- No recent history to show.");
                }
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

            case "exempt":
                if (args.length != 3) {
                    sender.sendMessage("§cIncorrect use: /banword exempt <add/remove> <player>");
                    return true;
                }

                List<String> exempt = plugin.getConfig().getStringList("exempt");

                if (args[1].equalsIgnoreCase("add")) {
                    if (!exempt.contains(args[2])) {
                        exempt.add(args[2]);
                        plugin.getConfig().set("exempt", exempt);
                        plugin.saveConfig();
                        sender.sendMessage("§aExempted §f" + args[2] + " §afrom BanWords.");
                    } else {
                        sender.sendMessage("§eThat player is already exempt.");
                    }
                    return true;
                }

                if (args[1].equalsIgnoreCase("remove")) {
                    if (exempt.remove(args[2])) {
                        plugin.getConfig().set("exempt", exempt);
                        plugin.saveConfig();
                        sender.sendMessage("§cRemoved exemption for §f" + args[2]);
                    } else {
                        sender.sendMessage("§eThat player is not exempt.");
                    }
                    return true;
                }

                sender.sendMessage("§cIncorrect use: /banword exempt <add/remove> <player>");
                return true;

            case "exemptlist":
                List<String> list = plugin.getConfig().getStringList("exempt");
                sender.sendMessage("§eExempt Players:");
                if (list.isEmpty()) {
                    sender.sendMessage("§7- None");
                } else {
                    for (String p : list) {
                        sender.sendMessage("§7- §f" + p);
                    }
                }
                return true;

            default:
                sender.sendMessage("§cUnknown argument.");
                sender.sendMessage("§eUse /banword help for a list of commands.");
                return true;
        }
    }
}
