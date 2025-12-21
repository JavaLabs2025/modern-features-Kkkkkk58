package org.lab.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public interface Repository<T> {

    T save(T entity);

    Optional<T> findById(UUID id);

    List<T> findAll(Predicate<T> predicate);

}
