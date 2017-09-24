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

import com.github.jonathanxd.adapterhelper.wrapper.AdapterList
import com.github.jonathanxd.adapterhelper.wrapper.AdapterMap
import com.github.jonathanxd.adapterhelper.wrapper.AdapterSet
import com.github.jonathanxd.adapterhelper.wrapper.func.AdapterCollectionFunc
import com.github.jonathanxd.adapterhelper.wrapper.func.AdapterListFunc
import com.github.jonathanxd.adapterhelper.wrapper.func.AdapterMapFunc
import com.github.jonathanxd.adapterhelper.wrapper.func.AdapterSetFunc
import com.github.jonathanxd.iutils.`object`.Pair
import com.github.jonathanxd.iutils.map.WeakValueHashMap
import com.github.jonathanxd.iutils.optional.Require
import com.github.jonathanxd.iutils.reflection.ClassUtil
import com.github.jonathanxd.iutils.type.Primitive
import java.util.*

/**
 * Manages all Adapter Specifications.
 *
 * Documentation Unification:
 *
 * As this is class have too much functions with same arguments and the main different is that some of them
 * are variants that returns or receives a different type or only have a little special behavior, we unified (or aggregated? or..
 * pasted everything below?) all the documentation here.
 *
 * `adapt` prefixed functions:
 *
 * Adapt/Wrap the instance into a type that matches another type (by assignability/hierarchy - but no order is applied).
 *
 * Parameters:
 *
 * - Adaptee (`adaptee`): Source type to adapt
 * - Instance (`instance`): Instance of `adaptee` type that should be adapted.
 * - Target (`toClass` or `toClasses`): May be a single [Class], multiple [Classes][Class] or no one class (zero-sized array).
 *   - Single class: Wraps into a type that is assignable to this class.
 *   - Multiple classes: Wraps into a type that is assignable to **all** specified classes.
 *   - No one class: Adapt to first found type regardless hierarchy.
 *
 * `asAny` suffixed functions:
 *
 * These functions uses [Any] as return type in their signature. These functions exists more to help
 * type system and have same functionality as others.
 *
 * `asAdapter` suffixed functions:
 *
 * These functions uses [Adapter] as return type in their signature. There is nothing special here, these
 * functions are commonly used to avoid casts.
 *
 * `Base` suffixed functions:
 *
 * These functions have a `fallback` resolution strategy: If the normal resolution fails, the function will call resolution
 * function again with `instance::class` as `Adaptee` type. This only happens if adapter resolution fails for `Adaptee` type
 * (provided as argument of function). This is used in some special cases.
 *
 * `convert` Prefixed functions:
 *
 * Converts a instance of a type (commonly a Data Type) to another instance.
 *
 * Parameters:
 *
 * - From (`from`): Source type to convert
 * - To (`to`): Target type to convert (output type)
 * - Input (`input`): Input instance of type `from` to convert to a instance of type `to`
 * - Adapter (`adapter`): Adapter that requested conversion (optional).
 *
 * `Unchecked` suffixed functions:
 *
 * Normally, all functions returns an instance of [Optional], [Optional.EMPTY] in case of absent [AdapterSpecification]
 * for specified arguments, or an [Optional.of] `adapted instance` if the specification was found and adapted successfully.
 * Function suffixed with `unchecked` throws an exception if [Optional.EMPTY] is returned by normal functions, and unbox
 * value in [Optional] if present and return the value.
 *
 * `Exact`:
 *
 * Resolution strategy that lookups for exact types on the storage.
 *
 * `Assignable` (**default**):
 *
 * Resolution strategy that first lookups for exact types on the storage, and them lookup
 * for assignable types. This is default because you commonly work with interfaces, and `exact lookup`
 * does not work with implementation types unless you register them or lookup using interface type.
 *
 */
open class AdapterManager {

    /**
     * Set that store all registered [AdapterSpecification].
     */
    protected val adapterSpecificationSet: MutableSet<AdapterSpecification<*, *>> = HashSet()

    /**
     * Immutable view of [adapterSpecificationSet].
     */
    val unmodAdapterSpecificationSet = Collections.unmodifiableSet(this.adapterSpecificationSet)

