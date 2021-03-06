package lesson04_delegates

import kotlin.properties.Delegates
import kotlin.reflect.KProperty

// Delegates

// Delegation is an OOP design pattern that allows object composition
// to achieve the same code reuse as inheritance

class Rectangle(val width: Int, val height: Int) {
    fun area() = width * height
}

// The class Window delegates the area() call to its internal Rectangle object
// (its delegate)

class Window(val bounds: Rectangle) {
    // Delegation
    fun area() = bounds.area()
}

// In delegation, an object handles a request by delegating to a second object
// (the delegate)

// Kotlin has a special syntax for this type of delegation

interface Base {
    fun print()
}

class BaseImpl(val x: Int) : Base {
    override fun print() = print(x)
}

class Derived(b: Base) : Base by b // Compiler generates all forwarding methods
// b is stored internally

fun main0(args: Array<String>) {
    val b = BaseImpl(10)
    Derived(b).print()
}

interface CoffeeRepo {
    fun getCoffee(): String
}

class CoffeeProvider : CoffeeRepo {
    override fun getCoffee(): String {
        TODO("not implemented")
    }
}

interface CustomerRepo {
    fun getCustomer(): String
}

class CustomerProvider : CustomerRepo {
    override fun getCustomer(): String {
        TODO("not implemented")
    }
}

class NetworkClient(val coffeeRepo: CoffeeProvider, val customerRepo: CustomerProvider)
    : CoffeeRepo by coffeeRepo, CustomerRepo by customerRepo


// Delegated Properties

// There are certain common kinds of properties, that we could implement them
// manually every time we need them, or we could implement them once and for
// all, and put into a library. Examples include:

// lazy properties: the value gets computed only upon first access;
// observable properties: listeners get notified about changes to this property;
// storing properties in a map, instead of a separate field for each property.

// To cover these (and other) cases, Kotlin supports delegated properties:

// Delegates don't need to implement an interface, but they do need to provide
// getValue() and setValue() (for var properties)
class Delegate {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return "$thisRef, thank you for delegating ${property.name} to me!"
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        println("$value has been assigned to ${property.name} in $thisRef")
    }
}

class Example {
    var p: String by Delegate()
}

//fun main1() {
//    val e = Example()
//    println(e.p)
//
//    e.p = "NEW"
//}

// A more practical example
class StringBundleDelegate {
    operator fun getValue(bundle: Bundle, property: KProperty<*>): String? {
        return bundle.getString(property.name)
    }

    operator fun setValue(bundle: Bundle, property: KProperty<*>, value: String?) {
        bundle.putString(property.name, value)
    }
}

class InfoDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)
        builder
            .setTitle(arguments?.dialogTitle)
            .setMessage(arguments?.dialogMessage)
            .setPositiveButton("Okay") { _, _ -> }

        return builder.create()
    }

    companion object {
        var Bundle.dialogTitle: String? by StringBundleDelegate()
        var Bundle.dialogMessage: String? by StringBundleDelegate()

        fun newInstance(title: String, message: String): InfoDialogFragment {
            val bundle = Bundle().apply {
                dialogTitle = title
                dialogMessage = message
            }

            return InfoDialogFragment().apply { arguments = bundle }
        }
    }
}

// Kotlin Standard Delegates

// Lazy - First call to get computes the value, subsequent calls return the
// remembered result

val lazyValue: String by lazy {
    println("Computed!")
    "Hello"
}

//fun main2() {
//    println(lazyValue) // prints Computed!\nHello
//    println(lazyValue) // prints Hello
//}

// Lazy evaluation is synchronized by default
// Use LazyThreadSafetyMode.PUBLICATION so multiple threads can execute it
// concurrently
// Use LazyThreadSafetyMode.NONE when you're sure the initialization happens on
// one thread to remove synchronization overhead

// Observable - Provides handler that's called AFTER value changes
class User {
    var name: String by Delegates.observable("<no name>") { property, oldValue, newValue ->
        println("$oldValue -> $newValue")
    }
}

//fun main3(args: Array<String>) {
//    val user = User()
//    user.name = "first"
//    user.name = "second"
//}


// Vetoable - Provides handler that's called BEFORE value changes so you can veto
class Piano {
    var numKeys: Int by Delegates.vetoable(88) { property, oldValue, newValue ->
        if (newValue in 0..100) {
            true
        } else {
            println("Veto")
            false
        }
    }
}

//fun main4() {
//    val piano = Piano()
//    println(piano.numKeys)
//
//    piano.numKeys = 200
//    println(piano.numKeys)
//}


class StringMapDelegate(val map: MutableMap<String, String?>) {
    operator fun getValue(thisRef: Customer, property: KProperty<*>): String? {
        return map[property.name]
    }

    operator fun setValue(thisRef: Customer, property: KProperty<*>, value: String?) {
        map[property.name] = value
    }
}

class Customer(val map: MutableMap<String, String?>) {
    var firstName: String? by StringMapDelegate(map)
    val lastName: String? by StringMapDelegate(map)
}

//fun main5() {
//    val map = mutableMapOf<String, String?>("firstName" to "John", "lastName" to "Adams")
//    val customer = Customer(map)
//
//    println(customer.firstName)
//
//    map["firstName"] = "Adam"
//
//    println(customer.firstName)
//
//    println(customer.lastName)
//    map["lastName"] = "Crews"
//    println(customer.lastName)
//}