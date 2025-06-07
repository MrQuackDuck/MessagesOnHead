package mrquackduck.messagesonhead.commands;

import mrquackduck.messagesonhead.MessagesOnHeadPlugin;
import mrquackduck.messagesonhead.classes.MessageStackRepository;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SayCommand implements CommandExecutor {
    private final MessageStackRepository messageStackRepository;
    private final MessagesOnHeadPlugin plugin;

    public SayCommand(MessagesOnHeadPlugin plugin, MessageStackRepository messageStackRepository) {
        this.plugin = plugin;
        this.messageStackRepository = messageStackRepository;
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

        var messageStack = messageStackRepository.getMessageStack(player);
        messageStack.pushMessage(message.toString());

        return true;
    }
}
