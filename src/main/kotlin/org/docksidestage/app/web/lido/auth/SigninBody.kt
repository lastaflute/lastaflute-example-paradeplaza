package org.docksidestage.app.web.lido.auth

import org.lastaflute.web.validation.Required

/**
 * @author s.tadokoro
 * @author jflute
 */
class SigninBody {

    @Required
    var account: String? = null
    @Required
    var password: String? = null
}