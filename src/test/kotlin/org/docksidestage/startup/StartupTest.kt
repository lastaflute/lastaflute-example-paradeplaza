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
package org.docksidestage.startup

import org.dbflute.infra.manage.refresh.DfRefreshResourceRequest
import org.docksidestage.app.logic.startup.StartupLogic
import org.docksidestage.unit.UnitHarborTestCase
import java.io.IOException
import java.util.*
import javax.annotation.Resource

/**
 * @author jflute
 */
class StartupTest : UnitHarborTestCase() {

    @Resource
    private lateinit var startupLogic: StartupLogic

    @Throws(Exception::class)
    fun test_startup_actually() {
        val domain = "dancingdb.org"
        val serviceName = "mythica"
        startupLogic.fromHarbor(projectDir, domain, serviceName)
        refresh(serviceName) // for retry
    }

    protected fun refresh(serviceName: String) {
        try {
            DfRefreshResourceRequest(Arrays.asList(serviceName), "http://localhost:8386/").refreshResources()
        } catch (continued: IOException) {
            log("*Cannot refresh for Eclipse, but no problem so continue: " + continued.message)
        } catch (continued: RuntimeException) {
            log("*Cannot refresh for Eclipse, but no problem so continue: " + continued.message)
        }

    }
}
