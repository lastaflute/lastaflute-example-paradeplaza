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
package org.docksidestage.app.web.base.paging

import org.dbflute.cbean.paging.numberlink.PageNumberLink
import org.dbflute.cbean.paging.numberlink.range.PageRangeOption
import org.dbflute.cbean.result.PagingResultBean
import org.lastaflute.di.helper.beans.factory.BeanDescFactory
import org.lastaflute.web.util.LaRequestUtil
import java.io.Serializable
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.function.Consumer

/**
 * @author jflute
 */
class PagingNavi
// ===================================================================================
//                                                                         Constructor
//                                                                         ===========
/**
 * @param page The selected page as bean of paging result. (NotNull)
 * @param opLambda The callback for option of page range. (NotNull)
 * @param queryForm The form for query string added to link. (NotNull)
 */
(page: PagingResultBean<*>, opLambda: Consumer<PageRangeOption>, queryForm: Any) : Serializable { // #app_customize

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    val isDisplayPagingNavi: Boolean
    val allRecordCount: Int
    val allPageCount: Int
    val currentPageNumber: Int
    // 'is' prefix for e.g. EL expression
    val isExistsPreviousPage: Boolean
    // 'is' prefix for e.g. EL expression
    val isExistsNextPage: Boolean
    val previousPageLinkHref: String
    val nextPageLinkHref: String
    val pageNumberLinkList: List<PageNumberLink>

    init {
        assertArgumentNotNull("page", page)
        assertArgumentNotNull("opLambda", opLambda)
        isDisplayPagingNavi = !page.isEmpty()

        allRecordCount = page.allRecordCount
        allPageCount = page.allPageCount
        currentPageNumber = page.currentPageNumber
        isExistsPreviousPage = page.existsPreviousPage()
        isExistsNextPage = page.existsNextPage()

        previousPageLinkHref = createTargetPageNumberLink(currentPageNumber - 1, queryForm)
        nextPageLinkHref = createTargetPageNumberLink(currentPageNumber + 1, queryForm)
        pageNumberLinkList = createPageNumberLinkList(page, opLambda, queryForm)
    }

    // ===================================================================================
    //                                                                     PageNumber Link
    //                                                                     ===============
    /**
     * Create target page number link.
     * @param pageNumber Target page number.
     * @param queryForm The form for query string added to link. (NotNull)
     * @return The link expression for target page. (NotNull)
     */
    protected fun createTargetPageNumberLink(pageNumber: Int, queryForm: Any): String {
        return pageNumber.toString() + buildQueryString(queryForm)
    }

    protected fun buildQueryString(queryForm: Any): String {
        val beanDesc = BeanDescFactory.getBeanDesc(queryForm.javaClass)
        val propSize = beanDesc.propertyDescSize
        val sb = StringBuilder()
        for (i in 0 until propSize) {
            val pd = beanDesc.getPropertyDesc(i)
            val value = beanDesc.getPropertyDesc(i).getValue(queryForm)
            if (value == null || value is String && value.isEmpty()) {
                continue
            }
            sb.append(if (sb.length == 0) "?" else "&")
            sb.append(encode(pd.propertyName)).append("=").append(encode(value.toString()))
        }
        return sb.toString()
    }

    protected fun encode(input: String): String {
        val encoding = LaRequestUtil.getRequest().characterEncoding
        try {
            return URLEncoder.encode(input, encoding)
        } catch (e: UnsupportedEncodingException) {
            throw IllegalStateException("Unknown encoding: $encoding", e)
        }

    }

    /**
     * Create the list of page number link.
     * @param page The bean of paging result. (NotNull)
     * @param opLambda The callback for option of page range. (NotNull)
     * @param paramForm The form for GET parameter added to link. (NullAllowed)
     * @return The list of page number link. (NotNull)
     */
    protected fun createPageNumberLinkList(page: PagingResultBean<*>, opLambda: Consumer<PageRangeOption>,
                                           paramForm: Any): List<PageNumberLink> {
        return page.pageRange { op -> opLambda.accept(op) }.buildPageNumberLinkList { pageNumberElement, current -> createPageNumberLink(paramForm, pageNumberElement, current) }
    }

    protected fun createPageNumberLink(queryForm: Any, pageNumberElement: Int, current: Boolean): PageNumberLink {
        val targetPageNumberLink = createTargetPageNumberLink(pageNumberElement, queryForm)
        return newPageNumberLink().initialize(pageNumberElement, current, targetPageNumberLink)
    }

    protected fun newPageNumberLink(): PageNumberLink {
        return PageNumberLink()
    }

    // ===================================================================================
    //                                                                       Assert Helper
    //                                                                       =============
    protected fun assertArgumentNotNull(variableName: String?, value: Any?) {
        if (variableName == null) {
            val msg = "The value should not be null: variableName=null value=" + value!!
            throw IllegalArgumentException(msg)
        }
        if (value == null) {
            val msg = "The value should not be null: variableName=$variableName"
            throw IllegalArgumentException(msg)
        }
    }

    // ===================================================================================
    //                                                                      Basic Override
    //                                                                      ==============
    override fun toString(): String {
        return ("{display=" + isDisplayPagingNavi + ", allRecordCount=" + allRecordCount + ", allPageCount=" + allPageCount
                + ", currentPageNumber=" + currentPageNumber + "}")
    }

    companion object {

        // ===================================================================================
        //                                                                          Definition
        //                                                                          ==========
        private const val serialVersionUID = 1L
    }
}
