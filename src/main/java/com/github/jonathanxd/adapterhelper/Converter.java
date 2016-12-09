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

/**
 * A converter that converts a instance of {@link I} to a instance of {@link O}.
 *
 * The {@link #revert()} may return null if the convert don't support reversion.
 *
 * @param <I> Input.
 * @param <O> Output
 */
@FunctionalInterface
public interface Converter<I, O> {

    /**
     * Converts from {@link I} to {@link O}.
     *
     * @param input   Input.
     * @param adapter Adapter instance (may be null).
     * @param manager Adapter Manager.
     * @return Converted instance (can't be null).
     */
    O convert(I input, Adapter<?> adapter, AdapterManager manager);

    /**
     * Returns a converter that converts from {@link O} to {@link I} (may be
     * null).
     *
     * @return A converter that converts from {@link O} to {@link I} (may be null).
     */
    default Converter<O, I> revert() {
        return null;
    }
}
