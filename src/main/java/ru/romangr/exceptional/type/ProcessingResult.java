package ru.romangr.exceptional.type;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import ru.romangr.exceptional.Exceptional;
import ru.romangr.exceptional.nullability.NonNullApi;

@NonNullApi
public final class ProcessingResult<T> {

  private final List<T> successResults;

  @Nullable
  private final Exception exception;

  public ProcessingResult(List<T> successResults, @Nullable Exception exception) {
    this.successResults = successResults;
    this.exception = exception;
  }

  public List<T> successResults() {
    return successResults;
  }

  public Exceptional<T> exception() {
    return Optional.ofNullable(exception)
        .map(Exceptional::<T>exceptional)
        .orElse(Exceptional.empty());
  }
}
