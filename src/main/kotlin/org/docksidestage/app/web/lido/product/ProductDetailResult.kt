package org.docksidestage.app.web.lido.product

import org.lastaflute.web.validation.Required

/**
 * @author s.tadokoro
 * @author jflute
 */
class ProductDetailResult {

    @Required
    var productId: Int? = null
    @Required
    var productName: String? = null
    @Required
    var categoryName: String? = null
    @Required
    var regularPrice: Int? = null
    @Required
    var productHandleCode: String? = null
}
