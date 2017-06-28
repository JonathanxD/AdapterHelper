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
import com.github.jonathanxd.adapterhelper.implgen.Field;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class DynamicFieldTest {

    @Test
    public void dynamicField() {
        AdapterManager manager = new AdapterManager();

        Lib1_Person person = new Lib1_Person("Mary", 20);

        manager.register(AdapterSpecification.createFromInterface(MyAdapterKt.class, MyPerson.class, Lib1_Person.class));

        Optional<MyPerson> adapt = manager.adapt(Lib1_Person.class, person, MyPerson.class);

        MyPerson myPerson = adapt.orElseThrow(NullPointerException::new);

        System.out.println(myPerson.getParents());
        System.out.println(myPerson.getName());


    }

    public interface MyPerson {
        String getName();

        int getAge();

        List<Person> getParents();
    }

    @Field(value = "parents", type = List.class /*List<MyPerson>*/, getter = "getParents", defaultValueProvider = ListProvider.class)
    public interface MyAdapter extends MyPerson, Adapter<Lib1_Person> {
        @Override
        default String getName() {
            return this.getOriginalInstance().getName();
        }

        @Override
        default int getAge() {
            return this.getOriginalInstance().getAge();
        }
    }

    public static class ListProvider implements Supplier<List> {

        public static final ListProvider INSTANCE = new ListProvider();

        protected ListProvider() {

        }

        @Override
        public List get() {
            return new ArrayList();
        }
    }
}
