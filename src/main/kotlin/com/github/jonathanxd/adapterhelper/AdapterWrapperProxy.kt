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
import com.github.jonathanxd.codeapi.type.GenericType
import com.github.jonathanxd.codeapi.type.LoadedCodeType
import com.github.jonathanxd.codeapi.util.applyType
import com.github.jonathanxd.codeapi.util.codeType
import com.github.jonathanxd.codeapi.util.getType
import com.github.jonathanxd.codeproxy.CodeProxy
import com.github.jonathanxd.codeproxy.ProxyData
import com.github.jonathanxd.codeproxy.handler.InvocationHandler
import com.github.jonathanxd.codeproxy.info.MethodInfo
import java.lang.invoke.MethodHandles
import java.lang.reflect.Modifier
import java.lang.reflect.Type

private val lookup = MethodHandles.lookup()

fun privateLookup(declaringClass: Class<*>): MethodHandles.Lookup {
    val constructor = MethodHandles.Lookup::class.java.getDeclaredConstructor(Class::class.java, Int::class.javaPrimitiveType)
    if (!constructor.isAccessible) {
        constructor.isAccessible = true
    }

    return constructor.newInstance(declaringClass, MethodHandles.Lookup.PRIVATE)
}

// Requires CodeAPI and CodeProxy

@Suppress("UNCHECKED_CAST")
fun <M : Any, T : Any, O> createProxy(base: Class<in M>,
                                      wrapped: T, target: Array<Class<*>>,
                                      adapterManager: AdapterManager,
                                      baseClass: Class<*>? = null,
                                      classType: GenericType): O {
    val jWrapped = baseClass ?: wrapped::class.java

    validate(jWrapped)

    val names = jWrapped.typeParameters.map { it.typeName }

    val generic = Generic.type(jWrapped.codeType).of(*jWrapped.typeParameters.map { it.codeType }.toTypedArray())

    val ih: InvocationHandler = InvocationHandler { instance: Any, methodInfo: MethodInfo, args: Array<Any?>, _: ProxyData ->

        val mappedArgs = args.toMutableList()
        val method = methodInfo.declaringClass.getDeclaredMethod(methodInfo.name, *methodInfo.parameterTypes.toTypedArray())

        val mNames = method.typeParameters.map { it.typeName }


        fun createProxy(type: Type, provider: () -> Any?): M? {
            val cType = type.codeType

            val anyName: Boolean
            var filledType = cType

            if (cType is Generic) {
                val iNames = cType.bounds.map(::getName).filterNotNull().filter { !mNames.contains(it) }
                anyName = names.any { out -> iNames.any { out == it } }

                iNames.forEach {
                    filledType = filledType.applyType(it, generic.getType(it, classType)!!)
                }
            } else {
                anyName = false
            }

            return if (cType is Generic
                    && cType.isType
                    && anyName) {
                val provided = provider() ?: return null

                createProxy(base, provided, target, adapterManager, (cType.codeType as LoadedCodeType<*>).loadedType, filledType as GenericType)
            } else null
        }

        fun map(type: Type, input: () -> Any?, isInput: Boolean): Any? {
            val pType = type.codeType

            if (pType is Generic && !pType.isType && !pType.isWildcard) {
                val name = pType.name

                val appliedType = pType.applyType(name, generic.getType(name, classType)!!)

                val typesAreEq = appliedType.`is`(base.codeType)

                if (!typesAreEq) {
                    val inpt = input() ?: return null

                    val inputIsTarget = target.any { it.isInstance(inpt) }

                    if (inputIsTarget) {
                        return (inpt as? AdapterBase<*>)?.originalInstance ?: adapterManager.convertUnchecked(inpt::class.java as Class<Any>, base as Class<Any>, inpt, null)
                    }

                    return adapterManager.adaptBaseUnchecked(base, inpt as M, target)
                }


            }

            val proxy = createProxy(type, input)

            if (proxy != null)
                return proxy

            return input()
        }

        val parameters = method.parameters

        parameters.forEachIndexed { index, parameter ->
            mappedArgs[index] = map(parameter.parameterizedType, { args[index] }, true)
        }

        return@InvocationHandler map(method.genericReturnType, {

            val declaringClass = method.declaringClass

            /*if (method.isDefault && declaringClass.isInterface) { << TODO: Temp
                val cl: Class<*> = if (instance::class.java != declaringClass) {
                    val superType = instance::class.java.superclass
                    if(superType == declaringClass || superType.hasSuperclass(declaringClass)) superType
                    else instance::class.java.interfaces.first { it == declaringClass || it.hasSuperclass(declaringClass) }
                } else instance::class.java

                return@map methodInfo
                        .resolveSpecialOrFail(cl, instance::class.java)
                        .bindTo(instance)
                        .invokeWithArguments(*mappedArgs.toTypedArray())
            } else*/
                return@map method.invoke(wrapped, *mappedArgs.toTypedArray())
        }, false)
    }



    return (if (!jWrapped.isInterface) {
        CodeProxy.newProxyInstance(jWrapped.classLoader ?: CodeProxy::class.java.classLoader, jWrapped, emptyArray(), ih)
    } else
        CodeProxy.newProxyInstance(jWrapped.classLoader ?: CodeProxy::class.java.classLoader, arrayOf(jWrapped), ih)) as O

}

private fun Class<*>.hasSuperclass(type: Class<*>): Boolean {
    if (this.superclass != null && (this.superclass == type || this.superclass.hasSuperclass(type))) return true
    if (this.interfaces.any { it == type || it.hasSuperclass(type) }) return true
    return false
}

private fun getName(type: GenericType): String? {

    if (type is Generic)
        if (type.isWildcard) {
            if (type.codeType is Generic)
                return getName(type.codeType as Generic)
        } else {
            return type.name
        }

    return null
}

private fun getName(bound: GenericType.Bound): String? {

    val type = bound.type

    if (type is GenericType)
        return getName(type)
    else
        return null
}


private fun validate(jClass: Class<*>) {
    if (!jClass.isInterface) {
        try {
            jClass.getDeclaredConstructor()
        } catch (e: Exception) {
            throw IllegalArgumentException("Input class '$jClass' is not valid, a empty constructor is required!")
        }
    }

    if (jClass.typeParameters.isEmpty())
        throw IllegalArgumentException("Input class '$jClass' must have at least one type parameter!")

    if (Modifier.isFinal(jClass.modifiers))
        throw IllegalArgumentException("Input class '$jClass' must not be final!")
}