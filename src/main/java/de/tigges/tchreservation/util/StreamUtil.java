package de.tigges.tchreservation.util;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class StreamUtil {
    private StreamUtil() {
        super();
    }

    public static <T> Stream<T> stream(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(),false);
    }
}
