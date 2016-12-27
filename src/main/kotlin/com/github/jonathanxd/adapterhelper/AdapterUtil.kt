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
package com.github.jonathanxd.adapterhelper

import com.github.jonathanxd.codeapi.CodeAPI
import com.github.jonathanxd.codeapi.MutableCodeSource
import com.github.jonathanxd.codeapi.builder.ClassBuilder
import com.github.jonathanxd.codeapi.classloader.CodeClassLoader
import com.github.jonathanxd.codeapi.common.CodeModifier
import com.github.jonathanxd.codeapi.common.CodeParameter
import com.github.jonathanxd.codeapi.common.InvokeType
import com.github.jonathanxd.codeapi.common.TypeSpec
import com.github.jonathanxd.codeapi.conversions.codeType
import com.github.jonathanxd.codeapi.conversions.extend
import com.github.jonathanxd.codeapi.gen.visit.bytecode.BytecodeGenerator
import com.github.jonathanxd.codeapi.helper.Helper
import com.github.jonathanxd.codeapi.helper.PredefinedTypes
import com.github.jonathanxd.codeapi.impl.MethodSpecImpl
import com.github.jonathanxd.codeapi.util.source.CodeArgumentUtil
import com.github.jonathanxd.codegenutil.CodeGen
import com.github.jonathanxd.codegenutil.implementer.Implementer
import com.github.jonathanxd.codegenutil.property.Property
import com.github.jonathanxd.codegenutil.property.PropertySystem
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * Adapter utility (Requires CodeAPI, CodeAPI-Conversions and CodeGenUtil).
 */
object AdapterUtil {

    private val loader = CodeClassLoader()

    private var inc: Int = 0

    private val incremental: Int
        get() {
            ++inc
            return inc
        }

    private const val adapteeInstanceField = "adapteeInstance"
    private const val adapterManagerField = "adapterManager"

    private const val adapteeInstanceGet = "getAdapteeInstance"
    private const val adapterManagerGet = "getAdapterManager"

    /**
     * Generate implementation of an [Adapter] interface that have a two-arg constructor
     * that receives either [T] and [AdapterManager].
     */
    @JvmStatic
    fun <T : Any> genImpl(klass: Class<out Adapter<T>>, type: Class<T>): Class<out Adapter<T>> {

        if (!klass.isInterface)
            throw IllegalArgumentException("The target '$klass' is not an interface.")

        val filter = klass.methods.filter { !isAdapterMethod(it) && Modifier.isAbstract(it.modifiers) }
        val count = filter.size

        if (count > 0)
            throw IllegalArgumentException("The target '$klass' has '$count' abstract methods. '${filter.map(Method::getName).joinToString()}'")

        val codeGen = CodeGen()

        codeGen.install(PropertySystem(
                Property(adapteeInstanceField, type.codeType),
                Property(adapterManagerField, AdapterManager::class.java.codeType)
        ))

        codeGen.install(Implementer { method ->
            val empty = method.parameters.isEmpty()

            if (empty && method.name == adapteeInstanceGet) {
                return@Implementer method.setBody(CodeAPI.sourceOfParts(
                        CodeAPI.returnValue(type.codeType, CodeAPI.accessThisField(type.codeType, adapteeInstanceField))
                ))
            } else if (empty && method.name == adapterManagerGet) {
                return@Implementer method.setBody(CodeAPI.sourceOfParts(
                        CodeAPI.returnValue(AdapterManager::class.java.codeType, CodeAPI.accessThisField(AdapterManager::class.java.codeType, adapterManagerField))
                ))
            } else {
                val methodType = TypeSpec(method.returnType.get(), method.parameters.map(CodeParameter::getRequiredType))
                val methodSpec = MethodSpecImpl(method.name, methodType, CodeArgumentUtil.argumentsFromParameters(method.parameters))

                val invoke = Helper.invoke(InvokeType.INVOKE_SPECIAL, klass, CodeAPI.accessThis(),
                        methodSpec)

                val part = if (method.returnType.get().`is`(PredefinedTypes.VOID)) invoke else CodeAPI.returnValue(methodType.returnType, invoke)

                return@Implementer method.setBody(CodeAPI.sourceOfParts(
                        part
                ))
            }
        })

        val declaration = codeGen.gen(ClassBuilder.builder()
                .withModifiers(CodeModifier.PUBLIC, CodeModifier.SYNTHETIC)
                .withQualifiedName("${klass.canonicalName}_$incremental")
                .withBody(MutableCodeSource())
                .build()
                .extend(klass))

        val decl = BytecodeGenerator().gen(declaration)

        @Suppress("UNCHECKED_CAST")
        return loader.define(decl) as Class<out Adapter<T>>
    }

    private fun isAdapterMethod(method: Method) =
            method.parameterCount == 0
                    && (method.name == adapteeInstanceGet || method.name == adapterManagerGet)
}