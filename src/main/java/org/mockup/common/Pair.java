package org.mockup.common;

public class Pair<A, B> {
    private A first;
    private B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A GetFirst() {
        return this.first;
    }

    public B GetSecond() {
        return this.second;
    }
}
