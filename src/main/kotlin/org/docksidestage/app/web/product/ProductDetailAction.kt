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
package org.docksidestage.app.web.product

import org.docksidestage.app.web.base.ParadeplazaBaseAction
import org.docksidestage.dbflute.exbhv.ProductBhv
import org.docksidestage.dbflute.exentity.Product
import org.docksidestage.mylasta.action.ParadeplazaHtmlPath
import org.lastaflute.web.Execute
import org.lastaflute.web.login.AllowAnyoneAccess
import org.lastaflute.web.response.HtmlResponse
import javax.annotation.Resource

/**
 * @author jflute
 */
@AllowAnyoneAccess
class ProductDetailAction : ParadeplazaBaseAction() {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private lateinit var productBhv: ProductBhv

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Execute
    fun index(productId: Int?): HtmlResponse {
        val product = selectProduct(productId!!)
        val bean = mappingToBean(product)
        return asHtml(ParadeplazaHtmlPath.path_Product_ProductDetailHtml).renderWith { data -> data.register("product", bean) }
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
    private fun mappingToBean(product: Product): ProductDetailBean {
        val bean = ProductDetailBean()
        bean.productId = product.productId
        bean.productName = product.productName
        bean.regularPrice = product.regularPrice
        bean.productHandleCode = product.productHandleCode
        product.productCategory.alwaysPresent { category -> bean.categoryName = category.productCategoryName }
        return bean
    }
}
