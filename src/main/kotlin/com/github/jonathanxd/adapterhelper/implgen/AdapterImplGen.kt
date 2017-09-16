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
package com.github.jonathanxd.adapterhelper.implgen

import com.github.jonathanxd.adapterhelper.Adapter
import com.github.jonathanxd.adapterhelper.AdapterManager
import com.github.jonathanxd.adapterhelper.Try
import com.github.jonathanxd.adapterhelper.implgen.add.AdditionalHandler
import com.github.jonathanxd.adapterhelper.implgen.add.AdditionalHandlerHelper
import com.github.jonathanxd.codeapi.Types
import com.github.jonathanxd.codeapi.base.*
import com.github.jonathanxd.codeapi.bytecode.classloader.CodeClassLoader
import com.github.jonathanxd.codeapi.bytecode.processor.BytecodeGenerator
import com.github.jonathanxd.codeapi.common.MethodTypeSpec
import com.github.jonathanxd.codeapi.common.VariableRef
import com.github.jonathanxd.codeapi.factory.*
import com.github.jonathanxd.codeapi.literal.Literals
import com.github.jonathanxd.codeapi.type.TypeRef
import com.github.jonathanxd.codeapi.util.codeType
import com.github.jonathanxd.codeapi.util.conversion.extend
import com.github.jonathanxd.codeapi.util.conversion.methodTypeSpec
import com.github.jonathanxd.codeapi.util.conversion.toInvocation
import com.github.jonathanxd.codeapi.util.conversion.toVariableAccess
import com.github.jonathanxd.codegenutil.CodeGen
import com.github.jonathanxd.codegenutil.implementer.Implementer
import com.github.jonathanxd.codegenutil.property.Property
import com.github.jonathanxd.codegenutil.property.PropertySystem
import com.github.jonathanxd.iutils.data.TypedData
import com.github.jonathanxd.iutils.function.collector.BiCollectors
import com.github.jonathanxd.iutils.type.TypeInfo
import com.github.jonathanxd.jwiutils.kt.biStream
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*
import java.util.function.Supplier

/**
 * Adapter utility (Requires CodeAPI, CodeAPI-BytecodeWriter and CodeGenUtil).
 */
object AdapterImplGen {

    private val loader = SaveCapableCodeClassLoader(CodeClassLoader())

    private var inc: Int = 0

    private val incremental: Int
        get() {
            ++inc
            return inc
        }

    private const val adapteeInstanceField = "adapteeInstance"
    private const val originalInstanceField = "originalInstance"
    private const val adapterManagerField = "adapterManager"

    private const val adapteeInstanceGet = "getAdapteeInstance"
    private const val originalInstanceGet = "getOriginalInstance"
    private const val adapterManagerGet = "getAdapterManager"

