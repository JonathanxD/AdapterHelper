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

import org.jetbrains.annotations.Contract

/**
 * Builder of adaptation target.
 *
 * This is a helper class that removes the needed to pass type arguments to `adapt` functions
 * every time you need to adapt a instance. The builder is linked to an [AdapterManager] and delegates
 * [FromTo.adapt] call to it.
 *
 * This class also provides a bi-directional adaptation capability.
 *
 * Obs: Bi-direction adaptation is only supported when you only provide one [Class] as target type.
 */
interface Builder {

    /**
     * Specifies source [type] (input).
     */
    fun <F : Any> from(type: Class<F>): From<F>

    /**
     * Specification of target [type] (output).
     */
    fun <T : Any> to(type: Class<T>): To<T>

    /**
     * Specification of target [types] (output).
     *
     * @param closestType The closest type assignable to all [types] (for type inference).
     */
    fun <T : Any> to(closestType: Class<T>, types: List<Class<out T>>): MultiTo<T>

    interface From<F : Any> {

        /**
         * Specification of target [type] (output).
         */
        fun <T : Any> to(type: Class<T>): FromTo<F, T>

        /**
         * Specification of target [types] (output).
         *
         * @param closestType The closest type assignable to all [types].
         */
        fun <T : Any> to(closestType: Class<T>, types: List<Class<out T>>): FromMultiTo<F, T>
    }

    interface To<T : Any> {

        /**
         * Specifies source [type] (input).
         */
        fun <F : Any> from(type: Class<F>): FromTo<F, T>

    }

    interface MultiTo<out T : Any> {

        /**
         * Specifies source [type] (input).
         */
        fun <F : Any> from(type: Class<F>): FromMultiTo<F, T>
    }

    /**
     * Bi-Direction adaptation helper.
     */
    interface FromTo<F : Any, T : Any> {

        /**
         * Creates adapter instance (or return cached instance).
         */
        @Contract("null -> null; !null -> !null")
        fun adapt(instance: F?): T?

        /**
         * Creates a reverse instance (or return a cached reverse instance.
         */
        fun reverse(): FromTo<T, F>
    }

    /**
     * Single-Directional adaptation helper.
     */
    interface FromMultiTo<in F : Any, out T : Any> {

        /**
         * Creates adapter instance (or return cached instance).
         */
        @Contract("null -> null; !null -> !null")
        fun adapt(instance: F?): T?

    }
}