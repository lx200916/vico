package pl.patrykgoworowski.liftchart_common.extension

import pl.patrykgoworowski.liftchart_common.constants.ERR_REPEATING_COLLECTION_EMPTY

fun <T> ArrayList<T>.getOrDefault(index: Int, getDefault: () -> T): T =
    getOrNull(index) ?: getDefault().also { add(it) }

fun <T> ArrayList<T>.getRepeatingOrDefault(index: Int, getDefault: () -> T) =
    getOrNull(index) ?: getOrNull(index % size.coerceAtLeast(1)) ?: getDefault().also { add(it) }

fun <T> List<T>.getRepeating(index: Int): T {
    if (isEmpty()) throw IllegalStateException(ERR_REPEATING_COLLECTION_EMPTY)
    return get(index % size.coerceAtLeast(1))
}

public fun <T> ArrayList<T>.setAll(other: Collection<T>) {
    clear()
    addAll(other)
}

public inline fun <T> Iterable<T>.sumByFloat(selector: (T) -> Float): Float {
    var sum = 0f
    for (element in this) {
        sum += selector(element)
    }
    return sum
}