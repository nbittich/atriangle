package tech.artcoded.atriangle.rest.annotation;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@ApiImplicitParams({
                     @ApiImplicitParam(name = "X-AUTH-TOKEN",
                                       value = "Access Token",
                                       paramType = "header"),
                     @ApiImplicitParam(name = "Authorization",
                                       value = "Access Token",
                                       defaultValue = "Basic YWRtaW5AY29nbmkuem9uZTphYkMxMjM0NTY3IT8=",
                                       paramType = "header")
                   })
public @interface SwaggerHeaderAuthentication {
}
