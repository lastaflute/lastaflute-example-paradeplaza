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

import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException
import org.apache.commons.fileupload.FileUploadException
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.commons.fileupload.servlet.ServletFileUpload
import org.dbflute.helper.message.ExceptionMessageBuilder
import org.lastaflute.core.message.UserMessages
import org.lastaflute.web.exception.Forced404NotFoundException
import org.lastaflute.web.ruts.multipart.MultipartFormFile
import org.lastaflute.web.ruts.multipart.MultipartRequestHandler
import org.lastaflute.web.ruts.multipart.MultipartRequestWrapper
import org.lastaflute.web.ruts.multipart.exception.MultipartExceededException
import org.lastaflute.web.util.LaServletContextUtil
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.Serializable
import java.util.*
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest

/**
 * @author modified by jflute (originated in Seasar)
 */
class ParadeplazaMultipartRequestHandler : MultipartRequestHandler {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected lateinit var elementsAll: MutableMap<String, Any>
    protected lateinit var elementsFile: MutableMap<String, MultipartFormFile>
    protected lateinit var elementsText: MutableMap<String, Array<String>>

    protected// one HTTP proxy tool already limits the size (e.g. 3450 bytes)
    // so specify this size for test
    // you can override as you like it
    val boundaryLimitSize: Int
        get() = 2000

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    protected val sizeMax: Long
        get() = DEFAULT_SIZE_MAX

    protected val sizeThreshold: Long
        get() = DEFAULT_SIZE_THRESHOLD.toLong()

    protected val repositoryPath: String?
        get() {
            val tempDirFile = LaServletContextUtil.getServletContext().getAttribute(CONTEXT_TEMPDIR_KEY) as File
            var tempDir: String? = tempDirFile.absolutePath
            if (tempDir == null || tempDir.isEmpty()) {
                tempDir = System.getProperty(JAVA_IO_TMPDIR_KEY)
            }
            return tempDir
        }

    // ===================================================================================
    //                                                                      Handle Request
    //                                                                      ==============
    @Throws(ServletException::class)
    override fun handleRequest(request: HttpServletRequest) {
        // /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
        // copied from super's method and extends it
        // basically for JVN#14876762
        // thought not all problems are resolved however the main case is safety
        // - - - - - - - - - -/
        val upload = createServletFileUpload(request)
        prepareElementsHash()
        try {
            val items = parseRequest(request, upload)
            mappingParameter(request, items)
        } catch (e: SizeLimitExceededException) {
            handleSizeLimitExceededException(request, e)
        } catch (e: FileUploadException) {
            handleFileUploadException(e)
        }
    }

    // ===================================================================================
    //                                                            Create ServletFileUpload
    //                                                            ========================
    protected fun createServletFileUpload(request: HttpServletRequest): ServletFileUpload {
        val fileItemFactory = createDiskFileItemFactory()
        val upload = newServletFileUpload(fileItemFactory)
        upload.headerEncoding = request.characterEncoding
        upload.sizeMax = sizeMax
        return upload
    }

    protected fun newServletFileUpload(fileItemFactory: DiskFileItemFactory): ServletFileUpload {
        return object : ServletFileUpload(fileItemFactory) {
            override fun getBoundary(contentType: String): ByteArray { // for security
                val boundary = super.getBoundary(contentType)
                checkBoundarySize(contentType, boundary)
                return boundary
            }
        }
    }

    protected fun checkBoundarySize(contentType: String, boundary: ByteArray) {
        val boundarySize = boundary.size
        val limitSize = boundaryLimitSize
        if (boundarySize > boundaryLimitSize) {
            throwTooLongBoundarySizeException(contentType, boundarySize, limitSize)
        }
    }

    protected fun throwTooLongBoundarySizeException(contentType: String, boundarySize: Int, limitSize: Int) {
        val br = ExceptionMessageBuilder()
        br.addNotice("Too long boundary size so treats it as 404.")
        br.addItem("Advice")
        br.addElement("Against for JVN14876762.")
        br.addElement("Boundary size is limited by Framework.")
        br.addElement("Too long boundary is treated as 404 because it's thought of as attack.")
        br.addElement("")
        br.addElement("While, you can override the boundary limit size")
        br.addElement(" in " + ParadeplazaMultipartRequestHandler::class.java.simpleName + ".")
        br.addItem("Content Type")
        br.addElement(contentType)
        br.addItem("Boundary Size")
        br.addElement(boundarySize)
        br.addItem("Limit Size")
        br.addElement(limitSize)
        val msg = br.buildExceptionMessage()
        throw Forced404NotFoundException(msg, UserMessages.empty()) // heavy attack!? so give no page to tell wasted action
    }

    protected fun createDiskFileItemFactory(): DiskFileItemFactory {
        val repository = createRepositoryFile()
        return DiskFileItemFactory(sizeThreshold.toInt(), repository)
    }

    protected fun createRepositoryFile(): File {
        return File(repositoryPath!!)
    }

    // ===================================================================================
    //                                                                      Handling Parts
    //                                                                      ==============
    protected fun prepareElementsHash() {
        elementsText = Hashtable()
        elementsFile = Hashtable()
        elementsAll = Hashtable()
    }

    @Throws(FileUploadException::class)
	@Suppress("deprecation")
    protected fun parseRequest(request: HttpServletRequest, upload: ServletFileUpload): List<FileItem> {
        return upload.parseRequest(request)
    }

    protected fun mappingParameter(request: HttpServletRequest, items: List<FileItem>) {
        showFieldLoggingTitle()
        val iter = items.iterator()
        while (iter.hasNext()) {
            val item = iter.next()
            if (item.isFormField) {
                showFormFieldParameter(item)
                addTextParameter(request, item)
            } else {
                showFileFieldParameter(item)
                val itemName = item.name
                if (itemName != null && !itemName.isEmpty()) {
                    addFileParameter(item)
                }
            }
        }
    }

