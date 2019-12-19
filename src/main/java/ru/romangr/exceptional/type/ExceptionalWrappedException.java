package ru.romangr.exceptional.type;

public final class ExceptionalWrappedException extends RuntimeException {

  public ExceptionalWrappedException(Throwable cause) {
    super(cause);
  }
}
