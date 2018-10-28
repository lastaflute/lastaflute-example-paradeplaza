package org.docksidestage.app.web.profile

import org.docksidestage.dbflute.exentity.Member
import org.docksidestage.dbflute.exentity.Purchase
import java.time.LocalDateTime

/**
 * @author deco
 */
class ProfileBean(member: Member) {

    val memberId: Int = member.memberId
    val memberName: String = member.memberName
    val memberStatusName: String = member.memberStatus.get().memberStatusName
    val servicePointCount: Int?
    val serviceRankName: String
    var purchaseList: List<PurchasedProductBean>? = null

    init {
        val memberService = member.memberServiceAsOne.get()
        this.servicePointCount = memberService.servicePointCount
        this.serviceRankName = memberService.serviceRank.get().serviceRankName
    }

    class PurchasedProductBean(purchase: Purchase) {

        val productName: String = purchase.product.get().productName
        val regularPrice: Int? = purchase.product.get().regularPrice
        val purchaseDateTime: LocalDateTime = purchase.purchaseDatetime

    }
}
