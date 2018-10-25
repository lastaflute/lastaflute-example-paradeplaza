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
package org.docksidestage.app.logic.startup

import org.dbflute.helper.filesystem.FileHierarchyTracer
import org.dbflute.helper.filesystem.FileHierarchyTracingHandler
import org.dbflute.helper.filesystem.FileTextIO
import org.dbflute.helper.filesystem.FileTextLineFilter
import org.dbflute.util.Srl
import org.lastaflute.di.util.LdiFileUtil
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

/**
 * @author jflute
 */
class NewProjectCreator(
        val appName: String, val projectDir: File, val serviceNameFilter: ServiceNameFilter) {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========

    interface ServiceNameFilter {
        fun filter(original: String): String
    }

    // ===================================================================================
    //                                                                         New Project
    //                                                                         ===========
    fun newProject() {
        FileHierarchyTracer().trace(projectDir, object : FileHierarchyTracingHandler {

            override fun isTargetFileOrDir(currentFile: File): Boolean {
                return determineTarget(currentFile)
            }

            @Throws(IOException::class)
            override fun handleFile(currentFile: File) {
                migrateFile(currentFile)
            }
        })
    }

    // -----------------------------------------------------
    //                              from isTargetFileOrDir()
    //                              ------------------------
    protected fun determineTarget(currentFile: File): Boolean {
        val canonicalPath: String
        try {
            canonicalPath = currentFile.canonicalPath.replace("\\", "/")
        } catch (e: IOException) {
            throw IllegalStateException("Failed to get canonical path: $currentFile")
        }

        if (isAppResource(canonicalPath) && !isAppMigrated(canonicalPath)) { // e.g. app.product
            return false
        }
        if (isWebInfViewResource(canonicalPath) && !isViewMigrated(canonicalPath)) { // e.g. /view/product
            return false
        }
        return !(isMyLastaOnlyExample(canonicalPath) // e.g. /mylasta/mail/
                || isResourcesOnlyExample(canonicalPath) // e.g. /resources/mail/
                || isStartUpTool(canonicalPath) // e.g. this
                || isDemoTestResource(canonicalPath) // e.g. .gitignore for DemoTest
                || isTestDbResource(canonicalPath) // e.g. H2 database
                || isTestClientResource(canonicalPath) // e.g. lidoisle
                || isDBFluteClientLog(canonicalPath) // e.g. dbflute.log
                || isH2DatabaseDDL(canonicalPath) // e.g. ...80-comment.sql
                || isErdImage(canonicalPath) // e.g. maihamadb.png
                || isOssText(canonicalPath) // e.g. LICENSE
                || isGitDir(canonicalPath) // e.g. .git
                || isBuildDir(canonicalPath))
    }

    // -----------------------------------------------------
    //                                     from handleFile()
    //                                     -----------------
    @Throws(IOException::class)
    protected fun migrateFile(currentFile: File) {
        val canonicalPath = currentFile.canonicalPath.replace("\\", "/")
        val baseDir = filterServiceName(Srl.substringLastFront(canonicalPath, "/"))
        val pureFile = filterServiceName(Srl.substringLastRear(canonicalPath, "/"))
        val outputFile = "$baseDir/$pureFile"
        if (isPlainMigration(baseDir, pureFile)) { // e.g. binary
            mkdirs(baseDir)
            copyFile(currentFile, File(outputFile))
        } else {
            val textIO = FileTextIO().encodeAsUTF8()
            val filtered: String?
            filtered = when {
                canonicalPath.endsWith("additionalForeignKeyMap.dfprop") -> textIO.readFilteringLine(canonicalPath, createAdditionalForeignKeyFilter())
                canonicalPath.endsWith("classificationDefinitionMap.dfprop") -> textIO.readFilteringLine(canonicalPath, createClassificationDefinitionFilter())
                canonicalPath.endsWith("classificationDeploymentMap.dfprop") -> textIO.readFilteringLine(canonicalPath, createClassificationDeploymentFilter())
                canonicalPath.endsWith("basicInfoMap.dfprop") -> textIO.readFilteringLine(canonicalPath, createBasicInfoMapFilter())
                canonicalPath.endsWith("databaseInfoMap.dfprop") -> textIO.readFilteringLine(canonicalPath, createDatabaseInfoMapFilter())
                canonicalPath.endsWith("databaseInfoMap+.dfprop") -> textIO.readFilteringLine(canonicalPath, createDatabaseInfoMapFilter())
                canonicalPath.endsWith("documentMap.dfprop") -> textIO.readFilteringLine(canonicalPath, createDocumentMapFilter())
                canonicalPath.endsWith("lastafluteMap.dfprop") -> textIO.readFilteringLine(canonicalPath, createLastaFluteMapFilter())
                canonicalPath.endsWith("_env.properties") -> textIO.readFilteringLine(canonicalPath, createEnvPropertiesFilter())
                canonicalPath.endsWith("pom.xml") -> textIO.readFilteringLine(canonicalPath, createPomXmlFilter())
                canonicalPath.endsWith("ParadeplazaFwAssistantDirector.java") -> textIO.readFilteringLine(canonicalPath, createFwAssistantDirectorFilter())
                else -> textIO.readFilteringLine(canonicalPath) { line -> filterServiceName(line) }
            }
            if (filtered == null) { // just in case
                val msg = "Filtered string was null or mpety: path=$canonicalPath, filtered=$filtered"
                throw IllegalStateException(msg)
            }
            writeFile(textIO, outputFile, filtered)
            adjustReplaceSchemaDdl(canonicalPath, baseDir, outputFile)
        }
    }

    // ===================================================================================
    //                                                                       Create Filter
    //                                                                       =============

    // -----------------------------------------------------
    //                                  Configuration Filter
    //                                  --------------------
    protected fun createAdditionalForeignKeyFilter(): FileTextLineFilter {
        return object : FileTextLineFilter {
            private var skipped: Boolean = false

            override fun filter(line: String): String? {
                return if (line.startsWith("map:{")) {
                    skipped = true
                    line
                } else if (line.startsWith("}")) {
                    skipped = false
                    line
                } else {
                    if (skipped) {
                        null
                    } else filterServiceName(line)
                }
            }
        }
    }

    protected fun createClassificationDefinitionFilter(): FileTextLineFilter {
        return object : FileTextLineFilter {
            private var skipped: Boolean = false

            override fun filter(line: String): String? {
                return when {
                    line.startsWith("    ; ServiceRank = ") -> { // Flg and MemberStatus only
                        skipped = true
                        null
                    }
                    line.startsWith("}") -> {
                        skipped = false
                        line
                    }
                    else -> if (skipped) {
                        null
                    } else filterServiceName(line)
                }
            }
        }
    }

    protected fun createClassificationDeploymentFilter(): FileTextLineFilter {
        return FileTextLineFilter { line ->
            if (line.startsWith("    ; PURCHASE_PAYMENT = ")) {
                return@FileTextLineFilter null
            }
            filterServiceName(line)
        }
    }

    protected fun createBasicInfoMapFilter(): FileTextLineFilter {
        return FileTextLineFilter { line ->
            if (line.trim { it <= ' ' }.startsWith("#")) { // to keep comment
                return@FileTextLineFilter line
            }
            filterServiceName(filterJdbcSettings(line))
        }
    }

    protected fun createDatabaseInfoMapFilter(): FileTextLineFilter {
        return FileTextLineFilter { line -> filterServiceName(filterJdbcSettings(line)) }
    }

    protected fun createDocumentMapFilter(): FileTextLineFilter {
        return FileTextLineFilter { line -> filterServiceName(filterJdbcSettings(line)) }
    }

    protected fun createLastaFluteMapFilter(): FileTextLineFilter {
        return object : FileTextLineFilter {
            private var appMap = false
            private var currentApp = false

            override fun filter(line: String): String? {
                if (line.trim { it <= ' ' }.startsWith("#")) {
                    return filterServiceName(line)
                }
                if (line.contains("; appMap = map:{")) {
                    appMap = true
                    return filterServiceName(line)
                }
                if (appMap) {
                    if (line.startsWith("    }")) {
                        appMap = false
                        return filterServiceName(line)
                    }
                    if (line.contains("; $appName = map:{")) {
                        currentApp = true
                    }
                    return if (currentApp) {
                        if (line.startsWith("        }")) {
                            currentApp = false
                        }
                        filterServiceName(line)
                    } else {
                        null
                    }
                } else {
                    return filterServiceName(line)
                }
            }
        }
    }

    // -----------------------------------------------------
    //                                  Configuration Filter
    //                                  --------------------
    protected fun createEnvPropertiesFilter(): FileTextLineFilter {
        return FileTextLineFilter { line -> filterServiceName(filterJdbcSettings(line)) }
    }

    protected fun createPomXmlFilter(): FileTextLineFilter {
        return FileTextLineFilter { line -> filterServiceName(filterJdbcSettings(line)) }
    }

    protected fun createFwAssistantDirectorFilter(): FileTextLineFilter {
        return FileTextLineFilter { line ->
            if (line.trim { it <= ' ' }.startsWith("direction.directCors")) {
                return@FileTextLineFilter null
            }
            if (line.trim { it <= ' ' }.endsWith("CorsHook;")) {
                null
            } else filterServiceName(line)
        }
    }

    // ===================================================================================
    //                                                                       Common Filter
    //                                                                       =============
    protected fun filterJdbcSettings(line: String): String {
        var line = line
        // e.g. basicInfoMap.dfprop, databaseInfoMap.dfprop, _env.properties
        val toDatabase = "; database = mysql"
        val toDriver = "com.mysql.jdbc.Driver"
        val toUrl = "jdbc:mysql://localhost:3306/maihamadb"
        val toSyncUrl = "jdbc:mysql://localhost:3306/maihamasyncdb"
        line = Srl.replace(line, "; database = h2", toDatabase)
        line = Srl.replace(line, "org.h2.Driver", toDriver)
        line = Srl.replace(line, "jdbc:h2:file:../etc/testdb/maihamadb", toUrl)
        line = Srl.replace(line, "jdbc:h2:file:\$classes(org.docksidestage.dbflute.allcommon.DBCurrent.class)/../../etc/testdb/maihamadb",
                toUrl)
        line = Srl.replace(line, "jdbc:h2:file:../etc/testdb/maihamasyncdb", toSyncUrl)

        // pom.xml
        val toProperty = "<mysql.jdbc.version>5.1.47</mysql.jdbc.version>"
        val toGroupId = "<groupId>mysql</groupId>"
        val artifactId = "<artifactId>mysql-connector-java</artifactId>"
        val toVersion = "<version>\${mysql.jdbc.version}</version>"
        val h2version = Srl.extractScopeFirst(line, "<h2.jdbc.version>", "</h2.jdbc.version>")
        if (h2version != null) {
            val version = h2version.content
            line = Srl.replace(line, "<h2.jdbc.version>$version</h2.jdbc.version>", toProperty)
        }
        line = Srl.replace(line, "<groupId>com.h2database</groupId>", toGroupId)
        line = Srl.replace(line, "<artifactId>h2</artifactId>", artifactId)
        line = Srl.replace(line, "<version>\${h2.jdbc.version}</version>", toVersion)
        return line
    }

    protected fun filterServiceName(str: String): String {
        return serviceNameFilter.filter(str)
    }

    // ===================================================================================
    //                                                                       ReplaceSchema
    //                                                                       =============
    @Throws(IOException::class)
    protected fun adjustReplaceSchemaDdl(canonicalPath: String, baseDir: String, outputFile: String) {
        val basicDdl = "replace-schema-10-basic.sql"
        val syncdbDdl = "replace-schema-99-syncdb.sql"
        if (Srl.endsWith(canonicalPath, "/$basicDdl", "/$syncdbDdl")) {
            val systemDdl = "replace-schema-00-system.sql"
            val srcFile = prepareStartupMySqlFile(systemDdl)
            val destFile = File("$baseDir/$systemDdl")
            copyFile(srcFile, destFile)
        }
        if (Srl.endsWith(canonicalPath, "/$basicDdl")) {
            val srcFile = prepareStartupMySqlFile(basicDdl)
            val destFile = File("$baseDir/$basicDdl")
            copyFile(srcFile, destFile) // override
        }
        if (Srl.endsWith(canonicalPath, "/$syncdbDdl")) {
            val srcFile = prepareStartupMySqlFile(basicDdl) // same as basic DDL
            val destFile = File(outputFile)
            copyFile(srcFile, destFile) // override
        }
    }

    @Throws(IOException::class)
    protected fun prepareStartupMySqlFile(systemDdl: String): File {
        return File(projectDir.canonicalPath + "/etc/startup/mysql-" + systemDdl)
    }

    // ===================================================================================
    //                                                                       Determination
    //                                                                       =============
    protected fun isAppResource(canonicalPath: String): Boolean {
        return canonicalPath.contains("/org/docksidestage/app/")
    }

    protected fun isAppMigrated(canonicalPath: String): Boolean {
        return (Srl.endsWith(canonicalPath, "/app/web", "/app/logic") //
                || Srl.containsAny(canonicalPath //
                , "/app/web/RootAction", "/app/web/base", "/app/web/signin", "/app/web/mypage" // web
                , "/app/logic/context", "/app/logic/i18n" // logic
        ))
    }

    protected fun isWebInfViewResource(canonicalPath: String): Boolean {
        return canonicalPath.contains("/WEB-INF/view/")
    }

    protected fun isViewMigrated(canonicalPath: String): Boolean {
        return Srl.containsAny(canonicalPath, "/view/common", "/view/error", "/view/signin", "/view/mypage")
    }

    protected fun isPlainMigration(baseDir: String, pureFile: String): Boolean {
        return (pureFile.endsWith(".xls") || pureFile.endsWith(".jar") // binary

                || baseDir.contains("/dbflute-1.x/") // no need to filter

                || baseDir.contains(".settings") // also

                || baseDir.contains("/etc/eclipse") // also

                || baseDir.contains("/etc/mysql")) // also
    }

    // -----------------------------------------------------
    //                                          Not Migrated
    //                                          ------------
    protected fun isMyLastaOnlyExample(canonicalPath: String): Boolean {
        return Srl.containsAny(canonicalPath, "/mylasta/mail/")
    }

    protected fun isResourcesOnlyExample(canonicalPath: String): Boolean {
        return Srl.containsAny(canonicalPath, "/resources/mail/")
    }

    protected fun isStartUpTool(canonicalPath: String): Boolean {
        return Srl.containsAny(canonicalPath, "/etc/startup", "/org/docksidestage/startup")
    }

    protected fun isDemoTestResource(canonicalPath: String): Boolean {
        return Srl.containsAny(canonicalPath, "/org/docksidestage/DemoTest", "/org/docksidestage/.gitignore")
    }

    protected fun isTestDbResource(canonicalPath: String): Boolean {
        return Srl.containsAny(canonicalPath, "/etc/testdb", "/etc/.gitignore")
    }

    protected fun isTestClientResource(canonicalPath: String): Boolean {
        return Srl.containsAny(canonicalPath, "/lidoisle")
    }

    protected fun isDBFluteClientLog(canonicalPath: String): Boolean {
        return Srl.containsAny(canonicalPath, "/log/dbflute.log", "/log/velocity.log")
    }

    protected fun isH2DatabaseDDL(canonicalPath: String): Boolean {
        return Srl.containsAny(canonicalPath, "/replace-schema-80-comment.sql")
    }

    protected fun isErdImage(canonicalPath: String): Boolean {
        return Srl.containsAny(canonicalPath, "/maihamadb.png")
    }

    protected fun isOssText(canonicalPath: String): Boolean {
        return Srl.containsAny(canonicalPath, "/README.md") // LICENSE, NOTICE are migrated just in case
    }

    protected fun isGitDir(canonicalPath: String): Boolean {
        return Srl.containsAny(canonicalPath, "/.git/")
    }

    protected fun isBuildDir(canonicalPath: String): Boolean {
        return Srl.containsAny(canonicalPath, "/target/")
    }

    // ===================================================================================
    //                                                                     Physical Helper
    //                                                                     ===============
    protected fun mkdirs(dirPath: String) {
        val dir = File(dirPath)
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }

    @Throws(IOException::class)
    protected fun copyFile(currentFile: File, outputFile: File) {
        logger.debug("...Copying to {}", bulidDisplayPath(outputFile.canonicalPath))
        LdiFileUtil.copy(currentFile, outputFile)
    }

    @Throws(IOException::class)
    protected fun writeFile(textIO: FileTextIO, outputFile: String, filtered: String) {
        logger.debug("...Writing to {}", bulidDisplayPath(outputFile))
        textIO.write(outputFile, filtered)
    }

    @Throws(IOException::class)
    protected fun bulidDisplayPath(outputFile: String): String {
        val parentFile = projectDir.parentFile
        return Srl.substringFirstRear(outputFile, parentFile.canonicalPath)
    }

    companion object {

        // ===================================================================================
        //                                                                          Definition
        //                                                                          ==========
        private val logger = LoggerFactory.getLogger(NewProjectCreator::class.java)
    }
}
