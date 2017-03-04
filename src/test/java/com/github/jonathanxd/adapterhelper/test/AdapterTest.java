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

import com.github.jonathanxd.adapterhelper.AdapterManager;
import com.github.jonathanxd.adapterhelper.AdapterSpecification;
import com.github.jonathanxd.iutils.list.PredicateArrayList;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class AdapterTest {

    @Test
    public void test() {
        AdapterManager adapterManager = AdapterManager.create();

        AdapterSpecification<OldPerson, SimpleAdapter> adapterSpecification = AdapterSpecification.create(SimpleAdapter::new, SimpleAdapter.class, OldPerson.class);

        adapterManager.register(adapterSpecification);

        adapterManager.registerConverter(Text.class, String.class, TextToStringConverter.INSTANCE);

        OldPerson oldPerson = new OldPerson("Josh", 32);

        Person adapter = adapterManager.adaptUnchecked(OldPerson.class, oldPerson, Person.class);

        Assert.assertEquals("Josh", adapter.getName().getPlainString());
        Assert.assertEquals(32, adapter.getAge());
    }

    @Test
    public void testAll() {
        AdapterManager adapterManager = AdapterManager.create();

        AdapterSpecification<OldPerson, SimpleAdapter> adapterSpecification = AdapterSpecification.create(SimpleAdapter::new, SimpleAdapter.class, OldPerson.class);

        adapterManager.register(adapterSpecification);

        adapterManager.registerConverter(Text.class, String.class, TextToStringConverter.INSTANCE);

        List<OldPerson> oldPersonList = new MyPredicateArrayList<>(oldPerson -> {
            System.out.println("[In -> " + oldPerson.h() + "]");
            return true;
        });

        oldPersonList.add(new OldPerson("Josh", 32));
        oldPersonList.add(new OldPerson("Mary", 19));
        oldPersonList.add(new OldPerson("Mrs", 42));

        List<Person> people = adapterManager.adaptAll(OldPerson.class, oldPersonList, Person.class);

        for (Person person : people) {
            System.out.println(person.getName());
            System.out.println(person.getAge());
        }

        Person jerry = adapterManager.adaptUnchecked(OldPerson.class, new OldPerson("Jerry", 32), Person.class);

        people.add(jerry);

        OldPerson oldPerson = oldPersonList.get(3);

        System.out.println(oldPerson.h());
    }

    @Test
    public void testAssignable() {
        AdapterManager adapterManager = AdapterManager.create();

        AdapterSpecification<OldPerson, SimpleAdapter> adapterSpecification = AdapterSpecification.create(SimpleAdapter::new, SimpleAdapter.class, OldPerson.class);

        adapterManager.register(adapterSpecification);

        adapterManager.registerConverter(Text.class, String.class, TextToStringConverter.INSTANCE);

        class ExtendedOldPerson extends OldPerson {

            private final String[] a;

            public ExtendedOldPerson(String h, int i, String[] a) {
                super(h, i);
                this.a = a;
            }

            public String[] getA() {
                return this.a.clone();
            }
        }

        ExtendedOldPerson extendedOldPerson = new ExtendedOldPerson("Josh", 32, new String[]{"Mary"});

        Person adapter = adapterManager.adaptUnchecked(ExtendedOldPerson.class, extendedOldPerson, Person.class);
        Person adapter2 = adapterManager.adaptUnchecked(ExtendedOldPerson.class, extendedOldPerson, Person.class);

        Assert.assertEquals("Josh", adapter.getName().getPlainString());
        Assert.assertEquals(32, adapter.getAge());
        Assert.assertEquals(adapter, adapter2);

    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void testAssignableConverter() {
        AdapterManager adapterManager = AdapterManager.create();

        adapterManager.registerConverter(Text.class, String.class, TextToStringConverter.INSTANCE);

        Assert.assertEquals(TextToStringConverter.INSTANCE, adapterManager.getConverter(SpecialText.class, String.class).get());
        Assert.assertEquals(TextToStringConverter.INSTANCE, adapterManager.getConverter(SpecialText.class, CharSequence.class).get());

        Assert.assertFalse(adapterManager.getConverter(String.class, SpecialText.class).isPresent());
        Assert.assertFalse(adapterManager.getConverter(CharSequence.class, SpecialText.class).isPresent());
    }

    @Test
    public void testMulti() {
        AdapterManager adapterManager = AdapterManager.create();

        AdapterSpecification<OldPerson, SimpleAdapter> adapterSpecification = AdapterSpecification.create(SimpleAdapter::new, SimpleAdapter.class, OldPerson.class);

        adapterManager.register(adapterSpecification);

        adapterManager.registerConverter(Text.class, String.class, TextToStringConverter.INSTANCE);

        OldPerson oldPerson = new OldPerson("Josh", 32);
        OldPerson oldPerson2 = new OldPerson("Mary", 23);

        List<Person> adapted = adapterManager.adaptAll(OldPerson.class, Arrays.asList(oldPerson, oldPerson2), Person.class);

        Assert.assertEquals("Josh", adapted.get(0).getName().getPlainString());
        Assert.assertEquals(32, adapted.get(0).getAge());

        Assert.assertEquals("Mary", adapted.get(1).getName().getPlainString());
        Assert.assertEquals(23, adapted.get(1).getAge());
    }

    @Test
    public void testFactoryAdapter() {
        AdapterManager adapterManager = AdapterManager.create();

        AdapterSpecification<OldAP, AP> spec = AdapterSpecification.create((o, manager) -> new AP(o.getI()), AP.class, OldAP.class);

        adapterManager.register(spec);

        Optional<AP> ap = adapterManager.adapt(OldAP.class, (OldAP) () -> 88, AP.class);

        Assert.assertEquals(88, ap.get().getI());
    }


    public interface OldAP {
        int getI();
    }

    public static final class AP {
        private final int i;

        public AP(int i) {
            this.i = i;
        }

        int getI() {
            return i;
        }
    }

    public static class MyPredicateArrayList<E> extends PredicateArrayList<E> {

        public MyPredicateArrayList(int initialCapacity, Predicate<E> predicate) {
            super(initialCapacity, predicate);
        }

        public MyPredicateArrayList() {
            super(e -> true);
        }

        public MyPredicateArrayList(Predicate<E> predicate) {
            super(predicate);
        }

        public MyPredicateArrayList(Collection<? extends E> c, Predicate<E> predicate) {
            super(c, predicate);
        }
    }
}