    /**
     * Weak value map that stores generated instances. (The value must be weak).
     */
    protected val cache: MutableMap<Pair<AdapterSpecification<*, *>, Any>, Any> = WeakValueHashMap()

    /**
     * Immutable view of [cache].
     */
    val unmodCache = Collections.unmodifiableMap(this.cache)

    /**
     * Value map that stores generated instances using strong reference.
     */
    protected val strongCache = mutableMapOf<Pair<AdapterSpecification<*, *>, Any>, Any>()

    /**
     * Immutable view of [strongCache].
     */
    val unmodStrongCache = Collections.unmodifiableMap(this.strongCache)

    /**
     * Map that store all registered converters.
     */
    protected val converterMap = mutableMapOf<Pair<Class<*>, Class<*>>, Converter<*, *>>()

    /**
     * Immutable view of [converterMap]
     */
    val unmodConverterMap = Collections.unmodifiableMap(this.converterMap)

    /**
     * AdapterManager dependent dynamic field storage, by default we use [WeakAdapteeStorage].
     */
    val storage: Storage = WeakAdapteeStorage()

    /**
     * Registers [adapterSpecification].
     */
    fun <E : Any> register(adapterSpecification: AdapterSpecification<E, out Any>) {
        Objects.requireNonNull(adapterSpecification)

        this.adapterSpecificationSet.add(adapterSpecification)
    }

    /**
     * Unregisters [adapterSpecification]
     */
    fun unregister(adapterSpecification: AdapterSpecification<*, *>) {
        Objects.requireNonNull(adapterSpecification)

        this.adapterSpecificationSet.remove(adapterSpecification)
    }

    /**
     * Registers [converter] that converts [from] type [I] [to] type [O].
     */
    fun <I : Any, O : Any> registerConverter(from: Class<I>, to: Class<O>, converter: Converter<I, O>) {
        this.converterMap.put(Pair.of<Class<*>, Class<*>>(from, to), converter)

        val revert = converter.revert()

        if (revert != null)
            this.converterMap.put(Pair.of<Class<*>, Class<*>>(to, from), revert)
    }


    /**
     * Unregisters the converter that converts [from] type [I] [to] type [O].
     */
    @Suppress("UNCHECKED_CAST")
    fun <I : Any, O : Any> unregisterConverter(from: Class<I>, to: Class<O>) {
        val pair = Pair.of<Class<*>, Class<*>>(from, to)

        val map = this.converterMap

        if (map.containsKey(pair)) {
            val converter = map[pair]!!

            map.remove(pair)

            if (converter.revert() != null) {
                map.remove(Pair.of<Class<*>, Class<*>>(to, from))
            }
        }
    }

    /**
     * Gets the [Converter] that converts [from] type [I] [to] type [O].
     */
    fun <I : Any, O : Any> getConverter(from: Class<I>, to: Class<O>): Optional<Converter<in I, out O>> {
        return this.getAssignableConverter(from, to)
    }

    /**
     * Gets the [Converter] that converts [from] type [I] [to] type [O]. If not found, throws an exception.
     */
    fun <I : Any, O : Any> getConverterUnchecked(from: Class<I>, to: Class<O>): Converter<in I, out O> {
        return Require.require(this.getConverter(from, to), "Can't find a converter that converts from '" + from.canonicalName + "' to '" + to.canonicalName + "'!")
    }

    /**
     * Converts [input] [from] type [I] [to] type [O].
     */
    fun <I : Any, O : Any> convert(from: Class<I>, to: Class<O>, input: I, adapter: Adapter<*>?): Optional<O> {
        return this.getConverter(from, to).map { it.convert(input, adapter, this) }
    }

    /**
     * Converts [input] [from] type [I] [to] type [O]. If converter cannot be found, throws an exception.
     */
    fun <I : Any, O : Any> convertUnchecked(from: Class<I>, to: Class<O>, input: I, adapter: Adapter<*>?): O {
        return Require.require(this.convert(from, to, input, adapter), "Can't convert from '" + from.canonicalName + "' to '" + to.canonicalName + "'!")
    }