    protected fun showFieldLoggingTitle() {
        // logging filter cannot show the parameters when multi-part so logging here
        if (logger.isDebugEnabled) {
            logger.debug("[Multipart Request Parameter]")
        }
    }

    protected fun showFormFieldParameter(item: FileItem) {
        if (logger.isDebugEnabled) {
            logger.debug("[param] {}={}", item.fieldName, item.string)
        }
    }

    protected fun showFileFieldParameter(item: FileItem) {
        if (logger.isDebugEnabled) {
            logger.debug("[param] {}:{name={}, size={}}", item.fieldName, item.name, item.size)
        }
    }

    protected fun handleSizeLimitExceededException(request: HttpServletRequest, e: SizeLimitExceededException) {
        val actual = e.actualSize
        val permitted = e.permittedSize
        val msg = "Exceeded size of the multipart request: actual=$actual permitted=$permitted"
        request.setAttribute(MultipartRequestHandler.MAX_LENGTH_EXCEEDED_KEY, MultipartExceededException(msg, actual, permitted, e))
        try {
            val `is` = request.inputStream
            try {
                val buf = ByteArray(1024)
                while (`is`.read(buf) != -1) {
                }
            } catch (ignored: Exception) {
            } finally {
                try {
                    `is`.close()
                } catch (ignored: Exception) {
                }

            }
        } catch (ignored: Exception) {
        }

    }

    @Throws(ServletException::class)
    protected fun handleFileUploadException(e: FileUploadException) {
        // suppress logging because it can be caught by logging filter
        //log.error("Failed to parse multipart request", e);
        throw ServletException("Failed to upload the file.", e)
    }

    // ===================================================================================
    //                                                                           Roll-back
    //                                                                           =========
    override fun rollback() {
        val iter = elementsFile.values.iterator()
        while (iter.hasNext()) {
            val formFile = iter.next()
            formFile.destroy()
        }
    }

    // ===================================================================================
    //                                                                            Add Text
    //                                                                            ========
    protected fun addTextParameter(request: HttpServletRequest, item: FileItem) {
        val name = item.fieldName
        val encoding = request.characterEncoding
        var value: String? = null
        var haveValue = false
        if (encoding != null) {
            try {
                value = item.getString(encoding)
                haveValue = true
            } catch (e: Exception) {
            }

        }
        if (!haveValue) {
            value = try {
                item.getString("ISO-8859-1")
            } catch (uee: java.io.UnsupportedEncodingException) {
                item.string
            }

			// no more used so comment out (also unneeded in Java example?)
            //haveValue = true
        }
        if (request is MultipartRequestWrapper) {
            request.setParameter(name, value)
        }
        if (value != null) {
            val oldArray = elementsText[name]
            val newArray: Array<String>
            newArray = if (oldArray != null) {
                oldArray + arrayOf(value)
            } else {
                arrayOf(value)
            }
            elementsText[name] = newArray
            elementsAll[name] = newArray
        }
    }

    protected fun addFileParameter(item: FileItem) {
        val formFile = newActionMultipartFormFile(item)
        elementsFile[item.fieldName] = formFile
        elementsAll[item.fieldName] = formFile
    }

    protected fun newActionMultipartFormFile(item: FileItem): ActionMultipartFormFile {
        return ActionMultipartFormFile(item)
    }

    // ===================================================================================
    //                                                                              Finish
    //                                                                              ======
    override fun finish() {
        rollback()
    }

    // ===================================================================================
    //                                                                           Form File
    //                                                                           =========
    protected class ActionMultipartFormFile(protected val fileItem: FileItem) : MultipartFormFile, Serializable {

        @Throws(IOException::class)
        override fun getFileData(): ByteArray {
            return fileItem.get()
        }

        @Throws(IOException::class)
        override fun getInputStream(): InputStream {
            return fileItem.inputStream
        }

        override fun getContentType(): String {
            return fileItem.contentType
        }

        override fun getFileSize(): Int {
            return fileItem.size.toInt()
        }

        override fun getFileName(): String {
            return getBaseFileName(fileItem.name)
        }

        protected fun getBaseFileName(filePath: String): String {
            val fileName = File(filePath).name
            var colonIndex = fileName.indexOf(":")
            if (colonIndex == -1) {
                colonIndex = fileName.indexOf("\\\\") // Windows SMB
            }
            val backslashIndex = fileName.lastIndexOf("\\")
            return if (colonIndex > -1 && backslashIndex > -1) {
                fileName.substring(backslashIndex + 1)
            } else {
                fileName
            }
        }

        override fun destroy() {
            fileItem.delete()
        }

        override fun toString(): String {
            return "formFile:{$fileName}"
        }

        companion object {

            private const val serialVersionUID = 1L
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    override fun getAllElements(): Map<String, Any> {
        return elementsAll
    }

    override fun getTextElements(): Map<String, Array<String>> {
        return elementsText
    }

    override fun getFileElements(): Map<String, MultipartFormFile> {
        return elementsFile
    }

    companion object {

        // ===================================================================================
        //                                                                          Definition
        //                                                                          ==========
        private val logger = LoggerFactory.getLogger(ParadeplazaMultipartRequestHandler::class.java)
        const val DEFAULT_SIZE_MAX = (250 * 1024 * 1024).toLong() // 250MB
        const val DEFAULT_SIZE_THRESHOLD = 256 * 1024 // 250KB
        protected const val CONTEXT_TEMPDIR_KEY = "javax.servlet.context.tempdir"
        protected const val JAVA_IO_TMPDIR_KEY = "java.io.tmpdir"
    }
}
