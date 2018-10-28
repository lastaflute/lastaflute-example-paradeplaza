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
package org.docksidestage.app.logic.context

import org.dbflute.hook.AccessContext
import org.dbflute.optional.OptionalThing
import org.lastaflute.core.time.TimeManager
import org.lastaflute.db.dbflute.accesscontext.AccessContextResource
import javax.annotation.Resource

/**
 * @author jflute
 */
class AccessContextLogic {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private lateinit var timeManager: TimeManager

    // ===================================================================================
    //                                                                  Resource Interface
    //                                                                  ==================
    @FunctionalInterface
    interface UserTypeSupplier {
        fun supply(): OptionalThing<String>
    }

    @FunctionalInterface
    interface UserInfoSupplier {
        fun supply(): OptionalThing<Any>
    }

    @FunctionalInterface
    interface AppTypeSupplier {
        fun supply(): String
    }

    @FunctionalInterface
    interface ClientInfoSupplier {
        fun supply(): OptionalThing<String>
    }

    // ===================================================================================
    //                                                                      Create Context
    //                                                                      ==============
    fun create(resource: AccessContextResource, userTypeSupplier: UserTypeSupplier, userBeanSupplier: UserInfoSupplier,
               appTypeSupplier: AppTypeSupplier, clientInfoSupplier: ClientInfoSupplier): AccessContext {
        val context = AccessContext()
        context.setAccessLocalDateTimeProvider { timeManager.currentDateTime() }
        context.setAccessUserProvider { buildAccessUserTrace(resource, userTypeSupplier, userBeanSupplier, appTypeSupplier, clientInfoSupplier) }
        return context
    }

    private fun buildAccessUserTrace(resource: AccessContextResource, userTypeSupplier: UserTypeSupplier,
                                     userBeanSupplier: UserInfoSupplier, appTypeSupplier: AppTypeSupplier, clientInfoSupplier: ClientInfoSupplier): String {
        // #change_it you can customize the user trace for common column
        // example default style: "M:7,DCK,ProductListAction,_" or "_:_,DCK,ProductListAction,_"
        val trace = buildString {
            append(userTypeSupplier.supply().orElse("_")).append(":")
            append(userBeanSupplier.supply().orElse("_"))
            append(",").append(appTypeSupplier.supply()).append(",").append(resource.moduleName)
            append(",").append(clientInfoSupplier.supply().orElse("_"))
        }
        val columnSize = 64 // should be less than size of trace column
        return if (trace.length > columnSize) trace.substring(0, columnSize) else trace
    }
}
