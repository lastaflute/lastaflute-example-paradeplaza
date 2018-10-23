package org.docksidestage.app.web.withdrawal

import org.docksidestage.app.web.base.HarborBaseAction
import org.docksidestage.app.web.signout.SignoutAction
import org.docksidestage.dbflute.exbhv.MemberBhv
import org.docksidestage.dbflute.exbhv.MemberWithdrawalBhv
import org.docksidestage.dbflute.exentity.Member
import org.docksidestage.dbflute.exentity.MemberWithdrawal
import org.docksidestage.mylasta.action.HarborHtmlPath
import org.docksidestage.mylasta.action.HarborMessages
import org.lastaflute.core.time.TimeManager
import org.lastaflute.core.util.LaStringUtil
import org.lastaflute.web.Execute
import org.lastaflute.web.response.HtmlResponse
import javax.annotation.Resource

/**
 * @author annie_pocket
 * @author jflute
 */
class WithdrawalAction : HarborBaseAction() {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private lateinit var timeManager: TimeManager
    @Resource
    private lateinit var memberBhv: MemberBhv
    @Resource
    private lateinit var memberWithdrawalBhv: MemberWithdrawalBhv

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Execute
    fun index(): HtmlResponse {
        return asEntryHtml()
    }

    @Execute
    fun confirm(form: WithdrawalForm): HtmlResponse {
        validate(form, { messages -> moreValidation(form, messages) }, { asEntryHtml() })
        return asConfirmHtml()
    }

    private fun moreValidation(form: WithdrawalForm, messages: HarborMessages) {
        if (form.selectedReason == null && LaStringUtil.isEmpty(form.reasonInput)) {
            messages.addConstraintsRequiredMessage("selectedReason")
        }
    }

    @Execute
    fun done(form: WithdrawalForm): HtmlResponse {
        validate(form, { message -> }, { asEntryHtml() })
        val memberId = userBean.get().memberId
        insertWithdrawal(form, memberId)
        updateStatusWithdrawal(memberId)
        return redirect(SignoutAction::class.java)
    }

    // ===================================================================================
    //                                                                              Update
    //                                                                              ======
    private fun insertWithdrawal(form: WithdrawalForm, memberId: Int?) {
        val withdrawal = MemberWithdrawal()
        withdrawal.memberId = memberId
        withdrawal.withdrawalReasonCodeAsWithdrawalReason = form.selectedReason
        withdrawal.withdrawalReasonInputText = form.reasonInput
        withdrawal.withdrawalDatetime = timeManager.currentDateTime()
        memberWithdrawalBhv.insert(withdrawal)
    }

    private fun updateStatusWithdrawal(memberId: Int?) {
        val member = Member()
        member.memberId = memberId
        member.setMemberStatusCode_Withdrawal()
        memberBhv.updateNonstrict(member)
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun asEntryHtml(): HtmlResponse {
        return asHtml(HarborHtmlPath.path_Withdrawal_WithdrawalEntryHtml)
    }

    private fun asConfirmHtml(): HtmlResponse {
        return asHtml(HarborHtmlPath.path_Withdrawal_WithdrawalConfirmHtml)
    }
}
