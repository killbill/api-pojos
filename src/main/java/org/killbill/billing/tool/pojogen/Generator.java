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

import java.nio.charset.Charset;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays; 
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.output.FileWriterWithEncoding;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedInterfaceDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.utils.Pair;
import com.github.javaparser.utils.SourceRoot;

public  class  Generator {
  private static final Log log = new Log(Generator.class);
  static final String[] KEYWORDSPLUS = new String[]{
    /* Java keywords */
    "abstract",	"continue",	"for",	"new",	"switch",
      "assert",	"default",	"goto",	"package",	"synchronized",
      "boolean",	"do",	"if",	"private",	"this",
      "break",	"double",	"implements",	"protected",	"throw",
      "byte",	"else",	"import",	"public",	"throws",
      "case",	"enum",	"instanceof",	"return",	"transient",
      "catch",	"extends",	"int",	"short",	"try",
      "char",	"final",	"interface",	"static",	"void",
      "class",	"finally",	"long",	"strictfp",	"volatile",
      "const","float",	"native",	"super",	"while",
      /* plus */
      "true",  "false", "null",
      /* plus */
      "equals", "hashCode", "toString",
      /* plus */
      "Builder"
  };
  private final Configuration configuration;
  private final Symbols symbols;

  public Generator(Configuration configuration) throws Exception{
    this.configuration = configuration;
    this.symbols =  new Symbols(Arrays.asList(KEYWORDSPLUS));
  }
  private List<SourceRoot> sources() throws IOException
  {
    ArrayList<SourceRoot> list = new ArrayList<SourceRoot>();
    CombinedTypeSolver solver = new CombinedTypeSolver();
    ParserConfiguration configuration = new ParserConfiguration();
    configuration.setCharacterEncoding(this.configuration.getEncoding());
    configuration.setSymbolResolver(new JavaSymbolSolver(solver));
    solver.add(new ReflectionTypeSolver(true));

    for(File dependency : this.configuration.getDependencies()){
      if(dependency.exists()){
        Iterator<File> jars =  FileUtils.iterateFiles(dependency,
            new WildcardFileFilter("*.jar", IOCase.INSENSITIVE),
            DirectoryFileFilter.INSTANCE);

        while(jars.hasNext()){
          File jar = jars.next();
          log.trace("Added jar dependency: %s", jar);
          solver.add(new JarTypeSolver(jar));
        }
      }else{
        log.warn("Dependency directory <%s> does not exit.", dependency);
      }
    }
    for(File source : this.configuration.getSources()){
      if(source.exists()){
        log.trace("Added source directory: %s", source);
        solver.add(new JavaParserTypeSolver(source, configuration));
        list.add( new SourceRoot(source.toPath(), configuration));
      }else{
        log.warn("Source directory <%s> does not exit.", source);
      }
    }
    if(list.isEmpty()){
      log.error("No source directory added.");;
    }
    return list;
  }
  public void run() throws Exception{
    ArrayList<ClassOrInterfaceDeclaration>  interfaces = new ArrayList<ClassOrInterfaceDeclaration>();
    for( SourceRoot source : this.sources()){
      List<ParseResult<CompilationUnit>>  results = source.tryToParse();

      for(ParseResult<CompilationUnit> result: results){
        if(result.isSuccessful()){
          Optional<CompilationUnit> unit = result.getResult();
          if(unit.isPresent()){
            List<ClassOrInterfaceDeclaration> declarations = unit.get().findAll(ClassOrInterfaceDeclaration.class);
            for(ClassOrInterfaceDeclaration declaration : declarations){
              if( declaration.isInterface() && !declaration.isGeneric() &&!declaration.isNestedType()) {
                interfaces.add(declaration); 
              }
            }
          }

        }
      }
    }
    if(interfaces.isEmpty()){
      log.warn("No interface found in source code by JavaParser." +
          "Please ensure that all your source code can be compiled and all the required dependencies are provided.");
    }else{
      ArrayList<Implementation> implementations = new ArrayList<Implementation>();
      ArrayList<String> skipped = new ArrayList<String>();
      for(ClassOrInterfaceDeclaration ast : interfaces){
        Optional<String> fqn = ast.getFullyQualifiedName();
        if(fqn.isPresent()){
          try{
            ResolvedReferenceTypeDeclaration  declaration  = ast.resolve();
            if(this.configuration.accepts(declaration.getPackageName(), declaration.getQualifiedName())){
              String namespace = this.configuration.rename(declaration.getPackageName());
              String name = this.configuration.rename(declaration.getPackageName(), declaration.getQualifiedName());
              Implementation implementation =  Implementation.create(this.configuration,
                  this.symbols, namespace, name, declaration.asInterface());
              implementations.add(implementation);
            }else{
              skipped.add(declaration.getQualifiedName());
            }
          }catch(UnsolvedSymbolException e){
            log.trace("%s\n%s", fqn.get(), ast);
            log.error("Unresolved symbol <%s> in interface %s", e.getName(), fqn.get());
            throw e;
          }catch(Exception e){
            log.trace("%s\n%s", fqn.get(), ast);
            log.error("Cannot implement interface %s", fqn.get());
            throw e;
          }
        }
      }
      for(Implementation implementation : implementations){
        render(implementation);
        render(new Test(implementation));
      }
      showSummary(skipped, implementations);
    }
  }
  private void render(Implementation implementation) throws Exception{
    List<String> path = Namespaces.path(implementation.getNamespace() ,implementation.getName());
    File output =  FileUtils.getFile(this.configuration.getOutput(), path.toArray(new String[0]));
    FileUtils.forceMkdirParent(output);
    try( FileWriterWithEncoding writer = new FileWriterWithEncoding(output,this.configuration.getEncoding())){
      this.configuration.getTemplates().render(implementation,  writer);
    }
    log.trace(implementation, output);
  }
  private void render(Test test) throws Exception{
    if(this.configuration.getTest() != null){
      List<String> path = Namespaces.path(test.getNamespace(), test.getName());
      File output =  FileUtils.getFile(this.configuration.getTest(), path.toArray(new String[0]));
      FileUtils.forceMkdirParent(output);
      try( FileWriterWithEncoding writer = new FileWriterWithEncoding(output,this.configuration.getEncoding())){
        this.configuration.getTemplates().render(test,  writer);
      }
    }
  }
  private void showSummary(List<String> skipped, List<Implementation> implementations){
    if(!skipped.isEmpty() || !implementations.isEmpty()){
      StringBuilder s =new StringBuilder();
      s.append("  > Summary\n\n");
      for(String name : skipped){
        s.append(String.format("    > [SKIPPED] %s\n", name));
      }
      s.append("\n");
      for(Implementation implementation: implementations){
        List<String> path = Namespaces.path(implementation.getNamespace() ,implementation.getName());
        File output =  FileUtils.getFile(this.configuration.getOutput(), path.toArray(new String[0]));
        s.append(String.format("    > [IMPLEMENTED] %s\n", implementation.getBase()));
        s.append(String.format("        as %s\n", implementation.getName()));
        s.append(String.format("        at %s\n\n", output));
      }
      log.info(s);
    }
  }
}
