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

import com.github.jonathanxd.adapterhelper.wrapper.func.TypeResolverFunc
import com.github.jonathanxd.iutils.reflection.ClassUtil
import java.lang.reflect.AnnotatedElement

fun Class<*>.getExplicitAndImplicitAnnotations(): Set<Annotation> {
    return mutableSetOf<Annotation>().also {
        this.getExplicitAndImplicitAnnotations(it)
    }
}

fun AnnotatedElement.getExplicitAndImplicitAnnotations(): Set<Annotation> {
    return mutableSetOf<Annotation>().also {
        this.getExplicitAndImplicitAnnotations(it)
    }
}

private fun Class<*>.getExplicitAndImplicitAnnotations(set: MutableSet<Annotation>) {
    (this as AnnotatedElement).getExplicitAndImplicitAnnotations(set)
    this.superclass?.getExplicitAndImplicitAnnotations(set)
    this.interfaces.forEach { it.getExplicitAndImplicitAnnotations(set) }
}

private fun AnnotatedElement.getExplicitAndImplicitAnnotations(set: MutableSet<Annotation>) {
    this.annotations.forEach {
        if (!set.contains(it)) {
            set += it
            it.annotationClass.java.getExplicitAndImplicitAnnotations(set)
        }
    }
}

fun Class<*>.hasExplicitOrImplicitAnnotation(type: Class<out Annotation>): Boolean =
        this.getExplicitAndImplicitAnnotations().any { it.annotationClass.java == type }


fun AnnotatedElement.hasExplicitOrImplicitAnnotation(type: Class<out Annotation>): Boolean =
        this.getExplicitAndImplicitAnnotations().any { it.annotationClass.java == type }

/**
 * Finds the correct exact type that an adapter exists to convert the type [to] to the
 * found type. The found type is based on super types of [from] (and itself), the function respects
 * the inheritance order (the first is the [from] class and the last is [Any]).
 */
fun AdapterManager.getFirstValid(from: Class<*>, to: Class<*>): Class<*>?
        = this.getFirstValid(ClassUtil.getSortedSuperTypes(from), to)

/**
 * Finds the correct exact type of the list [from] that an adapter exists to convert the type [to] to the
 * found type. The found type is based on super types of [from] (and itself), the function respects
 * the inheritance order (the first is the [from] class and the last is [Any]).
 *
 * The [from] parameters should contains all sorted super types of [from] type, this variant is used
 * when you have cached super types (Commonly retrieved with [ClassUtil.getSortedSuperTypes]).
 */
fun AdapterManager.getFirstValid(from: List<Class<*>>, to: Class<*>): Class<*>? {
    from.forEach {
        val exact = this.getExact(it, arrayOf(to))

        if (exact.isPresent)
            return it
    }

    return null
}

/**
 * Gets the first valid [AdapterSpecification] with adapter class assignable
 * to [adapter][from] that the [AdapterSpecification.adapteeClass] is equal to any super type of [to] (or itself),
 * the function respects the inheritance order (the first is the [from] class and the last is [Any]).
 */
fun AdapterManager.getFirstValidTo(from: Class<*>, to: Class<*>): AdapterSpecification<*, *>? =
        this.getFirstValidTo(from, ClassUtil.getSortedSuperTypes(to))

/**
 * Gets the first valid [AdapterSpecification] with adapter class assignable
 * to [adapter][from] that the [AdapterSpecification.adapteeClass] is equal to any [to] element (respect the order).
 *
 * The [to] parameters should contains all sorted super types of [to] type, this variant is used
 * when you have cached super types (Commonly retrieved with [ClassUtil.getSortedSuperTypes]).
 */
fun AdapterManager.getFirstValidTo(from: Class<*>, to: List<Class<*>>): AdapterSpecification<*, *>? {
    to.forEach {
        this.unmodAdapterSpecificationSet.forEach { c ->
            if (from.isAssignableFrom(c.adapterClass) && c.adapteeClass == it)
                return c
        }
    }

    return null
}

/**
 * A [TypeResolverFunc] that resolves types looking up in [AdapterManager].
 */
@Suppress("UNCHECKED_CAST")
class LookupFunc<A : Any, B : Any>(private val manager: AdapterManager,
                                   val from: Class<A>,
                                   val to: Class<B>) : TypeResolverFunc<A, B> {

    private val fromSubTypes = ClassUtil.getSortedSuperTypes(from)
    private val toSubTypes = ClassUtil.getSortedSuperTypes(to)

    override fun apply(a: A): Class<A> = a::class.java.let {
        this.getBasedOnClass(a, it)
                ?: this.getBasedOnSpec(it)
                ?: throw IllegalArgumentException("Failed to determine type of instance '$a' (from: '$from', to: '$to')")
    } as Class<A>

    override fun applyFromB(b: B): Class<A> = b::class.java.let {
        this.getBasedOnSpec(it)
                ?: this.getBasedOnClass(b, it)
                ?: throw IllegalArgumentException("Failed to determine type based on alternative instance '$b' (from: '$from', to: '$to')")
    } as Class<A>


    private fun Class<*>.types() = when {
        this == from::class.java -> fromSubTypes
        this == to::class.java -> toSubTypes
        else -> ClassUtil.getSortedSuperTypes(this)
    }

    private fun getBasedOnClass(instance: Any, type: Class<*>): Class<*>? {
        if (from.isAssignableFrom(type)) {
            val types = type.types()

            manager.getFirstValid(types, to)?.let {
                return it as Class<A>
            }

            if (instance is AdapterBase<*>) {
                manager.getFirstValid(types, instance.originalInstance::class.java)?.let {
                    return it as Class<A>
                }
            }
        }

        return null
    }

    private fun getBasedOnSpec(type: Class<*>): Class<*>? {
        if (to.isAssignableFrom(type)) {
            val types = type.types()

            manager.getFirstValidTo(from, types)?.let {
                return it.adapterClass as Class<A>
            }
        }

        return null
    }
}
