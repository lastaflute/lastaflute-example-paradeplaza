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
package org.docksidestage.app.web.signup

import org.docksidestage.app.web.base.ParadeplazaBaseAction
import org.docksidestage.app.web.base.login.ParadeplazaLoginAssist
import org.docksidestage.app.web.mypage.MypageAction
import org.docksidestage.app.web.signin.SigninAction
import org.docksidestage.dbflute.exbhv.MemberBhv
import org.docksidestage.dbflute.exbhv.MemberSecurityBhv
import org.docksidestage.dbflute.exbhv.MemberServiceBhv
import org.docksidestage.dbflute.exentity.Member
import org.docksidestage.dbflute.exentity.MemberSecurity
import org.docksidestage.dbflute.exentity.MemberService
import org.docksidestage.mylasta.action.ParadeplazaHtmlPath
import org.docksidestage.mylasta.action.ParadeplazaMessages
import org.docksidestage.mylasta.direction.ParadeplazaConfig
import org.docksidestage.mylasta.mail.member.WelcomeMemberPostcard
import org.lastaflute.core.mail.Postbox
import org.lastaflute.core.util.LaStringUtil
import org.lastaflute.web.Execute
import org.lastaflute.web.login.AllowAnyoneAccess
import org.lastaflute.web.response.HtmlResponse
import javax.annotation.Resource

/**
 * @author annie_pocket
 * @author jflute
 */
@AllowAnyoneAccess
class SignupAction : ParadeplazaBaseAction() {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private lateinit var postbox: Postbox
    @Resource
    private lateinit var config: ParadeplazaConfig
    @Resource
    private lateinit var memberBhv: MemberBhv
    @Resource
    private lateinit var memberSecurityBhv: MemberSecurityBhv
    @Resource
    private lateinit var memberServiceBhv: MemberServiceBhv
    @Resource
    private lateinit var loginAssist: ParadeplazaLoginAssist
    @Resource
    private lateinit var signupTokenAssist: SignupTokenAssist

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Execute
    fun index(): HtmlResponse {
        return asHtml(ParadeplazaHtmlPath.path_Signup_SignupHtml).useForm(SignupForm::class.java)
    }

    @Execute
    fun signup(form: SignupForm): HtmlResponse {
        validate(form, { messages -> moreValidate(form, messages) }, { asHtml(ParadeplazaHtmlPath.path_Signup_SignupHtml) })
        val member = insertProvisionalMember(form)
        val token = signupTokenAssist.saveSignupToken(member)
        sendSignupMail(form, token)
        return redirect(MypageAction::class.java).afterTxCommit {
            // for asynchronous DB access
            loginAssist.identityLogin(member.memberId) {} // #simple_for_example no remember for now
        }
    }

    private fun moreValidate(form: SignupForm, messages: ParadeplazaMessages) {
        if (LaStringUtil.isNotEmpty(form.memberAccount)) {
            val count = memberBhv.selectCount { cb -> cb.query().setMemberAccount_Equal(form.memberAccount) }
            if (count > 0) {
                messages.addErrorsSignupAccountAlreadyExists("memberAccount")
            }
        }
    }

    private fun sendSignupMail(form: SignupForm, token: String) {
        WelcomeMemberPostcard.droppedInto(postbox) { postcard ->
            postcard.setFrom(config.mailAddressSupport, ParadeplazaMessages.LABELS_MAIL_SUPPORT_PERSONAL)
            postcard.addTo(form.memberAccount!! + "@docksidestage.org") // #simple_for_example
            postcard.setDomain(config.serverDomain)
            postcard.setMemberName(form.memberName)
            postcard.setAccount(form.memberAccount)
            postcard.setToken(token)
            postcard.async()
            postcard.retry(3, 1000L)
            postcard.writeAuthor(this)
        }
    }

    @Execute
    fun register(account: String, token: String): HtmlResponse { // from mail link
        signupTokenAssist.verifySignupTokenMatched(account, token)
        updateMemberAsFormalized(account)
        return redirect(SigninAction::class.java)
    }

    // ===================================================================================
    //                                                                              Update
    //                                                                              ======
    private fun insertProvisionalMember(form: SignupForm): Member {
        val member = Member()
        member.memberName = form.memberName
        member.memberAccount = form.memberAccount
        member.setMemberStatusCode_Provisional()
        memberBhv.insert(member) // #simple_for_example same-name concurrent access as application exception

        val security = MemberSecurity()
        security.memberId = member.memberId
        security.loginPassword = loginAssist.encryptPassword(form.password)
        security.reminderQuestion = form.reminderQuestion
        security.reminderAnswer = form.reminderAnswer
        security.reminderUseCount = 0
        memberSecurityBhv.insert(security)

        val service = MemberService()
        service.memberId = member.memberId
        service.servicePointCount = 0
        service.setServiceRankCode_Plastic()
        memberServiceBhv.insert(service)
        return member
    }

    private fun updateMemberAsFormalized(account: String) {
        val member = Member()
        member.uniqueBy(account)
        member.setMemberStatusCode_Formalized()
        memberBhv.updateNonstrict(member)
    }
}
