package org.docksidestage.app.web.profile

import org.docksidestage.dbflute.exentity.Member
import org.docksidestage.dbflute.exentity.Purchase
import java.time.LocalDateTime

/**
 * @author deco
 */
class ProfileBean(member: Member) {

    val memberId: Int
    val memberName: String
    val memberStatusName: String
    val servicePointCount: Int?
    val serviceRankName: String
    var purchaseList: List<PurchasedProductBean>? = null

    init {
        this.memberId = member.memberId!!
        this.memberName = member.memberName
        this.memberStatusName = member.memberStatus.get().memberStatusName
        val memberService = member.memberServiceAsOne.get()
        this.servicePointCount = memberService.servicePointCount
        this.serviceRankName = memberService.serviceRank.get().serviceRankName
    }

    class PurchasedProductBean(purchase: Purchase) {

        val productName: String
        val regularPrice: Int?
        val purchaseDateTime: LocalDateTime

        init {
            this.productName = purchase.product.get().productName
            this.regularPrice = purchase.product.get().regularPrice
            this.purchaseDateTime = purchase.purchaseDatetime
        }
    }
}
