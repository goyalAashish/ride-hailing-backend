package com.ridehailing.repository;

import com.ridehailing.domain.User;
import com.ridehailing.exception.DuplicateResourceException;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class UserRepository extends InMemoryLongIdRepository<User> {

    private final ConcurrentMap<String, Long> phoneIndex = new ConcurrentHashMap<>();

    @Override
    protected Long getId(User entity) {
        return entity.getUserId();
    }

    @Override
    protected void assignId(User entity, Long id) {
        entity.setUserId(id);
    }

    @Override
    public User save(User user) {
        Objects.requireNonNull(user, "user is required");
        Objects.requireNonNull(user.getPhone(), "phone is required");

        Long id = user.getUserId() == null ? nextId() : user.getUserId();
        Long existingOwner = phoneIndex.putIfAbsent(user.getPhone(), id);
        if (existingOwner != null && !existingOwner.equals(id)) {
            throw new DuplicateResourceException("User", "phone", user.getPhone());
        }

        user.setUserId(id);
        store.put(id, user);
        return user;
    }

    public Optional<User> findByPhone(String phone) {
        if (phone == null) {
            return Optional.empty();
        }
        Long id = phoneIndex.get(phone);
        return id == null ? Optional.empty() : findById(id);
    }

    @Override
    public boolean deleteById(Long id) {
        findById(id).ifPresent(user -> phoneIndex.remove(user.getPhone(), id));
        return super.deleteById(id);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        phoneIndex.clear();
    }
}
