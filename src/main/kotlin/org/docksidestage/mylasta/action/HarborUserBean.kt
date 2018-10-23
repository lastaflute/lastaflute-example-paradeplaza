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
package org.docksidestage.mylasta.action

import org.docksidestage.dbflute.exentity.Member
import org.lastaflute.web.login.TypicalUserBean

/**
 * @author jflute
 */
class HarborUserBean// ===================================================================================
//                                                                         Constructor
//                                                                         ===========
(member: Member) : TypicalUserBean<Int>() { // #change_it also LoginAssist

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    val memberId: Int?
    val memberName: String
    val memberAccount: String

    init {
        memberId = member.memberId
        memberName = member.memberName
        memberAccount = member.memberAccount
    }

    // ===================================================================================
    //                                                                      Implementation
    //                                                                      ==============
    override fun getUserId(): Int? {
        return memberId
    }

    companion object {

        // ===================================================================================
        //                                                                          Definition
        //                                                                          ==========
        /** The serial version UID for object serialization. (Default)  */
        private val serialVersionUID = 1L
    }
}
