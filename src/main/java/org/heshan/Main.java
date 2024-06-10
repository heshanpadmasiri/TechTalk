package org.heshan;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(
        jvmArgsPrepend = {"-XX:-TieredCompilation"}
) // make sure everything is compiled with C2
public class Main {
    private static final int ITERATIONS = 10_000_000;
    private static final int SEED = 1234;

    @Benchmark
    public void staticMethodCall(Blackhole blackhole) {
        int sum = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            sum += StaticClass.fn(sum);
        }
        blackhole.consume(sum);
    }

    @Benchmark
    public void monomorphicInterfaceMethodCall(MonomorphicClassProvider provider, Blackhole blackhole) {
        int sum = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            sum += provider.interfaceVals[i].fn(sum);
        }
        blackhole.consume(sum);
    }

    @Benchmark
    public void monomorphicVirtualMethodCall(MonomorphicClassProvider provider, Blackhole blackhole) {
        int sum = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            sum += provider.abstractVals[i].fn(sum);
        }
        blackhole.consume(sum);
    }

    @Benchmark
    public void bimorphicInterfaceMethodCall(BimorphicClassProvider provider, Blackhole blackhole) {
        int sum = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            sum += provider.interfaceVals[i].fnB(sum);
        }
        blackhole.consume(sum);
    }

    @Benchmark
    public void bimorphicVirtualMethodCall(BimorphicClassProvider provider, Blackhole blackhole) {
        int sum = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            sum += provider.abstractVals[i].fnB(sum);
        }
        blackhole.consume(sum);
    }


    @Benchmark
    public void bimorphicPeeledOff(BimorphicClassProvider provider, Blackhole blackhole) {
        int sum = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            BimorphicAbstractClass val = provider.abstractVals[i];
            if (val instanceof BimorphicClass1 v1) {
                sum += v1.fnB(sum);
            } else if (val instanceof BimorphicClass2 v2) {
                sum += v2.fnB(sum);
            } else {
                sum += val.fnB(sum);
            }
        }
        blackhole.consume(sum);
    }


    @Benchmark
    public void megamorphicInterfaceMethodCall(MegamorphicClassProvider provider, Blackhole blackhole) {
        int sum = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            sum += provider.interfaceVals[i].fnM(sum);
        }
        blackhole.consume(sum);
    }

    @Benchmark
    public void megamorphicVirtualMethodCall(MegamorphicClassProvider provider, Blackhole blackhole) {
        int sum = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            sum += provider.abstractVals[i].fnM(sum);
        }
        blackhole.consume(sum);
    }

    @Benchmark
    public void megamorphicPeeledOff(MegamorphicClassProvider provider, Blackhole blackhole) {
        int sum = 0;
        for (int i = 0; i < ITERATIONS; i++) {
            MegamorphicAbstractClass val = provider.abstractVals[i];
            if (val instanceof MegamorphicClass1 v1) {
                sum += v1.fnM(sum);
            } else if (val instanceof MegamorphicClass2 v2) {
                sum += v2.fnM(sum);
            } else if (val instanceof MegamorphicClass3 v3) {
                sum += v3.fnM(sum);
            } else {
                sum += val.fnM(sum);
            }
        }
        blackhole.consume(sum);
    }

    @State(Scope.Benchmark)
    public static class MonomorphicClassProvider {
        final MonomorphicInterface[] interfaceVals = new MonomorphicInterface[ITERATIONS];
        final MonomorphicAbstractClass[] abstractVals = new MonomorphicAbstractClass[ITERATIONS];

        @Setup
        public void setup() {
            for (int i = 0; i < ITERATIONS; i++) {
                MyClass val = new MyClass();
                interfaceVals[i] = val;
                abstractVals[i] = val;
            }
        }
    }

    @State(Scope.Benchmark)
    public static class BimorphicClassProvider {
        final BimorphicInterface[] interfaceVals = new BimorphicInterface[ITERATIONS];
        final BimorphicAbstractClass[] abstractVals = new BimorphicAbstractClass[ITERATIONS];
        final Random random = new Random(SEED);

        @Setup
        public void setup() {
            for (int i = 0; i < ITERATIONS; i++) {
                int j = random.nextInt();
                BimorphicAbstractClass val = j % 2 == 0 ? new BimorphicClass1() : new BimorphicClass2();
                interfaceVals[i] = (BimorphicInterface) val;
                abstractVals[i] = val;
            }
        }
    }

    @State(Scope.Benchmark)
    public static class MegamorphicClassProvider {
        final MegamorphicInterface[] interfaceVals = new MegamorphicInterface[ITERATIONS];
        final MegamorphicAbstractClass[] abstractVals = new MegamorphicAbstractClass[ITERATIONS];
        final Random random = new Random(SEED);

        @Setup
        public void setup() {
            for (int i = 0; i < ITERATIONS; i++) {
                int j = random.nextInt();
                MegamorphicAbstractClass val = switch (j % 3) {
                    case 0 -> new MegamorphicClass1();
                    case 1 -> new MegamorphicClass2();
                    default -> new MegamorphicClass3();
                };
                interfaceVals[i] = (MegamorphicInterface) val;
                abstractVals[i] = val;
            }
        }
    }

}

final class StaticClass {
    private StaticClass() {
    }

    static int fn(int val) {
        return val + 1;
    }
}

interface MonomorphicInterface {
    int fn(int val);
}

abstract class MonomorphicAbstractClass {
    abstract public int fn(int val);
}

final class MyClass extends MonomorphicAbstractClass implements MonomorphicInterface {
    @Override
    public int fn(int val) {
        return val + 1;
    }
}

interface BimorphicInterface {
    int fnB(int val);
}

abstract class BimorphicAbstractClass {

    abstract public int fnB(int val);

    public static int fnB1(int val) {
        return val + 1;
    }

    public static int fnB2(int val) {
        return val + 1;
    }
}

final class BimorphicClass1 extends BimorphicAbstractClass implements BimorphicInterface {

    @Override
    public int fnB(int val) {
        return val + 1;
    }
}

final class BimorphicClass2 extends BimorphicAbstractClass implements BimorphicInterface {

    @Override
    public int fnB(int val) {
        return val + 1;
    }
}

interface MegamorphicInterface {
    int fnM(int val);
}

abstract class MegamorphicAbstractClass {
    abstract public int fnM(int val);
}

final class MegamorphicClass1 extends MegamorphicAbstractClass implements MegamorphicInterface {
    @Override
    public int fnM(int val) {
        return val + 1;
    }
}

final class MegamorphicClass2 extends MegamorphicAbstractClass implements MegamorphicInterface {
    @Override
    public int fnM(int val) {
        return val + 1;
    }
}

final class MegamorphicClass3 extends MegamorphicAbstractClass implements MegamorphicInterface {
    @Override
    public int fnM(int val) {
        return val + 1;
    }
}
