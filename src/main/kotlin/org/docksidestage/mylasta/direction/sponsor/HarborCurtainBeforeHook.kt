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
package org.docksidestage.mylasta.direction.sponsor

import java.util.TimeZone

import org.dbflute.system.DBFluteSystem
import org.dbflute.system.provider.DfFinalTimeZoneProvider
import org.dbflute.util.DfTypeUtil
import org.lastaflute.core.direction.CurtainBeforeHook
import org.lastaflute.core.direction.FwAssistantDirector

/**
 * @author jflute
 */
class HarborCurtainBeforeHook : CurtainBeforeHook {

    override fun hook(assistantDirector: FwAssistantDirector) {
        processDBFluteSystem()
    }

    protected fun processDBFluteSystem() {
        DBFluteSystem.unlock()
        DBFluteSystem.setFinalTimeZoneProvider(createFinalTimeZoneProvider())
        DBFluteSystem.lock()
    }

    protected fun createFinalTimeZoneProvider(): DfFinalTimeZoneProvider {
        return object : DfFinalTimeZoneProvider {
            protected val provided = HarborUserTimeZoneProcessProvider.centralTimeZone

            override fun provide(): TimeZone {
                return provided
            }

            override fun toString(): String {
                return DfTypeUtil.toClassTitle(this) + ":{" + provided.id + "}"
            }
        }
    }
}
