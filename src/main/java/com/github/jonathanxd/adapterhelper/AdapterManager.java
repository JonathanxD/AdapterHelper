/*
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
package com.github.jonathanxd.adapterhelper;

import com.github.jonathanxd.iutils.map.WeakValueHashMap;
import com.github.jonathanxd.iutils.object.Pair;
import com.github.jonathanxd.iutils.optional.Require;
import com.github.jonathanxd.iutils.reflection.ClassUtil;
import com.github.jonathanxd.iutils.type.Primitive;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Manages all Adapter Specifications.
 */
public class AdapterManager {

    /**
     * Set that store all registered {@link AdapterSpecification}.
     */
    private final Set<AdapterSpecification<?, ?>> adapterSpecificationSet = new HashSet<>();
    private final Set<AdapterSpecification<?, ?>> unmodAdapterSpecificationSet = Collections.unmodifiableSet(this.adapterSpecificationSet);

    /**
     * Weak value map that stores generated instances. (The value must be weak).
     */
    private final Map<Pair<AdapterSpecification<?, ?>, Object>, Object> cache = new WeakValueHashMap<>();
    private final Map<Pair<AdapterSpecification<?, ?>, Object>, Object> unmodCache = Collections.unmodifiableMap(this.cache);

    /**
     * Map that store all registered converters.
     */
    private final Map<Pair<Class<?>, Class<?>>, Converter<?, ?>> converterMap = new HashMap<>();
    private final Map<Pair<Class<?>, Class<?>>, Converter<?, ?>> unmodConverterMap = Collections.unmodifiableMap(this.converterMap);

    protected AdapterManager() {
    }

    /**
     * Creates a instance of Adapter Manager.
     *
     * @return A new instance of Adapter Manager.
     */
    public static AdapterManager create() {
        return new AdapterManager();
    }

    /**
     * Register an {@link AdapterSpecification}.
     *
     * @param adapterSpecification specification to register.
     * @param <T>                  Adapter type.
     * @param <E>                  Adaptee type.
     */
    public <T extends Adapter<E>, E> void register(AdapterSpecification<T, E> adapterSpecification) {
        Objects.requireNonNull(adapterSpecification);

        this.getAdapterSpecificationMutableSet().add(adapterSpecification);
    }

    /**
     * Unregister adapter specification.
     *
     * @param adapterSpecification Specification to unregister.
     */
    public void unregister(AdapterSpecification<?, ?> adapterSpecification) {
        Objects.requireNonNull(adapterSpecification);

        this.getAdapterSpecificationMutableSet().remove(adapterSpecification);
    }

    /**
     * Register a converter that converts from {@link I} to {@link O}.
     *
     * @param from      Input type.
     * @param to        Output type.
     * @param converter Converter.
     * @param <I>       Input type.
     * @param <O>       Output type.
     */
    public <I, O> void registerConverter(Class<I> from, Class<O> to, Converter<I, O> converter) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        Objects.requireNonNull(converter);

        this.getConverterMutableMap().put(Pair.of(from, to), converter);

        Converter<?, ?> revert = converter.revert();

