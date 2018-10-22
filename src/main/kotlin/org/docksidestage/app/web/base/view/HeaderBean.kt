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
package org.docksidestage.app.web.base.view

import org.docksidestage.mylasta.action.HarborUserBean

/**
 * @author jflute
 */
class HeaderBean { // #change_it #delete_ifapi

    val memberId: Int?
    val memberName: String?
    @JvmField // TODO remove @JvmField
    val isLogin: Boolean

    private constructor() {
        this.memberId = null
        this.memberName = null
        this.isLogin = false
    }

    constructor(userBean: HarborUserBean) {
        this.memberId = userBean.memberId
        this.memberName = userBean.memberName
        this.isLogin = true
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("{").append(memberId)
        sb.append(",").append(memberName)
        if (isLogin) {
            sb.append(", isLogin")
        }
        sb.append("}")
        return sb.toString()
    }

    companion object {

        private val EMPTY_INSTANCE = HeaderBean()

        fun empty(): HeaderBean {
            return EMPTY_INSTANCE
        }
    }
}