    /**
     * Adapts [instance] of [adaptee] type [E] to an instance assignable [toClasses].
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any> adaptSingleAsAny(adaptee: Class<in E>, instance: E, toClasses: Array<Class<*>>?): Optional<Any> {
        var toClasses = toClasses

        if (toClasses == null)
            toClasses = emptyArray()

        // Gets the specification of Adapter that adapts 'adaptee' to 'toClasses'
        val adapterSpecificationOpt = this[adaptee, toClasses]

        // Check if is no one specification was found
        if (!adapterSpecificationOpt.isPresent) {

            // Check if instance if adapter base
            if (instance is AdapterBase<*>) {
                // Gets original instance/adapted instance
                val origin = instance.originalInstance

                // If target classes is empty or original instance is instance of all types
                if (toClasses.isEmpty() || toClasses.all { it.isInstance(origin) })
                // Returns original instance.
                    return Optional.of(origin)
            }

            // Returns empty
            return Optional.empty()
        }

        return this.adaptSingleAsAny(instance, adapterSpecificationOpt.get() as AdapterSpecification<E, Any>)
    }

    /**
     * Adapts [instance] based on [adapterSpecification].
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any> adaptSingleAsAny(instance: E, adapterSpecification: AdapterSpecification<E, Any>): Optional<Any> {
        // Gets the cache map
        val cache = this.cache

        // Gets the strong cache map
        val strongCache = this.strongCache

        // Create the pair representing the specification and instance to adapt
        val pair = Pair.of<AdapterSpecification<*, *>, Any>(adapterSpecification, instance)

        // Gets cached instance
        val cacheGet = cache[pair]

        // If not null, returns cached adapter instance.
        if (cacheGet != null) {
            return Optional.of(cacheGet as E)
        }

        // Gets strong cached instance
        val strongCacheGet = strongCache[pair]

        // If not null, returns cached adapter instance.
        if (strongCacheGet != null) {
            return Optional.of(strongCacheGet as E)
        }

        // Create adapter instance;
        val t = adapterSpecification.create(instance, this)

        // If should strong cache
        if (adapterSpecification.strongCache(t)) {
            // Strong cache the instance
            strongCache[pair] = t
        } else {
            // Cache the instance
            cache[pair] = t
        }

        // Returns the adapted instance.
        return Optional.of(t)
    }

    /**
     * See [AdapterManager]
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any> adaptAsAny(adaptee: Class<in E>, instance: E, toClasses: Array<Class<*>>): Optional<Any> {
        return this.adaptSingleAsAny(adaptee, instance, toClasses)
    }

    /**
     * See [AdapterManager]
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any> adaptAsAny(adaptee: Class<in E>, instance: E, toClass: Class<*>?): Optional<Any> {
        return this.adaptSingleAsAny(adaptee, instance, toClass?.let { arrayOf(it) } ?: emptyArray())
    }

    /**
     * See [AdapterManager]
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any> adaptAsAdapter(adaptee: Class<in E>, instance: E, toClasses: Array<Class<*>>): Optional<Adapter<E>> {
        return this.adaptSingleAsAny(adaptee, instance, toClasses) as Optional<Adapter<E>>
    }

    /**
     * See [AdapterManager]
     */
    fun <E : Any> adaptAsAdapter(adaptee: Class<in E>, instance: E, toClass: Class<*>?): Optional<Adapter<E>> {
        return this.adaptAsAdapter(adaptee, instance, toClass?.let { arrayOf(it) } ?: emptyArray())
    }

    /**
     * See [AdapterManager]
     */
    fun <E : Any> adaptUncheckedAsAdapter(adaptee: Class<in E>, instance: E, toClasses: Array<Class<*>>): Adapter<E> {
        return Require.require(this.adapt(adaptee, instance, toClasses), "Can't find adapter of '" + adaptee + "' to '" + Arrays.toString(toClasses) + "'!")
    }

    /**
     * See [AdapterManager]
     */
    fun <E : Any> adaptUncheckedAsAdapter(adaptee: Class<in E>, instance: E, toClass: Class<*>): Adapter<E> {
        return Require.require(this.adaptAsAdapter(adaptee, instance, toClass), "Can't find adapter of '$adaptee' to '$toClass'!")
    }

