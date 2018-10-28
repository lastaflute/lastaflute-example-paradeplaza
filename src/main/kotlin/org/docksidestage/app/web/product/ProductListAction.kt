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

import org.dbflute.cbean.result.PagingResultBean
import org.dbflute.optional.OptionalThing
import org.docksidestage.app.web.base.ParadeplazaBaseAction
import org.docksidestage.app.web.base.paging.PagingAssist
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
class ProductListAction : ParadeplazaBaseAction() {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private lateinit var productBhv: ProductBhv
    @Resource
    private lateinit var pagingAssist: PagingAssist

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Execute
    fun index(pageNumber: OptionalThing<Int>, form: ProductSearchForm): HtmlResponse {
        validate(form, {}, { asHtml(ParadeplazaHtmlPath.path_Product_ProductListHtml) })
        val page = selectProductPage(pageNumber.orElse(1), form)
        val beans = page.map { product -> mappingToBean(product) }
        return asHtml(ParadeplazaHtmlPath.path_Product_ProductListHtml).renderWith { data ->
            data.register("beans", beans)
            pagingAssist.registerPagingNavi(data, page, form)
        }
    }

    // #hope showDerived() as JsonResponse by jflute (2016/08/08)

    // ===================================================================================
    //                                                                              Select
    //                                                                              ======
    private fun selectProductPage(pageNumber: Int, form: ProductSearchForm): PagingResultBean<Product> {
        verifyOrClientError("The pageNumber should be positive number: $pageNumber", pageNumber > 0)
        return productBhv.selectPage { cb ->
            cb.setupSelect_ProductStatus()
            cb.setupSelect_ProductCategory()
            cb.specify().derivedPurchase().max({ purchaseCB -> purchaseCB.specify().columnPurchaseDatetime() }, Product.ALIAS_latestPurchaseDate)
            if (form.productName != null) {
                cb.query().setProductName_LikeSearch(form.productName) { op -> op.likeContain() }
            }
            if (form.purchaseMemberName != null) {
                cb.query().existsPurchase { purchaseCB -> purchaseCB.query().queryMember().setMemberName_LikeSearch(form.purchaseMemberName) { op -> op.likeContain() } }
            }
            if (form.productStatus != null) {
                cb.query().setProductStatusCode_Equal_AsProductStatus(form.productStatus)
            }
            cb.query().addOrderBy_ProductName_Asc()
            cb.query().addOrderBy_ProductId_Asc()
            cb.paging(4, pageNumber)
        }
    }

    // ===================================================================================
    //                                                                             Mapping
    //                                                                             =======
    private fun mappingToBean(product: Product): ProductSearchRowBean {
        val bean = ProductSearchRowBean()
        bean.productId = product.productId
        bean.productName = product.productName
        product.productStatus.alwaysPresent { status -> bean.productStatus = status.productStatusName }
        product.productCategory.alwaysPresent { category -> bean.productCategory = category.productCategoryName }
        bean.regularPrice = product.regularPrice
        bean.latestPurchaseDate = product.latestPurchaseDate
        return bean
    }
}
