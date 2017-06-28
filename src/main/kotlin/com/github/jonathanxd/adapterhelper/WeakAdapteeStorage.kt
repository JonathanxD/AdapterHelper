/**
 *      AdapterHelper - Adapter management helper. <https://github.com/JonathanxD/AdapterHelper>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2017 TheRealBuggy/JonathanxD (https://github.com/JonathanxD/ & https://github.com/TheRealBuggy/) <jonathan.scripter@programmer.net>
 *      Copyright (c) contributors
 *
 *
 *      Permission is hereby granted, free of charge, to any person obtaining a copy
 *      of this software and associated documentation files (the "Software"), to deal
 *      in the Software without restriction, including without limitation the rights
 *      to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *      copies of the Software, and to permit persons to whom the Software is
 *      furnished to do so, subject to the following conditions:
 *
 *      The above copyright notice and this permission notice shall be included in
 *      all copies or substantial portions of the Software.
 *
 *      THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *      IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *      FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *      AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *      LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *      OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *      THE SOFTWARE.
 */
package com.github.jonathanxd.adapterhelper

import java.util.WeakHashMap

interface Storage {

    /**
     * Associates a [value] to [adaptee], [name] and [adapterType].
     */
    fun <E : Any, T : Any, V : Any> store(adaptee: E, name: String, value: V?, adapterType: Class<T>)

    /**
     * Retrieve a value associated to [adaptee], [name] and [adapterType].
     */
    fun <E : Any, T : Any, V : Any> retrieve(adaptee: E, name: String, adapterType: Class<T>): V?

    /**
     * Returns true if this storage has a value associated to [adaptee], [name] and [adapterType].
     */
    fun <E: Any, T: Any> hasValue(adaptee: E, name: String, adapterType: Class<T>): Boolean
}

/**
 * [WeakHashMap] backed [Storage]. The keys are stored weakly and values
 * using a [Map] of [String] and `[Any]?`.
 */
class WeakAdapteeStorage : Storage {

    private val map = WeakHashMap<Any, MutableMap<String, Any?>>()

    override fun <E : Any, T : Any, V : Any> store(adaptee: E, name: String, value: V?, adapterType: Class<T>) {
        this.map.computeIfAbsent(adaptee, {mutableMapOf()}).put(name, value)
    }

    override fun <E : Any, T : Any, V : Any> retrieve(adaptee: E, name: String, adapterType: Class<T>): V? {
        @Suppress("UNCHECKED_CAST")
        return this.map[adaptee]?.get(name) as V?
    }

    override fun <E : Any, T : Any> hasValue(adaptee: E, name: String, adapterType: Class<T>): Boolean {
        return this.map[adaptee]?.contains(name) ?: false
    }

    companion object {
        @JvmField
        val GLOBAL = WeakAdapteeStorage()
    }


}