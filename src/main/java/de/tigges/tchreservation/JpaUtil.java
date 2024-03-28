package de.tigges.tchreservation;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class JpaUtil {
    private JpaUtil() {
        super();
    }

    public static <T> Stream<T> stream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(),false);
    }
}
