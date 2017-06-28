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

import com.github.jonathanxd.codeproxy.CodeProxy;

import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;

// These are not real world examples, consider that in real world these methods are more complex.

public class ReadmeExamples {

    private final Lib1_Person lib1_person = new Lib1_Person("Mary", 20);
    private final Lib2_Person lib2_person = new Lib2_Person("Carl", 19, Collections.emptyList());

    @Test
    public void destruct() {
        new Destruct().process(lib1_person);
        new Destruct().process(lib2_person);
    }

    @Test
    public void perMethod() {
        new PerMethod().process(lib1_person);
        new PerMethod().process(lib2_person);
    }

    @Test
    public void javaProxyMethod() {
        new JavaProxy().process(lib1_person);
        new JavaProxy().process(lib2_person);
    }

    @Test
    public void codeProxyMethod() { // For fun
        new CodeProxy_().process(lib1_person);
        new CodeProxy_().process(lib2_person);
    }

    @Test
    public void adapteClassMethod() { // For fun
        new AdapterClass().process(lib1_person);
        new AdapterClass().process(lib2_person);
    }

}

class Destruct {
    public void process(Lib1_Person person) {
        process(person.getName(), person.getAge());
    }

    public void process(Lib2_Person person) {
        process(person.getName(), person.getAge());
    }

    private void process(String name, int age) {
        System.out.println(String.format("Person{name=%s, age=%d}", name, age));
    }
}

class PerMethod {
    public void process(Lib1_Person person) {
        System.out.println(String.format("Person{name=%s, age=%d}", person.getName(), person.getAge()));
    }

    public void process(Lib2_Person person) {
        System.out.println(String.format("Person{name=%s, age=%d}", person.getName(), person.getAge()));
    }
}

class JavaProxy {

    public static IPerson getPerson(Object person) {
        return (IPerson) Proxy.newProxyInstance(person.getClass().getClassLoader(), new Class[]{IPerson.class},
                (proxy, method, args) ->
                        person.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes()).invoke(person, args));
    }

    public void process(Lib1_Person person) {
        process(getPerson(person));
    }

    public void process(Lib2_Person person) {
        process(getPerson(person));
    }

    public void process(IPerson person) {
        System.out.println(String.format("Person{name=%s, age=%d}", person.getName(), person.getAge()));
    }
}

class CodeProxy_ { // Only an addition to make me happy :D

    public static IPerson getPerson(Object person) {
        return (IPerson) CodeProxy.newProxyInstance(person.getClass().getClassLoader(), new Class[]{IPerson.class},
                (proxy, methodInfo, args, proxyData) ->
                        methodInfo.resolveOrFail(person.getClass()).bindTo(person).invokeWithArguments(args));
    }

    public void process(Lib1_Person person) {
        process(getPerson(person));
    }

    public void process(Lib2_Person person) {
        process(getPerson(person));
    }

    public void process(IPerson person) {
        System.out.println(String.format("Person{name=%s, age=%d}", person.getName(), person.getAge()));
    }
}

class AdapterClass {

    public void process(Lib1_Person person) {
        process(new Lib1_PersonAdapter(person));
    }

    public void process(Lib2_Person person) {
        process(new Lib2_PersonAdapter(person));
    }

    public void process(IPerson person) {
        System.out.println(String.format("Person{name=%s, age=%d}", person.getName(), person.getAge()));
    }

    class Lib1_PersonAdapter implements IPerson {
        private final Lib1_Person adaptee;

        Lib1_PersonAdapter(Lib1_Person adaptee) {
            this.adaptee = adaptee;
        }

        @Override
        public String getName() {
            return adaptee.getName();
        }

        @Override
        public int getAge() {
            return adaptee.getAge();
        }
    }

    class Lib2_PersonAdapter implements IPerson {
        private final Lib2_Person adaptee;

        Lib2_PersonAdapter(Lib2_Person adaptee) {
            this.adaptee = adaptee;
        }

        @Override
        public String getName() {
            return adaptee.getName();
        }

        @Override
        public int getAge() {
            return adaptee.getAge();
        }
    }
}