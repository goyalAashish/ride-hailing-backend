package com.ridehailing.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread-safe in-memory store keyed by {@code ID}. Subclasses decide how keys are assigned
 * (generated {@link Long} ids vs natural keys such as coupon codes).
 */
public abstract class InMemoryRepository<ID, T> {

    protected final ConcurrentMap<ID, T> store = new ConcurrentHashMap<>();

    public Optional<T> findById(ID id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(id));
    }

    public boolean existsById(ID id) {
        return id != null && store.containsKey(id);
    }

    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    public boolean deleteById(ID id) {
        return id != null && store.remove(id) != null;
    }

    public long count() {
        return store.size();
    }

    /**
     * Clears the primary map and any secondary indexes maintained by subclasses.
     * Also resets id sequences so tests can start from 1.
     */
    public void deleteAll() {
        store.clear();
        onCleared();
    }

    protected void onCleared() {
        // secondary indexes / sequences
    }
}
