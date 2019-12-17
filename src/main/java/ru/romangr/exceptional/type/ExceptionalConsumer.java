package ru.romangr.exceptional.type;

@FunctionalInterface
public interface ExceptionalConsumer<T> {

    void accept(T value) throws Exception;
}
