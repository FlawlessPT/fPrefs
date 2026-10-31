package pt.flawless.fprefs.storage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Armazenamento das escolhas dos jogadores. */
public interface PrefsRepository {

    /** Todas as escolhas do jogador (id da preferência para id da opção). */
    CompletableFuture<Map<String, String>> findAll(UUID uuid);

    CompletableFuture<Void> save(UUID uuid, String prefId, String value);
}
