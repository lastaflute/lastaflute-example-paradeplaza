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
package org.docksidestage.app.web.base

import org.dbflute.optional.OptionalThing
import org.docksidestage.app.logic.context.AccessContextLogic
import org.docksidestage.app.web.base.login.HarborLoginAssist
import org.docksidestage.app.web.base.view.HeaderBean
import org.docksidestage.mylasta.action.HarborHtmlPath
import org.docksidestage.mylasta.action.HarborMessages
import org.docksidestage.mylasta.action.HarborUserBean
import org.lastaflute.db.dbflute.accesscontext.AccessContextArranger
import org.lastaflute.web.TypicalAction
import org.lastaflute.web.login.LoginManager
import org.lastaflute.web.response.ActionResponse
import org.lastaflute.web.ruts.process.ActionRuntime
import org.lastaflute.web.servlet.request.RequestManager
import org.lastaflute.web.token.DoubleSubmitManager
import org.lastaflute.web.validation.ActionValidator
import org.lastaflute.web.validation.LaValidatable
import javax.annotation.Resource

/**
 * @author jflute
 */
abstract class HarborBaseAction : TypicalAction // has several interfaces for direct use
(), LaValidatable<HarborMessages>, HarborHtmlPath {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private lateinit var requestManager: RequestManager
    @Resource
    private lateinit var doubleSubmitManager: DoubleSubmitManager
    @Resource
    private lateinit var accessContextLogic: AccessContextLogic
    @Resource
    private lateinit var loginAssist: HarborLoginAssist

    // ===================================================================================
    //                                                                               Hook
    //                                                                              ======
    // to suppress unexpected override by sub-class
    override fun godHandPrologue(runtime: ActionRuntime): ActionResponse {
        return super.godHandPrologue(runtime)
    }

    override fun godHandMonologue(runtime: ActionRuntime): ActionResponse {
        return super.godHandMonologue(runtime)
    }

    override fun godHandEpilogue(runtime: ActionRuntime) {
        super.godHandEpilogue(runtime)
    }

    // #app_customize you can customize the action hook
    override fun hookBefore(runtime: ActionRuntime?): ActionResponse { // application may override
        return super.hookBefore(runtime)
    }

    override fun hookFinally(runtime: ActionRuntime?) { // application may override
        if (runtime!!.isForwardToHtml) { // #delete_ifapi
            runtime.registerData("headerBean", userBean.map { userBean -> HeaderBean(userBean) }.orElse(HeaderBean.empty()))
        }
        super.hookFinally(runtime)
    }

    // ===================================================================================
    //                                                                      Access Context
    //                                                                      ==============
    override fun newAccessContextArranger(): AccessContextArranger { // for framework
        return AccessContextArranger { resource ->
            accessContextLogic.create(resource, object : AccessContextLogic.UserTypeSupplier {
                override fun supply(): OptionalThing<String> {
                    return myUserType()
                }
            }, object : AccessContextLogic.UserInfoSupplier {
                override fun supply(): OptionalThing<Any> {
                    return userBean.map { userBean ->
                        userBean.userId // as user expression
                    }
                }
            }, object : AccessContextLogic.AppTypeSupplier {
                override fun supply(): String {
                    return myAppType()
                }
            }, object : AccessContextLogic.ClientInfoSupplier {
                override fun supply(): OptionalThing<String> {
                    return requestManager.headerUserAgent
                }
            })
        }
    }

    // ===================================================================================
    //                                                                           User Info
    //                                                                           =========
    // -----------------------------------------------------
    //                                      Application Info
    //                                      ----------------
    override fun myAppType(): String { // for framework
        return APP_TYPE
    }

    // -----------------------------------------------------
    //                                            Login Info
    //                                            ----------
    // #app_customize return empty if isLogin is unused
    override fun getUserBean(): OptionalThing<HarborUserBean> { // application may call, overriding for co-variant
        return loginAssist.savedUserBean
    }

    override fun myUserType(): OptionalThing<String> { // for framework
        return OptionalThing.of(USER_TYPE)
    }

    override fun myLoginManager(): OptionalThing<LoginManager> { // for framework
        return OptionalThing.of(loginAssist)
    }

    // ===================================================================================
    //                                                                          Validation
    //                                                                          ==========
    @Suppress("CONFLICTING_OVERLOADS")
    override fun createValidator(): ActionValidator<HarborMessages> { // for co-variant
        return super.createValidator()
    }

    override fun createMessages(): HarborMessages { // application may call
        return HarborMessages() // overriding to change return type to concrete-class
    }

    companion object {

        // ===================================================================================
        //                                                                          Definition
        //                                                                          ==========
        /** The application type for HarBoR, e.g. used by access context.  */
        protected const val APP_TYPE = "HBR" // #change_it_first

        /** The user type for Member, e.g. used by access context.  */
        protected const val USER_TYPE = "M" // #change_it_first (can delete if no isLogin)
    }

    // ===================================================================================
    //                                                                            Document
    //                                                                            ========
    // #app_customize you should override javadoc when you add new methods for sub class at super class.
    ///**
    // * {@inheritDoc} <br>
    // * Application Native Methods:
    // * <pre>
    // * <span style="font-size: 130%; color: #553000">[xxx]</span>
    // * o xxx() <span style="color: #3F7E5E">// xxx</span>
    // * </pre>
    // */
    //    override fun document1_CallableSuperMethod: Unit {
    //        super.document1_CallableSuperMethod()
    //    }
}
