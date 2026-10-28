package com.wardenplus.client;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public final class ClientWardenState {
    private static final Int2ObjectOpenHashMap<State> STATES = new Int2ObjectOpenHashMap<>();

    public static final class State {
        public int stage = 1;
        public int anim;
        public int animStart;
        public int animDuration;
        public float awaken;
        public int variant;
    }

    private ClientWardenState() {
    }

    public static State get(int id) {
        return STATES.computeIfAbsent(id, k -> new State());
    }

    public static State peek(int id) {
        return STATES.get(id);
    }

    public static int variant(int id) {
        State s = STATES.get(id);
        return s == null ? 0 : s.variant;
    }

    public static void clear() {
        STATES.clear();
    }

    public static void remove(int id) {
        STATES.remove(id);
    }
}




