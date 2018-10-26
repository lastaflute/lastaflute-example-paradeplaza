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
package org.docksidestage.app.web.base.login

import org.dbflute.optional.OptionalEntity
import org.dbflute.optional.OptionalThing
import org.docksidestage.app.web.signin.SigninAction
import org.docksidestage.dbflute.cbean.MemberCB
import org.docksidestage.dbflute.exbhv.MemberBhv
import org.docksidestage.dbflute.exbhv.MemberLoginBhv
import org.docksidestage.dbflute.exentity.Member
import org.docksidestage.dbflute.exentity.MemberLogin
import org.docksidestage.mylasta.action.ParadeplazaUserBean
import org.docksidestage.mylasta.direction.ParadeplazaConfig
import org.lastaflute.core.magic.async.AsyncManager
import org.lastaflute.core.time.TimeManager
import org.lastaflute.db.jta.stage.TransactionStage
import org.lastaflute.web.login.PrimaryLoginManager
import org.lastaflute.web.login.TypicalLoginAssist
import org.lastaflute.web.login.credential.UserPasswordCredential
import org.lastaflute.web.login.option.LoginSpecifiedOption
import javax.annotation.Resource

/**
 * @author jflute
 */
class ParadeplazaLoginAssist : TypicalLoginAssist<Int, ParadeplazaUserBean, Member> // #change_it also UserBean
(), PrimaryLoginManager { // #app_customize

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private lateinit var timeManager: TimeManager
    @Resource
    private lateinit var asyncManager: AsyncManager
    @Resource
    private lateinit var transactionStage: TransactionStage
    @Resource
    private lateinit var config: ParadeplazaConfig
    @Resource
    private lateinit var memberBhv: MemberBhv
    @Resource
    private lateinit var memberLoginBhv: MemberLoginBhv

    // ===================================================================================
    //                                                                           Find User
    //                                                                           =========
    override fun checkCredential(checker: CredentialChecker) {
        checker.check(UserPasswordCredential::class.java) { credential -> memberBhv.selectCount { cb -> arrangeLoginByCredential(cb, credential) } > 0 }
    }

    override fun resolveCredential(resolver: CredentialResolver) {
        resolver.resolve(UserPasswordCredential::class.java) { credential -> memberBhv.selectEntity { cb -> arrangeLoginByCredential(cb, credential) } }
    }

    private fun arrangeLoginByCredential(cb: MemberCB, credential: UserPasswordCredential) {
        cb.query().arrangeLogin(credential.user, encryptPassword(credential.password))
    }

    override fun doFindLoginUser(userId: Int?): OptionalEntity<Member> {
        return memberBhv.selectEntity { cb -> cb.query().arrangeLoginByIdentity(userId) }
    }

    // ===================================================================================
    //                                                                       Login Process
    //                                                                       =============
    override fun createUserBean(userEntity: Member): ParadeplazaUserBean {
        return ParadeplazaUserBean(userEntity)
    }

    override fun getCookieRememberMeKey(): OptionalThing<String> {
        return OptionalThing.of(config.cookieRememberMeParadeplazaKey)
    }

    override fun toTypedUserId(userKey: String): Int? {
        return Integer.valueOf(userKey)
    }

    override fun saveLoginHistory(member: Member, userBean: ParadeplazaUserBean, option: LoginSpecifiedOption) {
        asyncManager.async { transactionStage.requiresNew<Any> { tx -> insertLogin(member) } }
    }

    protected fun insertLogin(member: Member) {
        val login = MemberLogin()
        login.memberId = member.memberId
        login.loginMemberStatusCodeAsMemberStatus = member.memberStatusCodeAsMemberStatus
        login.loginDatetime = timeManager.currentDateTime()
        login.setMobileLoginFlg_False() // mobile unsupported for now
        memberLoginBhv.insert(login)
    }

    // ===================================================================================
    //                                                                      Login Resource
    //                                                                      ==============
    override fun getUserBeanType(): Class<ParadeplazaUserBean> {
        return ParadeplazaUserBean::class.java
    }

    override fun getLoginActionType(): Class<*> {
        return SigninAction::class.java
    }
}