    /**
     * Generate implementation of an [Adapter] interface with two-arg constructor
     * that receives either [T] and [AdapterManager].
     */
    @JvmStatic
    @JvmOverloads
    fun <F : Any, T : Any> genImpl(klass: Class<out F>, type: Class<T>,
                                   additionalHandlers_: List<AdditionalHandler> = emptyList()): Class<out F> {


        if (!klass.isInterface)
            throw IllegalArgumentException("The target '$klass' is not an interface.")

        if (!Modifier.isPublic(klass.modifiers))
            throw IllegalArgumentException("The target '$klass' is inaccessible.")

        val additionalHandlers = additionalHandlers_ +
                klass.getDeclaredAnnotation(Additional::class.java)?.value?.map {
                    AdditionalHandlerHelper.from(it.java)
                }.orEmpty()

        val handlerDataMap = mutableMapOf<AdditionalHandler, TypedData>()

        val typedDataGet: (AdditionalHandler) -> TypedData = {
            handlerDataMap.computeIfAbsent(it, {
                val data = TypedData()
                data.set("interface", type, TypeInfo.of(Class::class.java))
                data
            })
        }

        val fields = klass.getAnnotationsByType(Field::class.java).toMutableList() +
                klass.getAnnotationsByType(Fields::class.java).flatMap { it.value.toMutableList() }

        val shouldIncludeManager = Adapter::class.java.isAssignableFrom(klass)

        val owner = TypeRef(null, "${klass.canonicalName}_$incremental", false)

        // Additional

        val mapOfMethodToHandler = additionalHandlers.biStream {
            it to it.getMethodsToImplement(owner, klass, typedDataGet(it))
        }.collect(BiCollectors.toMap())

        // /Additional

        val codeGen = CodeGen()

        val properties = mutableListOf(Property(originalInstanceField, type.codeType))

        if (shouldIncludeManager) properties += Property(adapterManagerField, AdapterManager::class.java.codeType)

        val cproperties = Collections.unmodifiableList(properties.map {
            VariableRef(it.type, it.name)
        })

        val additionalProperties = additionalHandlers.flatMap {
            it.generateAdditionalProperties(cproperties, owner, klass, typedDataGet(it))
                    .map { (a, b) -> Property(b, a.codeType) }
        }

        properties += additionalProperties

        val fcodeFields = fields.map {
            val builder = FieldDeclaration.Builder.builder()
                    .type(it.type.java.codeType)
                    .name(it.value)

            if (it.setter.isNotEmpty())
                builder.modifiers(CodeModifier.PRIVATE)
            else
                builder.modifiers(CodeModifier.PRIVATE, CodeModifier.FINAL)

            if (it.defaultValueProvider == NullProvider::class) {
                builder.value(Literals.NULL)
            } else {
                val defaultProviderType = it.defaultValueProvider.java.codeType

                builder.value(accessStaticField(defaultProviderType, defaultProviderType, "INSTANCE").invoke(
                        invokeType = InvokeType.INVOKE_INTERFACE,
                        localization = Supplier::class.java,
                        name = "get",
                        spec = TypeSpec(Any::class.java),
                        arguments = emptyList()
                ))
            }

            builder.build()
        }

        val ccodeFields = Collections.unmodifiableList(fcodeFields)

        val additionalFields = additionalHandlers.flatMap {
            it.generateAdditionalFields(ccodeFields, owner, klass, typedDataGet(it))
        }

        val codeFields = fcodeFields + additionalFields

        val defaultImpls = mutableMapOf<MethodTypeSpec, Method>()

        val filter = klass.methods.filter {
            Modifier.isAbstract(it.modifiers)
                    && !it.isFieldMethod(fields)
                    && !(getDefaultImpl(klass, it)?.let { x -> defaultImpls.put(it.methodTypeSpec, x); true } ?: false)
                    && !isInstanceMethod(it)
                    && (shouldIncludeManager && !isAdapterMethod(it))
                    && !isNotImplementedByAdditional(it, mapOfMethodToHandler)
                    && !it.isAdditionalPropertyMethod(additionalProperties)
        }

        val count = filter.size

        if (count > 0)
            throw IllegalArgumentException("The target '$klass' has '$count' abstract methods. '${filter.joinToString(transform = Method::getName)}'")

        codeGen.install(PropertySystem(
                *properties.toTypedArray()
        ))

        codeGen.install(Implementer { method ->
            val empty = method.parameters.isEmpty()

            fun Field.toRef() = VariableRef(this.type.java, this.value)
            fun Property.toRef() = VariableRef(this.type, this.name)

            val additionalGetter =
                    if(empty) additionalProperties.firstOrNull { method.name == "get${it.name.capitalize()}" }?.toRef()
                    else null

            val additionalSetter =
                    if (method.parameters.size == 1) additionalProperties
                            .firstOrNull { method.name == "set${it.name.capitalize()}" }?.toRef()
                    else null

            val fieldGetter =
                    if (empty) fields.firstOrNull { it.getter == method.name }?.toRef()
                    else null

            val fieldSetter =
                    if (method.parameters.size == 1) fields.firstOrNull { it.setter == method.name }?.toRef()
                    else null

            if (empty && (method.name == originalInstanceGet || method.name == adapteeInstanceGet)) {
                return@Implementer method.builder().body(source(
                        returnValue(type.codeType, accessThisField(type.codeType, originalInstanceField))
                )).build()
            } else if (empty && method.name == adapterManagerGet && shouldIncludeManager) {
                return@Implementer method.builder().body(source(
                        returnValue(AdapterManager::class.java.codeType, accessThisField(AdapterManager::class.java.codeType, adapterManagerField))
                )).build()
            } else if (empty && fieldGetter != null) {
                return@Implementer method.builder().body(source(
                        returnValue(fieldGetter.type,
                                accessThisField(fieldGetter.type, fieldGetter.name))
                )).build()
            } else if (fieldSetter != null) {
                return@Implementer method.builder().body(source(
                        setThisFieldValue(fieldSetter.type, fieldSetter.name,
                                method.parameters[0].toVariableAccess()),
                        returnVoid()
                )).build()
            } else {
                val get = defaultImpls.entries.firstOrNull {
                    it.key.methodName == method.name
                            && it.key.typeSpec == method.typeSpec
                }

                if (get != null) {
                    method.builder().body(source(
                            returnValue(method.returnType, get.value.toInvocation(InvokeType.INVOKE_STATIC, Access.STATIC,
                                    listOf(Access.THIS) + method.parameters.map { it.toVariableAccess() }))
                    )).build()
                } else {
                    val spec = MethodTypeSpec(owner, method.name, method.typeSpec)

                    mapOfMethodToHandler.toList().filter { (_, v) ->
                        v.any { it.methodName == spec.methodName
                                && it.typeSpec.isConreteEq(spec.typeSpec) }
                    }.forEach { (k, _) ->
                        k.generateImplementation(method, owner, klass, typedDataGet(k)).orElse(null)?.let {
                            return@Implementer it
                        }
                    }

                    if (additionalGetter != null) {
                        return@Implementer method.builder().body(source(
                                returnValue(additionalGetter.type,
                                        accessThisField(additionalGetter.type, additionalGetter.name))
                        )).build()
                    } else if (additionalSetter != null) {
                        return@Implementer method.builder().body(source(
                                setThisFieldValue(additionalSetter.type, additionalSetter.name,
                                        method.parameters[0].toVariableAccess()),
                                returnVoid()
                        )).build()
                    }

                    return@Implementer method
                }
            }
        })

        val cdeclaration = codeGen.visit(ClassDeclaration.Builder.builder()
                .modifiers(CodeModifier.PUBLIC, CodeModifier.SYNTHETIC)
                .fields(codeFields)
                .outerClass(owner.outerType)
                .qualifiedName(owner.specifiedName)
                .superClass(Types.OBJECT)
                .build()
                .extend(klass))

        val cfields = Collections.unmodifiableList(cdeclaration.fields)
        val methods = cdeclaration.methods.toMutableList()
        val cmethods = Collections.unmodifiableList(methods)
        val ctr = cdeclaration.constructors.toMutableList()
        val cctr = Collections.unmodifiableList(ctr)

        val add = additionalHandlers.flatMap {
            it.generateAdditionalMethodsAndConstructors(cctr, cmethods, cfields, owner, klass, typedDataGet(it)).also {
                it.forEach {
                    if (it is MethodDeclaration) methods += it
                    else if (it is ConstructorDeclaration) ctr += it
                }
            }
        }

        val declaration = cdeclaration.builder()
                .constructors(cdeclaration.constructors.map { lctr ->
                    lctr.builder().body(lctr.body.toMutable().also { source ->
                        additionalHandlers.forEach {
                            source += it.generateAdditionalConstructorBody(lctr, owner, klass, typedDataGet(it))
                        }

                    }).build()
                } + add.filterIsInstance<ConstructorDeclaration>())
                .methods(cdeclaration.methods + add.filterIsInstance<MethodDeclaration>())
                .build()

        val decl = BytecodeGenerator().process(declaration)

        @Suppress("UNCHECKED_CAST")
        return loader.define(decl) as Class<out F>
    }

