package org.docksidestage.app.web.lido.product

import org.dbflute.optional.OptionalThing
import org.docksidestage.unit.UnitHarborTestCase
import org.junit.Assert

/**
 * @author jflute
 */
class LidoProductListActionTest : UnitHarborTestCase() {

    fun test_index_searchByName() {
        // ## Arrange ##
        val action = LidoProductListAction()
        inject(action)
        val body = ProductSearchBody()
        body.productName = "P"

        // ## Act ##
        val response = action.index(OptionalThing.of(1), body)

        // ## Assert ##
        showJson(response)
        val data = validateJsonData(response)
        data.jsonResult.rows.forEach { bean ->
            log(bean)
            Assert.assertTrue(bean.productName!!.contains(body.productName!!))
        }
    }
}
