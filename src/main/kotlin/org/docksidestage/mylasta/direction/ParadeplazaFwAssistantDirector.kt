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
package org.docksidestage.mylasta.direction

import org.docksidestage.mylasta.direction.sponsor.*
import org.lastaflute.core.direction.CachedFwAssistantDirector
import org.lastaflute.core.direction.CurtainBeforeHook
import org.lastaflute.core.direction.FwAssistDirection
import org.lastaflute.core.direction.FwCoreDirection
import org.lastaflute.core.json.JsonResourceProvider
import org.lastaflute.core.security.InvertibleCryptographer
import org.lastaflute.core.security.OneWayCryptographer
import org.lastaflute.core.security.SecurityResourceProvider
import org.lastaflute.core.time.TimeResourceProvider
import org.lastaflute.db.dbflute.classification.ListedClassificationProvider
import org.lastaflute.db.direction.FwDbDirection
import org.lastaflute.thymeleaf.ThymeleafRenderingProvider
import org.lastaflute.web.api.ApiFailureHook
import org.lastaflute.web.direction.FwWebDirection
import org.lastaflute.web.path.ActionAdjustmentProvider
import org.lastaflute.web.ruts.multipart.MultipartResourceProvider
import org.lastaflute.web.ruts.renderer.HtmlRenderingProvider
import org.lastaflute.web.servlet.cookie.CookieResourceProvider
import org.lastaflute.web.servlet.filter.cors.CorsHook
import org.lastaflute.web.servlet.request.UserLocaleProcessProvider
import org.lastaflute.web.servlet.request.UserTimeZoneProcessProvider
import java.util.function.Consumer
import javax.annotation.Resource

/**
 * @author jflute
 */
class ParadeplazaFwAssistantDirector : CachedFwAssistantDirector() {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private lateinit var config: ParadeplazaConfig

    // ===================================================================================
    //                                                                              Assist
    //                                                                              ======
    override fun prepareAssistDirection(direction: FwAssistDirection) {
        direction.directConfig(Consumer { nameList -> nameList.add("paradeplaza_config.properties") }, "paradeplaza_env.properties")
    }

    // ===================================================================================
    //                                                                               Core
    //                                                                              ======
    override fun prepareCoreDirection(direction: FwCoreDirection) {
        // this configuration is on paradeplaza_env.properties because this is true only when development
        direction.directDevelopmentHere(config.isDevelopmentHere)

        // titles of the application for logging are from configurations
        direction.directLoggingTitle(config.domainTitle, config.environmentTitle)

        // this configuration is on sea_env.properties because it has no influence to production
        // even if you set true manually and forget to set false back
        direction.directFrameworkDebug(config.isFrameworkDebug) // basically false

        // you can add your own process when your application is booting
        direction.directCurtainBefore(createCurtainBeforeHook())

        direction.directSecurity(createSecurityResourceProvider())
        direction.directTime(createTimeResourceProvider())
        direction.directJson(createJsonResourceProvider())
        direction.directMail(createMailDeliveryDepartmentCreator().create())
    }

    protected fun createCurtainBeforeHook(): CurtainBeforeHook {
        return ParadeplazaCurtainBeforeHook()
    }

    protected fun createSecurityResourceProvider(): SecurityResourceProvider { // #change_it_first
        val inver = InvertibleCryptographer.createAesCipher("parade:dockside:")
        val oneWay = OneWayCryptographer.createSha256Cryptographer()
        return ParadeplazaSecurityResourceProvider(inver, oneWay)
    }

    protected fun createTimeResourceProvider(): TimeResourceProvider {
        return ParadeplazaTimeResourceProvider(config)
    }

    protected fun createJsonResourceProvider(): JsonResourceProvider {
        return ParadeplazaJsonResourceProvider()
    }

    protected fun createMailDeliveryDepartmentCreator(): ParadeplazaMailDeliveryDepartmentCreator {
        return ParadeplazaMailDeliveryDepartmentCreator(config)
    }

    // ===================================================================================
    //                                                                                 DB
    //                                                                                ====
    override fun prepareDbDirection(direction: FwDbDirection) {
        direction.directClassification(createListedClassificationProvider())
    }

    protected fun createListedClassificationProvider(): ListedClassificationProvider {
        return ParadeplazaListedClassificationProvider()
    }

    // ===================================================================================
    //                                                                                Web
    //                                                                               =====
    override fun prepareWebDirection(direction: FwWebDirection) {
        direction.directRequest(createUserLocaleProcessProvider(), createUserTimeZoneProcessProvider())
        direction.directCookie(createCookieResourceProvider())
        direction.directAdjustment(createActionAdjustmentProvider())
        direction.directMessage(Consumer { nameList -> nameList.add("paradeplaza_message") }, "paradeplaza_label")
        direction.directApiCall(createApiFailureHook())
        direction.directCors(CorsHook("http://localhost:5000")) // #change_it
        direction.directHtmlRendering(createHtmlRenderingProvider())
        direction.directMultipart(createMultipartResourceProvider())
    }

    protected fun createUserLocaleProcessProvider(): UserLocaleProcessProvider {
        return ParadeplazaUserLocaleProcessProvider()
    }

    protected fun createUserTimeZoneProcessProvider(): UserTimeZoneProcessProvider {
        return ParadeplazaUserTimeZoneProcessProvider()
    }

    protected fun createCookieResourceProvider(): CookieResourceProvider { // #change_it_first
        val cr = InvertibleCryptographer.createAesCipher("dockside:parade:")
        return ParadeplazaCookieResourceProvider(config, cr)
    }

    protected fun createActionAdjustmentProvider(): ActionAdjustmentProvider {
        return ParadeplazaActionAdjustmentProvider()
    }

    protected fun createApiFailureHook(): ApiFailureHook {
        return ParadeplazaApiFailureHook()
    }

    protected fun createHtmlRenderingProvider(): HtmlRenderingProvider {
        return ThymeleafRenderingProvider().asDevelopment(config.isDevelopmentHere)
    }

    protected fun createMultipartResourceProvider(): MultipartResourceProvider {
        return MultipartResourceProvider { ParadeplazaMultipartRequestHandler() }
    }
}
