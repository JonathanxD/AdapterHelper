/*
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
package com.github.jonathanxd.adapterhelper.wrapper;

import com.github.jonathanxd.adapterhelper.AdapterManager;
import com.github.jonathanxd.adapterhelper.Builder;
import com.github.jonathanxd.iutils.collection.view.ViewCollections;
import com.github.jonathanxd.iutils.collection.view.ViewSet;
import com.github.jonathanxd.iutils.iterator.IteratorUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class AdapterMap<KT, VT, KA, VA> implements Map<KT, VT> {

    private final Map<KA, VA> wrapped;
    private final Class<KT> targetKey;
    private final Class<VT> targetValue;
    private final Class<KA> adapteeKey;
    private final Class<VA> adapteeValue;
    private final AdapterManager manager;
    private final Builder.FromTo<KT, KA> fromToKey;
    private final Builder.FromTo<VT, VA> fromToValue;

    public AdapterMap(Map<KA, VA> wrapped,
                      Class<KT> targetKey,
                      Class<VT> targetValue,
                      Class<KA> adapteeKey,
                      Class<VA> adapteeValue,
                      AdapterManager manager) {
        this.wrapped = wrapped;
        this.targetKey = targetKey;
        this.targetValue = targetValue;
        this.adapteeKey = adapteeKey;
        this.adapteeValue = adapteeValue;
        this.manager = manager;
        this.fromToKey = manager.builder().from(targetKey).to(adapteeKey);
        this.fromToValue = manager.builder().from(targetValue).to(adapteeValue);
    }

    public Class<KA> getAdapteeKey() {
        return this.adapteeKey;
    }

    public Class<KT> getTargetKey() {
        return this.targetKey;
    }

    public Class<VT> getTargetValue() {
        return this.targetValue;
    }

    public Class<VA> getAdapteeValue() {
        return this.adapteeValue;
    }

    public Map<KA, VA> getWrapped() {
        return this.wrapped;
    }

    public AdapterManager getManager() {
        return this.manager;
    }

    public Builder.FromTo<KT, KA> getFromToKey() {
        return this.fromToKey;
    }

    public Builder.FromTo<VT, VA> getFromToValue() {
        return this.fromToValue;
    }

    @Override
    public int size() {
        return this.getWrapped().size();
    }

    @Override
    public boolean isEmpty() {
        return this.getWrapped().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.getWrapped().containsKey(this.adaptToKeyA(key));
    }

    @Override
    public boolean containsValue(Object value) {
        return this.getWrapped().containsValue(this.adaptToValueA(value));
    }

    @Override
    public VT get(Object key) {
        return this.getFromToValue().reverse().adapt(this.getWrapped().get(this.adaptToKeyA(key)));
    }

    @Override
    public VT put(KT key, VT value) {
        return this.getFromToValue().reverse().adapt(this.getWrapped().put(this.adaptToKeyA(key), this.adaptToValueA(value)));
    }

    @Override
    public VT remove(Object key) {
        return this.getFromToValue().reverse().adapt(this.getWrapped().remove(this.adaptToKeyA(key)));
    }

    @Override
    public void putAll(@NotNull Map<? extends KT, ? extends VT> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        this.wrapped.clear();
    }

    @NotNull
    @Override
    public Set<KT> keySet() {
        return new AdapterSet<>(this.wrapped.keySet(), this.getTargetKey(), this.getAdapteeKey(), this.getManager());
    }

    @NotNull
    @Override
    public Collection<VT> values() {
        return new AdapterSet<>(this.wrapped.values(), this.getTargetValue(), this.getAdapteeValue(), this.getManager());
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public Set<Entry<KT, VT>> entrySet() {
        Set<Entry<KA, VA>> target = this.getWrapped().entrySet();

        ViewSet<Entry<KA, VA>, AdapterEntry> viewSet = ViewCollections.setMapped(target,
                AdapterEntry::new,
                y -> target.add(y.getWrapped()),
                adapterEntry -> target.remove(adapterEntry.getWrapped())
        );

        return (Set<Entry<KT, VT>>) (Set) viewSet; // Map does not support structure changes on entrySet,
        // so this cast is not a problem.
    }

    /**
     * Adapts generic object key {@code o} to {@link KA} if {@code o} is assignable to {@link
     * #targetKey}.
     *
     * @param o Object to adapt.
     * @return Object or adapted instance.
     */
    @SuppressWarnings("unchecked")
    protected KA adaptToKeyA(Object o) {
        if (!this.getTargetKey().isInstance(o))
            return (KA) o;

        return this.getFromToKey().adapt((KT) o);
    }

    /**
     * Adapts generic object key {@code o} to {@link VA} if {@code o} is assignable to {@link
     * #targetValue}.
     *
     * @param o Object to adapt.
     * @return Object or adapted instance.
     */
    @SuppressWarnings("unchecked")
    protected VA adaptToValueA(Object o) {
        if (!this.getTargetValue().isInstance(o))
            return (VA) o;

        return this.getFromToValue().adapt((VT) o);
    }

    class AdapterEntry implements Entry<KT, VT> {

        private final Entry<KA, VA> wrapped;

        AdapterEntry(Entry<KA, VA> wrapped) {
            this.wrapped = wrapped;
        }

        public Entry<KA, VA> getWrapped() {
            return this.wrapped;
        }

        @Override
        public KT getKey() {
            return getFromToKey().reverse().adapt(this.getWrapped().getKey());
        }

        @Override
        public VT getValue() {
            return getFromToValue().reverse().adapt(this.getWrapped().getValue());
        }

        @Override
        public VT setValue(VT value) {
            return getFromToValue().reverse().adapt(this.getWrapped().setValue(getFromToValue().adapt(value)));
        }
    }

}
