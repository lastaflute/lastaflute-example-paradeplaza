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
package org.docksidestage.app.web.lido.auth

import org.docksidestage.app.web.base.ParadeplazaBaseAction
import org.docksidestage.app.web.base.login.ParadeplazaLoginAssist
import org.docksidestage.dbflute.exbhv.MemberBhv
import org.docksidestage.dbflute.exbhv.MemberSecurityBhv
import org.docksidestage.dbflute.exbhv.MemberServiceBhv
import org.docksidestage.dbflute.exentity.Member
import org.docksidestage.dbflute.exentity.MemberSecurity
import org.docksidestage.dbflute.exentity.MemberService
import org.docksidestage.mylasta.action.ParadeplazaMessages
import org.docksidestage.mylasta.direction.ParadeplazaConfig
import org.docksidestage.mylasta.mail.member.WelcomeMemberPostcard
import org.lastaflute.core.mail.Postbox
import org.lastaflute.core.security.PrimaryCipher
import org.lastaflute.core.util.LaStringUtil
import org.lastaflute.web.Execute
import org.lastaflute.web.login.AllowAnyoneAccess
import org.lastaflute.web.login.credential.UserPasswordCredential
import org.lastaflute.web.response.JsonResponse
import org.lastaflute.web.servlet.request.ResponseManager
import org.lastaflute.web.servlet.session.SessionManager
import java.util.*
import javax.annotation.Resource

// the 'lido' package is example for JSON API in simple project
// client application is riot.js in lidoisle directory
/**
 * @author s.tadokoro
 * @author jflute
 */
class LidoAuthAction : ParadeplazaBaseAction() {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private lateinit var primaryCipher: PrimaryCipher
    @Resource
    private lateinit var postbox: Postbox
    @Resource
    private lateinit var responseManager: ResponseManager
    @Resource
    private lateinit var sessionManager: SessionManager
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

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    // -----------------------------------------------------
    //                                           Sign in/out
    //                                           -----------
    @Execute
    @AllowAnyoneAccess
    fun signin(body: SigninBody): JsonResponse<Void> {
        validateApi(body) { _ -> }
        loginAssist.login(UserPasswordCredential(body.account!!, body.password!!)) { _ -> }
        return JsonResponse.asEmptyBody()
    }

    @Execute
    fun signout(): JsonResponse<Void> {
        loginAssist.logout()
        return JsonResponse.asEmptyBody()
    }

    // -----------------------------------------------------
    //                                               Sign up
    //                                               -------
    @Execute
    fun signup(body: SignupBody): JsonResponse<Void> {
        validateApi(body) { messages -> moreValidate(body, messages) }
        val memberId = newMember(body)
        loginAssist.identityLogin(memberId) { _ -> } // no remember-me here

        val signupToken = saveSignupToken()
        sendSignupMail(body, signupToken)
        return JsonResponse.asEmptyBody()
    }

    private fun moreValidate(body: SignupBody, messages: ParadeplazaMessages) {
        if (LaStringUtil.isNotEmpty(body.memberAccount)) {
            val count = memberBhv.selectCount { cb -> cb.query().setMemberAccount_Equal(body.memberAccount) }
            if (count > 0) {
                messages.addErrorsSignupAccountAlreadyExists("memberAccount")
            }
        }
    }

    private fun saveSignupToken(): String {
        val token = primaryCipher.encrypt(Random().nextInt().toString()) // #simple_for_example
        sessionManager.setAttribute(SIGNUP_TOKEN_KEY, token)
        return token
    }

    private fun sendSignupMail(body: SignupBody, signupToken: String) {
        WelcomeMemberPostcard.droppedInto(postbox) { postcard ->
            postcard.setFrom(config.mailAddressSupport, "Paradeplaza Support") // #simple_for_example
            postcard.addTo(body.memberAccount!! + "@docksidestage.org") // #simple_for_example
            postcard.setDomain(config.serverDomain)
            postcard.setMemberName(body.memberName)
            postcard.setAccount(body.memberAccount)
            postcard.setToken(signupToken)
        }
    }

    @Execute
    fun register(account: String, token: String): JsonResponse<Void> { // from mail link
        verifySignupTokenMatched(account, token)
        updateStatusFormalized(account)
        return JsonResponse.asEmptyBody()
    }

    private fun verifySignupTokenMatched(account: String, token: String) {
        val saved = sessionManager.getAttribute(SIGNUP_TOKEN_KEY, String::class.java).orElseTranslatingThrow<Throwable, RuntimeException> { cause -> responseManager.new404("Not found the signupToken in session: $account") { op -> op.cause(cause) } }
        if (saved != token) {
            throw responseManager.new404("Unmatched signupToken in session: saved=$saved, requested=$token")
        }
    }

    // ===================================================================================
    //                                                                              Update
    //                                                                              ======
    private fun newMember(body: SignupBody): Int? {
        val member = Member()
        member.memberAccount = body.memberAccount
        member.memberName = body.memberName
        member.setMemberStatusCode_Provisional()
        memberBhv.insert(member) // #simple_for_example same-name concurrent access as application exception

        val security = MemberSecurity()
        security.memberId = member.memberId
        security.loginPassword = loginAssist.encryptPassword(body.password)
        security.reminderQuestion = body.reminderQuestion
        security.reminderAnswer = body.reminderAnswer
        security.reminderUseCount = 0
        memberSecurityBhv.insert(security)

        val service = MemberService()
        service.memberId = member.memberId
        service.servicePointCount = 0
        service.setServiceRankCode_Plastic()
        memberServiceBhv.insert(service)
        return member.memberId
    }

    private fun updateStatusFormalized(account: String) {
        val member = Member()
        member.memberAccount = account
        member.setMemberStatusCode_Formalized()
        memberBhv.updateNonstrict(member)
    }

    companion object {

        // ===================================================================================
        //                                                                          Definition
        //                                                                          ==========
        private const val SIGNUP_TOKEN_KEY = "signupToken"
    }
}