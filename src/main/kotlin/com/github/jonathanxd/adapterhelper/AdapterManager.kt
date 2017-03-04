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

import com.github.jonathanxd.iutils.`object`.Pair
import com.github.jonathanxd.iutils.map.WeakValueHashMap
import com.github.jonathanxd.iutils.optional.Require
import com.github.jonathanxd.iutils.reflection.ClassUtil
import com.github.jonathanxd.iutils.type.Primitive
import java.util.*

/**
 * Manages all Adapter Specifications.
 */
open class AdapterManager {

    /**
     * Set that store all registered [AdapterSpecification].
     */
    private val adapterSpecificationSet = HashSet<AdapterSpecification<*, *>>()
    private val unmodAdapterSpecificationSet = Collections.unmodifiableSet(this.adapterSpecificationSet)

    /**
     * Weak value map that stores generated instances. (The value must be weak).
     */
    private val cache = WeakValueHashMap<Pair<AdapterSpecification<*, *>, Any>, Any>()
    private val unmodCache = Collections.unmodifiableMap(this.cache)

    /**
     * Map that store all registered converters.
     */
    private val converterMap = HashMap<Pair<Class<*>, Class<*>>, Converter<*, *>>()
    private val unmodConverterMap = Collections.unmodifiableMap(this.converterMap)

    /**
     * Register an [AdapterSpecification].
     *
     * @param adapterSpecification Specification to register.
     * @param E                    Adaptee type.
     */
    fun <E : Any> register(adapterSpecification: AdapterSpecification<E, out Any>) {
        Objects.requireNonNull(adapterSpecification)

        this.adapterSpecificationMutableSet.add(adapterSpecification)
    }

    /**
     * Unregister adapter specification.
     *
     * @param adapterSpecification Specification to unregister.
     */
    fun unregister(adapterSpecification: AdapterSpecification<*, *>) {
        Objects.requireNonNull(adapterSpecification)

        this.adapterSpecificationMutableSet.remove(adapterSpecification)
    }

    /**
     * Register a converter that converts from [I] to [O].
     *
     * @param from      Input type.
     * @param to        Output type.
     * @param converter Converter.
     * @param I         Input type.
     * @param O         Output type.
     */
    fun <I : Any, O : Any> registerConverter(from: Class<I>, to: Class<O>, converter: Converter<I, O>) {
        this.converterMutableMap.put(Pair.of<Class<*>, Class<*>>(from, to), converter)

        val revert = converter.revert()

        if (revert != null)
            this.converterMutableMap.put(Pair.of<Class<*>, Class<*>>(to, from), revert)
    }


    /**
     * Unregister a converter that converts from [I] to [O].
     *
     * @param from Input type.
     * @param to   Output type.
     * @param I    Input type.
     * @param O    Output type.
     */
    @Suppress("UNCHECKED_CAST")
    fun <I : Any, O : Any> unregisterConverter(from: Class<I>, to: Class<O>) {
        val pair = Pair.of<Class<*>, Class<*>>(from, to)

        val map = this.converterMutableMap

        if (map.containsKey(pair)) {
            val converter = map[pair]!!

            map.remove(pair)

            if (converter.revert() != null) {
                map.remove(Pair.of<Class<*>, Class<*>>(to, from))
            }
        }
    }

    /**
     * Gets the converter from [I] to [O].
     *
     * @param from Input type.
     * @param to   Output type.
     * @param I    Input type.
     * @param O    Output type.
     * @return Converter from [I] to [O].
     */
    fun <I : Any, O : Any> getConverter(from: Class<I>, to: Class<O>): Optional<Converter<in I, out O>> {
        return this.getAssignableConverter(from, to)
    }

    /**
     * Gets the converter from [I] to [O].
     *
     * @param from Input type.
     * @param to   Output type.
     * @param I    Input type.
     * @param O    Output type.
     * @return Converter from [I] to [O].
     */
    fun <I : Any, O : Any> getConverterUnchecked(from: Class<I>, to: Class<O>): Converter<in I, out O> {
        return Require.require(this.getConverter(from, to), "Can't find a converter that converts from '" + from.canonicalName + "' to '" + to.canonicalName + "'!")
    }

