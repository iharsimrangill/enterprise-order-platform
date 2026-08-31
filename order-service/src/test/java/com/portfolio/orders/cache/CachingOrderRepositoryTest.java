package com.portfolio.orders.cache;

import com.portfolio.orders.domain.Order;
import com.portfolio.orders.persistence.OrderPersistenceAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CachingOrderRepositoryTest {

    private OrderPersistenceAdapter delegate;
    private RedisOrderCache cache;
    private CachingOrderRepository repository;

    @BeforeEach
    void setUp() {
        delegate = mock(OrderPersistenceAdapter.class);
        cache = mock(RedisOrderCache.class);
        repository = new CachingOrderRepository(delegate, cache);
    }

    @Test
    void shouldReturnOrderFromCacheWithoutCallingDatabase() {
        UUID orderId = UUID.randomUUID();
        Order order = mock(Order.class);

        when(cache.get(orderId)).thenReturn(Optional.of(order));

        Optional<Order> result = repository.findById(orderId);

        assertTrue(result.isPresent());
        assertSame(order, result.get());

        verify(cache).get(orderId);
        verifyNoInteractions(delegate);
    }

    @Test
    void shouldLoadOrderFromDatabaseAndPopulateCacheOnCacheMiss() {
        UUID orderId = UUID.randomUUID();
        Order order = mock(Order.class);

        when(cache.get(orderId)).thenReturn(Optional.empty());
        when(delegate.findById(orderId)).thenReturn(Optional.of(order));

        Optional<Order> result = repository.findById(orderId);

        assertTrue(result.isPresent());
        assertSame(order, result.get());

        verify(cache).get(orderId);
        verify(delegate).findById(orderId);
        verify(cache).put(order);
    }

    @Test
    void shouldNotPopulateCacheWhenOrderDoesNotExist() {
        UUID orderId = UUID.randomUUID();

        when(cache.get(orderId)).thenReturn(Optional.empty());
        when(delegate.findById(orderId)).thenReturn(Optional.empty());

        Optional<Order> result = repository.findById(orderId);

        assertTrue(result.isEmpty());

        verify(cache).get(orderId);
        verify(delegate).findById(orderId);
        verify(cache, never()).put(any());
    }

    @Test
    void shouldUpdateCacheAfterSavingOrder() {
        Order order = mock(Order.class);
        Order saved = mock(Order.class);

        when(delegate.save(order)).thenReturn(saved);

        Order result = repository.save(order);

        assertSame(saved, result);

        verify(delegate).save(order);
        verify(cache).put(saved);
    }
}
