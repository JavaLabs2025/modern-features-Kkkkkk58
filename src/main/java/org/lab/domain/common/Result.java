package org.lab.domain.common;

public sealed interface Result<T> {

    record Success<T>(T value) implements Result<T> {}
    record Failure<T>(String error) implements Result<T> {}

    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    static <T> Result<T> failure(String error) {
        return new Failure<>(error);
    }

    default boolean isSuccess() {
        return this instanceof Success;
    }

    default boolean isFailure() {
        return this instanceof Failure;
    }

    default T orElseThrow() {
        return switch (this) {
            case Success<T>(var v) -> v;
            case Failure<T>(var e) -> throw new IllegalStateException(e);
        };
    }
}
