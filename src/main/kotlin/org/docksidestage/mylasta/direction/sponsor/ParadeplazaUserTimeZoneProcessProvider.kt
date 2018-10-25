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

import org.dbflute.optional.OptionalObject
import org.dbflute.optional.OptionalThing
import org.dbflute.util.DfTypeUtil
import org.lastaflute.web.ruts.process.ActionRuntime
import org.lastaflute.web.servlet.request.RequestManager
import org.lastaflute.web.servlet.request.UserTimeZoneProcessProvider
import java.util.*

/**
 * @author jflute
 */
class ParadeplazaUserTimeZoneProcessProvider : UserTimeZoneProcessProvider {

    override fun isUseTimeZoneHandling(): Boolean {
        return false
    }

    override fun isAcceptCookieTimeZone(): Boolean {
        return false
    }

    override fun findBusinessTimeZone(runtimeMeta: ActionRuntime, requestManager: RequestManager): OptionalThing<TimeZone> {
        return OptionalObject.empty()
    }

    override fun getRequestedTimeZone(requestManager: RequestManager): TimeZone { // not null
        return centralTimeZone // you can change it if you like
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(DfTypeUtil.toClassTitle(this))
        sb.append(":{useTimeZoneHandling=").append(isUseTimeZoneHandling)
        sb.append(", acceptCookieTimeZone=").append(isAcceptCookieTimeZone)
        sb.append("}@").append(Integer.toHexString(hashCode()))
        return sb.toString()
    }

    companion object {

        val centralTimeZone = TimeZone.getDefault()!!
    }
}
