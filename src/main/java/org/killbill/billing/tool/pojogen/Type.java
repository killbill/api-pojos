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

import java.util.List;
import com.github.javaparser.resolution.MethodUsage;
import com.github.javaparser.resolution.declarations.ResolvedTypeParameterDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedArrayType;
import com.github.javaparser.resolution.types.ResolvedLambdaConstraintType;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.resolution.types.ResolvedTypeVariable;
import com.github.javaparser.resolution.types.ResolvedUnionType;
import com.github.javaparser.resolution.types.ResolvedWildcard;
import com.github.javaparser.utils.Pair;

public class Type {

  public static final String OPENING = "[";
  public static final String CLOSING = "[";

  private static final Log log = new Log(Type.class);

  public static class Param {
    private final ResolvedTypeParameterDeclaration parameter;
    private final Mapping mapping;
    public Param(ResolvedTypeParameterDeclaration parameter, Mapping mapping){
      this.parameter = parameter;
      this.mapping = new Mapping(mapping);
    }
    @Override
    public String toString(){
      return toString(this.parameter, this.mapping);
    }
    public static String toString(ResolvedTypeParameterDeclaration parameter){
      return toString(parameter, new Mapping());
    }
    public static String toString(ResolvedTypeParameterDeclaration parameter, Mapping mapping){
      return unparse(parameter, mapping, false);
    }
    public static String unparse(ResolvedTypeParameterDeclaration parameter, Mapping mapping, boolean debug){
      StringBuilder s = new StringBuilder();
      if(debug) s.append(OPENING);
      if(parameter.hasName()){
        if(debug){
          s.append(parameter.getName());
        }else{
          s.append(mapping.resolve(parameter.getQualifiedName()));
        }
      }else{
        s.append("?");
      }
      List<ResolvedTypeParameterDeclaration.Bound> bounds = parameter.getBounds();
      if(!bounds.isEmpty()){
        if(parameter.hasUpperBound()){
          s.append(" super ");
        }
        if(parameter.hasLowerBound()){
          s.append(" extends ");
        }
        for(int i = 0 ; i < bounds.size() ; i++){
          if( i > 0 ) s.append(" & ");
          s.append(Type.unparse(bounds.get(i).getType(), mapping, debug)); 
        }
      }
      if(debug) s.append(CLOSING);
      return s.toString();
    }
  }
  private final ResolvedType type;
  private final Mapping mapping;

  public Type(ResolvedType type, Mapping mapping){
    this.type = type;
    this.mapping = new Mapping(mapping);
  }
  public boolean isArray(){
    return this.type.isArray();
  }
  public boolean isPrimitive(){
    return this.type.isPrimitive();
  }
  public String getName(){
    return toString(this.type);
  }
  public ResolvedType getType(){
    return this.type;
  }
  @Override
  public String toString(){
    return toString(this.type, this.mapping);
  }
  public static MethodUsage specialize(MethodUsage method, ResolvedReferenceType reference){
    for( Pair<ResolvedTypeParameterDeclaration,ResolvedType> pair: reference.getTypeParametersMap()){
      method = method.replaceTypeParameter(pair.a, pair.b);
    }
    return method;
  }
  public static ResolvedReferenceType specialize(ResolvedReferenceType type, ResolvedReferenceType reference){
    return reference.useThisTypeParametersOnTheGivenType(type).asReferenceType();
  }
  public static String toString(ResolvedType type,Mapping mapping){
    return unparse(type, mapping, false);
  }
  public static String toString(ResolvedType type){
    return toString(type, new Mapping());
  }
  public static String unparse(ResolvedType type,Mapping mapping, boolean debug){
    StringBuilder s = new StringBuilder();
    if(debug) s.append(OPENING);
    if(type.isReferenceType()){
      ResolvedReferenceType reference = type.asReferenceType();
      s.append(mapping.resolve(reference.getQualifiedName()));
      List<Pair<ResolvedTypeParameterDeclaration,ResolvedType>>	parameters = reference.getTypeParametersMap();
      if(!parameters.isEmpty()){
        s.append("<");
        for( int i = 0; i < parameters.size() ; i++ ){
          if(i > 0){
            s.append(", ");
          }
          s.append(unparse(parameters.get(i).b, mapping, debug));
        }
        s.append(">");
      }
    }else if( type.isTypeVariable() ){
      ResolvedTypeVariable variable = type.asTypeVariable();
      ResolvedTypeParameterDeclaration parameter = variable.asTypeParameter();
      if(debug){
        s.append(parameter.getName());
      }else{
        s.append(mapping.resolve(parameter.getQualifiedName()));
      }
    }
    else if(type.isWildcard()){
      ResolvedWildcard wildcard = type.asWildcard();
      s.append("?");
      if(wildcard.isBounded()){
        if(wildcard.isUpperBounded()){
          s.append(" super ");
        }
        if(wildcard.isLowerBounded()){
          s.append(" extends ");
        }
        s.append(unparse(wildcard.getBoundedType(), mapping, debug)); 
      }
    }else if(type.isArray()){
      ResolvedArrayType array = type.asArrayType();
      s.append(unparse(array.getComponentType(), mapping, debug)); 
      s.append("[]"); 
    }else if(type.isConstraint()){
      ResolvedLambdaConstraintType constraint = type.asConstraintType();
      s.append("?");
      s.append(" super ");
      s.append(unparse(constraint.getBound(), mapping, debug));
    }else if(type.isUnionType()){
      ResolvedUnionType union = type.asUnionType();
      s.append(union.describe());
    }else if(type.isVoid()){
      s.append(type.describe());
    }else if(type.isNull()){
      s.append(type.describe());
    }else if(type.isPrimitive()){
      ResolvedPrimitiveType primitive = type.asPrimitive();
      s.append(primitive.describe());
    }else if(type.isInferenceVariable()){
      s.append(type.describe());
    }else if(type.isNumericType()){
      s.append(type.describe());
    }else if(type.isReference()){
      s.append(type.describe());
    }else{
      s.append(type.describe());
    }
    if(debug) s.append(CLOSING);
    return s.toString();
  }
}
