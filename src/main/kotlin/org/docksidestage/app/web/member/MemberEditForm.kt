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

import org.docksidestage.dbflute.allcommon.CDef
import org.lastaflute.web.validation.ClientError
import org.lastaflute.web.validation.Required
import org.lastaflute.web.validation.theme.conversion.ValidateTypeFailure
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * @author jflute
 */
class MemberEditForm {

    @Required(groups = [ClientError::class])
    var memberId: Int? = null

    @Required
    var memberName: String? = null

    @Required
    var memberAccount: String? = null

    @ValidateTypeFailure // using calendar so basically unneeded but just in case
    var birthdate: LocalDate? = null

    var formalizedDate: LocalDate? = null

    @Required
    var memberStatus: CDef.MemberStatus? = null

    var latestLoginDatetime: LocalDateTime? = null // only view

    var updateDatetime: LocalDateTime? = null // only view

    @Required(groups = [ClientError::class])
    var previousStatus: CDef.MemberStatus? = null

    @Required(groups = [ClientError::class])
    var versionNo: Long? = null
}