package pt.flawless.fprefs.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import pt.flawless.fapi.enums.FInventorySize;
import pt.flawless.fapi.inventory.FInventory;
import pt.flawless.fapi.items.FItemStack;
import pt.flawless.fapi.prefs.Pref;
import pt.flawless.fapi.prefs.PrefOption;
import pt.flawless.fprefs.service.PrefsService;

import java.util.ArrayList;
import java.util.List;

/** Menu {@code /prefs}: moldura de vidro e uma preferência por slot interior. O holder identifica o inventário. */
public class PrefsMenu implements InventoryHolder {
    private static final int SIZE = 54;
    private static final List<Integer> INNER = new ArrayList<>();

    static {
        for (int s = 0; s < SIZE; s++) if (!isBorder(s)) INNER.add(s);
    }

    private final PrefsService service;
    private final Player player;
    private final Inventory inventory;

    public PrefsMenu(PrefsService service, Player player) {
        this.service = service;
        this.player = player;
        this.inventory = new FInventory(this, FInventorySize.SIZE_54, "§8Preferências").getInventory();
    }

    private static boolean isBorder(int slot) {
        int row = slot / 9, col = slot % 9;
        return row == 0 || row == 5 || col == 0 || col == 8;
    }

    /** Slot da preferência (pela ordem de registo), ou -1 se não existir. */
    public int slotOf(Pref pref) {
        int i = service.prefs().indexOf(pref);
        return i < 0 || i >= INNER.size() ? -1 : INNER.get(i);
    }

    /** Desenha o estado atual; chamar também depois de uma alteração. */
    public void render() {
        org.bukkit.inventory.ItemStack glass = new FItemStack(Material.GRAY_STAINED_GLASS_PANE, 1)
                .setDisplayName(" ").createItem();
        for (int i = 0; i < SIZE; i++) if (isBorder(i)) inventory.setItem(i, glass);
        for (Pref pref : service.prefs()) {
            int slot = slotOf(pref);
            if (slot < 0) continue;
            String current = service.get(player.getUniqueId(), pref.id());
            String currentName = pref.options().stream().filter(o -> o.id().equals(current))
                    .map(PrefOption::name).findFirst().orElse(current);
            FItemStack item = new FItemStack(pref.icon(), 1).setDisplayName("§e" + pref.name() + ": " + currentName);
            for (String line : pref.description()) item.addLoreLine(line);
            item.addLoreLine("");
            for (PrefOption o : pref.options()) {
                item.addLoreLine((o.id().equals(current) ? "§a▶ " : "§7  ") + o.name());
            }
            item.addLoreLine("§7Clica para mudar.");
            inventory.setItem(slot, item.createItem());
        }
    }

    public void open() {
        render();
        player.openInventory(inventory);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }
}
