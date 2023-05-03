/*
 * Copyright 2022-2022 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.tool.pojogen;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.SourceRoot;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class Generator {
    private static final Log log = new Log(Generator.class);
    private final Configuration configuration;
    private final Symbols symbols;

    public Generator(Configuration configuration) throws Exception {
        this.configuration = configuration;
        this.symbols = Symbols.java();
    }

    private List<SourceRoot> sources() throws IOException {
        ArrayList<SourceRoot> list = new ArrayList<SourceRoot>();
        CombinedTypeSolver solver = new CombinedTypeSolver();
        ParserConfiguration configuration = new ParserConfiguration();
        configuration.setCharacterEncoding(this.configuration.getEncoding());
        configuration.setSymbolResolver(new JavaSymbolSolver(solver));
        solver.add(new ReflectionTypeSolver(true));

        for (File dependency : this.configuration.getDependencies()) {
            if (dependency.exists()) {
                Iterator<File> jars = FileUtils.iterateFiles(dependency,
                        new WildcardFileFilter("*.jar", IOCase.INSENSITIVE),
                        DirectoryFileFilter.INSTANCE);

                while (jars.hasNext()) {
                    File jar = jars.next();
                    log.trace("Added jar dependency: %s", jar);
                    solver.add(new JarTypeSolver(jar));
                }
            } else {
                log.warn("Dependency directory <%s> does not exit.", dependency);
            }
        }
        for (File source : this.configuration.getSources()) {
            if (source.exists()) {
                log.trace("Added source directory: %s", source);
                solver.add(new JavaParserTypeSolver(source, configuration));
                list.add(new SourceRoot(source.toPath(), configuration));
            } else {
                log.warn("Source directory <%s> does not exit.", source);
            }
        }
        if (list.isEmpty()) {
            log.error("No source directory added.");
            ;
        }
        return list;
    }

    public void run() throws Exception {
        ArrayList<ClassOrInterfaceDeclaration> interfaces = new ArrayList<ClassOrInterfaceDeclaration>();
        for (SourceRoot source : this.sources()) {
            List<ParseResult<CompilationUnit>> results = source.tryToParse();

            for (ParseResult<CompilationUnit> result : results) {
                if (result.isSuccessful()) {
                    Optional<CompilationUnit> unit = result.getResult();
                    if (unit.isPresent()) {
                        List<ClassOrInterfaceDeclaration> declarations = unit.get().findAll(ClassOrInterfaceDeclaration.class);
                        for (ClassOrInterfaceDeclaration declaration : declarations) {
                            if (declaration.isInterface() && !declaration.isGeneric() && !declaration.isNestedType()) {
                                interfaces.add(declaration);
                            }
                        }
                    }

                }
            }
        }
        if (interfaces.isEmpty()) {
            log.warn("No interface found in source code by JavaParser." +
                    "Please ensure that all your source code can be compiled and all the required dependencies are provided.");
        } else {
            ArrayList<Implementation> implementations = new ArrayList<Implementation>();
            ArrayList<String> skipped = new ArrayList<String>();
            for (ClassOrInterfaceDeclaration ast : interfaces) {
                Optional<String> fqn = ast.getFullyQualifiedName();
                if (fqn.isPresent()) {
                    try {
                        ResolvedReferenceTypeDeclaration declaration = ast.resolve();
                        if (this.configuration.accepts(declaration.getPackageName(), declaration.getQualifiedName())) {
                            String namespace = this.configuration.rename(declaration.getPackageName());
                            String name = this.configuration.rename(declaration.getPackageName(), declaration.getQualifiedName());
                            Entity entity = new Entity(namespace, name);
                            Entity base = new Entity(declaration);
                            Implementation implementation = Implementation.create(this.configuration,
                                    this.symbols, declaration.asInterface(), entity, base);
                            implementations.add(implementation);
                        } else {
                            skipped.add(declaration.getQualifiedName());
                        }
                    } catch (UnsolvedSymbolException e) {
                        log.trace("%s\n%s", fqn.get(), ast);
                        log.error("Unresolved symbol <%s> in interface %s", e.getName(), fqn.get());
                        throw e;
                    } catch (Exception e) {
                        log.trace("%s\n%s", fqn.get(), ast);
                        log.error("Cannot implement interface %s", fqn.get());
                        throw e;
                    }
                }
            }

            process(implementations);

            showSummary(skipped, implementations);
        }
    }

    private void process(List<Implementation> implementations) throws Exception {

        if (!implementations.isEmpty()) {
            ArrayList<Resolver> resolvers = new ArrayList<Resolver>();
            ArrayList<Module> modules = new ArrayList<Module>();
            ArrayList<Test> tests = new ArrayList<Test>();

            if (this.configuration.getTest() != null) {
                for (Implementation implementation : implementations) {
                    Test test = Test.create(this.configuration, this.symbols, implementation);
                    tests.add(test);
                }
            }
            if (this.configuration.getResolver() != null) {
                HashMap<String, List<Implementation>> map = new HashMap<String, List<Implementation>>();

                for (Implementation implementation : implementations) {
                    if (!map.containsKey(implementation.getNamespace())) {
                        map.put(implementation.getNamespace(), new ArrayList<Implementation>());
                    }
                    map.get(implementation.getNamespace()).add(implementation);
                }
                for (String namespace : map.keySet()) {
                    Resolver resolver = Resolver.create(this.configuration, this.symbols, namespace, Implementation.sort(map.get(namespace)));
                    resolvers.add(resolver);
                }
            }
            if (this.configuration.getModule() != null) {
                for (Resolver resolver : resolvers) {
                    Module module = Module.create(this.configuration, this.symbols, resolver);
                    modules.add(module);
                }
            }
            Service service = (this.configuration.getResource() != null && modules.size() > 0) ? Service.create(modules) : null;

            for (Implementation implementation : implementations) {
                render(implementation);
            }
            for (Resolver resolver : resolvers) {
                render(resolver);
            }
            for (Module module : modules) {
                render(module);
            }
            if (service != null) {
                render(service);
            }
            for (Test test : tests) {
                render(test);
            }
        }
    }

    private void write(File output, String content) throws Exception {
        FileUtils.forceMkdirParent(output);
        try (BufferedWriter writer = Files.newBufferedWriter(output.toPath(), this.configuration.getEncoding())) {
            writer.write(content);
        }
    }

    /**
     * <p>Special case of {@link #write(File, String)} where we need to append the output file properly.</p>
     *
     * <p>
     *     Current {@link #write(File, String)} implementation always rewrite generated files. This is Ok for most cases,
     *     but causing a problem for shared files between multiple sources.
     * </p>
     *
     * <p>
     *     Example: {@link #render(Service)} use {@code com.fasterxml.jackson.databind.Module} file to put all module(s)
     *     from any sources/project. For example, {@code killbill-api} and {@code killbill-plugin-api} will use this
     *     file together. Using just {@link #write(File, String)} lead a problem like explained in
     *     <a href="https://github.com/killbill/api-pojos/issues/12">this issue</a> .
     * </p>
     */
    private void writeOrAppend(File output, String content) throws Exception {
        boolean outputExist = output != null && output.exists();
        if (outputExist) {
            final List<String> contentList = List.of(content.split(System.lineSeparator()));
            final List<String> outputContentList = FileUtils.readLines(output, this.configuration.getEncoding());
            try (final BufferedWriter writer = Files.newBufferedWriter(output.toPath(), configuration.getEncoding(), StandardOpenOption.APPEND)) {
                for (final String contentLine : contentList) {
                    if (!outputContentList.contains(contentLine)) {
                        writer.write(contentLine);
                        writer.newLine();
                    }
                }
            }
        } else {
            write(output, content);
        }
    }


    private void render(Implementation implementation) throws Exception {
        File output = Namespaces.file(this.configuration.getOutput(), implementation.getNamespace(), implementation.getName());
        String content = this.configuration.getTemplates().render(implementation);
        write(output, content);
        log.trace(implementation, output);
    }

    private void render(Resolver resolver) throws Exception {
        File output = Namespaces.file(this.configuration.getOutput(), resolver.getNamespace(), resolver.getName());
        String content = this.configuration.getTemplates().render(resolver);
        write(output, content);
    }

    private void render(Module module) throws Exception {
        File output = Namespaces.file(this.configuration.getOutput(), module.getNamespace(), module.getName());
        String content = this.configuration.getTemplates().render(module);
        write(output, content);
    }

    private void render(Service service) throws Exception {
        File output = new File(this.configuration.getResource(), "META-INF/services/com.fasterxml.jackson.databind.Module");
        String content = this.configuration.getTemplates().render(service);
        writeOrAppend(output, content);
    }

    private void render(Test test) throws Exception {
        File output = Namespaces.file(this.configuration.getTest(), test.getNamespace(), test.getName());
        String content = this.configuration.getTemplates().render(test);
        write(output, content);
    }

    private void showSummary(List<String> skipped, List<Implementation> implementations) {
        if (!skipped.isEmpty() || !implementations.isEmpty()) {
            StringBuilder s = new StringBuilder();
            s.append("  > Summary\n\n");
            for (String name : skipped) {
                s.append(String.format("    > [SKIPPED] %s\n", name));
            }
            s.append("\n");
            for (Implementation implementation : implementations) {
                File output = Namespaces.file(this.configuration.getOutput(), implementation.getNamespace(), implementation.getName());
                s.append(String.format("    > [IMPLEMENTED] %s\n", implementation.getBase()));
                s.append(String.format("        as %s\n", implementation.getName()));
                s.append(String.format("        at %s\n\n", output));
            }
            log.info(s);
        }
    }
}
