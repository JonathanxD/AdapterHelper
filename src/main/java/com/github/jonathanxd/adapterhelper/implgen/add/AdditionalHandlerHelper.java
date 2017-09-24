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
package com.github.jonathanxd.adapterhelper.implgen.add;

import com.github.jonathanxd.codeproxy.CodeProxy;
import com.github.jonathanxd.codeproxy.InvokeSuper;
import com.github.jonathanxd.codeproxy.gen.DirectInvocationCustom;

/**
 * Additional handler proxy factory.
 */
public class AdditionalHandlerHelper {

    public static AdditionalHandler createAdd(Class<?> refc) {

        return (AdditionalHandler) CodeProxy.newProxyInstance(new Class[0], new Object[0], it ->
                it.superClass(Object.class)
                        .classLoader(refc.getClassLoader())
                        .invocationHandler((a, b, c, d) -> InvokeSuper.INSTANCE)
                        .addInterface(AdditionalHandler.class)
                        .addCustomGenerator(InvokeSuper.class)
                        .addCustom(new DirectInvocationCustom.Static(refc))
        );

    }

    public static AdditionalHandler from(Class<?> refc) {
        if (AdditionalHandler.class.isAssignableFrom(refc)) {
            Exception e = null;
            try {
                try {
                    return (AdditionalHandler) refc.getDeclaredField("INSTANCE").get(null);
                } catch (NoSuchFieldException | IllegalAccessException e0) {
                    e = e0;
                }
                return (AdditionalHandler) refc.newInstance();
            } catch (InstantiationException | IllegalAccessException e0) {
                e0.addSuppressed(e);

                throw new IllegalArgumentException(
                        String.format("Input handler class '%s' need to have an empty constructor or a INSTANCE static field.",
                                refc.getCanonicalName()),
                        e0);
            }
        }

        return AdditionalHandlerHelper.createAdd(refc);
    }
}
