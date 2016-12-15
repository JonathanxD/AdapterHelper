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

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Specification of adapter class.
 */
public final class AdapterSpecification<E,T extends Adapter<E>> {

    /**
     * Instance factory.
     */
    private final BiFunction<E, AdapterManager, T> factory;

    /**
     * Adapter class.
     */
    private final Class<T> adapterClass;

    /**
     * Adaptee class.
     */
    private final Class<E> adapteeClass;

    /**
     * Create adapter specification.
     *
     * @param factory      Factory function.
     * @param adapterClass Adapter class.
     * @param adapteeClass Adaptee class.
     */
    protected AdapterSpecification(BiFunction<E, AdapterManager, T> factory, Class<T> adapterClass, Class<E> adapteeClass) {
        Objects.requireNonNull(factory);
        Objects.requireNonNull(adapterClass);
        Objects.requireNonNull(adapteeClass);

        this.factory = factory;
        this.adapterClass = adapterClass;
        this.adapteeClass = adapteeClass;
    }

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
    public static <E, T extends Adapter<E>> AdapterSpecification<E, T> create(BiFunction<E, AdapterManager, T> factory, Class<T> adapterClass, Class<E> adapteeClass) {
        return new AdapterSpecification<>(factory, adapterClass, adapteeClass);
    }

    /**
     * Create the adapter class instance.
     *
     * @param target  Target instance.
     * @param manager Adapter manager.
     * @return Adapter instance.
     */
    public T create(E target, AdapterManager manager) {
        return this.getFactory().apply(target, manager);
    }

    /**
     * Gets the factory function.
     *
     * @return Factory function.
     */
    public BiFunction<E, AdapterManager, T> getFactory() {
        return this.factory;
    }

    /**
     * Gets the adapter class.
     *
     * @return Adapter class.
     */
    public Class<T> getAdapterClass() {
        return this.adapterClass;
    }

    /**
     * Gets the adaptee class.
     *
     * @return Adaptee class.
     */
    public Class<E> getAdapteeClass() {
        return this.adapteeClass;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getAdapteeClass(), this.getAdapterClass());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {

        return !(obj == null || obj.getClass() != AdapterSpecification.class)
                && this.getAdapteeClass().equals(((AdapterSpecification<E, T>) obj).getAdapteeClass())
                && this.getAdapterClass().equals(((AdapterSpecification<E, T>) obj).getAdapterClass());

    }
}
