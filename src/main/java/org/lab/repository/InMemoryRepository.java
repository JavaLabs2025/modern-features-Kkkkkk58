package org.lab.repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class InMemoryRepository<T> implements Repository<T> {

    private final ConcurrentMap<UUID, T> storage = new ConcurrentHashMap<>();
    private final Function<T, UUID> idExtractor;

    public InMemoryRepository(Function<T, UUID> idExtractor) {
        this.idExtractor = Objects.requireNonNull(idExtractor);
    }

    @Override
    public T save(T entity) {
        storage.put(idExtractor.apply(entity), entity);
        return entity;
    }

    @Override
    public Optional<T> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll(Predicate<T> predicate) {
        return storage.values().stream().filter(predicate).toList();
    }

    protected Optional<T> findFirst(Predicate<T> predicate) {
        return findAll(predicate).stream().findFirst();
    }
}
