/*
 * Copyright 2015-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.docksidestage.app.web.member

import org.dbflute.cbean.result.PagingResultBean
import org.dbflute.optional.OptionalThing
import org.docksidestage.app.web.base.ParadeplazaBaseAction
import org.docksidestage.app.web.base.paging.PagingAssist
import org.docksidestage.dbflute.exbhv.MemberBhv
import org.docksidestage.dbflute.exentity.Member
import org.docksidestage.mylasta.action.ParadeplazaHtmlPath
import org.lastaflute.core.util.LaStringUtil
import org.lastaflute.web.Execute
import org.lastaflute.web.response.HtmlResponse
import javax.annotation.Resource

/**
 * @author jflute
 */
class MemberListAction : ParadeplazaBaseAction() {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private lateinit var memberBhv: MemberBhv
    @Resource
    private lateinit var pagingAssist: PagingAssist

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Execute
    fun index(pageNumber: OptionalThing<Int>, form: MemberSearchForm): HtmlResponse {
        validate(form, {}, { asHtml(ParadeplazaHtmlPath.path_Member_MemberListHtml) })
        val page = selectMemberPage(pageNumber.orElse(1), form)
        val beans = page.mappingList { member -> mappingToBean(member) }
        return asHtml(ParadeplazaHtmlPath.path_Member_MemberListHtml).renderWith { data ->
            data.register("beans", beans)
            pagingAssist.registerPagingNavi(data, page, form)
        }
    }

    // ===================================================================================
    //                                                                              Select
    //                                                                              ======
    protected fun selectMemberPage(pageNumber: Int, form: MemberSearchForm): PagingResultBean<Member> {
        verifyOrIllegalTransition("The pageNumber should be positive number: $pageNumber", pageNumber > 0)
        return memberBhv.selectPage { cb ->
            cb.setupSelect_MemberStatus()
            cb.specify().derivedPurchase().count({ purchaseCB -> purchaseCB.specify().columnPurchaseId() }, Member.ALIAS_purchaseCount)
            if (LaStringUtil.isNotEmpty(form.memberName)) {
                cb.query().setMemberName_LikeSearch(form.memberName) { op -> op.likeContain() }
            }
            if (LaStringUtil.isNotEmpty(form.purchaseProductName) || form.unpaid) {
                cb.query().existsPurchase { purchaseCB ->
                    if (LaStringUtil.isNotEmpty(form.purchaseProductName)) {
                        purchaseCB.query().queryProduct().setProductName_LikeSearch(form.purchaseProductName) { op -> op.likeContain() }
                    }
                    if (form.unpaid) {
                        purchaseCB.query().setPaymentCompleteFlg_Equal_False()
                    }
                }
            }
            if (form.memberStatus != null) {
                cb.query().setMemberStatusCode_Equal_AsMemberStatus(form.memberStatus)
            }
            if (form.formalizedFrom != null || form.formalizedTo != null) {
                val fromTime = if (form.formalizedFrom != null) form.formalizedFrom!!.atStartOfDay() else null
                val toTime = if (form.formalizedFrom != null) form.formalizedFrom!!.atStartOfDay() else null
                cb.query().setFormalizedDatetime_FromTo(fromTime, toTime) { op -> op.compareAsDate().allowOneSide() }
            }
            cb.query().addOrderBy_UpdateDatetime_Desc()
            cb.query().addOrderBy_MemberId_Asc()
            cb.paging(4, pageNumber)
        }
    }

    // ===================================================================================
    //                                                                             Mapping
    //                                                                             =======
    private fun mappingToBean(member: Member): MemberSearchRowBean {
        val bean = MemberSearchRowBean()
        bean.memberId = member.memberId
        bean.memberName = member.memberName
        member.memberStatus.alwaysPresent { status -> bean.memberStatusName = status.memberStatusName }
        val formalizedDatetime = member.formalizedDatetime
        if (formalizedDatetime != null) {
            bean.formalizedDate = formalizedDatetime.toLocalDate()
        }
        bean.updateDatetime = member.updateDatetime
        bean.withdrawalMember = member.isMemberStatusCodeWithdrawal
        bean.purchaseCount = member.purchaseCount
        return bean
    }
}
