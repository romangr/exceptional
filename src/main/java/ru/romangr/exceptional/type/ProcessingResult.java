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

  /**
   * @return list of successfully processed elements of collection.
   */
  public List<T> successResults() {
    return successResults;
  }

  /**
   * @return an instance of {@link Exceptional} with exception that occurred during collection
   * processing or empty {@link Exceptional} if no exception occured.
   */
  public Exceptional<T> exception() {
    return Optional.ofNullable(exception)
        .map(Exceptional::<T>exceptional)
        .orElse(Exceptional.empty());
  }
}
