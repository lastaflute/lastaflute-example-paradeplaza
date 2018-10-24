package org.docksidestage.app.web.mypage

import org.docksidestage.dbflute.exentity.Product
import org.lastaflute.core.util.Lato
import org.lastaflute.web.validation.Required

/**
 * @author jflute
 */
class MypageProductBean(product: Product) {

    @Required
    val productName: String = product.productName
    @Required
    val regularPrice: Int? = product.regularPrice

    override fun toString(): String {
        return Lato.string(this)
    }
}