    /**
     * Convert [I] to [O].
     *
     * @param from    Input type.
     * @param to      Output type.
     * @param input   Input value.
     * @param adapter Adapter instance (may be null).
     * @param I       Input type.
     * @param O       Output type.
     * @return Converted instance.
     */
    fun <I : Any, O : Any> convert(from: Class<I>, to: Class<O>, input: I, adapter: Adapter<*>?): Optional<O> {
        return this.getConverter(from, to).map { it.convert(input, adapter, this) }
    }

    /**
     * Convert [I] to [O].

     * @param from    Input type.
     * @param to      Output type.
     * @param input   Input value.
     * @param adapter Adapter instance (may be null).
     * @param I       Input type.
     * @param O       Output type.
     * @return Converted instance.
     */
    fun <I : Any, O : Any> convertUnchecked(from: Class<I>, to: Class<O>, input: I, adapter: Adapter<*>?): O {
        return Require.require(this.convert(from, to, input, adapter), "Can't convert from '" + from.canonicalName + "' to '" + to.canonicalName + "'!")
    }


    /**
     * Adapt all instance in `iterableInstances` to instances assignable to `toClasses`.
     *
     * @param adaptee           Adaptee class.
     * @param instance          Adaptee instance.
     * @param toClasses         Expected classes.
     * @param E                 Adaptee type.
     * @return Immutable list of all adapted instances (iteration order).
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any> adaptSingleAsAny(adaptee: Class<in E>, instance: E, toClasses: Array<Class<*>>?): Optional<Any> {
        var toClasses = toClasses

        if (toClasses == null)
            toClasses = emptyArray()

        // Gets the specification of Adapter that adapts 'adaptee' to 'toClasses'
        val adapterSpecificationOpt = this[adaptee, toClasses]

        // Check if is no one specification was found
        if (!adapterSpecificationOpt.isPresent)
        // Returns empty
            return Optional.empty()

        return this.adaptSingleAsAny(instance, adapterSpecificationOpt.get() as AdapterSpecification<E, Any>)
    }

    /**
     * Adapt all instance in `iterableInstances` to instances assignable to `toClasses`.
     *
     * @param instance              Instance to adapt
     * @param adapterSpecification  Adapter specification.
     * @param E                     Adaptee type.
     * @return Immutable list of all adapted instances (iteration order).
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any> adaptSingleAsAny(instance: E, adapterSpecification: AdapterSpecification<E, Any>): Optional<Any> {
        // Gets the cache map
        val cache = this.mutableCache

        // Create the pair representing the specification and instance to adapt
        val pair = Pair.of<AdapterSpecification<*, *>, Any>(adapterSpecification, instance)

        // Check if cache contains an Adapter instance that adapted 'instance'
        if (cache.containsKey(pair)) {
            // Add the cached instance to list
            return Optional.of(cache[pair] as E)
        } else {
            // Create adapter instance;
            val t = adapterSpecification.create(instance, this)

            // Cache the instance
            cache.put(pair, t)

            // Returns the adapted instance.
            return Optional.of(t)
        }
    }

    fun <E : Any> adaptAllAsAny(adaptee: Class<in E>, instances: List<E>, toClasses: Array<Class<*>>): List<Any> =
            this.adaptAllAsAny(adaptee, instances as Iterable<E>, toClasses) as List<Any>

    fun <E : Any> adaptAllAsAny(adaptee: Class<in E>, instances: Set<E>, toClasses: Array<Class<*>>): Set<Any> =
            this.adaptAllAsAny(adaptee, instances as Iterable<E>, toClasses) as Set<Any>

    fun <E : Any> adaptAllAsAny(adaptee: Class<in E>, instances: Collection<E>, toClasses: Array<Class<*>>): Collection<Any> =
            this.adaptAllAsAny(adaptee, instances as Iterable<E>, toClasses) as Collection<Any>

    /**
     * Adapt all instance in `iterableInstances` to instances assignable to `toClasses`.
     *
     * @param adaptee           Adaptee class.
     * @param instances         Adaptee instances.
     * @param toClasses         Expected classes.
     * @param E                 Adaptee type.
     * @return Iterable type of the same input type.
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any> adaptAllAsAny(adaptee: Class<in E>, instances: Iterable<E>, toClasses: Array<Class<*>>): Iterable<Any> {
        /*return when (instances) {
            is List<*> -> AdapterWrappedList(adaptee, instances as List<E>, this, toClasses)
            is Set<*> -> AdapterWrappedSet(adaptee, instances as Set<E>, this, toClasses)
            is Collection<*> -> AdapterWrappedCollection(adaptee, instances as Collection<E>, this, toClasses)
            else ->
                if (instances::class.java == Iterable::class.java)
                    AdapterWrappedIterable(adaptee, instances, this, toClasses)
                else
                    throw IllegalArgumentException("Cannot convert Iterable of type ${instances::class.java} to AdapterWrapped")
        }*/
        return createProxy(adaptee, instances, toClasses, this)
    }

    /**
     * Adapt `instance` to a instance assignable to `toClasses`.
     *
     * @param adaptee   Adaptee class.
     * @param instance  Adaptee instance.
     * @param toClass   Expected class.
     * @return Any.
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any> adaptAllAsAny(adaptee: Class<in E>, instances: Iterable<E>, toClass: Class<*>?): Iterable<Any> {
        return this.adaptAllAsAny(adaptee, instances, toClass?.let { arrayOf(it) } ?: emptyArray())
    }

    /**
     * Adapt all instance in `iterableInstances` to instances assignable to `toClasses`.
     *
     * @param adaptee           Adaptee class.
     * @param instances         Adaptee instances.
     * @param toClasses         Expected classes.
     * @param E                 Adaptee type.
     * @return Immutable list of all adapted instances (iteration order).
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any, I : Iterable<Adapter<E>>> adaptAllAsAdapter(adaptee: Class<in E>, instances: I, toClasses: Array<Class<*>>): I {
        return this.adaptAllAsAny(adaptee, instances as Iterable<E>, toClasses) as I
    }

    /**
     * Adapt `instance` to a instance assignable to `toClasses`.
     *
     * @param adaptee   Adaptee class.
     * @param instance  Adaptee instance.
     * @param toClasses Expected classes.
     * @return Any.
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any> adaptAsAny(adaptee: Class<in E>, instance: E, toClasses: Array<Class<*>>): Optional<Any> {
        return this.adaptSingleAsAny(adaptee, instance, toClasses)
    }

    /**
     * Adapt `instance` to a instance assignable to `toClasses`.
     *
     * @param adaptee   Adaptee class.
     * @param instance  Adaptee instance.
     * @param toClass   Expected class.
     * @return Any.
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any> adaptAsAny(adaptee: Class<in E>, instance: E, toClass: Class<*>?): Optional<Any> {
        return this.adaptSingleAsAny(adaptee, instance, toClass?.let { arrayOf(it) } ?: emptyArray())
    }

    /**
     * Adapt `instance` to a instance assignable to `toClasses`.
     *
     * @param adaptee   Adaptee class.
     * @param instance  Adaptee instance.
     * @param toClasses Expected classes.
     * @param E         Adaptee type.
     * @return Adapter instance.
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any> adaptAsAdapter(adaptee: Class<in E>, instance: E, toClasses: Array<Class<*>>): Optional<Adapter<E>> {
        return this.adaptSingleAsAny(adaptee, instance, toClasses) as Optional<Adapter<E>>
    }

    /**
     * Adapt `instance` to a instance assignable to `toClass`.
     *
     * @param adaptee  Adaptee class.
     * @param instance Adaptee instance.
     * @param toClass  Expected class.
     * @param E        Adaptee type.
     * @return Adapter instance.
     */
    fun <E : Any> adaptAsAdapter(adaptee: Class<in E>, instance: E, toClass: Class<*>?): Optional<Adapter<E>> {
        return this.adaptAsAdapter(adaptee, instance, toClass?.let { arrayOf(it) } ?: emptyArray())
    }

    /**
     * Adapt `instance` to a instance assignable to `toClasses`.
     *
     * @param adaptee   Adaptee class.
     * @param instance  Adaptee instance.
     * @param toClasses Expected classes.
     * @param E         Adaptee type.
     * @return Adapter instance.
     */
    fun <E : Any> adaptUncheckedAsAdapter(adaptee: Class<in E>, instance: E, toClasses: Array<Class<*>>): Adapter<E> {
        return Require.require(this.adapt(adaptee, instance, toClasses), "Can't find adapter of '" + adaptee + "' to '" + Arrays.toString(toClasses) + "'!")
    }

    /**
     * Adapt `instance` to a instance assignable to `toClasses`.
     *
     * @param adaptee  Adaptee class.
     * @param instance Adaptee instance.
     * @param toClass  Expected class.
     * @param E        Adaptee type.
     * @return Adapter instance.
     */
    fun <E : Any> adaptUncheckedAsAdapter(adaptee: Class<in E>, instance: E, toClass: Class<*>): Adapter<E> {
        return Require.require(this.adaptAsAdapter(adaptee, instance, toClass), "Can't find adapter of '$adaptee' to '$toClass'!")
    }

    /**
     * Adapt all instance in `iterableInstances` to instances assignable to `toClass`.
     *
     * @param adaptee           Adaptee class.
     * @param instances Adaptee instances.
     * @param toClass           Expected class.
     * @param E                 Adaptee type.
     * @return Immutable list of all adapted instances (iteration order).
     */
    fun <E : Any, I : Iterable<Adapter<E>>> adaptAllAsAdapter(adaptee: Class<in E>, instances: I, toClass: Class<*>?): I {
        return this.adaptAllAsAdapter(adaptee, instances, toClass?.let { arrayOf(it) } ?: emptyArray())
    }


    @Suppress("UNCHECKED_CAST")
    fun <E : Any, O : Any> adaptAll(adaptee: Class<in E>, instances: List<E>, toClass: Class<O>): List<O> =
            this.adaptAll(adaptee, instances as Iterable<E>, toClass) as List<O>

    @Suppress("UNCHECKED_CAST")
    fun <E : Any, O : Any> adaptAll(adaptee: Class<in E>, instances: Set<E>, toClass: Class<O>): Set<O> =
            this.adaptAll(adaptee, instances as Iterable<E>, toClass) as Set<O>

    @Suppress("UNCHECKED_CAST")
    fun <E : Any, O : Any> adaptAll(adaptee: Class<in E>, instances: Collection<E>, toClass: Class<O>): Collection<O> =
            this.adaptAll(adaptee, instances as Iterable<E>, toClass) as Collection<O>

    /**
     * Adapt all instance in `iterableInstances` to instances assignable to `toClass`.
     *
     * @param adaptee           Adaptee class.
     * @param instances Adaptee instances.
     * @param toClass           Expected class.
     * @param E                 Adaptee type.
     * @return Immutable list of all adapted instances (iteration order).
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any, O : Any> adaptAll(adaptee: Class<in E>, instances: Iterable<E>, toClass: Class<O>): Iterable<O> {
        return this.adaptAllAsAny(adaptee, instances, toClass) as Iterable<O>
    }

    /**
     * Adapt `instance` to a instance assignable to `toClasses`.
     *
     * This method will try to resolve adapter of [adaptee] class, if not found, and [adaptee] class
     * is not equal to [instance::class][instance], this method will try to resolve adapter of [instance::class][instance].
     *
     * @param adaptee   Adaptee class.
     * @param instance  Adaptee instance.
     * @param toClasses Expected classes.
     * @param E         Adaptee type.
     * @param O         Expected type.
     * @return Expected instance.
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any, O : Any> adaptBase(adaptee: Class<in E>, instance: E, toClasses: Array<Class<*>>): Optional<O> {
        return this.adaptSingleAsAny(adaptee, instance, toClasses).let {
            if (!it.isPresent && adaptee != instance::class.java)
                this.adaptSingleAsAny(instance::class.java as Class<in E>, instance, toClasses)
            else
                it
        } as Optional<O>
    }

    /**
     * Adapt `instance` to a instance assignable to `toClasses`.
     *
     * @param adaptee   Adaptee class.
     * @param instance  Adaptee instance.
     * @param toClasses Expected classes.
     * @param E         Adaptee type.
     * @param O         Expected type.
     * @return Expected instance.
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any, O : Any> adapt(adaptee: Class<in E>, instance: E, toClasses: Array<Class<*>>): Optional<O> {
        return this.adaptSingleAsAny(adaptee, instance, toClasses) as Optional<O>
    }

    /**
     * Adapt `instance` to a instance assignable to `toClass`.
     *
     * @param adaptee  Adaptee class.
     * @param instance Adaptee instance.
     * @param toClass  Expected class.
     * @param E        Adaptee type.
     * @param O        Expected type.
     * @return Expected instance.
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any, O : Any> adapt(adaptee: Class<in E>, instance: E, toClass: Class<O>): Optional<O> {
        return this.adaptSingleAsAny(adaptee, instance, arrayOf<Class<*>>(toClass)) as Optional<O>
    }

    /**
     * Adapt `instance` to a instance assignable to `toClasses`.
     *
     * This method will try to resolve adapter of [adaptee] class, if not found, and [adaptee] class
     * is not equal to [instance::class][instance], this method will try to resolve adapter of [instance::class][instance].
     *
     * @param adaptee   Adaptee class.
     * @param instance  Adaptee instance.
     * @param toClasses Expected classes.
     * @param E         Adaptee type.
     * @param O         Expected type.
     * @return Expected instance.
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any, O : Any> adaptBaseUnchecked(adaptee: Class<in E>, instance: E, toClasses: Array<Class<*>>): O {
        return Require.require(this.adaptBase(adaptee, instance, toClasses), "Can't find adapter of '$adaptee' (and of '${instance::class.java}') to '${toClasses.contentToString()}'!")
    }

    /**
     * Adapt `instance` to a instance assignable to `toClass`.
     *
     * @param adaptee  Adaptee class.
     * @param instance Adaptee instance.
     * @param toClass  Expected class.
     * @param E        Adaptee type.
     * @param O        Expected type.
     * @return Expected instance.
     */
    fun <E : Any, O : Any> adaptUnchecked(adaptee: Class<in E>, instance: E, toClass: Class<O>): O {
        return Require.require(this.adapt(adaptee, instance, toClass), "Can't find adapter of '$adaptee' to '$toClass'!")
    }

    /**
     * Adapt `instance` to a instance assignable to `toClasses`.
     *
     * @param adaptee   Adaptee class.
     * @param instance  Adaptee instance.
     * @param toClasses Expected classes.
     * @param E         Adaptee type.
     * @param O         Expected type.
     * @return Expected instance.
     */
    fun <E : Any, O : Any> adaptUnchecked(adaptee: Class<in E>, instance: E, toClasses: Array<Class<*>>): O {
        return Require.require(this.adapt(adaptee, instance, toClasses), "Can't find adapter of '" + adaptee + "' to '" + Arrays.toString(toClasses) + "'!")
    }

    /**
     * Gets the Immutable [Set] instance of all registered [AdapterSpecification].
     *
     * @return Immutable [Set] instance of all registered [AdapterSpecification].
     */
    fun getAdapterSpecificationSet(): Set<AdapterSpecification<*, *>> {
        return this.unmodAdapterSpecificationSet
    }

    /**
     * Gets the Immutable [Map] of cached adapter instance.
     *
     * @return Immutable [Map] of cached adapter instance.
     */
    fun getCache(): Map<Pair<AdapterSpecification<*, *>, Any>, Any> {
        return this.unmodCache
    }

    /**
     * Gets the Immutable [Map] of registered converters.
     *
     * @return Immutable [Map] of registered converters.
     */
    fun getConverterMap(): Map<Pair<Class<*>, Class<*>>, Converter<*, *>> {
        return unmodConverterMap
    }

    /**
     * Gets the Mutable [Set] instance of all registered [AdapterSpecification].
     *
     * @return Mutable [Set] instance of all registered [AdapterSpecification].
     */
    protected val adapterSpecificationMutableSet: MutableSet<AdapterSpecification<*, *>>
        get() = this.adapterSpecificationSet

    /**
     * Gets the Mutable [Map] of cached adapter instance.
     *
     * @return Mutable [Map] of cached adapter instance.
     */
    protected val mutableCache: MutableMap<Pair<AdapterSpecification<*, *>, Any>, Any>
        get() = this.cache

    /**
     * Gets the Mutable [Map] of registered converters.
     *
     * @return Mutable [Map] of registered converters.
     */
    protected val converterMutableMap: MutableMap<Pair<Class<*>, Class<*>>, Converter<*, *>>
        get() = this.converterMap

    /**
     * Cleanup [Adapter Instance Cache][cache].
     */
    fun cleanupInstanceCache() {
        this.mutableCache.clear()
    }

    /**
     * Gets the converter from [I] to [O].
     *
     * @param from Input type.
     * @param to   Output type.
     * @param I    Input type.
     * @param O    Output type.
     * @return Converter from [I] to [O].
     */
    @Suppress("UNCHECKED_CAST", "NAME_SHADOWING")
    private fun <I : Any, O : Any> getExactConverter(from: Class<I>, to: Class<O>): Optional<Converter<in I, out O>> {
        var from = from
        var to = to

        // Convert primitive types to boxed version.
        if (from.isPrimitive) {
            from = Primitive.box(from) as Class<I>
        }

        if (to.isPrimitive) {
            to = Primitive.box(to) as Class<O>
        }

        val pair = Pair.of<Class<*>, Class<*>>(from, to)

        return Optional.ofNullable(this.converterMutableMap[pair] as Converter<I, O>?)
    }

    /**
     * Gets the converter from any instance assignable to [I] to [O] or super-types of [O].
     *
     * @param from Input type.
     * @param to   Output type.
     * @param I    Input type.
     * @param O    Output type.
     * @return Converter from [I] to [O].
     */
    @Suppress("UNCHECKED_CAST")
    private fun <I : Any, O : Any> getAssignableConverter(from: Class<I>, to: Class<O>): Optional<Converter<in I, out O>> {
        val exactConverter = this.getExactConverter(from, to)

        if (exactConverter.isPresent)
            return exactConverter

        val map = this.converterMutableMap

        for ((key, value) in map) {

            // The confuse section of documentation
            // Only thing that you have to known about this code is that it determines if the converter is valid
            // This code leads with Covariance and Contravariance
            // Converter#from is contravariant and Converter#to is covariant
            if (key._1().isAssignableFrom(from)) {
                // If the parameter 'from' is assignable to converter@from
                if (to.isAssignableFrom(key._2()))
                // If converter@to is assignable from parameter 'to'
                    return Optional.of(value as Converter<I, O>) // Returns the converter
            }
        }

        return Optional.empty()
    }

    /**
     * Gets the [AdapterSpecification] that provide the adapter of `adaptee` class that
     * inherits all `relation` classes.
     *
     * @param adaptee  Adapter class.
     * @param relation Inheritance relation.
     * @return [AdapterSpecification] that provide the adapter of `adaptee` class that
     * inherits all `relation` classes.
     */
    private operator fun get(adaptee: Class<*>, relation: Array<Class<*>>): Optional<AdapterSpecification<*, *>> {
        return this.getAssignable(adaptee, relation)
    }

    /**
     * Gets the [AdapterSpecification] that provide the adapter of `adaptee` class that
     * inherits all `relation` classes.
     *
     * @param adaptee  Adapter class.
     * @param relation Inheritance relation.
     * @return [AdapterSpecification] that provide the adapter of `adaptee` class that
     * inherits all `relation` classes.
     */
    private fun getExact(adaptee: Class<*>, relation: Array<Class<*>>): Optional<AdapterSpecification<*, *>> {

        return Optional.ofNullable(this.adapterSpecificationMutableSet
                .firstOrNull { adapterSpecification ->

                    if (adapterSpecification.adapteeClass != adaptee)
                        return@firstOrNull false

                    if (relation.isEmpty())
                        return@firstOrNull true

                    val adapterClass = adapterSpecification.adapterClass

                    return@firstOrNull !relation.any { !it.isAssignableFrom(adapterClass) }
                })

    }

    /**
     * Gets the [AdapterSpecification] that provide the adapter of `adaptee`, or a
     * adapter of superclasses of `adaptee`, class that inherits all `relation`
     * classes.
     *
     * @param adaptee  Adapter class.
     * @param relation Inheritance relation.
     * @return [AdapterSpecification] that provide the adapter of `adaptee` class that
     * inherits all `relation` classes.
     */
    private fun getAssignable(adaptee: Class<*>, relation: Array<Class<*>>): Optional<AdapterSpecification<*, *>> {
        Objects.requireNonNull(adaptee)
        Objects.requireNonNull(relation)

        val sortedSuperTypes = ClassUtil.getSortedSuperTypes(adaptee).iterator()

        var exact = this.getExact(adaptee, relation)

        if (exact.isPresent)
            return exact

        while (!exact.isPresent && sortedSuperTypes.hasNext()) {
            exact = this.getExact(sortedSuperTypes.next(), relation)
        }

        return exact
    }

    companion object {

        /**
         * Creates a instance of Adapter Manager.
         *
         * @return A new instance of Adapter Manager.
         */
        @JvmStatic
        fun create(): AdapterManager {
            return AdapterManager()
        }
    }
}
