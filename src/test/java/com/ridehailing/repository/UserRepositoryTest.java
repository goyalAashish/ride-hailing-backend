package com.ridehailing.repository;

import com.ridehailing.domain.User;
import com.ridehailing.exception.DuplicateResourceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRepositoryTest {

    private UserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new UserRepository();
    }

    @Test
    void save_assignsMonotonicIds() {
        User first = repository.save(new User("Ada", "111"));
        User second = repository.save(new User("Bob", "222"));

        assertThat(first.getUserId()).isEqualTo(1L);
        assertThat(second.getUserId()).isEqualTo(2L);
        assertThat(repository.count()).isEqualTo(2);
    }

    @Test
    void save_rejectsDuplicatePhone() {
        repository.save(new User("Ada", "111"));

        assertThatThrownBy(() -> repository.save(new User("Ada Clone", "111")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("phone");
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void save_sameUserTwice_doesNotDuplicate() {
        User user = repository.save(new User("Ada", "111"));
        repository.save(user);

        assertThat(repository.count()).isEqualTo(1);
        assertThat(repository.findByPhone("111")).contains(user);
    }

    @Test
    void findById_returnsEmptyForUnknownId() {
        assertThat(repository.findById(99L)).isEmpty();
        assertThat(repository.findByPhone("missing")).isEmpty();
    }

    @Test
    void deleteById_removesPhoneIndex() {
        User user = repository.save(new User("Ada", "111"));

        assertThat(repository.deleteById(user.getUserId())).isTrue();
        assertThat(repository.findByPhone("111")).isEmpty();

        User reused = repository.save(new User("Ada 2", "111"));
        assertThat(reused.getUserId()).isEqualTo(2L);
    }

    @Test
    void deleteAll_resetsSequenceAndIndexes() {
        repository.save(new User("Ada", "111"));
        repository.deleteAll();

        User next = repository.save(new User("Ada", "111"));
        assertThat(next.getUserId()).isEqualTo(1L);
    }

    @Test
    void save_concurrentSamePhone_onlyOneSucceeds() throws Exception {
        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger successes = new AtomicInteger();
        AtomicInteger duplicates = new AtomicInteger();
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                start.await();
                try {
                    repository.save(new User("Ada", "999"));
                    successes.incrementAndGet();
                } catch (DuplicateResourceException ex) {
                    duplicates.incrementAndGet();
                }
                return null;
            }));
        }

        start.countDown();
        for (Future<?> future : futures) {
            future.get();
        }
        pool.shutdown();

        assertThat(successes.get()).isEqualTo(1);
        assertThat(duplicates.get()).isEqualTo(threads - 1);
        assertThat(repository.count()).isEqualTo(1);
    }
}
