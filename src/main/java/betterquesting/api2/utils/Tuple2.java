package betterquesting.api2.utils;

import net.minecraft.util.Tuple;

// Purely so I don't have to do casting every damn time I want to use a Tuple
public class Tuple2<T, K> extends Tuple {
    public Tuple2(T first, K second) {
        super(first, second);
    }

    @Override
    public T getFirst() {
        return (T) super.getFirst();
    }

    @Override
    public K getSecond() {
        return (K) super.getSecond();
    }
}
