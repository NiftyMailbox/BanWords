package me.plugin.banwords;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BanWordTabCompleter implements TabCompleter {

    private final BanWords plugin;

    public BanWordTabCompleter(BanWords plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(
            CommandSender sender,
            Command command,
            String alias,
            String[] args) {

        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("banwords.admin")) {
            return completions;
        }

        /* /banword <subcommand> */
        if (args.length == 1) {
            return Arrays.asList(
                    "help",
                    "add",
                    "remove",
                    "edit",
                    "list",
                    "get",
                    "history",
                    "historyclear",
                    "confirm",
                    "exempt",
                    "exemptlist"
            );
        }

        /* /banword <sub> <arg> */
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {

                case "add":
                case "remove":
                case "edit": {
                    ConfigurationSection sec =
                            plugin.getConfig().getConfigurationSection("words");
                    if (sec != null) completions.addAll(sec.getKeys(false));
                    return completions;
                }

                case "get": {
                    ConfigurationSection history =
                            plugin.getConfig().getConfigurationSection("history");
                    if (history != null) completions.addAll(history.getKeys(false));
                    return completions;
                }

                case "exempt":
                    return Arrays.asList("add", "remove");
            }
        }

        /* /banword add|edit <word> <mute|ban> */
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("add")
                    || args[0].equalsIgnoreCase("edit")) {
                return Arrays.asList("mute", "ban");
            }
        }

        /* /banword add|edit <word> <type> <duration> */
        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("add")
                    || args[0].equalsIgnoreCase("edit")) {
                return Arrays.asList("5m", "10m", "30m", "1h", "1d", "perm");
            }
        }

        return completions;
    }
}
