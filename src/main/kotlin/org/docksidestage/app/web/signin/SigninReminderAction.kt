package org.docksidestage.app.web.signin

import org.docksidestage.app.web.base.HarborBaseAction
import org.docksidestage.mylasta.action.HarborHtmlPath
import org.lastaflute.web.Execute
import org.lastaflute.web.response.HtmlResponse

/**
 * @author masaki.kamachi
 * @author jflute
 */
class SigninReminderAction : HarborBaseAction() {

    // #pending now making...
    @Execute
    fun index(): HtmlResponse {
        return asHtml(HarborHtmlPath.path_Signin_SigninReminderHtml)
    }
}
