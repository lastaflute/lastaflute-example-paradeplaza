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
package org.docksidestage.app.web.signup

import org.dbflute.helper.filesystem.FileTextIO
import org.dbflute.util.DfResourceUtil
import org.docksidestage.dbflute.exentity.Member
import org.lastaflute.web.servlet.request.ResponseManager
import java.io.IOException
import java.util.*
import javax.annotation.Resource

/**
 * @author jflute
 */
class SignupTokenAssist {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private lateinit var responseManager: ResponseManager

    // ===================================================================================
    //                                                                               Save
    //                                                                              ======
    // #simple_for_example no no no no way, normally use database or KVS, and prepare time limit
    fun saveSignupToken(member: Member): String {
        val token = generateToken()
        createFileTextIO().write(buildTokenFilePath(token), member.memberAccount)
        return token
    }

    private fun generateToken(): String {
        return Integer.toHexString(Random().nextInt())
    }

    // ===================================================================================
    //                                                                              Verify
    //                                                                              ======
    fun verifySignupTokenMatched(account: String, token: String) {
        val saved: String
        try {
            saved = createFileTextIO().read(buildTokenFilePath(token))
        } catch (e: RuntimeException) { // contains "file not found"
            throw responseManager.new404("Unmatched signup token: requested=$token") { op -> op.cause(e) }
        }

        if (saved != account) {
            throw responseManager.new404("Unmatched signup account: saved=$saved, requested=$account")
        }
    }

    // ===================================================================================
    //                                                                        Assist Logic
    //                                                                        ============
    private fun buildTokenFilePath(token: String): String {
        try {
            return DfResourceUtil.getBuildDir(javaClass).parentFile.canonicalPath + "/signup-" + token + ".txt"
        } catch (e: IOException) {
            throw IllegalStateException("Failed to get canonical path for the token: $token", e)
        }

    }

    private fun createFileTextIO(): FileTextIO {
        return FileTextIO().encodeAsUTF8()
    }
}
