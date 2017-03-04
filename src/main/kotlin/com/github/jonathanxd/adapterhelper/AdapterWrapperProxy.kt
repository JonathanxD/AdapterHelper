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

import com.github.jonathanxd.codeapi.type.Generic
import com.github.jonathanxd.codeapi.type.LoadedCodeType
import com.github.jonathanxd.codeapi.util.codeType
import com.github.jonathanxd.codeproxy.CodeProxy
import com.github.jonathanxd.codeproxy.ProxyData
import com.github.jonathanxd.codeproxy.handler.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Modifier


@Suppress("UNCHECKED_CAST")
fun <M : Any, T : Any, O> createProxy(base: Class<in M>, wrapped: T, target: Array<Class<*>>, adapterManager: AdapterManager, baseClass: Class<*>? = null): O {
    val jWrapped = baseClass ?: wrapped::class.java

    validate(jWrapped)

    val input = jWrapped.typeParameters[0].typeName

    val ih: InvocationHandler = InvocationHandler { instance: Any, method: Method, args: Array<Any>, proxyData: ProxyData ->

        val mappedArgs = args.toMutableList()

        if (method.typeParameters.isEmpty()) {
            val parameters = method.parameters

            parameters.forEachIndexed { index, parameter ->
                if (parameter.parameterizedType.typeName == input)
                    mappedArgs[index] = (args[index] as AdapterBase<*>).originalInstance
            }

            val cType = method.genericReturnType.codeType

            if (cType is Generic
                    && cType.isType
                    && cType.bounds.size == 1
                    && cType.bounds[0].type.let { it is Generic && it.name == input })
                return@InvocationHandler createProxy(base, method.invoke(wrapped, *mappedArgs.toTypedArray()) as T, target, adapterManager, (cType.codeType as LoadedCodeType<*>).loadedType)


            if (cType is Generic && cType.name == input)
                return@InvocationHandler adapterManager.adaptBaseUnchecked(base, method.invoke(wrapped, *mappedArgs.toTypedArray()) as M, target)

        }

        return@InvocationHandler method.invoke(wrapped, *mappedArgs.toTypedArray())
    }



    return (if (!jWrapped.isInterface) {
        CodeProxy.newProxyInstance(jWrapped.classLoader ?: CodeProxy::class.java.classLoader, jWrapped, emptyArray(), ih)
    } else
        CodeProxy.newProxyInstance(jWrapped.classLoader ?: CodeProxy::class.java.classLoader, arrayOf(jWrapped), ih)) as O

}

private fun validate(jClass: Class<*>) {
    if (!jClass.isInterface) {
        try {
            jClass.getDeclaredConstructor()
        } catch (e: Exception) {
            throw IllegalArgumentException("Input class '$jClass' is not valid, a empty constructor is required!")
        }
    }

    if (jClass.typeParameters.size != 1)
        throw IllegalArgumentException("Input class '$jClass' must have only one type parameter!")

    if (Modifier.isFinal(jClass.modifiers))
        throw IllegalArgumentException("Input class '$jClass' must not be final!")
}