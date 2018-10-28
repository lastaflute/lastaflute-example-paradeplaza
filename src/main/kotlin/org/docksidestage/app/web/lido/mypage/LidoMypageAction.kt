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
package org.docksidestage.app.web.lido.mypage

import org.docksidestage.app.web.base.ParadeplazaBaseAction
import org.docksidestage.dbflute.exbhv.ProductBhv
import org.lastaflute.web.Execute
import org.lastaflute.web.login.AllowAnyoneAccess
import org.lastaflute.web.response.JsonResponse
import javax.annotation.Resource

// the 'lido' package is example for JSON API in simple project
// client application is riot.js in lidoisle directory
/**
 * @author s.tadokoro
 * @author jflute
 */
class LidoMypageAction : ParadeplazaBaseAction() {

    @Resource
    private lateinit var productBhv: ProductBhv

    @AllowAnyoneAccess // #for_now s.tadokoro Remove this when JSON Login feature is implemented.
    @Execute
    fun index(): JsonResponse<List<MypageProductResult>> {
        val memberList = productBhv.selectList { cb ->
            cb.query().addOrderBy_RegularPrice_Desc()
            cb.fetchFirst(3)
        }
        val beans = memberList.map { member -> MypageProductResult(member) }
        return asJson(beans)
    }
}
