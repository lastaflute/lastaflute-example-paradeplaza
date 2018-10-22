package org.docksidestage.app.web.lido.auth

import org.lastaflute.web.validation.Required

/**
 * @author s.tadokoro
 * @author jflute
 */
class SignupBody {

    // member
    @Required
    var memberName: String? = null
    @Required
    var memberAccount: String? = null

    // security
    @Required
    var password: String? = null
    @Required
    var reminderQuestion: String? = null
    @Required
    var reminderAnswer: String? = null
}
