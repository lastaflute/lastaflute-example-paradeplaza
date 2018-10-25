package org.docksidestage.app.web.signin

import org.docksidestage.app.web.base.ParadeplazaBaseAction
import org.docksidestage.mylasta.action.ParadeplazaHtmlPath
import org.lastaflute.web.Execute
import org.lastaflute.web.response.HtmlResponse

/**
 * @author masaki.kamachi
 * @author jflute
 */
class SigninReminderAction : ParadeplazaBaseAction() {

    // #pending now making...
    @Execute
    fun index(): HtmlResponse {
        return asHtml(ParadeplazaHtmlPath.path_Signin_SigninReminderHtml)
    }
}
