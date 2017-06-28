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

import com.github.jonathanxd.adapterhelper.Debug
import com.github.jonathanxd.codeapi.base.TypeDeclaration
import com.github.jonathanxd.codeapi.bytecode.BytecodeClass
import com.github.jonathanxd.codeapi.bytecode.classloader.CodeClassLoader
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class SaveCapableCodeClassLoader(val wrapped: CodeClassLoader) : CodeClassLoader() {

    override fun define(typeDeclaration: TypeDeclaration, bytes: ByteArray): Class<*> {
        this.save(BytecodeClass(typeDeclaration, bytes))

        return this.wrapped.define(typeDeclaration, bytes)
    }

    override fun define(bytecodeClass: BytecodeClass): Class<*> {
        this.save(bytecodeClass)
        return this.wrapped.define(bytecodeClass)
    }

    override fun define(bytecodeClasses: Array<out BytecodeClass>): Class<*> {
        bytecodeClasses.forEach { this.save(it) }
        return this.wrapped.define(bytecodeClasses)
    }

    override fun define(bytecodeClasses: Collection<BytecodeClass>): Class<*> {
        bytecodeClasses.forEach { this.save(it) }

        return this.wrapped.define(bytecodeClasses)
    }

    override fun define(bytecodeClasses: Iterator<BytecodeClass>): Class<*> {
        bytecodeClasses.forEach { this.save(it) }
        return this.wrapped.define(bytecodeClasses)
    }

    private fun save(bytecodeClass: BytecodeClass) {
        try {

            if (!Debug.isDebug())
                return

            val typeDeclaration = bytecodeClass.type

            var canonicalName = "gen/adapterhelper/" + typeDeclaration.canonicalName

            canonicalName = canonicalName.replace('.', '/')

            val file = File(canonicalName)

            if (file.parentFile != null && file.parentFile.exists()) {
                file.parentFile.delete()
            }

            if (file.parentFile != null && !file.parentFile.exists()) {

                file.parentFile.mkdirs()
            }

            Files.write(Paths.get(canonicalName + ".disassembled"), bytecodeClass.disassembledCode.toByteArray(charset("UTF-8")), StandardOpenOption.CREATE)
            Files.write(Paths.get(canonicalName + ".class"), bytecodeClass.bytecode, StandardOpenOption.CREATE)

        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }
}