package org.docksidestage.mylasta.direction

import org.docksidestage.unit.UnitHarborTestCase
import org.junit.Assert
import org.lastaflute.core.security.PrimaryCipher
import org.lastaflute.web.servlet.cookie.CookieCipher
import javax.annotation.Resource

/**
 * @author jflute
 */
class CipherSetupTest : UnitHarborTestCase() {

    @Resource
    private lateinit var primaryCipher: PrimaryCipher
    @Resource
    private lateinit var cookieCipher: CookieCipher

    @Throws(Exception::class)
    fun test_primary() {
        val encrypted = primaryCipher.encrypt("sea")
        log("encrypted: {}", encrypted)
        val decrypted = primaryCipher.decrypt(encrypted)
        log("decrypted: {}", decrypted)
        Assert.assertEquals("sea", decrypted)
        log(primaryCipher.oneway("land")) // expects no exception
    }

    @Throws(Exception::class)
    fun test_cookie() {
        val encrypted = cookieCipher.encrypt("sea")
        log("encrypted: {}", encrypted)
        val decrypted = cookieCipher.decrypt(encrypted)
        log("decrypted: {}", decrypted)
        Assert.assertEquals("sea", decrypted)
    }
}
