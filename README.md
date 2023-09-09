# exceptional ![build status](https://github.com/romangr/exceptional/actions/workflows/buildAndPublish.yml/badge.svg)

Exceptional is a small util to simplify error handling in Java.

The problems it's trying to solve:
* Exceptions are kind of "goto" instructions in unexpected places in your code. It's easier to handle errors as a valid return type of your methods.
* You never know when you can get an exception that need to handle because it's common practice to use unchecked exceptions
* try-catch blocks are difficult to read because of their syntax

That's how your methods can look like:
```java
class Parser {
  public Exceptional<Integer> parseInteger(String possibleInt) {
    return Exceptional.attempt(() -> Integer.parseInt(possibleInt));
  }
}
```

In this example you know `Integer.parseInt` can throw an exception but you don't want to catch it.
You just need to know if it was parsed or not. Exceptional makes exactly that.

## Available at Maven Central

### Maven
```xml
<dependency>
  <groupId>ru.romangr</groupId>
  <artifactId>exceptional</artifactId>
  <version>0.5.0</version>
</dependency>
```

### Gradle
```groovy
implementation 'ru.romangr:exceptional:0.5.0'
```
