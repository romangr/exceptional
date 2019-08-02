package ru.romangr.exceptional.nullability;

import java.lang.annotation.ElementType;
import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;

@Nonnull
@TypeQualifierDefault({ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE,
    ElementType.FIELD})
public @interface NonNullApi {
}
