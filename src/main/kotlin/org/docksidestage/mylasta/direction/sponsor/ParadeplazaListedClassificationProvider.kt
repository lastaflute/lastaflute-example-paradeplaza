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
package org.docksidestage.mylasta.direction.sponsor

import org.dbflute.jdbc.ClassificationMeta
import org.dbflute.optional.OptionalThing
import org.docksidestage.dbflute.allcommon.CDef
import org.docksidestage.dbflute.allcommon.DBCurrent
import org.docksidestage.mylasta.appcls.AppCDef
import org.lastaflute.db.dbflute.classification.TypicalListedClassificationProvider
import org.lastaflute.db.dbflute.exception.ProvidedClassificationNotFoundException
import java.util.function.Function

/**
 * @author jflute
 */
class ParadeplazaListedClassificationProvider : TypicalListedClassificationProvider() { // basically for HTML response

    @Throws(ProvidedClassificationNotFoundException::class)
    override fun chooseClassificationFinder(projectName: String): Function<String, ClassificationMeta> {
        if (DBCurrent.getInstance().projectName() == projectName) {
            return Function { clsName -> onMainSchema(clsName).orElse(null) } // null means not found
        } else {
            throw ProvidedClassificationNotFoundException("Unknown DBFlute project name: $projectName")
        }
    }

    override fun getDefaultClassificationFinder(): Function<String, ClassificationMeta> {
        return Function { clsName ->
            onMainSchema(clsName).orElseGet {
                onAppCls(clsName).orElse(null) // null means not found
            }
        }
    }

    protected fun onMainSchema(clsName: String): OptionalThing<ClassificationMeta> {
        return findMeta(CDef.DefMeta::class.java, clsName)
    }

    protected fun onAppCls(clsName: String): OptionalThing<ClassificationMeta> {
        return findMeta(AppCDef.DefMeta::class.java, clsName)
    }
}
