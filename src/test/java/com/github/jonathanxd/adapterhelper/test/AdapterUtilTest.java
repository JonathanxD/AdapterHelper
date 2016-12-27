/**
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

import com.github.jonathanxd.adapterhelper.Adapter;
import com.github.jonathanxd.adapterhelper.AdapterManager;
import com.github.jonathanxd.adapterhelper.AdapterSpecification;
import com.github.jonathanxd.adapterhelper.AdapterUtil;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class AdapterUtilTest {

    private final AdapterManager manager = new AdapterManager();

    @Test(expected = IllegalArgumentException.class)
    public void testAdapterUtilAbstract() throws Exception {
        Class<? extends Adapter<OldPerson>> aClass = AdapterUtil.genImpl(A.class, OldPerson.class);

        Constructor<? extends Adapter<OldPerson>> declaredConstructor = aClass.getDeclaredConstructor(OldPerson.class, AdapterManager.class);

        manager.register(AdapterSpecification.createGeneric((o, manager1) -> {
            try {
                return declaredConstructor.newInstance(o, manager1);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }, aClass.getClass(), OldPerson.class));
    }

    @Test
    public void testAdapterUtil() throws Exception {
        Class<? extends Adapter<OldPerson>> aClass = AdapterUtil.genImpl(B.class, OldPerson.class);

        Constructor<? extends Adapter<OldPerson>> declaredConstructor = aClass.getDeclaredConstructor(OldPerson.class, AdapterManager.class);

        manager.register(AdapterSpecification.createGeneric((o, manager1) -> {
            try {
                return declaredConstructor.newInstance(o, manager1);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }, aClass, OldPerson.class));

        Person b = manager.adaptUnchecked(OldPerson.class, new OldPerson("B", 22), Person.class);

        Assert.assertEquals("getAge", 22, b.getAge());
        Assert.assertEquals("getName", "B", b.getName().getPlainString());
    }

    public interface A extends Adapter<OldPerson>, Person {

    }

    public interface B extends Adapter<OldPerson>, Person {
        @Override
        default int getAge() {
            return this.getAdapteeInstance().i();
        }

        @Override
        default Text getName() {
            return new Text(this.getAdapteeInstance().h());
        }
    }
}
