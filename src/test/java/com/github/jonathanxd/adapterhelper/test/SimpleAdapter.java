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

import org.jetbrains.annotations.NotNull;

public class SimpleAdapter implements Person, Adapter<OldPerson> {

    private final OldPerson old;
    private final AdapterManager adapterManager;

    public SimpleAdapter(OldPerson old, AdapterManager adapterManager) {
        this.old = old;
        this.adapterManager = adapterManager;
    }

    @Override
    public Text getName() {
        return this.adapterManager.convertUnchecked(String.class, Text.class, this.getAdapteeInstance().h(), this);
    }

    @Override
    public int getAge() {
        return this.getAdapteeInstance().i();
    }

    @NotNull
    @Override
    public AdapterManager getAdapterManager() {
        return this.adapterManager;
    }

    @NotNull
    @Override
    public OldPerson getOriginalInstance() {
        return this.old;
    }

    @NotNull
    @Override
    public OldPerson getAdapteeInstance() {
        return (OldPerson) Adapter.DefaultImpls.getAdapteeInstance(this);
    }


}
