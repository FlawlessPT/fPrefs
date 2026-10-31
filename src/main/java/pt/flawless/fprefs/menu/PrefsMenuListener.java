package pt.flawless.fprefs.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.plugin.Plugin;
import pt.flawless.fapi.prefs.Pref;
import pt.flawless.fapi.prefs.PrefOption;
import pt.flawless.fapi.sounds.FSound;
import pt.flawless.fprefs.service.PrefsService;

import java.util.List;

public class PrefsMenuListener implements Listener {
    private final Plugin plugin;
    private final PrefsService service;

    public PrefsMenuListener(Plugin plugin, PrefsService service) {
        this.plugin = plugin;
        this.service = service;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof PrefsMenu menu)) return;
        e.setCancelled(true);
        if (e.getClickedInventory() != e.getInventory()) return;

        Pref clicked = null;
        for (Pref pref : service.prefs()) {
            if (menu.slotOf(pref) == e.getRawSlot()) clicked = pref;
        }
        if (clicked == null) return;

        Player player = menu.getPlayer();
        List<PrefOption> options = clicked.options();
        String current = service.get(player.getUniqueId(), clicked.id());
        int idx = 0;
        for (int i = 0; i < options.size(); i++) if (options.get(i).id().equals(current)) idx = i;
        String next = options.get((idx + 1) % options.size()).id();

        service.set(player.getUniqueId(), clicked.id(), next).whenComplete((v, error) -> {
            // Completa fora da thread principal: voltar a ela antes de mexer no inventário.
            try {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (error != null) {
                        plugin.getLogger().warning("Falha ao gravar a preferência: " + error);
                        player.sendMessage("§cNão foi possível guardar a preferência.");
                        FSound.fail(player);
                        return;
                    }
                    menu.render();
                    FSound.success(player);
                });
            } catch (RuntimeException ex) {
                // plugin a desligar
            }
        });
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof PrefsMenu) e.setCancelled(true);
    }
}
