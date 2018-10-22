package org.docksidestage.app.web.signup

import org.lastaflute.web.validation.Required

/**
 * @author annie_pocket
 * @author jflute
 */
class SignupForm {

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
