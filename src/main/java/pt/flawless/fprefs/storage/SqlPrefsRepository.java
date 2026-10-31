package pt.flawless.fprefs.storage;

import pt.flawless.fapi.database.DatabaseService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** {@link PrefsRepository} sobre a tabela {@code fprefs_values}. */
public class SqlPrefsRepository implements PrefsRepository {

    private static final String SELECT = "SELECT pref_id, value FROM fprefs_values WHERE uuid = ?";
    private static final String UPSERT =
            "INSERT INTO fprefs_values (uuid, pref_id, value) VALUES (?, ?, ?) AS new "
                    + "ON DUPLICATE KEY UPDATE value = new.value";

    private final DatabaseService database;

    public SqlPrefsRepository(DatabaseService database) {
        this.database = database;
    }

    @Override
    public CompletableFuture<Map<String, String>> findAll(UUID uuid) {
        return database.supplyAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(SELECT)) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    Map<String, String> values = new HashMap<>();
                    while (rs.next()) values.put(rs.getString(1), rs.getString(2));
                    return values;
                }
            }
        });
    }

    @Override
    public CompletableFuture<Void> save(UUID uuid, String prefId, String value) {
        return database.runAsync(connection -> {
            try (PreparedStatement ps = connection.prepareStatement(UPSERT)) {
                ps.setString(1, uuid.toString());
                ps.setString(2, prefId);
                ps.setString(3, value);
                ps.executeUpdate();
            }
        });
    }
}
