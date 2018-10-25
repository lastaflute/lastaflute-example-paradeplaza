/*
 * Copyright 2015-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.docksidestage.mylasta.direction.sponsor

import org.dbflute.optional.OptionalThing
import org.docksidestage.mylasta.direction.sponsor.ParadeplazaApiFailureHook.UnifiedFailureResult.FailureErrorPart
import org.lastaflute.web.api.ApiFailureHook
import org.lastaflute.web.api.ApiFailureResource
import org.lastaflute.web.api.BusinessFailureMapping
import org.lastaflute.web.login.exception.LoginFailureException
import org.lastaflute.web.login.exception.LoginRequiredException
import org.lastaflute.web.response.ApiResponse
import org.lastaflute.web.response.JsonResponse
import org.lastaflute.web.validation.Required
import java.util.stream.Collectors
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid
import javax.validation.constraints.NotNull

/**
 * @author jflute
 */
class ParadeplazaApiFailureHook : ApiFailureHook { // #change_it for handling API failure

    // ===================================================================================
    //                                                                    Business Failure
    //                                                                    ================
    override fun handleValidationError(resource: ApiFailureResource): ApiResponse {
        val result = createFailureResult(UnifiedFailureType.VALIDATION_ERROR, resource)
        return asJson(result).httpStatus(BUSINESS_FAILURE_STATUS)
    }

    override fun handleApplicationException(resource: ApiFailureResource, cause: RuntimeException): ApiResponse {
        val failureType = failureTypeMapping.findAssignable(cause).orElseGet { UnifiedFailureType.BUSINESS_ERROR }
        val result = createFailureResult(failureType, resource)
        return asJson(result).httpStatus(BUSINESS_FAILURE_STATUS)
    }

    // ===================================================================================
    //                                                                      System Failure
    //                                                                      ==============
    override fun handleClientException(resource: ApiFailureResource, cause: RuntimeException): OptionalThing<ApiResponse> {
        val result = createFailureResult(UnifiedFailureType.CLIENT_ERROR, resource)
        return OptionalThing.of(asJson(result)) // HTTP status will be automatically sent as client error for the cause
    }

    override fun handleServerException(resource: ApiFailureResource, cause: Throwable): OptionalThing<ApiResponse> {
        return OptionalThing.empty() // means empty body, HTTP status will be automatically sent as server error
    }

    // ===================================================================================
    //                                                                          JSON Logic
    //                                                                          ==========
    protected fun createFailureResult(failureType: UnifiedFailureType, resource: ApiFailureResource): UnifiedFailureResult {
        return UnifiedFailureResult(failureType, toErrors(resource.propertyMessageMap))
    }

    protected fun toErrors(propertyMessageMap: Map<String, List<String>>): List<FailureErrorPart> {
        return propertyMessageMap.entries
                .stream()
                .map { entry -> FailureErrorPart(entry.key, entry.value) }
                .collect(Collectors.toList())
    }

    protected fun asJson(result: UnifiedFailureResult): JsonResponse<UnifiedFailureResult> {
        return JsonResponse(result)
    }

    // ===================================================================================
    //                                                                      Failure Result
    //                                                                      ==============
    class UnifiedFailureResult(@field:Required
                               val cause: UnifiedFailureType, @field:NotNull
                               @field:Valid
                               var errors: List<FailureErrorPart>) {

        class FailureErrorPart(@field:Required
                               val field: String, @field:NotNull
                               val messages: List<String>)
    }

    enum class UnifiedFailureType {
        VALIDATION_ERROR // special type
        ,
        LOGIN_FAILURE, LOGIN_REQUIRED // specific type of application exception
        ,
        BUSINESS_ERROR // default type of application exception
        ,
        CLIENT_ERROR // e.g. 404 not found
    }// you can add your application exception type if you need it
    //, ALREADY_DELETED, ALREADY_UPDATED

    companion object {

        // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
        // [Reference Site]
        // http://dbflute.seasar.org/ja/lastaflute/howto/impldesign/jsondesign.html
        //
        // [Front-side Implementation Example]
        // if (HTTP Status: 200) { // success
        //     [Business]JsonResult result = parseJsonAsSuccess(response);
        //     ...(do process per action)
        // } else if (HTTP Status: 400) { // e.g. validation error, application exception, client exception
        //     FailureResult result = parseJsonAsFailure(response);
        //     ...(show result.errors or do process per result.cause)
        // } else if (HTTP Status: 404) { // e.g. real not found, invalid parameter
        //     showNotFoundError();
        // } else { // basically 500 or other client errors
        //     showSystemError();
        // }
        // _/_/_/_/_/_/_/_/_/_/

        // ===================================================================================
        //                                                                          Definition
        //                                                                          ==========
        protected const val BUSINESS_FAILURE_STATUS = HttpServletResponse.SC_BAD_REQUEST
        protected val failureTypeMapping: BusinessFailureMapping<UnifiedFailureType> // for application exception

        init { // you can add mapping of failure type with exception
            failureTypeMapping = BusinessFailureMapping { failureMap ->
                failureMap[LoginFailureException::class.java] = UnifiedFailureType.LOGIN_FAILURE
                failureMap[LoginRequiredException::class.java] = UnifiedFailureType.LOGIN_REQUIRED
            }
        }
    }
}
