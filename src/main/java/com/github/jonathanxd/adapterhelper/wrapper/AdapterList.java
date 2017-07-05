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
import com.github.jonathanxd.iutils.iterator.IteratorUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

public class AdapterList<T, A> extends AdapterCollection<T, A> implements List<T> {

    public AdapterList(Collection<A> wrapped, Class<T> target, Class<A> adaptee, AdapterManager manager) {
        super(wrapped, target, adaptee, manager);
    }

    @Override
    public List<A> getWrapped() {
        return (List<A>) super.getWrapped();
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        return this.getWrapped().addAll(index, this.adaptAllT(c));
    }

    @Override
    public T get(int index) {
        return this.getFromTo().reverse().adapt(this.getWrapped().get(index));
    }

    @Override
    public T set(int index, T element) {
        return this.getFromTo().reverse().adapt(this.getWrapped().set(index, this.getFromTo().adapt(element)));
    }

    @Override
    public void add(int index, T element) {
        this.getWrapped().set(index, this.getFromTo().adapt(element));
    }

    @Override
    public T remove(int index) {
        return this.getFromTo().reverse().adapt(this.getWrapped().remove(index));
    }

    @Override
    public int indexOf(Object o) {
        return this.getWrapped().indexOf(this.adaptToA(o));
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.getWrapped().lastIndexOf(this.adaptToA(o));
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        return this.listIterator(0);
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator(int index) {
        return IteratorUtil.mappedIterator(
                this.getWrapped().listIterator(index),
                a -> this.getFromTo().reverse().adapt(a),
                r -> this.getFromTo().adapt(r));
    }

    @NotNull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return new AdapterList<>(this.getWrapped().subList(fromIndex, toIndex), this.getTarget(), this.getAdaptee(), this.getManager());
    }
}