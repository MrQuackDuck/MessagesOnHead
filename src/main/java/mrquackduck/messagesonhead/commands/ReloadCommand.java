package mrquackduck.messagesonhead.commands;

import mrquackduck.messagesonhead.MessagesOnHeadPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class ReloadCommand implements CommandExecutor {
    private MessagesOnHeadPlugin plugin;

    public ReloadCommand(MessagesOnHeadPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        try {
            plugin.reload();
            commandSender.sendMessage((MessagesOnHeadPlugin.getMessage("reloaded")));
        }
        catch (Exception ex) {
            commandSender.sendMessage(MessagesOnHeadPlugin.getMessage("error-during-reload"));
            plugin.getLogger().log(Level.SEVERE, ex.getMessage());
        }

        return true;
    }
}
