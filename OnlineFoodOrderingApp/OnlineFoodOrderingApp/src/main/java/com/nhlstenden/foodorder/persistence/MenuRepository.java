package com.nhlstenden.foodorder.persistence;

import com.nhlstenden.foodorder.model.MenuEntry;

import java.util.List;

public interface MenuRepository
{
    List<MenuEntry> findAll();
    void save(MenuEntry entry);
    void deleteByKey(String key);
}
