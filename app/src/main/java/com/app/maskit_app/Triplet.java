package com.app.maskit_app;

public class Triplet<T, U, V> {

    final T first;
    final U second;
    final V third;

    public Triplet(T first, U second, V third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public T getFirst() { return first; }
    public U getSecond() { return second; }
    public V getThird() { return third; }
}
