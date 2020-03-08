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
                     @ApiImplicitParam(name = "X-AUTH-TOKEN",
                                       value = "Access Token",
                                       paramType = "header"),
                     @ApiImplicitParam(name = "Authorization",
                                       value = "Access Token",
                                       defaultValue = "Basic YWRtaW5AY29nbmkuem9uZTphYkMxMjM0NTY3IT8=",
                                       paramType = "header"),
                     @ApiImplicitParam(name = "page", dataType = "integer", paramType = "query",
                                       value = "Results page you want to retrieve (0..N)", defaultValue = "0"),
                     @ApiImplicitParam(name = "size", dataType = "integer", paramType = "query",
                                       value = "Number of records per page.", defaultValue = "5"),
                     @ApiImplicitParam(name = "sort", allowMultiple = true, dataType = "string", paramType = "query",
                                       value = "Sorting criteria in the format: property(,asc|desc). " +
                                         "Default sort order is ascending. " +
                                         "Multiple sort criteria are supported.")
                   })
public @interface SwaggerHeaderAuthenticationPageable {
}
