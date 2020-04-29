package tech.artcoded.atriangle.core.rest.annotation;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@ApiImplicitParams({
  @ApiImplicitParam(name = "X-AUTH-TOKEN", value = "Access Token", paramType = "header"),
  @ApiImplicitParam(
      name = "Authorization",
      value = "Access Token",
      defaultValue = "Basic YWRtaW46YWRtaW4=",
      paramType = "header")
})
public @interface SwaggerHeaderAuthentication {}
