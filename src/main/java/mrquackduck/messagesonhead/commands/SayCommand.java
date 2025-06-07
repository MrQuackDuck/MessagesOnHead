package mrquackduck.messagesonhead.commands;

import mrquackduck.messagesonhead.MessagesOnHeadPlugin;
import mrquackduck.messagesonhead.classes.MessageStack;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SayCommand implements CommandExecutor {
    private final MessagesOnHeadPlugin plugin;

    public SayCommand(MessagesOnHeadPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        Player player = plugin.getServer().getPlayer(args[1]);
        if (player == null) {
            commandSender.sendMessage(MessagesOnHeadPlugin.getMessage(""));
            return true;
        }

        StringBuilder message = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i != 2) message.append(" ");
            message.append(args[i]);
        }

        var stack = MessageStack.getMessagesStack(player, plugin);
        stack.pushMessage(message.toString());

        return true;
    }
}