    /**
     * Creates an wrapper list that delegates calls to wrapped set and adapt values calling [AdapterManager] methods.
     * This list wrapper will *adapt* values of type [E] of [instanceList]
     * to values of type [O] (and vice-versa when needed).
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any, O : Any> createAdapterList(adaptee: Class<E>, instanceList: List<E>, toClass: Class<O>): List<O> =
            AdapterList(instanceList, toClass, adaptee, this)

    /**
     * Creates an wrapper set that delegates calls to wrapped set and adapt values calling [AdapterManager] methods.
     * This set wrapper will *adapt* values of type [E] of [instanceSet]
     * to values of type [O] (and vice-versa when needed).
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any, O : Any> createAdapterSet(adaptee: Class<E>, instanceSet: Set<E>, toClass: Class<O>): Set<O> =
            AdapterSet(instanceSet, toClass, adaptee, this)

    /**
     * Creates an wrapper collection that delegates calls to wrapped collection and adapt values calling [AdapterManager] methods.
     * This collection wrapper will *adapt* values of type [E] of [instanceCollection]
     * to values of type [O] (and vice-versa when needed).
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any, O : Any> createAdapterCollection(adaptee: Class<E>,
                                                   instanceCollection: Collection<E>,
                                                   toClass: Class<O>): Collection<O> =
            AdapterList(instanceCollection, toClass, adaptee, this)


    /**
     * Creates an wrapper map that delegates calls to wrapped map and adapt values calling [AdapterManager] methods.
     * This map wrapper will *adapt* keys of type [KE] and values of type [VE] of [instanceMap] to keys and values
     * of types [KO] and [VO] consecutively (and vice-versa when needed).
     */
    @Suppress("UNCHECKED_CAST")
    fun <KE : Any, VE : Any, KO : Any, VO : Any> createAdapterMap(adapteeKey: Class<KE>, adapteeValue: Class<VE>,
                                                                  instanceMap: Map<KE, VE>,
                                                                  toKey: Class<KO>, toValue: Class<VO>): Map<KO, VO> =
            AdapterMap(instanceMap, toKey, toValue, adapteeKey, adapteeValue, this)

    // Dynamic

    /**
     * Creates an wrapper list that delegates calls to wrapped set and adapt values calling [AdapterManager] methods.
     * This list wrapper will *adapt* values of type [E] of [instanceList]
     * to values of type [O] (and vice-versa when needed).
     *
     * This variant respects type inheritance.
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any, O : Any> createDynamicAdapterList(adaptee: Class<E>, instanceList: List<E>, toClass: Class<O>): List<O> =
            AdapterListFunc<O, E>(instanceList,
                    LookupFunc(this, toClass, adaptee),
                    LookupFunc(this, adaptee, toClass),
                    this)

    /**
     * Creates an wrapper set that delegates calls to wrapped set and adapt values calling [AdapterManager] methods.
     * This set wrapper will *adapt* values of type [E] of [instanceSet]
     * to values of type [O] (and vice-versa when needed).
     *
     * This variant respects type inheritance.
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any, O : Any> createDynamicAdapterSet(adaptee: Class<E>, instanceSet: Set<E>, toClass: Class<O>): Set<O> =
            AdapterSetFunc<O, E>(instanceSet,
                    LookupFunc(this, toClass, adaptee),
                    LookupFunc(this, adaptee, toClass),
                    this)

    /**
     * Creates an wrapper collection that delegates calls to wrapped collection and adapt values calling [AdapterManager] methods.
     * This collection wrapper will *adapt* values of type [E] of [instanceCollection]
     * to values of type [O] (and vice-versa when needed).
     *
     * This variant respects type inheritance.
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any, O : Any> createDyanmicAdapterCollection(adaptee: Class<E>,
                                                          instanceCollection: Collection<E>,
                                                          toClass: Class<O>): Collection<O> =
            AdapterCollectionFunc<O, E>(instanceCollection,
                    LookupFunc(this, toClass, adaptee),
                    LookupFunc(this, adaptee, toClass),
                    this)


    /**
     * Creates an wrapper map that delegates calls to wrapped map and adapt values calling [AdapterManager] methods.
     * This map wrapper will *adapt* keys of type [KE] and values of type [VE] of [instanceMap] to keys and values
     * of types [KO] and [VO] consecutively (and vice-versa when needed).
     *
     * This variant respects type inheritance.
     */
    @Suppress("UNCHECKED_CAST")
    fun <KE : Any, VE : Any, KO : Any, VO : Any> createDynamicAdapterMap(adapteeKey: Class<KE>, adapteeValue: Class<VE>,
                                                                         instanceMap: Map<KE, VE>,
                                                                         toKey: Class<KO>, toValue: Class<VO>): Map<KO, VO> =
            AdapterMapFunc<KO, VO, KE, VE>(instanceMap,
                    LookupFunc(this, toKey, adapteeKey),
                    LookupFunc(this, toValue, adapteeValue),
                    LookupFunc(this, adapteeKey, toKey),
                    LookupFunc(this, adapteeValue, toValue),
                    this)

