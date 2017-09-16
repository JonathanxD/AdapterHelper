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

import com.github.jonathanxd.codeapi.CodeSource;
import com.github.jonathanxd.codeapi.base.CodeModifier;
import com.github.jonathanxd.codeapi.base.FieldDeclaration;
import com.github.jonathanxd.codeapi.base.MethodDeclaration;
import com.github.jonathanxd.codeapi.base.TypeSpec;
import com.github.jonathanxd.codeapi.common.MethodTypeSpec;
import com.github.jonathanxd.codeapi.common.VariableRef;
import com.github.jonathanxd.codeapi.factory.Factories;
import com.github.jonathanxd.codeapi.factory.InvocationFactory;
import com.github.jonathanxd.codeapi.literal.Literals;
import com.github.jonathanxd.codeapi.type.TypeRef;
import com.github.jonathanxd.iutils.collection.Collections3;
import com.github.jonathanxd.iutils.data.TypedData;
import com.github.jonathanxd.iutils.type.TypeInfo;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ImplAddTest {

    @NotNull
    public static List<FieldDeclaration> generateAdditionalFields(@NotNull List<FieldDeclaration> fields,
                                                                  @NotNull TypeRef owner,
                                                                  @NotNull Class<?> base,
                                                                  @NotNull TypedData data) {
        return Collections3.listOf(
                FieldDeclaration.Builder.builder()
                        .modifiers(CodeModifier.PRIVATE, CodeModifier.FINAL)
                        .type(String.class)
                        .name("str")
                .build()
        );
    }

    @NotNull
    public static List<VariableRef> generateAdditionalProperties(@NotNull List<VariableRef> currentProperties,
                                                                 @NotNull TypeRef owner,
                                                                 @NotNull Class<?> base,
                                                                 @NotNull TypedData data) {
        return Collections3.listOf(
                new VariableRef(String.class, "ss")
        );
    }

    @NotNull
    public static List<MethodTypeSpec> getMethodsToImplement(@NotNull TypeRef owner,
                                                             @NotNull Class<?> base,
                                                             @NotNull TypedData data) {
        return Collections3.listOf(
                new MethodTypeSpec(AddTest.MyPerson.class, "p", new TypeSpec(Class.class))
        );
    }

    @NotNull
    public static Optional<MethodDeclaration> generateImplementation(@NotNull MethodDeclaration declaration,
                                                                     @NotNull TypeRef owner,
                                                                     @NotNull Class<?> base,
                                                                     @NotNull TypedData data) {
        if (declaration.getParameters().isEmpty()) {

            if (declaration.getName().equals("p")) {

                Class<?> itf = data.getOptional("interface", TypeInfo.of(Class.class)).orElse(base);

                return Optional.of(declaration)
                        .map(it -> it.builder()
                                .body(CodeSource.fromPart(Factories.returnValue(Class.class, Literals.CLASS(itf))))
                                .build());
            }
        }

        return Optional.empty();
    }
}
