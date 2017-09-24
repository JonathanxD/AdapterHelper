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
package com.github.jonathanxd.adapterhelper.wrapper.func;

import java.util.function.Function;

/**
 * Type resolver function, this function can resolve the type of {@link A} from {@link A} instance
 * or an alternative {@link B} instance.
 *
 * This function is used in Adapter collection to resolve a target adapt type or adaptee type of a
 * instance dynamically, the resolution is made using either: {@link A original instance} or {@link
 * B alternative instance}. When the type is to be resolved using {@link B alternative instance},
 * the function should resolve in a pure way, meaning that if {@code X} is resolved to {@link A
 * original instance} of type {@code Y}, when the resolution occurs against {@link B alternative
 * instance} of type {@code X}, the type {@code Y} should be returned. In other words, if you return
 * type {@code X} for a instance of type {@code Y} when {@link #apply(Object)} is called, then you
 * should return {@code Y} when {@link #applyFromB(Object)} is called with an object of type {@code
 * X}.
 *
 * @param <A> Type to resolve.
 * @param <B> Alternative type.
 */
public interface TypeResolverFunc<A, B> {

    static <A, B> Impl<A, B> fromFuncs(Function<A, Class<A>> func, Function<B, Class<A>> alternative) {
        return new Impl<>(func, alternative);
    }

    /**
     * Resolves type of {@link A} from {@code a} instance.
     *
     * @param a Instance.
     * @return Type of {@code a}.
     */
    Class<A> apply(A a);

    /**
     * Resolves type of {@link A} from alternative {@code b} instance.
     *
     * @param b Alternative instance.
     * @return Type of {@code a}.
     */
    Class<A> applyFromB(B b);

    final class Impl<A, B> implements TypeResolverFunc<A, B> {
        private final Function<A, Class<A>> func;
        private final Function<B, Class<A>> altFunc;

        Impl(Function<A, Class<A>> func, Function<B, Class<A>> altFunc) {
            this.func = func;
            this.altFunc = altFunc;
        }

        @Override
        public Class<A> apply(A a) {
            return this.func.apply(a);
        }

        @Override
        public Class<A> applyFromB(B b) {
            return this.altFunc.apply(b);
        }
    }
}
