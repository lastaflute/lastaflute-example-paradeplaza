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

import java.util.Locale

import org.dbflute.optional.OptionalObject
import org.dbflute.optional.OptionalThing
import org.dbflute.util.DfTypeUtil
import org.lastaflute.web.ruts.process.ActionRuntime
import org.lastaflute.web.servlet.request.RequestManager
import org.lastaflute.web.servlet.request.UserLocaleProcessProvider

/**
 * @author jflute
 */
class HarborUserLocaleProcessProvider : UserLocaleProcessProvider {

    override fun isAcceptCookieLocale(): Boolean {
        return false
    }

    override fun findBusinessLocale(runtimeMeta: ActionRuntime, requestManager: RequestManager): OptionalThing<Locale> {
        return OptionalObject.empty() // to next determination
    }

    override fun getRequestedLocale(requestManager: RequestManager): OptionalThing<Locale> {
        return OptionalObject.empty() // means browser default
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    override fun toString(): String {
        val hash = Integer.toHexString(hashCode())
        return DfTypeUtil.toClassTitle(this) + ":{acceptCookieLocale=" + isAcceptCookieLocale + "}@" + hash
    }
}
