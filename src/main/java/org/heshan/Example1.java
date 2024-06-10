package org.heshan;

abstract class SuperType {
    abstract void fn();
}

class SubType1 extends SuperType {
    @Override
    void fn() {
        System.out.println("Child1");
    }
}

class SubType2 extends SuperType {
    @Override
    void fn() {
        System.out.println("Child2");
    }
}
