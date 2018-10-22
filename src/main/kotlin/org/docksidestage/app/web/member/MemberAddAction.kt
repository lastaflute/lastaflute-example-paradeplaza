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
package org.docksidestage.app.web.member

import org.docksidestage.app.web.base.HarborBaseAction
import org.docksidestage.dbflute.allcommon.CDef
import org.docksidestage.dbflute.exbhv.MemberBhv
import org.docksidestage.dbflute.exentity.Member
import org.docksidestage.mylasta.action.HarborHtmlPath
import org.lastaflute.core.time.TimeManager
import org.lastaflute.web.Execute
import org.lastaflute.web.response.HtmlResponse
import javax.annotation.Resource

/**
 * @author jflute
 */
class MemberAddAction : HarborBaseAction() {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private lateinit var timeManager: TimeManager
    @Resource
    private lateinit var memberBhv: MemberBhv

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Execute
    fun index(): HtmlResponse {
        saveToken()
        return asHtml(HarborHtmlPath.path_Member_MemberAddHtml).useForm(MemberAddForm::class.java) { op ->
            op.setup { form ->
                form.memberStatus = CDef.MemberStatus.Provisional // as default
            }
        }
    }

    @Execute
    fun register(form: MemberAddForm): HtmlResponse {
        validate(form, { messages -> }, { asHtml(HarborHtmlPath.path_Member_MemberAddHtml) })
        verifyToken { asHtml(HarborHtmlPath.path_Error_ShowErrorsHtml) }
        insertMember(form)
        return redirect(MemberListAction::class.java)
    }

    // ===================================================================================
    //                                                                              Update
    //                                                                              ======
    private fun insertMember(form: MemberAddForm) {
        val member = Member()
        member.memberName = form.memberName
        member.memberAccount = form.memberAccount
        member.birthdate = form.birthdate
        member.memberStatusCodeAsMemberStatus = form.memberStatus
        if (member.isMemberStatusCodeFormalized) {
            member.formalizedDatetime = timeManager!!.currentDateTime()
        }
        memberBhv!!.insert(member)
    }
}
