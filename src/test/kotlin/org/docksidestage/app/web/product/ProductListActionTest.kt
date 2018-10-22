package org.docksidestage.app.web.product

import org.dbflute.optional.OptionalThing
import org.docksidestage.app.web.base.paging.PagingAssist
import org.docksidestage.app.web.base.paging.PagingNavi
import org.docksidestage.mylasta.action.HarborHtmlPath
import org.docksidestage.unit.UnitHarborTestCase
import org.junit.Assert

/**
 * @author jflute
 */
class ProductListActionTest : UnitHarborTestCase() {

    @Throws(Exception::class)
    fun test_index_success_by_productName() {
        // ## Arrange ##
        val action = ProductListAction()
        inject(action)
        val pageNumber = 2
        val form = ProductSearchForm()
        form.productName = "a"

        // ## Act ##
        val response = action.index(OptionalThing.of(pageNumber), form)

        // ## Assert ##
        val htmlData = validateHtmlData(response)
        htmlData.assertHtmlForward(HarborHtmlPath.path_Product_ProductListHtml)
        htmlData.requiredList("beans", ProductSearchRowBean::class.java).forEach { bean ->
            log(bean)
            assertContainsIgnoreCase(bean.productName!!, form.productName)
        }
        val pagingNavi = htmlData.required(PagingAssist.NAVI_KEY, PagingNavi::class.java)
        log(pagingNavi)
        Assert.assertEquals(pageNumber, pagingNavi.currentPageNumber)
    }
}
