package ru.romangr.exceptional.type;

@FunctionalInterface
public interface ExceptionalFunction<X, Y> {

  Y apply(X t) throws Exception;

}
