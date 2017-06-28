/*
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
package com.github.jonathanxd.adapterhelper.wrapper;

import com.github.jonathanxd.adapterhelper.AdapterManager;
import com.github.jonathanxd.adapterhelper.Builder;
import com.github.jonathanxd.iutils.iterator.IteratorUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class AdapterCollection<T, A> implements Collection<T> {

    private final Collection<A> wrapped;
    private final Class<T> target;
    private final Class<A> adaptee;
    private final AdapterManager manager;
    private final Builder.FromTo<T, A> fromTo;

    public AdapterCollection(Collection<A> wrapped, Class<T> target, Class<A> adaptee, AdapterManager manager) {
        this.wrapped = wrapped;
        this.target = target;
        this.adaptee = adaptee;
        this.manager = manager;
        this.fromTo = manager.builder().from(target).to(adaptee);
    }

    public Collection<A> getWrapped() {
        return this.wrapped;
    }

    public Class<A> getAdaptee() {
        return this.adaptee;
    }

    public Class<T> getTarget() {
        return this.target;
    }

    public AdapterManager getManager() {
        return this.manager;
    }

    public Builder.FromTo<T, A> getFromTo() {
        return this.fromTo;
    }

    @Override
    public int size() {
        return this.getWrapped().size();
    }

    @Override
    public boolean isEmpty() {
        return this.getWrapped().isEmpty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean contains(Object o) {
        return this.getWrapped().contains(this.adaptToA(o));
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return IteratorUtil.mappedIterator(this.getWrapped().iterator(), a -> this.getFromTo().reverse().adapt(a));
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public Object[] toArray() {

        /*A[]*/
        Object[] objects = this.getWrapped().toArray();
        /*T[]*/
        Object[] transformed = new Object[objects.length];

        for (int i = 0; i < objects.length; i++) {
            transformed[i] = this.getFromTo().reverse().adapt((A) objects[i]);
        }

        return transformed;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public <T1> T1[] toArray(@NotNull T1[] a) {

        Object[] array = this.toArray();

        for (int i = 0; i < a.length; i++) {
            a[i] = (T1) array[i];
        }

        return a;
    }

    @Override
    public boolean add(T t) {
        return this.getWrapped().add(this.getFromTo().adapt(t));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        return this.getWrapped().remove(this.adaptToA(o));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return this.getWrapped().containsAll(this.adaptAll(c));
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        return this.getWrapped().addAll(this.adaptAllT(c));
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return this.getWrapped().removeAll(this.adaptAll(c));
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return this.getWrapped().retainAll(this.adaptAll(c));
    }

    /**
     * Adapts all objects of generic collection {@code c} which is instance of {@link #target} to
     * {@link A}, other objects (that are not instance of {@link #target} is not adapted and is
     * added as they are).
     *
     * @param c Collection with objects.
     * @return Collection with both adapted and not adapted objects.
     */
    @SuppressWarnings("unchecked")
    protected Collection<Object> adaptAll(Collection<?> c) {
        Collection<Object> mapped = new ArrayList<>();

        for (Object o : c) {
            mapped.add(this.adaptToA(o));
        }
        return mapped;
    }

    /**
     * Adapts all objects of generic collection {@code c} which is instance of {@link #target} to
     * {@link A}, other objects (that are not instance of {@link #target} is not adapted and is
     * added as they are).
     *
     * @param c Collection with objects.
     * @return Collection with both adapted and not adapted objects.
     */
    @SuppressWarnings("unchecked")
    protected Collection<? extends A> adaptAllT(Collection<? extends T> c) {
        Collection<A> mapped = new ArrayList<>();

        for (T o : c) {
            mapped.add(this.getFromTo().adapt(o));
        }

        return mapped;
    }

    /**
     * Adapts generic object {@code o} to {@link A} if {@code o} is assignable to {@link #target}.
     *
     * @param o Object to adapt.
     * @return Object or adapted instance.
     */
    @SuppressWarnings("unchecked")
    protected A adaptToA(Object o) {
        if (!this.getTarget().isInstance(o))
            return (A) o;

        return this.getFromTo().adapt((T) o);
    }

    @Override
    public void clear() {
        this.getWrapped().clear();
    }
}
