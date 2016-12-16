/**
 *      AdapterHelper - Adapter management helper. <https://github.com/JonathanxD/AdapterHelper>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2016 TheRealBuggy/JonathanxD (https://github.com/JonathanxD/ & https://github.com/TheRealBuggy/) <jonathan.scripter@programmer.net>
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

import java.util.*

/**
 * Specification of adapter class.
 *
 * @param factory Adapter Instance Factory.
 * @param adapterClass Adapter class.
 * @param adapteeClass Adaptee class (target class/class to adapt).
 */
class AdapterSpecification<E: Any, T : Adapter<E>> private constructor(
        val factory: (E, AdapterManager) -> T,
        val adapterClass: Class<T>,
        val adapteeClass: Class<E>) {

    /**
     * Create the adapter class instance.
     *
     * @param target  Target instance.
     * @param manager Adapter manager.
     * @return Adapter instance.
     */
    fun create(target: E, manager: AdapterManager): T {
        return this.factory(target, manager)
    }

    override fun hashCode(): Int = Objects.hash(this.adapterClass, this.adapteeClass)

    @Suppress("UNCHECKED_CAST")
    override fun equals(other: Any?): Boolean {

        return !(other == null || other.javaClass != AdapterSpecification::class.java)
                && this.adapterClass == (other as AdapterSpecification<E, T>).adapterClass
                && this.adapteeClass == other.adapteeClass

    }

    companion object {
        /**
         * Create adapter specification.
         *
         * @param factory      Factory function.
         * @param adapterClass Adapter class.
         * @param adapteeClass Adaptee class.
         * @param <T>          Adapter type.
         * @param <E>          Adaptee type.
         * @return Adapter specification.
         */
        @JvmStatic
        fun <E: Any, T : Adapter<E>> create(factory: (E, AdapterManager) -> T, adapterClass: Class<T>, adapteeClass: Class<E>): AdapterSpecification<E, T>
                = AdapterSpecification(factory, adapterClass, adapteeClass)

        /**
         * Create adapter specification.
         *
         * @param factory      Factory function.
         * @param <T>          Adapter type.
         * @param <E>          Adaptee type.
         * @return Adapter specification.
         */
        inline fun <reified E: Any, reified T : Adapter<E>> create(noinline factory: (E, AdapterManager) -> T): AdapterSpecification<E, T>
                = AdapterSpecification.create(factory, T::class.java, E::class.java)
    }

}