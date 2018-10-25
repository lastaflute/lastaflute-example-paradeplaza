package org.docksidestage.app.web.mypage

import org.docksidestage.unit.UnitParadeplazaTestCase

/**
 * @author jflute
 */
class MypageActionTest : UnitParadeplazaTestCase() {

    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        mockLogin()
    }

    fun test_index() {
        // ## Arrange ##
        val action = MypageAction()
        inject(action)

        // ## Act ##
        val response = action.index()

        // ## Assert ##
        val data = validateHtmlData(response)
        log("Recent:")
        data.requiredList("recentProducts", MypageProductBean::class.java).forEach { bean -> log(bean) }
        log("High-Price:")
        data.requiredList("highPriceProducts", MypageProductBean::class.java).forEach { bean -> log(bean) }
    }
}
