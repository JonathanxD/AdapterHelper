/**
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
package com.github.jonathanxd.adapterhelper

import java.lang.reflect.AnnotatedElement

fun Class<*>.getExplicitAndImplicitAnnotations(): Set<Annotation> {
    return mutableSetOf<Annotation>().also {
        this.getExplicitAndImplicitAnnotations(it)
    }
}

fun AnnotatedElement.getExplicitAndImplicitAnnotations(): Set<Annotation> {
    return mutableSetOf<Annotation>().also {
        this.getExplicitAndImplicitAnnotations(it)
    }
}

private fun Class<*>.getExplicitAndImplicitAnnotations(set: MutableSet<Annotation>) {
    (this as AnnotatedElement).getExplicitAndImplicitAnnotations(set)
    this.superclass?.getExplicitAndImplicitAnnotations(set)
    this.interfaces.forEach { it.getExplicitAndImplicitAnnotations(set) }
}

private fun AnnotatedElement.getExplicitAndImplicitAnnotations(set: MutableSet<Annotation>) {
    this.annotations.forEach {
        if(!set.contains(it)) {
            set += it
            it.annotationClass.java.getExplicitAndImplicitAnnotations(set)
        }
    }
}

fun Class<*>.hasExplicitOrImplicitAnnotation(type: Class<out Annotation>): Boolean =
        this.getExplicitAndImplicitAnnotations().any { it.annotationClass.java == type }


fun AnnotatedElement.hasExplicitOrImplicitAnnotation(type: Class<out Annotation>): Boolean =
        this.getExplicitAndImplicitAnnotations().any { it.annotationClass.java == type }

