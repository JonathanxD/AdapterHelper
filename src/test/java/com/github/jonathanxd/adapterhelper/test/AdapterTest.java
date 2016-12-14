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
package com.github.jonathanxd.adapterhelper.test;

import com.github.jonathanxd.adapterhelper.AdapterManager;
import com.github.jonathanxd.adapterhelper.AdapterSpecification;

import org.junit.Assert;
import org.junit.Test;

public class AdapterTest {

    @Test
    public void test() {
        AdapterManager adapterManager = AdapterManager.create();

        AdapterSpecification<SimpleAdapter, OldPerson> adapterSpecification = AdapterSpecification.create(SimpleAdapter::new, SimpleAdapter.class, OldPerson.class);

        adapterManager.register(adapterSpecification);

        adapterManager.registerConverter(Text.class, String.class, TextToStringConverter.INSTANCE);

        OldPerson oldPerson = new OldPerson("Josh", 32);

        Person adapter = adapterManager.adaptUnchecked(OldPerson.class, oldPerson, Person.class);

        Assert.assertEquals("Josh", adapter.getName().getPlainString());
        Assert.assertEquals(32, adapter.getAge());
    }

    @Test
    public void testAssignable() {
        AdapterManager adapterManager = AdapterManager.create();

        AdapterSpecification<SimpleAdapter, OldPerson> adapterSpecification = AdapterSpecification.create(SimpleAdapter::new, SimpleAdapter.class, OldPerson.class);

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


}
