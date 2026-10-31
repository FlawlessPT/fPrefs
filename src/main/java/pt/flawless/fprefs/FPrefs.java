package pt.flawless.fprefs;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import pt.flawless.fapi.database.DatabaseService;
import pt.flawless.fapi.database.Databases;
import pt.flawless.fapi.enums.EMessageTypeEnum;
import pt.flawless.fapi.logs.FConsoleLogger;
import pt.flawless.fapi.prefs.PrefsApi;
import pt.flawless.fprefs.listeners.PrefsListener;
import pt.flawless.fprefs.service.PrefsService;
import pt.flawless.fprefs.storage.SqlPrefsRepository;

import java.util.List;

public class FPrefs extends JavaPlugin {
    private final FConsoleLogger consoleLogger = new FConsoleLogger(this.getName());

    @Override
    public void onEnable() {
        try {
            DatabaseService database = Databases.get();
            database.migrate(this, List.of("db/fprefs/V001__create_values.sql"));

            PrefsService service = new PrefsService(new SqlPrefsRepository(database), this::logError);
            getServer().getServicesManager().register(PrefsApi.class, service, this, ServicePriority.Normal);
            Bukkit.getPluginManager().registerEvents(new PrefsListener(service), this);

            // Jogadores já online (/reload) não passam pelo PlayerJoinEvent.
            for (Player p : Bukkit.getOnlinePlayers()) service.load(p.getUniqueId());
        } catch (Exception e) {
            consoleLogger.sendMessage("não foi possível iniciar as preferências: " + e.getMessage(), EMessageTypeEnum.ERROR);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        consoleLogger.sendEnablePluginMessage();
    }

    private void logError(Throwable error) {
        consoleLogger.sendMessage(String.valueOf(error), EMessageTypeEnum.ERROR);
    }

    @Override
    public void onDisable() {
        consoleLogger.sendMessage("desativado", EMessageTypeEnum.INFO);
    }
}
