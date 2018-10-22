package org.docksidestage.app.web.startup

import org.lastaflute.web.validation.Required

/**
 * @author iwamatsu0430
 * @author jflute
 */
class StartupForm {

    @Required
    var domain: String? = null

    @Required
    var serviceName: String? = null
}