        if (revert != null)
            this.getConverterMutableMap().put(Pair.of(to, from), revert);
    }

    /**
     * Unregister a converter that converts from {@link I} to {@link O}.
     *
     * @param from Input type.
     * @param to   Output type.
     * @param <I>  Input type.
     * @param <O>  Output type.
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    public <I, O> void unregisterConverter(Class<I> from, Class<O> to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);

        Pair<Class<?>, Class<?>> pair = Pair.of(from, to);

        Map<Pair<Class<?>, Class<?>>, Converter<?, ?>> map = this.getConverterMutableMap();

        if (map.containsKey(pair)) {
            Converter<?, ?> converter = map.get(pair);
            map.remove(pair);

            if (converter.revert() != null) {
                map.remove(Pair.of(to, from)); // Why SuspiciousMethodCalls IntelliJ?
            }
        }
    }

    /**
     * Gets the converter from {@link I} to {@link O}.
     *
     * @param from Input type.
     * @param to   Output type.
     * @param <I>  Input type.
     * @param <O>  Output type.
     * @return Converter from {@link I} to {@link O}.
     */
    @SuppressWarnings("unchecked")
    public <I, O> Optional<Converter<? super I, ? extends O>> getConverter(Class<I> from, Class<O> to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);

        return this.getAssignableConverter(from, to);
    }

    /**
     * Gets the converter from {@link I} to {@link O}.
     *
     * @param from Input type.
     * @param to   Output type.
     * @param <I>  Input type.
     * @param <O>  Output type.
     * @return Converter from {@link I} to {@link O}.
     */
    @SuppressWarnings("unchecked")
    private <I, O> Optional<Converter<? super I, ? extends O>> getExactConverter(Class<I> from, Class<O> to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);

        // Convert primitive types to boxed version.
        if(from.isPrimitive()) {
            from = (Class<I>) Primitive.box(from);
        }

        if(to.isPrimitive()) {
            to = (Class<O>) Primitive.box(to);
        }

        Pair<Class<?>, Class<?>> pair = Pair.of(from, to);

        return Optional.ofNullable((Converter<I, O>) this.getConverterMutableMap().get(pair));
    }

    /**
     * Gets the converter from any instance assignable to {@link I} to {@link O} or super-types of {@link O}.
     *
     * @param from Input type.
     * @param to   Output type.
     * @param <I>  Input type.
     * @param <O>  Output type.
     * @return Converter from {@link I} to {@link O}.
     */
    @SuppressWarnings("unchecked")
    private <I, O> Optional<Converter<? super I, ? extends O>> getAssignableConverter(Class<I> from, Class<O> to) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);

        Optional<Converter<? super I, ? extends O>> exactConverter = this.getExactConverter(from, to);

        if(exactConverter.isPresent())
            return exactConverter;

        List<Class<?>> toSuperTypes = ClassUtil.getSortedSuperTypes(to);

        Map<Pair<Class<?>, Class<?>>, Converter<?, ?>> map = this.getConverterMutableMap();

        for (Map.Entry<Pair<Class<?>, Class<?>>, Converter<?, ?>> pairConverterEntry : map.entrySet()) {
            Pair<Class<?>, Class<?>> key = pairConverterEntry.getKey();
            Converter<?, ?> value = pairConverterEntry.getValue();

            // The confuse section of documentation
            // Only thing that you have to known about this code is that it determines if the converter is valid
            // This code leads with Covariance and Contravariance
            // Converter#from is contravariant and Converter#to is covariant
            if(key._1().isAssignableFrom(from)) { // If the parameter 'from' is assignable to converter@from
                if(to.isAssignableFrom(key._2())) // If converter@to is assignable from parameter 'to'
                    return Optional.of((Converter<I, O>) value); // Returns the converter
            }
        }

        return Optional.empty();
    }

    /**
     * Gets the converter from {@link I} to {@link O}.
     *
     * @param from Input type.
     * @param to   Output type.
     * @param <I>  Input type.
     * @param <O>  Output type.
     * @return Converter from {@link I} to {@link O}.
     */
    @SuppressWarnings("unchecked")
    public <I, O> Converter<? super I, ? extends O> getConverterUnchecked(Class<I> from, Class<O> to) {
        return Require.require(this.getConverter(from, to), "Can't find a converter that converts from '" + from.getCanonicalName() + "' to '" + to.getCanonicalName() + "'!");
    }

    /**
     * Convert {@link I} to {@link O}.
     *
     * @param from    Input type.
     * @param to      Output type.
     * @param input   Input value.
     * @param adapter Adapter instance (may be null).
     * @param <I>     Input type.
     * @param <O>     Output type.
     * @return Converted instance.
     */
    public <I, O> Optional<O> convert(Class<I> from, Class<O> to, I input, Adapter<?> adapter) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        Objects.requireNonNull(input);

        return this.getConverter(from, to).map(converter -> Objects.requireNonNull(converter.convert(input, adapter, this)));
    }

    /**
     * Convert {@link I} to {@link O}.
     *
     * @param from    Input type.
     * @param to      Output type.
     * @param input   Input value.
     * @param adapter Adapter instance (may be null).
     * @param <I>     Input type.
     * @param <O>     Output type.
     * @return Converted instance.
     */
    public <I, O> O convertUnchecked(Class<I> from, Class<O> to, I input, Adapter<?> adapter) {
        return Require.require(this.convert(from, to, input, adapter), "Can't convert from '" + from.getCanonicalName() + "' to '" + to.getCanonicalName() + "'!");
    }

    /**
     * Adapt {@code instance} to a instance assignable to {@code toClass}.
     *
     * @param adaptee  Adaptee class.
     * @param instance Adaptee instance.
     * @param toClass  Expected class.
     * @param <T>      Adapter type.
     * @param <E>      Adaptee type.
     * @return Adapter instance.
     */
    @SuppressWarnings("unchecked")
    public <T extends Adapter<E>, E> Optional<T> adapt(Class<? extends E> adaptee, E instance, Class<? super T> toClass) {
        Objects.requireNonNull(adaptee);
        Objects.requireNonNull(instance);

        Class[] classes;

        if (toClass == null)
            classes = new Class[0];
        else
            classes = new Class[]{toClass};

        return this.adapt(adaptee, instance, classes);
    }

    /**
     * Adapt {@code instance} to a instance assignable to {@code toClass}.
     *
     * @param adaptee  Adaptee class.
     * @param instance Adaptee instance.
     * @param toClass  Expected class.
     * @param <T>      Adapter type.
     * @param <E>      Adaptee type.
     * @return Adapter instance.
     */
    @SuppressWarnings("unchecked")
    public <T extends Adapter<E>, E> T adaptUnchecked(Class<? extends E> adaptee, E instance, Class<? super T> toClass) {
        return Require.require(this.adapt(adaptee, instance, toClass), "Can't find adapter of '" + adaptee + "' to '" + toClass + "'!");
    }

    /**
     * Adapt {@code instance} to a instance assignable to {@code toClasses}.
     *
     * @param adaptee   Adaptee class.
     * @param instance  Adaptee instance.
     * @param toClasses Expected classes.
     * @param <T>       Adapter type.
     * @param <E>       Adaptee type.
     * @return Adapter instance.
     */
    @SuppressWarnings("unchecked")
    public <T extends Adapter<E>, E> Optional<T> adapt(Class<? extends E> adaptee, E instance, Class<?>[] toClasses) {
        Objects.requireNonNull(adaptee);
        Objects.requireNonNull(instance);

        if (toClasses == null)
            toClasses = new Class[0];

        // Gets the specification of Adapter that adapts 'adaptee' to 'toClasses'
        Optional<AdapterSpecification<?, ?>> adapterSpecificationOpt = this.get(adaptee, toClasses);

        // Check if is no one specification was found
        if (!adapterSpecificationOpt.isPresent())
            // Returns empty
            return Optional.empty();

        // Unbox the specification and unsafe cast
        AdapterSpecification<T, E> adapterSpecification = (AdapterSpecification<T, E>) adapterSpecificationOpt.get();

        // Gets the cache map
        Map<Pair<AdapterSpecification<?, ?>, Object>, Object> cache = this.getMutableCache();

        // Create the pair representing the specification and instance to adapt
        Pair<AdapterSpecification<?, ?>, Object> pair = Pair.of(adapterSpecification, instance);

        // Check if cache contains an Adapter instance that adapted 'instance'
        if (cache.containsKey(pair)) {
            // Returns the cached instance
            return Optional.of((T) cache.get(pair));
        }

        // Create adapter instance;
        T t = adapterSpecification.create(instance, this);

        // Cache the instance
        cache.put(pair, t);

        // Returns the instance.
        return Optional.of(t);
    }

    /**
     * Adapt {@code instance} to a instance assignable to {@code toClass}.
     *
     * @param adaptee   Adaptee class.
     * @param instance  Adaptee instance.
     * @param toClasses Expected classes.
     * @param <T>       Adapter type.
     * @param <E>       Adaptee type.
     * @return Adapter instance.
     */
    @SuppressWarnings("unchecked")
    public <T extends Adapter<E>, E> T adaptUnchecked(Class<? extends E> adaptee, E instance, Class<?>[] toClasses) {
        return Require.require(this.adapt(adaptee, instance, toClasses), "Can't find adapter of '" + adaptee + "' to '" + Arrays.toString(toClasses) + "'!");
    }

    /**
     * Gets the {@link AdapterSpecification} that provide the adapter of {@code adaptee} class that
     * inherits all {@code relation} classes.
     *
     * @param adaptee  Adapter class.
     * @param relation Inheritance relation.
     * @return {@link AdapterSpecification} that provide the adapter of {@code adaptee} class that
     * inherits all {@code relation} classes.
     */
    private Optional<AdapterSpecification<?, ?>> get(Class<?> adaptee, Class<?>[] relation) {
        return this.getAssignable(adaptee, relation);
    }

    /**
     * Gets the {@link AdapterSpecification} that provide the adapter of {@code adaptee} class that
     * inherits all {@code relation} classes.
     *
     * @param adaptee  Adapter class.
     * @param relation Inheritance relation.
     * @return {@link AdapterSpecification} that provide the adapter of {@code adaptee} class that
     * inherits all {@code relation} classes.
     */
    private Optional<AdapterSpecification<?, ?>> getExact(Class<?> adaptee, Class<?>[] relation) {
        return this.getExact(adaptee, relation, true);
    }

    /**
     * Gets the {@link AdapterSpecification} that provide the adapter of {@code adaptee} class that
     * inherits all {@code relation} classes.
     *
     * @param adaptee  Adapter class.
     * @param relation Inheritance relation.
     * @param cache    Should cache.
     * @return {@link AdapterSpecification} that provide the adapter of {@code adaptee} class that
     * inherits all {@code relation} classes.
     */
    private Optional<AdapterSpecification<?, ?>> getExact(Class<?> adaptee, Class<?>[] relation, boolean cache) {
        Objects.requireNonNull(adaptee);
        Objects.requireNonNull(relation);

        return this.getAdapterSpecificationMutableSet().stream()
                .filter(adapterSpecification -> {

                    if (!adapterSpecification.getAdapteeClass().equals(adaptee))
                        return false;

                    if (relation.length == 0)
                        return true;

                    Class<?> adapterClass = adapterSpecification.getAdapterClass();

                    for (Class<?> aClass : relation) {
                        if (!aClass.isAssignableFrom(adapterClass))
                            return false;
                    }

                    return true;
                })
                .findFirst();

    }

    /**
     * Gets the {@link AdapterSpecification} that provide the adapter of {@code adaptee}, or a
     * adapter of superclasses of {@code adaptee}, class that inherits all {@code relation}
     * classes.
     *
     * @param adaptee  Adapter class.
     * @param relation Inheritance relation.
     * @return {@link AdapterSpecification} that provide the adapter of {@code adaptee} class that
     * inherits all {@code relation} classes.
     */
    @SuppressWarnings("OptionalIsPresent")
    private Optional<AdapterSpecification<?, ?>> getAssignable(Class<?> adaptee, Class<?>[] relation) {
        Objects.requireNonNull(adaptee);
        Objects.requireNonNull(relation);

        Iterator<Class<?>> sortedSuperTypes = ClassUtil.getSortedSuperTypes(adaptee).iterator();

        Optional<AdapterSpecification<?, ?>> exact = this.getExact(adaptee, relation);

        if (exact.isPresent())
            return exact;

        while (!exact.isPresent() && sortedSuperTypes.hasNext()) {
            exact = this.getExact(sortedSuperTypes.next(), relation, false);
        }

        return exact;
    }

    /**
     * Gets the Immutable {@link Set} instance of all registered {@link AdapterSpecification}.
     *
     * @return Immutable {@link Set} instance of all registered {@link AdapterSpecification}.
     */
    public Set<AdapterSpecification<?, ?>> getAdapterSpecificationSet() {
        return this.unmodAdapterSpecificationSet;
    }

    /**
     * Gets the Immutable {@link Map} of cached adapter instance.
     *
     * @return Immutable {@link Map} of cached adapter instance.
     */
    public Map<Pair<AdapterSpecification<?, ?>, Object>, Object> getCache() {
        return this.unmodCache;
    }

    /**
     * Gets the Immutable {@link Map} of registered converters.
     *
     * @return Immutable {@link Map} of registered converters.
     */
    public Map<Pair<Class<?>, Class<?>>, Converter<?, ?>> getConverterMap() {
        return unmodConverterMap;
    }

    /**
     * Gets the Mutable {@link Set} instance of all registered {@link AdapterSpecification}.
     *
     * @return Mutable {@link Set} instance of all registered {@link AdapterSpecification}.
     */
    protected Set<AdapterSpecification<?, ?>> getAdapterSpecificationMutableSet() {
        return this.adapterSpecificationSet;
    }

    /**
     * Gets the Mutable {@link Map} of cached adapter instance.
     *
     * @return Mutable {@link Map} of cached adapter instance.
     */
    protected Map<Pair<AdapterSpecification<?, ?>, Object>, Object> getMutableCache() {
        return this.cache;
    }

    /**
     * Gets the Mutable {@link Map} of registered converters.
     *
     * @return Mutable {@link Map} of registered converters.
     */
    protected Map<Pair<Class<?>, Class<?>>, Converter<?, ?>> getConverterMutableMap() {
        return this.converterMap;
    }

    /**
     * Cleanup {@link #cache Adapter Instance Cache}.
     */
    public void cleanupInstanceCache() {
        this.getMutableCache().clear();
    }
}
