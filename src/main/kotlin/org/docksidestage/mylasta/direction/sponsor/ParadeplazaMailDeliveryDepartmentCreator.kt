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

import org.dbflute.mail.CardView
import org.dbflute.mail.send.SMailDeliveryDepartment
import org.dbflute.mail.send.SMailPostalMotorbike
import org.dbflute.mail.send.SMailPostalParkingLot
import org.dbflute.mail.send.SMailPostalPersonnel
import org.dbflute.mail.send.embedded.personnel.SMailDogmaticPostalPersonnel
import org.dbflute.mail.send.supplement.async.SMailAsyncStrategy
import org.dbflute.mail.send.supplement.filter.SMailSubjectFilter
import org.dbflute.mail.send.supplement.label.SMailLabelStrategy
import org.dbflute.optional.OptionalThing
import org.dbflute.util.DfStringUtil
import org.dbflute.util.Srl
import org.docksidestage.mylasta.direction.ParadeplazaConfig
import org.lastaflute.core.magic.async.AsyncManager
import org.lastaflute.core.magic.async.ConcurrentAsyncCall
import org.lastaflute.core.message.MessageManager
import org.lastaflute.core.util.ContainerUtil
import java.util.*

/**
 * @author jflute
 */
class ParadeplazaMailDeliveryDepartmentCreator(protected val config: ParadeplazaConfig) {
    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========

    // ===================================================================================
    //                                                                           Component
    //                                                                           =========
    protected val messageManager: MessageManager
        get() = ContainerUtil.getComponent(MessageManager::class.java)

    protected val asyncManager: AsyncManager
        get() = ContainerUtil.getComponent(AsyncManager::class.java)

    // ===================================================================================
    //                                                                              Create
    //                                                                              ======
    fun create(): SMailDeliveryDepartment {
        return SMailDeliveryDepartment(createPostalParkingLot(), createPostalPersonnel())
    }

    // -----------------------------------------------------
    //                                    Postal Parking Lot
    //                                    ------------------
    protected fun createPostalParkingLot(): SMailPostalParkingLot {
        val parkingLot = SMailPostalParkingLot()
        val motorbike = SMailPostalMotorbike()
        val hostAndPort = config.mailSmtpServerMainHostAndPort
        val hostPortList = DfStringUtil.splitListTrimmed(hostAndPort, ":")
        motorbike.registerConnectionInfo(hostPortList[0], Integer.parseInt(hostPortList[1]))
        motorbike.registerReturnPath(config.mailReturnPath)
        parkingLot.registerMotorbikeAsMain(motorbike)
        return parkingLot
    }

    // -----------------------------------------------------
    //                                      Postal Personnel
    //                                      ----------------
    protected fun createPostalPersonnel(): SMailPostalPersonnel {
        val personnel = createDogmaticPostalPersonnel()
        return if (config.isMailSendMock) personnel.asTraining() else personnel
    }

    protected fun createDogmaticPostalPersonnel(): SMailDogmaticPostalPersonnel { // #ext_point e.g. locale, database
        val testPrefix = config.mailSubjectTestPrefix
        val asyncManager = asyncManager
        val messageManager = messageManager
        return object : SMailDogmaticPostalPersonnel() {

            // *if you need user locale switching or templates from database,
            // override createConventionReceptionist() (see the method for the details)

            override fun createSubjectFilter(): OptionalThing<SMailSubjectFilter> {
                return OptionalThing.of(SMailSubjectFilter { view, subject -> if (!subject.startsWith(testPrefix)) testPrefix + subject else subject })
            }

            override fun createAsyncStrategy(): OptionalThing<SMailAsyncStrategy> {
                return OptionalThing.of(object : SMailAsyncStrategy {
                    override fun async(view: CardView, runnable: Runnable) {
                        asyncRunnable(asyncManager, runnable)
                    }

                    override fun alwaysAsync(view: CardView?): Boolean {
                        return true // as default of LastaFlute example
                    }
                })
            }

            override fun createLabelStrategy(): OptionalThing<SMailLabelStrategy> {
                return OptionalThing.of(SMailLabelStrategy { view, locale, label -> resolveLabelIfNeeds(messageManager, locale, label) })
            }
        }
    }

    // ===================================================================================
    //                                                                        Asynchronous
    //                                                                        ============
    protected fun asyncRunnable(asyncManager: AsyncManager, runnable: Runnable) {
        asyncManager.async(object : ConcurrentAsyncCall {
            override fun callback() {
                runnable.run()
            }

            override fun asPrimary(): Boolean {
                return true // mail is primary business
            }
        })
    }

    // ===================================================================================
    //                                                                       Resolve Label
    //                                                                       =============
    protected fun resolveLabelIfNeeds(messageManager: MessageManager, locale: Locale, label: String): String {
        return if (Srl.startsWith(label, "labels.", "{labels.")) messageManager.getMessage(locale, label) else label
    }
}
