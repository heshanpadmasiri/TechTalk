package org.heshan;

abstract class Super {
    private final boolean val;

    Super(boolean val) {
        this.val = val;
    }

    final boolean isTrue() {
        return val;
    }
}

class ChildTrue extends Super  {
    ChildTrue() {
        super(true);
    }
}

class ChildFalse extends Super  {
    ChildFalse() {
        super(true);
    }
}

abstract class Super1 {
    abstract boolean isTrue();
}

class ChildTrue1 extends Super1 {
    @Override
    boolean isTrue() {
        return true;
    }
}

class ChildFalse1 extends Super1 {
    @Override
    boolean isTrue() {
        return false;
    }
}
