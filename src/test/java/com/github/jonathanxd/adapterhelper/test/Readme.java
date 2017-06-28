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
package com.github.jonathanxd.adapterhelper.test;

import com.github.jonathanxd.adapterhelper.Adapter;
import com.github.jonathanxd.adapterhelper.AdapterManager;
import com.github.jonathanxd.adapterhelper.AdapterSpecification;
import com.github.jonathanxd.adapterhelper.Converter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class Readme {

    @Test
    public void basicAdapter() {
        AdapterManager manager = new AdapterManager();

        manager.register(AdapterSpecification.createFromInterface(MyAdapter.class, IPerson.class, Lib1_Person.class));

        Lib1_Person person = new Lib1_Person("Mary", 20);

        IPerson adapt = manager.adaptUnchecked(Lib1_Person.class, person, IPerson.class);

        System.out.println(adapt.getName());
    }

    @Test
    public void converter() {
        AdapterManager manager = new AdapterManager();

        manager.register(AdapterSpecification.createFromInterface(MyAdapter.class, IPerson.class, Lib1_Person.class));

        Lib1_Person person = new Lib1_Person("Mary", 20);

        IPerson adapt = manager.adaptUnchecked(Lib1_Person.class, person, IPerson.class);

        System.out.println(adapt.getName());
    }

    public interface MyAdapter extends IPerson, Adapter<Lib1_Person> {

        @Override
        default String getName() {
            return this.getOriginalInstance().getName();
        }

        @Override
        default int getAge() {
            return this.getOriginalInstance().getAge();
        }

    }

    public static class MyConverter implements Converter<String, Lib1_ProductName> {

        public static final MyConverter INSTANCE = new MyConverter();

        @NotNull
        @Override
        public Lib1_ProductName convert(@NotNull String input, @Nullable Adapter<?> adapter, @NotNull AdapterManager manager) {

            for (Map.Entry<String, String> stringStringEntry : NameMapping.EN.entrySet()) {
                if(stringStringEntry.getValue().equals(input))
                    return new Lib1_ProductName(stringStringEntry.getKey());
            }

            return new Lib1_ProductName("?");
        }

        @Nullable
        @Override
        public Converter<Lib1_ProductName, String> revert() {
            return Revert.INSTANCE;
        }

        public static class Revert implements Converter<Lib1_ProductName, String> {
            public static final Revert INSTANCE = new Revert();

            @NotNull
            @Override
            public String convert(@NotNull Lib1_ProductName input, @Nullable Adapter<?> adapter, @NotNull AdapterManager manager) {
                return input.resolve(Language.EN);
            }

            @Nullable
            @Override
            public Converter<String, Lib1_ProductName> revert() {
                return MyConverter.INSTANCE;
            }
        }
    }

    public static class Lib1_Product {
        private final Lib1_ProductName name;
        private final double cost;

        public Lib1_Product(Lib1_ProductName name, double cost) {
            this.name = name;
            this.cost = cost;
        }

        public Lib1_ProductName getName() {
            return this.name;
        }

        public double getCost() {
            return this.cost;
        }
    }

    public static class Lib1_ProductName {
        private final String localization;

        public Lib1_ProductName(String localization) {
            this.localization = localization;
        }

        public String resolve(Language language) {
            switch (language) {
                case EN: return NameMapping.EN.get(this.getLocalization());
                default: return NameMapping.EN.get(this.getLocalization()) + " (untranslated)";
            }
        }

        public String getLocalization() {
            return this.localization;
        }
    }

    public static class Lib2_Product {
        private final String name;
        private final double cost;

        public Lib2_Product(String name, double cost) {
            this.name = name;
            this.cost = cost;
        }

        public String getName() {
            return this.name;
        }

        public double getCost() {
            return this.cost;
        }
    }

    public static class NameMapping {
        public static final Map<String, String> EN = new HashMap<>();
    }

    enum Language {
        EN
    }
}
