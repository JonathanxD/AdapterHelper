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
package com.github.jonathanxd.adapterhelper.test;

import com.github.jonathanxd.adapterhelper.Adapter;
import com.github.jonathanxd.adapterhelper.AdapterManager;
import com.github.jonathanxd.adapterhelper.Converter;

import org.jetbrains.annotations.NotNull;

public final class TextToStringConverter implements Converter<Text, String> {

    public static final TextToStringConverter INSTANCE = new TextToStringConverter();

    private TextToStringConverter() {
    }

    @Override
    public String convert(@NotNull Text input, Adapter<?> adapter, @NotNull AdapterManager manager) {
        return input.getPlainString();
    }

    @Override
    public Converter<String, Text> revert() {
        return StringToTextConverter.INSTANCE;
    }

    private final static class StringToTextConverter implements Converter<String, Text> {
        static final StringToTextConverter INSTANCE = new StringToTextConverter();

        private StringToTextConverter() {
        }

        @NotNull
        @Override
        public Text convert(@NotNull String input, Adapter<?> adapter, @NotNull AdapterManager manager) {
            return new Text(input);
        }

        @Override
        public Converter<Text, String> revert() {
            return TextToStringConverter.INSTANCE;
        }
    }
}
