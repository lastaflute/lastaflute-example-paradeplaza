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

import org.lastaflute.core.util.Lato
import org.lastaflute.web.validation.Required

/**
 * @author jflute
 */
class ProductDetailBean {

    @Required
    var productId: Int? = null
    @Required
    var productName: String? = null
    @Required
    var categoryName: String? = null
    @Required
    var regularPrice: Int? = null
    @Required
    var productHandleCode: String? = null

    override fun toString(): String {
        return Lato.string(this)
    }
}
