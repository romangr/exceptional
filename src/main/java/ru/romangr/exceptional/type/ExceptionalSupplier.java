package ru.romangr.exceptional.type;

@FunctionalInterface
public interface ExceptionalSupplier<T> {

  T get() throws Exception;

}
