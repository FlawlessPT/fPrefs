package pt.flawless.fprefs.service;

import pt.flawless.fapi.prefs.Pref;
import pt.flawless.fapi.prefs.PrefsApi;
import pt.flawless.fprefs.storage.PrefsRepository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/** {@link PrefsApi} com cache dos jogadores online sobre um {@link PrefsRepository}. */
public class PrefsService implements PrefsApi {

    /** Máximo de preferências: o menu tem uma página de 28 slots. */
    public static final int MAX_PREFS = 28;

    private final PrefsRepository repo;
    private final Consumer<Throwable> onError;
    private final Map<String, Pref> defs = new LinkedHashMap<>();
    private final Map<UUID, Map<String, String>> cache = new ConcurrentHashMap<>();

    public PrefsService(PrefsRepository repo, Consumer<Throwable> onError) {
        this.repo = repo;
        this.onError = onError;
    }

    @Override
    public synchronized void register(Pref pref) {
        if (defs.containsKey(pref.id())) {
            throw new IllegalStateException("preferência já registada: " + pref.id());
        }
        if (defs.size() >= MAX_PREFS) {
            throw new IllegalStateException("limite de " + MAX_PREFS + " preferências atingido");
        }
        defs.put(pref.id(), pref);
    }

    /** Preferências por ordem de registo. */
    public synchronized List<Pref> prefs() {
        return List.copyOf(defs.values());
    }

    private synchronized Pref def(String prefId) {
        Pref pref = defs.get(prefId);
        if (pref == null) throw new IllegalArgumentException("preferência inexistente: " + prefId);
        return pref;
    }

    @Override
    public String get(UUID uuid, String prefId) {
        Pref pref = def(prefId);
        Map<String, String> values = cache.get(uuid);
        String value = values == null ? null : values.get(prefId);
        boolean valid = value != null && pref.options().stream().anyMatch(o -> o.id().equals(value));
        return valid ? value : pref.defaultOption();
    }

    /** Grava primeiro; a cache só muda se a gravação correr bem. */
    @Override
    public CompletableFuture<Void> set(UUID uuid, String prefId, String optionId) {
        Pref pref;
        try {
            pref = def(prefId);
        } catch (IllegalArgumentException e) {
            return CompletableFuture.failedFuture(e);
        }
        if (pref.options().stream().noneMatch(o -> o.id().equals(optionId))) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("opção inexistente: " + optionId));
        }
        return repo.save(uuid, prefId, optionId)
                .thenRun(() -> cache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(prefId, optionId));
    }

    /** Carrega as escolhas para a cache; se falhar, regista o erro e valem as opções por omissão. */
    public void load(UUID uuid) {
        repo.findAll(uuid).whenComplete((found, error) -> {
            if (error != null) {
                onError.accept(error);
            } else {
                cache.put(uuid, new ConcurrentHashMap<>(found));
            }
        });
    }

    public void unload(UUID uuid) {
        cache.remove(uuid);
    }
}
