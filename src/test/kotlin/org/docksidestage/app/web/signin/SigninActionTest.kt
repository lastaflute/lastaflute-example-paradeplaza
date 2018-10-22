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
package org.docksidestage.app.web.signin

import org.docksidestage.app.web.base.login.HarborLoginAssist
import org.docksidestage.app.web.mypage.MypageAction
import org.docksidestage.dbflute.exbhv.MemberLoginBhv
import org.docksidestage.mylasta.action.HarborHtmlPath
import org.docksidestage.mylasta.action.HarborMessages
import org.docksidestage.unit.UnitHarborTestCase
import org.junit.Assert
import org.lastaflute.core.time.TimeManager
import org.lastaflute.web.validation.Required
import javax.annotation.Resource

/**
 * @author jflute
 */
class SigninActionTest : UnitHarborTestCase() {

    @Resource
    private val timeManager: TimeManager? = null
    @Resource
    private val memberLoginBhv: MemberLoginBhv? = null
    @Resource
    private val loginAssist: HarborLoginAssist? = null

    // ===================================================================================
    //                                                                               Basic
    //                                                                               =====
    fun test_signin_basic() {
        // ## Arrange ##
        changeAsyncToNormalSync()
        changeRequiresNewToRequired()
        val action = SigninAction()
        inject(action)
        val form = SigninForm()
        form.account = "Pixy"
        form.password = "sea"

        // ## Act ##
        val response = action.signin(form)

        // ## Assert ##
        val htmlData = validateHtmlData(response)
        htmlData.assertRedirect(MypageAction::class.java)

        val userBean = loginAssist!!.savedUserBean.get()
        log(userBean)
        Assert.assertEquals(form.account, userBean.memberAccount)
        Assert.assertEquals(1, memberLoginBhv!!.selectCount { cb ->
            cb.query().setLoginDatetime_Equal(timeManager!!.currentDateTime()) // transaction time
        })
    }

    // ===================================================================================
    //                                                                          Validation
    //                                                                          ==========
    fun test_signin_validationError_required() {
        // ## Arrange ##
        val action = SigninAction()
        inject(action)
        val form = SigninForm()

        // ## Act ##
        // ## Assert ##
        assertValidationError { action.signin(form) }.handle { data ->
            data.requiredMessageOf("account", Required::class.java)
            data.requiredMessageOf("password", Required::class.java)
            val htmlData = validateHtmlData(data.hookError())
            htmlData.assertHtmlForward(HarborHtmlPath.path_Signin_SigninHtml)
        }
    }

    fun test_signin_validationError_loginFailure() {
        // ## Arrange ##
        val action = SigninAction()
        inject(action)
        val form = SigninForm()
        form.account = "Pixy"
        form.password = "land"

        // ## Act ##
        // ## Assert ##
        assertValidationError { action.signin(form) }.handle { data ->
            data.requiredMessageOf("account", HarborMessages.ERRORS_LOGIN_FAILURE)
            val htmlData = validateHtmlData(data.hookError())
            htmlData.assertHtmlForward(HarborHtmlPath.path_Signin_SigninHtml)
            Assert.assertNull(form.password) // should cleared for security
        }
    }
}
