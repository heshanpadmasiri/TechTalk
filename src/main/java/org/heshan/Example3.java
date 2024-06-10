package org.heshan;

public class Example3 {

    public static int example(Data data) {
        return switch (data.tag()) {
            case A -> Logic.fn1(data);
            case B -> Logic.fn2(data);
            case C -> Logic.fn3(data);
            case D -> Logic.fn4(data);
        };
    }

    private final static class Logic {
        public static int fn1(Data data) {
            return data.val() + 1;
        }

        public static int fn2(Data data) {
            return data.val() + 2;
        }

        public static int fn3(Data data) {
            return data.val() + 3;
        }

        public static int fn4(Data data) {
            return data.val() + 4;
        }
    }
}

record Data(Tag tag, int val) {}

enum Tag {
    A, B, C, D
}