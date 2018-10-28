package org.docksidestage.app.web.startup

import org.dbflute.util.DfResourceUtil
import org.dbflute.util.Srl
import org.docksidestage.app.logic.startup.StartupLogic
import org.docksidestage.app.web.base.ParadeplazaBaseAction
import org.docksidestage.mylasta.action.ParadeplazaHtmlPath
import org.lastaflute.web.Execute
import org.lastaflute.web.login.AllowAnyoneAccess
import org.lastaflute.web.response.HtmlResponse
import java.io.File
import javax.annotation.Resource

/**
 * @author iwamatsu0430
 * @author jflute
 */
@AllowAnyoneAccess
class StartupAction : ParadeplazaBaseAction() {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    @Resource
    private lateinit var startupLogic: StartupLogic

    private// e.g. [workspace]/[project]/target/classes
    val projectDir: File
        get() = DfResourceUtil.getBuildDir(javaClass).parentFile.parentFile

    // ===================================================================================
    //                                                                             Execute
    //                                                                             =======
    @Execute
    fun index(): HtmlResponse {
        return asHtml(ParadeplazaHtmlPath.path_Startup_StartupHtml)
    }

    @Execute
    fun create(form: StartupForm): HtmlResponse {
        validate(form, { _ -> }, { asHtml(ParadeplazaHtmlPath.path_Startup_StartupHtml) })

        val projectDir = projectDir
        startupLogic.fromParadeplaza(projectDir, form.domain!!, form.serviceName!!)

        val bean = mappingToBean(form, projectDir)
        return asHtml(ParadeplazaHtmlPath.path_Startup_StartupHtml).renderWith { data -> data.register("bean", bean) }
    }

    private fun mappingToBean(form: StartupForm, projectDir: File): StartupBean {
        val projectPath = Srl.replace(projectDir.parentFile.path, "\\", "/") + "/" + form.serviceName
        return StartupBean(projectPath)
    }
}