    // /Dynamic

    /**
     * See [AdapterManager]
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
     * See [AdapterManager]
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any, O : Any> adapt(adaptee: Class<in E>, instance: E, toClasses: Array<Class<*>>): Optional<O> {
        return this.adaptSingleAsAny(adaptee, instance, toClasses) as Optional<O>
    }

    /**
     * See [AdapterManager]
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any, O : Any> adapt(adaptee: Class<in E>, instance: E, toClass: Class<O>): Optional<O> {
        return this.adaptSingleAsAny(adaptee, instance, arrayOf<Class<*>>(toClass)) as Optional<O>
    }

    /**
     * See [AdapterManager]
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any, O : Any> adaptBaseUnchecked(adaptee: Class<in E>, instance: E, toClasses: Array<Class<*>>): O {
        return Require.require(this.adaptBase(adaptee, instance, toClasses), "Can't find adapter of '$adaptee' (and of '${instance::class.java}') to '${toClasses.contentToString()}'!")
    }

    /**
     * See [AdapterManager]
     */
    fun <E : Any, O : Any> adaptUnchecked(adaptee: Class<in E>, instance: E, toClass: Class<O>): O {
        return Require.require(this.adapt(adaptee, instance, toClass), "Can't find adapter of '$adaptee' to '$toClass'!")
    }

    /**
     * See [AdapterManager]
     */
    @Suppress("UNCHECKED_CAST")
    fun <E : Any, O : Any> adaptUnchecked(adaptee: Class<in E>, instance: E, toClasses: Array<Class<*>>): O {
        return Require.require(this.adapt(adaptee, instance, toClasses), "Can't find adapter of '" + adaptee + "' to '" + Arrays.toString(toClasses) + "'!")
    }

    /**
     * Cleanup [Adapter Instance Cache][cache].
     */
    fun cleanupInstanceCache() {
        this.cache.clear()
    }

    /**
     * Cleanup [Strong Adapter Instance Cache][cache].
     */
    fun cleanupStrongInstanceCache() {
        this.strongCache.clear()
    }

    /**
     * Removes strong cache entry of [adapteeInstance] that was adapted by adapter specified by [specification],
     * and returns remove adapter instance, or null if not present.
     */
    fun uncacheStrong(adapteeInstance: Any, specification: AdapterSpecification<*, *>): Any? =
            this.strongCache.remove(Pair.of(specification, adapteeInstance))

    /**
     * Removes all strong cache entry associated to [adapteeInstance] regardless the adapter,
     * and returns true if any value was removed as result of this operation.
     */
    fun uncacheAllStrong(adapteeInstance: Any): Boolean
            = this.strongCache.entries.removeIf { it.key.second == adapteeInstance }


    /**
     * See [AdapterManager]
     */
    @Suppress("UNCHECKED_CAST", "NAME_SHADOWING")
    fun <I : Any, O : Any> getExactConverter(from: Class<I>, to: Class<O>): Optional<Converter<in I, out O>> {
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

        return Optional.ofNullable(this.converterMap[pair] as Converter<I, O>?)
    }

