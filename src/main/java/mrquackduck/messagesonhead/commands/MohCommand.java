package mrquackduck.messagesonhead.commands;

import mrquackduck.messagesonhead.MessagesOnHeadPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MohCommand implements CommandExecutor, TabCompleter {
    private final MessagesOnHeadPlugin plugin;

    public MohCommand(MessagesOnHeadPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("info")) {
            return new InfoCommand().onCommand(commandSender, command, s, args);
        }
        else if (args[0].equalsIgnoreCase("say") && args.length >= 3 && commandSender.hasPermission("messagesonhead.admin")) {
            return new SayCommand(plugin).onCommand(commandSender, command, s, args);
        }
        else if (args[0].equalsIgnoreCase("reload") && commandSender.hasPermission("messagesonhead.admin")) {
            return new ReloadCommand(plugin).onCommand(commandSender, command, s, args);
        }

        commandSender.sendMessage(MessagesOnHeadPlugin.getMessage("command-not-found"));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> options = new ArrayList<>();
        List<String> completions = new ArrayList<>();

        if (args.length == 2 && args[0].equals("say") && commandSender.hasPermission("messagesonhead.admin")) {
            for (Player p : Bukkit.getOnlinePlayers()) options.add(p.getName());
            StringUtil.copyPartialMatches(args[1], options, completions);
            return completions;
        }
        if (args.length != 1) return completions;

        if (commandSender.hasPermission("messagesonhead.admin")) options.add("reload");
        if (commandSender.hasPermission("messagesonhead.admin")) options.add("info");
        if (commandSender.hasPermission("messagesonhead.admin")) options.add("say");

        StringUtil.copyPartialMatches(args[0], options, completions);
        return completions;
    }
}
