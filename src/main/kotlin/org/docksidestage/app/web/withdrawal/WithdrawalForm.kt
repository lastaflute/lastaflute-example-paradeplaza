package org.docksidestage.app.web.withdrawal

import org.docksidestage.dbflute.allcommon.CDef
import org.hibernate.validator.constraints.Length

/**
 * @author annie_pocket
 * @author jflute
 */
class WithdrawalForm {

    var selectedReason: CDef.WithdrawalReason? = null

    @Length(max = 3)
    var reasonInput: String? = null
}