    private fun isNotImplementedByAdditional(method: Method,
                                             mapOfMethodToHandler: Map<AdditionalHandler, List<MethodTypeSpec>>): Boolean =
            method.methodTypeSpec.let { spec ->
                mapOfMethodToHandler.values.any { it.any {
                    it.methodName == spec.methodName
                            && it.typeSpec.isConreteEq(spec.typeSpec) }
                }
            }


    private fun Method.isFieldMethod(fields: List<Field>) =
            fields.any {
                (this.parameterCount == 0 && this.name == it.getter)
                        || (this.parameterCount == 1 && this.name == it.setter)
            }

    private fun Method.isAdditionalPropertyMethod(properties: List<Property>) =
            properties.any {
                (this.parameterCount == 0 && this.name == "get${it.name.capitalize()}")
                        || (this.parameterCount == 1 && this.name == "set${it.name.capitalize()}")
            }


    private fun getDefaultImpl(klass: Class<*>, method: Method): Method? {
        klass.classes.firstOrNull { it.name.endsWith("DefaultImpls") }?.let {
            Try { it.getDeclaredMethod(method.name, it.enclosingClass, *method.parameterTypes) }.first?.let {
                if (Modifier.isStatic(it.modifiers))
                    return it
            }
        }

        klass.superclass?.let {
            getDefaultImpl(it, method)?.let { return it }
        }

        klass.interfaces.forEach {
            getDefaultImpl(it, method)?.let { return it }
        }

        return null
    }

    private fun isInstanceMethod(method: Method) =
            method.parameterCount == 0
                    && (method.name == originalInstanceGet || method.name == adapteeInstanceGet)

    private fun isAdapterMethod(method: Method) =
            method.parameterCount == 0
                    && method.name == adapterManagerGet
}