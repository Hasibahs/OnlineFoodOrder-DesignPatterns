package com.nhlstenden.foodorder.util;

import java.util.ArrayList;
import java.util.List;

public class ToppingEventManager {
    private static final List<Runnable> listeners = new ArrayList<>();

    public static void subscribe(Runnable listener) {
        listeners.add(listener);
    }

    public static void fire() {
        for (Runnable l : listeners) {
            l.run();
        }
    }
}
