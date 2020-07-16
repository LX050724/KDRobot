package com.company.KDRobot.util;

public class Pair<K, V> {
    public V first;
    public K second;

    public Pair(K second, V first) {
        this.second = second;
        this.first = first;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}
