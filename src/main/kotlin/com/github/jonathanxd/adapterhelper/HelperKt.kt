/**
 *      AdapterHelper - Adapter management helper. <https://github.com/JonathanxD/AdapterHelper>
 *
 *         The MIT License (MIT)
 *
 *      Copyright (c) 2018 TheRealBuggy/JonathanxD (https://github.com/JonathanxD/ & https://github.com/TheRealBuggy/) <jonathan.scripter@programmer.net>
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

inline fun <reified E : Any, reified O : Any> AdapterManager.adaptUnchecked(instance: E) =
    this.adaptUnchecked(E::class.java, instance, O::class.java)

inline fun <reified E : Any, reified O : Any> AdapterManager.adapt(instance: E) =
    this.adapt(E::class.java, instance, O::class.java)

inline fun <reified F : Any, reified T : Any> AdapterManager.convertUnchecked(
    instance: F,
    adapter: Adapter<*>? = null
) =
    this.convertUnchecked(F::class.java, T::class.java, instance, adapter)

inline fun <reified I : Any, reified O : Any> AdapterManager.convert(
    instance: I,
    adapter: Adapter<*>? = null
) =
    this.convert(I::class.java, O::class.java, instance, adapter)

inline fun <reified I : Any, reified O : Any> AdapterManager.registerConverter(converter: Converter<I, O>) =
    this.registerConverter(I::class.java, O::class.java, converter)

// Collection

inline fun <reified E : Any, reified O : Any> AdapterManager.createAdapterList(from: List<E>) =
    this.createAdapterList(E::class.java, from, O::class.java)

inline fun <reified E : Any, reified O : Any> AdapterManager.createAdapterSet(from: Set<E>) =
    this.createAdapterSet(E::class.java, from, O::class.java)

inline fun <reified E : Any, reified O : Any> AdapterManager.createAdapterCollection(from: Collection<E>) =
    this.createAdapterCollection(E::class.java, from, O::class.java)

inline fun <reified KE : Any, reified VE : Any, reified KO : Any, reified VO : Any> AdapterManager.createAdapterMap(
    from: Map<KE, VE>
) = this.createAdapterMap(KE::class.java, VE::class.java, from, KO::class.java, VO::class.java)

// Factory

inline fun <reified A : Any, reified O : Any> AdapterManager.register(noinline factory: (O, AdapterManager) -> A) {
    this.register(AdapterSpecification.create(factory, A::class.java, O::class.java))
}

// Dynamic

/**
 * @param A Adapter type
 * @param T Common interface
 * @param F Type to adapt (e.g platform type).
 */
inline fun <reified A : T, reified T : Any, reified F : Any> fromInterface() =
    AdapterSpecification.createFromInterface(A::class.java, T::class.java, F::class.java)

/**
 * @param A Adapter type
 * @param T Common interface
 * @param F Type to adapt (e.g platform type).
 */
inline fun <reified A : T, reified T : Any, reified F : Any> fromInterface(
    noinline factory: (genClass: Class<*>, e: F, manager: AdapterManager) -> A
) = AdapterSpecification.createFromInterface(A::class.java, T::class.java, F::class.java, factory)