    /**
     * See [AdapterManager]
     */
    @Suppress("UNCHECKED_CAST")
    fun <I : Any, O : Any> getAssignableConverter(from: Class<I>, to: Class<O>): Optional<Converter<in I, out O>> {
        val exactConverter = this.getExactConverter(from, to)

        if (exactConverter.isPresent)
            return exactConverter

        val map = this.converterMap

        for ((key, value) in map) {

            // The confuse section of documentation
            // Only thing that you have to known about this code is that it determines if the converter is valid
            // This code leads with Covariance and Contravariance
            // Converter#from is contravariant and Converter#to is covariant
            if (key.first.isAssignableFrom(from)) {
                // If the parameter 'from' is assignable to converter@from
                if (to.isAssignableFrom(key.second))
                // If converter@to is assignable from parameter 'to'
                    return Optional.of(value as Converter<I, O>) // Returns the converter
            }
        }

        return Optional.empty()
    }

    /**
     * See [AdapterManager]
     */
    operator fun get(adaptee: Class<*>, relation: Array<Class<*>>): Optional<AdapterSpecification<*, *>> {
        return this.getAssignable(adaptee, relation)
    }

    /**
     * See [AdapterManager]
     */
    fun getExact(adaptee: Class<*>, relation: Array<Class<*>>): Optional<AdapterSpecification<*, *>> {

        return Optional.ofNullable(this.adapterSpecificationSet
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
     * See [AdapterManager]
     */
    fun getAssignable(adaptee: Class<*>, relation: Array<Class<*>>): Optional<AdapterSpecification<*, *>> {
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

    /**
     * Creates a [Builder] instance that allows a construction two way adapter helper instance ([Builder.FromTo]).
     */
    fun builder(): Builder = ABuilder()

    inner class ABuilder : Builder {
        override fun <F : Any> from(type: Class<F>): Builder.From<F> = AFrom(type)
        override fun <T : Any> to(type: Class<T>): Builder.To<T> = ATo(type)
        override fun <T : Any> to(closestType: Class<T>, types: List<Class<out T>>): Builder.MultiTo<T> {
            if (types.any { !closestType.isAssignableFrom(it) })
                throw IllegalArgumentException("Can't assign a type of ${types.joinToString()} to closestType: $closestType.")

            return AMultiTo(types.toTypedArray())
        }
    }

    inner class AFrom<F : Any>(val from: Class<F>) : Builder.From<F> {
        override fun <T : Any> to(type: Class<T>): Builder.FromTo<F, T> = AFromTo(from, type)
        override fun <T : Any> to(closestType: Class<T>, types: List<Class<out T>>): Builder.FromMultiTo<F, T> {
            if (types.any { !closestType.isAssignableFrom(it) })
                throw IllegalArgumentException("Can't assign a type of ${types.joinToString()} to closestType: $closestType.")

            return AFromMultiTo(from, types.toTypedArray())
        }
    }

    inner class ATo<T : Any>(val to: Class<T>) : Builder.To<T> {
        override fun <F : Any> from(type: Class<F>): Builder.FromTo<F, T> = AFromTo(type, to)
    }

    inner class AMultiTo<T : Any>(val to: Array<Class<out T>>) : Builder.MultiTo<T> {
        override fun <F : Any> from(type: Class<F>): Builder.FromMultiTo<F, T> = AFromMultiTo(type, to)
    }

    inner class AFromTo<F : Any, T : Any>(val from: Class<F>, val to: Class<T>) : Builder.FromTo<F, T> {
        private val reverse = ReverseAFromTo(this)

        override fun reverse(): Builder.FromTo<T, F> = reverse
        override fun adapt(instance: F?): T? = if (instance == null) null else adaptUnchecked(from, instance, to)
    }

    inner class ReverseAFromTo<F : Any, T : Any>(val origin: AFromTo<T, F>) : Builder.FromTo<F, T> {

        override fun reverse(): Builder.FromTo<T, F> = origin
        override fun adapt(instance: F?): T? = if (instance == null) null else adaptUnchecked(origin.to, instance, origin.from)
    }

    inner class AFromMultiTo<F : Any, T : Any>(val from: Class<F>, val to: Array<Class<out T>>) : Builder.FromMultiTo<F, T> {
        @Suppress("UNCHECKED_CAST")
        override fun adapt(instance: F?): T? = if (instance == null) null else adaptUnchecked(from, instance, to as Array<Class<*>>)
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
