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
package com.github.jonathanxd.adapterhelper.wrapper.func;

import com.github.jonathanxd.adapterhelper.AdapterManager;
import com.github.jonathanxd.adapterhelper.Builder;
import com.github.jonathanxd.adapterhelper.wrapper.AdapterSet;
import com.github.jonathanxd.iutils.collection.view.ViewCollections;
import com.github.jonathanxd.iutils.collection.view.ViewSet;
import com.github.jonathanxd.iutils.iterator.IteratorUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class AdapterMapFunc<KT, VT, KA, VA> implements Map<KT, VT> {

    // KT = Target key
    // VT = Target value
    // KA = Adaptee key
    // VA = Adaptee value

    private final Map<KA, VA> wrapped;

    /**
     * Resolver of target key from adaptee key
     */
    private final TypeResolverFunc<KT, KA> targetKeyTypeResolver;

    /**
     * Resolver of target value from adaptee value
     */
    private final TypeResolverFunc<VT, VA> targetValueTypeResolver;

    /**
     * Adaptee key resolver from target key
     */
    private final TypeResolverFunc<KA, KT> adapteeKeyTypeResolver;

    /**
     * Adaptee value resolver from target value
     */
    private final TypeResolverFunc<VA, VT> adapteeValueTypeResolver;
    private final AdapterManager manager;

    public AdapterMapFunc(Map<KA, VA> wrapped,
                          TypeResolverFunc<KT, KA> targetKeyTypeResolver,
                          TypeResolverFunc<VT, VA> targetValueTypeResolver,
                          TypeResolverFunc<KA, KT> adapteeKeyTypeResolver,
                          TypeResolverFunc<VA, VT> adapteeValueTypeResolver,
                          AdapterManager manager) {
        this.wrapped = wrapped;
        this.targetKeyTypeResolver = targetKeyTypeResolver;
        this.targetValueTypeResolver = targetValueTypeResolver;
        this.adapteeKeyTypeResolver = adapteeKeyTypeResolver;
        this.adapteeValueTypeResolver = adapteeValueTypeResolver;
        this.manager = manager;
    }

    public Builder builder() {
        return this.getManager().builder();
    }


    public TypeResolverFunc<KT, KA> getTargetKeyTypeResolver() {
        return this.targetKeyTypeResolver;
    }

    public TypeResolverFunc<KA, KT> getAdapteeKeyTypeResolver() {
        return this.adapteeKeyTypeResolver;
    }

    public TypeResolverFunc<VT, VA> getTargetValueTypeResolver() {
        return this.targetValueTypeResolver;
    }

    public TypeResolverFunc<VA, VT> getAdapteeValueTypeResolver() {
        return this.adapteeValueTypeResolver;
    }

    public Class<KT> getFromKeyTarget(KT kt) {
        return this.getTargetKeyTypeResolver().apply(kt);
    }

    public Class<KT> getFromKeyAdapter(KA ka) {
        return this.getTargetKeyTypeResolver().applyFromB(ka);
    }

    public Class<KA> getToKeyAdapter(KA ka) {
        return this.getAdapteeKeyTypeResolver().apply(ka);
    }

    public Class<KA> getToKeyTarget(KT kt) {
        return this.getAdapteeKeyTypeResolver().applyFromB(kt);
    }

    public Class<VT> getFromValueTarget(VT vt) {
        return this.getTargetValueTypeResolver().apply(vt);
    }

    public Class<VT> getFromValueAdapter(VA va) {
        return this.getTargetValueTypeResolver().applyFromB(va);
    }

    public Class<VA> getToValueAdapter(VA va) {
        return this.getAdapteeValueTypeResolver().apply(va);
    }

    public Class<VA> getToValueTarget(VT vt) {
        return this.getAdapteeValueTypeResolver().applyFromB(vt);
    }


    public Map<KA, VA> getWrapped() {
        return this.wrapped;
    }

    public AdapterManager getManager() {
        return this.manager;
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
        return this.adaptValueFromAdapteeToTarget(this.getWrapped().get(this.adaptToKeyA(key)));
    }

    @Override
    public VT put(KT key, VT value) {
        return this.adaptValueFromAdapteeToTarget(
                this.getWrapped().put(this.adaptKeyFromTargetToAdaptee(key), this.adaptValueFromTargetToAdaptee(value))
        );
    }

    @Override
    public VT remove(Object key) {
        return this.adaptValueFromAdapteeToTarget(
                this.getWrapped().remove(this.adaptToKeyA(key))
        );
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
        return new AdapterSetFunc<>(
                this.wrapped.keySet(),
                this.getTargetKeyTypeResolver(),
                this.getAdapteeKeyTypeResolver(),
                this.getManager()
        );
    }

    @NotNull
    @Override
    public Collection<VT> values() {
        return new AdapterCollectionFunc<>(
                this.wrapped.values(),
                this.getTargetValueTypeResolver(),
                this.getAdapteeValueTypeResolver(),
                this.getManager()
        );
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
        return this.adaptKeyFromTargetToAdaptee((KT) o);
    }

    @SuppressWarnings("unchecked")
    protected VA adaptToValueA(Object o) {
        return this.adaptValueFromTargetToAdaptee((VT) o);
    }


    protected KA adaptKeyFromTargetToAdaptee(KT kt) {
        return kt == null
                ? null
                : this.builder()
                .from(this.getFromKeyTarget(kt))
                .to(this.getToKeyTarget(kt))
                .adapt(kt);
    }

    protected KT adaptKeyFromAdapteeToTarget(KA ka) {
        return ka == null
                ? null
                : this.builder()
                .from(this.getToKeyAdapter(ka))
                .to(this.getFromKeyAdapter(ka))
                .adapt(ka);
    }


    protected VA adaptValueFromTargetToAdaptee(VT vt) {
        return vt == null
                ? null
                : this.builder()
                .from(this.getFromValueTarget(vt))
                .to(this.getToValueTarget(vt))
                .adapt(vt);
    }

    protected VT adaptValueFromAdapteeToTarget(VA va) {
        return va == null
                ? null
                : this.builder()
                .from(this.getToValueAdapter(va))
                .to(this.getFromValueAdapter(va))
                .adapt(va);
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
            return AdapterMapFunc.this.adaptKeyFromAdapteeToTarget(this.getWrapped().getKey());
        }

        @Override
        public VT getValue() {
            return AdapterMapFunc.this.adaptValueFromAdapteeToTarget(this.getWrapped().getValue());
        }

        @Override
        public VT setValue(VT value) {
            return AdapterMapFunc.this.adaptValueFromAdapteeToTarget(
                    this.getWrapped().setValue(AdapterMapFunc.this.adaptValueFromTargetToAdaptee(value))
            );
        }
    }

}
