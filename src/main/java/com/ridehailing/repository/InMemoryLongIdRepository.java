package com.ridehailing.repository;

import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link InMemoryRepository} variant that assigns monotonically increasing {@link Long} ids
 * via {@link AtomicLong} on first save.
 */
public abstract class InMemoryLongIdRepository<T> extends InMemoryRepository<Long, T> {

    private final AtomicLong idSequence = new AtomicLong(0);

    protected abstract Long getId(T entity);

    protected abstract void assignId(T entity, Long id);

    protected Long nextId() {
        return idSequence.incrementAndGet();
    }

    /**
     * Persists {@code entity}, generating an id when it is new. Subclasses typically override
     * to maintain unique secondary indexes around this call.
     */
    public T save(T entity) {
        Long id = getId(entity);
        if (id == null) {
            id = nextId();
            assignId(entity, id);
        }
        store.put(id, entity);
        return entity;
    }

    @Override
    protected void onCleared() {
        idSequence.set(0);
    }
}
