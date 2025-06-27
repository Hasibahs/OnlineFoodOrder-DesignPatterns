package com.nhlstenden.foodorder.persistence;

import java.util.List;

public interface OrderRepository {
    void save(OrderRecord record);
    List<OrderRecord> findAll();
    List<OrderRecord> findAllByUser(String username);
    List<OrderRecord> findAllVisibleToUser(String username);
    void clearAll();
    void deleteById(int id);
    void softDeleteById(int id, String username);
}
