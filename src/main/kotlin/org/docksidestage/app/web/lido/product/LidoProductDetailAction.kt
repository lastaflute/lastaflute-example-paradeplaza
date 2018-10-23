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
package org.docksidestage.app.web.lido.product

import org.docksidestage.app.web.base.HarborBaseAction
import org.docksidestage.dbflute.exbhv.ProductBhv
import org.docksidestage.dbflute.exentity.Product
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
@AllowAnyoneAccess
class LidoProductDetailAction : HarborBaseAction() {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private lateinit var productBhv: ProductBhv

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Execute
    fun index(productId: Int?): JsonResponse<ProductDetailResult> {
        val product = selectProduct(productId!!)
        return asJson(mappingToBean(product))
    }

    // ===================================================================================
    //                                                                              Select
    //                                                                              ======
    private fun selectProduct(productId: Int): Product {
        return productBhv.selectEntity { cb ->
            cb.setupSelect_ProductCategory()
            cb.query().setProductId_Equal(productId)
        }.get()
    }

    // ===================================================================================
    //                                                                             Mapping
    //                                                                             =======
    private fun mappingToBean(product: Product): ProductDetailResult {
        val bean = ProductDetailResult()
        bean.productId = product.productId
        bean.productName = product.productName
        bean.regularPrice = product.regularPrice
        bean.productHandleCode = product.productHandleCode
        product.productCategory.alwaysPresent { category -> bean.categoryName = category.productCategoryName }
        return bean
    }
}
