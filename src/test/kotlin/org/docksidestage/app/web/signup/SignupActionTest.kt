package org.docksidestage.app.web.signup

import org.docksidestage.app.web.base.login.ParadeplazaLoginAssist
import org.docksidestage.app.web.mypage.MypageAction
import org.docksidestage.dbflute.exbhv.MemberBhv
import org.docksidestage.mylasta.action.ParadeplazaHtmlPath
import org.docksidestage.mylasta.mail.member.WelcomeMemberPostcard
import org.docksidestage.unit.UnitParadeplazaTestCase
import org.junit.Assert
import javax.annotation.Resource

/**
 * @author jflute
 */
class SignupActionTest : UnitParadeplazaTestCase() {

    @Resource
    private lateinit var memberBhv: MemberBhv
    @Resource
    private lateinit var loginAssist: ParadeplazaLoginAssist

    fun test_index_success() {
        // ## Arrange ##
        val action = SignupAction()
        inject(action)

        // ## Act ##
        val response = action.index()

        // ## Assert ##
        val htmlData = validateHtmlData(response)
        htmlData.assertHtmlForward(ParadeplazaHtmlPath.path_Signup_SignupHtml)
    }

    fun test_signup_success() {
        // ## Arrange ##
        // for isLogin history registration
        changeAsyncToNormalSync()
        changeRequiresNewToRequired()

        val action = SignupAction()
        inject(action)
        val form = SignupForm()
        form.memberName = "sea"
        form.memberAccount = "land"
        form.password = "piari"
        form.reminderQuestion = "bonvo?"
        form.reminderAnswer = "dstore!"

        reserveMailAssertion { mailData ->
            mailData.required(WelcomeMemberPostcard::class.java).forEach { message ->
                message.requiredToList().forEach { addr ->
                    assertContains(addr.address, form.memberAccount) // e.g. land@docksidestage.org
                }
                message.assertPlainTextContains(form.memberName!!)
                message.assertPlainTextContains(form.memberAccount!!)
            }
        }

        // ## Act ##
        val response = action.signup(form)
        response.afterTxCommitHook.alwaysPresent { hook ->
            hook.hook() // isLogin
        }

        // ## Assert ##
        val htmlData = validateHtmlData(response)
        htmlData.assertRedirect(MypageAction::class.java)

        memberBhv.selectEntity { cb ->
            cb.setupSelect_MemberLoginAsLatest()
            cb.setupSelect_MemberSecurityAsOne()
            cb.query().setMemberAccount_Equal(form.memberAccount)
        }.alwaysPresent { member ->
            Assert.assertEquals(form.memberName, member.memberName)
            Assert.assertTrue(member.isMemberStatusCodeProvisional)

            val security = member.memberSecurityAsOne.get()
            Assert.assertEquals(form.reminderQuestion, security.reminderQuestion)

            val login = member.memberLoginAsLatest.get()
            Assert.assertTrue(login.isLoginMemberStatusCodeProvisional)
        }
    }
}
