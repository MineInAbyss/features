# Dependency Injection

Dependency Injection (DI) is a commonly used pattern for splitting your code into reusable parts, the Android Developer Docs have a good intro page on the idea here: [Dependency injection in Android](https://developer.android.com/training/dependency-injection).

For our purposes the idea is: when your class depends on other functionality, place it in its constructor.

```kotlin
// An interface for our dependency
interface PlayerBankRepository {
  fun addBalance(user: Player, amount: Int)
  fun removeBalance(user: Player, amount: Int): Boolean // Returns whether had enough
}

class MyService(
  val bank: PlayerBankRepository // We "inject" the dependency here
) {
  fun transfer(from: Player, to: Player, amount: Int) {
    if (bank.removeBalance(from, amount)) {
      bank.addBalance(to, amount)
    }
  }
}

fun main() {
  val bank = SomeImplOfPlayerBankRepository()
  val service = MyService(bank)
  service.transfer(playerA, playerB)
}
```

The Android guide goes into more benefits, but some quick ones in the example above are:
- We can let other parts of code worry about implementation by using interfaces for dependencies (ex. our bank could be implemented in a SQL database, or simple json file, the server owner could choose between them.)
- We can create multiple instances of dependencies, which a singleton `object` can't do. For instance, we might need a new copy of MyService for each world in a game, each of which has its own separate bank.
- We can switch out implementations easily when writing tests, supporting multiple platform versions, or different setups

# What this library does

In the example above, we manually create and inject our dependencies in `main`, this can get unwieldly when every class starts needing 5+ dependencies. For Mine in Abyss we also often want to split code into "modules" that can each be reloaded while the server is running (ex. to read updated configs) without needing a full restart. `dependencies-kt` helps you on both these ends!
