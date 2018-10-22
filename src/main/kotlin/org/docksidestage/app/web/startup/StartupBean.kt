package org.docksidestage.app.web.startup

import org.lastaflute.web.validation.Required

/**
 * @author iwamatsu0430
 * @author jflute
 */
class StartupBean(@field:Required
                  val projectPath: String)
