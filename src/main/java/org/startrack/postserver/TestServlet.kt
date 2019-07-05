package org.startrack.postserver

import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException
import javax.servlet.ServletException
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TestServlet : HttpServlet() {
    @Throws(IOException::class, ServletException::class)
    public override fun doGet(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse) {
        println("got GET request")
        httpServletResponse.writer.print("Hello from servlet")
    }

    @Throws(ServletException::class, IOException::class)
    public override fun doPost(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse) {
        println("got POST request")

        val type = httpServletRequest.getParameter("type")
        println("type: $type")

        val headerNames = httpServletRequest.headerNames
        println("headers:")
        while (headerNames.hasMoreElements()) {
            val name = headerNames.nextElement()
            println("$name: ${httpServletRequest.getHeader(name)}")
        }

        val fileTime =  httpServletRequest.getHeader("filetime")
        if (fileTime != null) {
            println("filetime: $fileTime")
        }

        val imei =  httpServletRequest.getHeader("imei")
        if (imei != null) {
            println("imei: $imei")
        }

        val fileType =  httpServletRequest.getHeader("fileType")
        if (fileType != null) {
            println("fileType: $fileType")
        }

        val fileMd5 =  httpServletRequest.getHeader("fileMD5")
        if (fileMd5 != null) {
            println("fileMD5: $fileMd5")
        }

        val fileId =  httpServletRequest.getHeader("fileId")
        if (fileId != null) {
            println("fileId: $fileId")
        }

        val content = readBody(httpServletRequest)
        if (content!!.isEmpty()) {
            httpServletResponse.addHeader("code","1")
        }
        else {
            FileOutputStream("upload\\$fileTime$fileType").use { fos ->
                fos.write(content)
                fos.close()
            }
            httpServletResponse.addHeader("code", "0")
        }
        httpServletResponse.addHeader("ext", fileId)
        httpServletResponse.addHeader("md5", fileMd5)
    }

    private fun readBody(req: HttpServletRequest): ByteArray? {
        var body: ByteArray? = null
        try {
            val input = req.inputStream
            val output = ByteArrayOutputStream()

            val sizeStr = req.getHeader("fileSize")
            val size = if (sizeStr != null)
                Integer.parseInt(sizeStr)
            else
                Integer.parseInt(req.getHeader("content-length"))
            var recvsize = 0
            val buff = ByteArray(10240)
            var b = input.read(buff)
            while(b > -1) {
                recvsize += b
                output.write(buff, 0, b)
                b = input.read(buff)
            }
            if (output.size() > 0)
                body = output.toByteArray()
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
        return body
    }

}
