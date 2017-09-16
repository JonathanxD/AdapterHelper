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

import com.github.jonathanxd.codeapi.CodeSource;
import com.github.jonathanxd.codeapi.base.ConstructorDeclaration;
import com.github.jonathanxd.codeapi.base.FieldDeclaration;
import com.github.jonathanxd.codeapi.base.MethodDeclaration;
import com.github.jonathanxd.codeapi.base.MethodDeclarationBase;
import com.github.jonathanxd.codeapi.common.MethodTypeSpec;
import com.github.jonathanxd.codeapi.common.VariableRef;
import com.github.jonathanxd.codeapi.type.TypeRef;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Additional handler can be used to generate additional things for AdapterHelper implementation
 * generator, dependency on {@code AdapterHelper} is not required for this work, only dependency on
 * {@code CodeAPI} is required. To work without implementing this interface directly, you must to
 * have all methods static following the interface methods signature, a Proxy will be generated to
 * call the static methods of your class, and {@code default} behavior of interface remains the
 * same, this means that if you don't implement the method on the class, the default method
 * implementation declared in this interface will be invoked.
 */
public interface AdditionalHandler {

    /**
     * Returns additional fields for {@code owner}.
     *
     * @param fields Current fields (without handler additions).
     * @param owner  Owner.
     * @return Additional fields.
     */
    @NotNull
    default List<FieldDeclaration> generateAdditionalFields(@NotNull List<FieldDeclaration> fields,
                                                            @NotNull TypeRef owner) {
        return Collections.emptyList();
    }

    /**
     * Generates additional properties, properties adds a field to class as well as a parameter to
     * constructor with initialization in the body.
     *
     * @param currentProperties Current properties (without handler additions).
     * @param owner             Owner of the constructor.
     * @return Additional properties.
     */
    @NotNull
    default List<VariableRef> generateAdditionalProperties(@NotNull List<VariableRef> currentProperties,
                                                           @NotNull TypeRef owner) {
        return Collections.emptyList();
    }

    /**
     * Generates additional constructor body instructions.
     *
     * @param constructorDeclaration Current constructor declaration definition (without handler
     *                               additions).
     * @param owner                  Owner of the constructor.
     * @return Additional constructor body instructions.
     */
    @NotNull
    default CodeSource generateAdditionalConstructorBody(@NotNull ConstructorDeclaration constructorDeclaration,
                                                         @NotNull TypeRef owner) {
        return CodeSource.empty();
    }

    /**
     * Returns the specification of that methods this class is capable to implement.
     *
     * @param owner Owner class.
     * @return Specification of methods that this class is capable to implement.
     */
    @NotNull
    default List<MethodTypeSpec> getMethodsToImplement(@NotNull TypeRef owner) {
        return Collections.emptyList();
    }

    /**
     * Generates implementation for {@code declaration} of {@code owner}.
     *
     * @param declaration Method declaration to generate implementation.
     * @param owner       Owner of method declaration.
     * @return {@link Optional} of {@link MethodDeclaration} with implementation, or empty {@link
     * Optional} if implementation cannot be generated.
     */
    @NotNull
    default Optional<MethodDeclaration> generateImplementation(@NotNull MethodDeclaration declaration,
                                                               @NotNull TypeRef owner) {
        return Optional.empty();
    }

    /**
     * Generates additional methods and constructors, called after {@link
     * #generateImplementation(MethodDeclaration, TypeRef)}.
     *
     * @param constructors Current method constructors (with elements added by others {@link
     *                     AdditionalHandler}s).
     * @param methods      Current method declarations to be added to generated type (with elements
     *                     added by others {@link AdditionalHandler}s).
     * @param fields       Current fields to be added to generated type (with elements added by
     *                     others {@link AdditionalHandler}s)..
     * @param owner        Owner of {@code constructors}, {@code methods}, {@code fields}
     * @return Additional methods and constructors (only {@link MethodDeclaration} and {@link
     * ConstructorDeclaration} are acceptable).
     */
    @NotNull
    default List<MethodDeclarationBase> generateAdditionalMethodsAndConstructors(@NotNull List<ConstructorDeclaration> constructors,
                                                                                 @NotNull List<MethodDeclaration> methods,
                                                                                 @NotNull List<FieldDeclaration> fields,
                                                                                 @NotNull TypeRef owner) {
        return Collections.emptyList();
    }

}
