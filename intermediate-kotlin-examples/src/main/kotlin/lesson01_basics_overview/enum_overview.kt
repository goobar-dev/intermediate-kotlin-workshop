package lesson01_basics_overview

// Enums

// Kotlin supports the concept of enums
// they behave much the same as other languages such as Java or C++

// define an enum by adding the enum modifier to a class
// must explicitly define each value of the sealed enum type
// Each enum is an object
enum class Direction {
    NORTH, SOUTH, WEST, EAST
}

// Enums can also be initialized with a value
// Each enum must initialize this value
enum class Color(val rgb: Int) {
    RED(0xFF0000),
    GREEN(0x00FF00),
    BLUE(0x0000FF)
}

// Enums can declare their own anonymous classes
enum class LightSwitchState {
    ON {
        override fun flip() = OFF
    },
    OFF {
        override fun flip() = ON
    }; // Have to include semi-colon if you declare members after the enums

    abstract fun flip(): LightSwitchState
}

enum class EnumWithUnknown(val value: String) {
    FOO("FOO"),
    BAR("BAR"),
    UNKNOWN("")
}

enum class Weather {
    SUNNY,
    RAINY,
    UNKNOWN
}

// Working with enums
fun main() {
    // Every Enum class has a synthetic method called values() which returns
    // an array of all the enum values
    val directions: Array<Direction> = Direction.values()
    println("All directions = ${directions.toList()}")

    // Enums also have a synthetic method call valueOf(String) that takes a
    // String and returns the Enum represented by that String. This method
    // throws an IllegalArgumentException if no enums match the String.
    val north = Direction.valueOf("NORTH")

    // What if we want to provide a default value instead of throwing?
    // We can do this with the use of a special function: enumValues()
    // enumValues() returns an array containing all values for an enum.
    // Just pass in the type.
    val directions1: Array<Direction> = enumValues<Direction>()

    // Can optionally drop the type here because the compiler already knows it.
    val directions2: Array<Direction> = enumValues()

    // Or you can only specify it on the function call
    val directions3 = enumValues<Direction>()

    val enum: EnumWithUnknown =
        toTypedEnum("weird value", default = EnumWithUnknown.UNKNOWN)
    println(enum)

    // Can case over enums with when statements
    // If when statement is used as an expression, all cases must be covered.
    val action: String = when (toTypedEnum<Weather>("Hail")) {
        Weather.SUNNY -> "Bring shades"
        Weather.RAINY -> "Bring an umbrella"
        Weather.UNKNOWN -> "Look out your window"
        // Don't need else, compiler knows all cases covered
        // If a new enum value is created, this will cause a compiler error until
        // all cases are covered again.
    }

    // Every enum has a name and ordinal property
    println("Name of SUNNY is ${Weather.SUNNY.name}, ordinal = ${Weather.SUNNY.ordinal}")

    // Enum constants also implement the Comparable interface with the natural
    // order being the order in which they were defined
}

// inline keyword means this function will be pasted in by the compiler instead
// of a function call

// reified keyword allows us to pass a type into a function and use it
// without reflection.
inline fun <reified T : Enum<T>> toTypedEnum(
    value: String?,
    default: T = enumValues<T>().last()
): T = enumValues<T>()
    .firstOrNull { it.name.equals(value, ignoreCase = true) }
    ?: default