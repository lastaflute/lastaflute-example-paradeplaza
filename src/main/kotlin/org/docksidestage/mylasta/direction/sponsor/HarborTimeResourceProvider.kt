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

import org.docksidestage.mylasta.direction.HarborConfig
import org.lastaflute.core.time.TypicalTimeResourceProvider
import java.util.*

/**
 * @author jflute
 */
class HarborTimeResourceProvider(protected val config: HarborConfig) : TypicalTimeResourceProvider() {
    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========

    // ===================================================================================
    //                                                                      Basic Handling
    //                                                                      ==============
    override fun getCentralTimeZone(): TimeZone {
        return HarborUserTimeZoneProcessProvider.centralTimeZone
    }

    // ===================================================================================
    //                                                                     Time Adjustment
    //                                                                     ===============
    override fun getTimeAdjustTimeMillis(): String {
        return config.timeAdjustTimeMillis
    }

    override fun getTimeAdjustTimeMillisAsLong(): Long? {
        return config.timeAdjustTimeMillisAsLong
    }
}
