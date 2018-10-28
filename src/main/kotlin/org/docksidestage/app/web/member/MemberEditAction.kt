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

import org.docksidestage.app.web.base.ParadeplazaBaseAction
import org.docksidestage.dbflute.exbhv.MemberBhv
import org.docksidestage.dbflute.exentity.Member
import org.docksidestage.mylasta.action.ParadeplazaHtmlPath
import org.lastaflute.core.time.TimeManager
import org.lastaflute.web.Execute
import org.lastaflute.web.response.HtmlResponse
import javax.annotation.Resource

/**
 * @author jflute
 */
class MemberEditAction : ParadeplazaBaseAction() {

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
    fun index(memberId: Int?): HtmlResponse {
        val member = selectMember(memberId)
        saveToken()
        return asHtml(ParadeplazaHtmlPath.path_Member_MemberEditHtml).useForm(MemberEditForm::class.java) { op -> op.setup { form -> mappingToForm(member, form) } }
    }

    @Execute
    fun update(form: MemberEditForm): HtmlResponse {
        validate(form, { _ -> }, { asHtml(ParadeplazaHtmlPath.path_Member_MemberEditHtml) })
        verifyToken { asHtml(ParadeplazaHtmlPath.path_Error_ShowErrorsHtml) }
        val member = updateMember(form)
        return redirectById(MemberEditAction::class.java, member.memberId)
    }

    @Execute
    fun withdrawal(form: MemberEditForm): HtmlResponse {
        validate(form, { _ -> }, { asHtml(ParadeplazaHtmlPath.path_Member_MemberEditHtml) })
        updateMemberToWithdrawal(form)
        return redirect(MemberListAction::class.java)
    }

    // ===================================================================================
    //                                                                              Select
    //                                                                              ======
    private fun selectMember(memberId: Int?): Member {
        return memberBhv.selectEntity { cb ->
            cb.specify().derivedMemberLogin().max({ loginCB -> loginCB.specify().columnLoginDatetime() }, Member.ALIAS_latestLoginDatetime)
            cb.query().setMemberId_Equal(memberId)
            cb.query().setMemberStatusCode_InScope_ServiceAvailable()
        }.get() // automatically exclusive controlled if not found
    }

    // ===================================================================================
    //                                                                              Update
    //                                                                              ======
    private fun updateMember(form: MemberEditForm): Member {
        val member = Member()
        member.memberId = form.memberId
        member.memberName = form.memberName
        member.birthdate = form.birthdate // may be updated as null
        member.memberStatusCodeAsMemberStatus = form.memberStatus
        member.memberAccount = form.memberAccount
        if (member.isMemberStatusCodeFormalized) {
            if (form.previousStatus!!.isShortOfFormalized) {
                member.formalizedDatetime = timeManager.currentDateTime()
            }
        } else if (member.isMemberStatusCode_ShortOfFormalized) {
            member.formalizedDatetime = null
        }
        member.versionNo = form.versionNo
        memberBhv.update(member)
        return member
    }

    private fun updateMemberToWithdrawal(form: MemberEditForm) {
        val member = Member()
        member.memberId = form.memberId
        member.setMemberStatusCode_Withdrawal()
        member.versionNo = form.versionNo
        memberBhv.update(member)
    }

    // ===================================================================================
    //                                                                             Mapping
    //                                                                             =======
    private fun mappingToForm(member: Member, form: MemberEditForm) {
        form.memberId = member.memberId
        form.memberName = member.memberName
        form.memberAccount = member.memberAccount
        form.memberStatus = member.memberStatusCodeAsMemberStatus
        form.birthdate = member.birthdate
        val formalizedDatetime = member.formalizedDatetime
        if (formalizedDatetime != null) {
            form.formalizedDate = formalizedDatetime.toLocalDate()
        }
        form.latestLoginDatetime = member.latestLoginDatetime
        form.updateDatetime = member.updateDatetime
        form.previousStatus = member.memberStatusCodeAsMemberStatus // to determine new formalized member
        form.versionNo = member.versionNo
    }
}
