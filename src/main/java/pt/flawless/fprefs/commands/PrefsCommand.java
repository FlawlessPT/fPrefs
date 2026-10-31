package pt.flawless.fprefs.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pt.flawless.fprefs.menu.PrefsMenu;
import pt.flawless.fprefs.service.PrefsService;

public class PrefsCommand implements CommandExecutor {
    private final PrefsService service;

    public PrefsCommand(PrefsService service) {
        this.service = service;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Só jogadores podem usar este comando.");
            return true;
        }
        new PrefsMenu(service, player).open();
        return true;
    }
}
