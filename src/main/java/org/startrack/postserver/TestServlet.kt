package org.startrack.postserver

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.*
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
        println("${Date()} - got POST request")

        val type = httpServletRequest.getParameter("type")
        println("type: $type")

        val headerNames = httpServletRequest.headerNames
        println("headers:")
        while (headerNames.hasMoreElements()) {
            val name = headerNames.nextElement()
            println("$name: ${httpServletRequest.getHeader(name)}")
        }

        val fileTime =  httpServletRequest.getHeader("filetime")
        val imei =  httpServletRequest.getHeader("imei")
        val fileType =  httpServletRequest.getHeader("fileType")
        val fileMd5 =  httpServletRequest.getHeader("fileMD5")
        val fileId =  httpServletRequest.getHeader("fileId")

        val content = readBody(httpServletRequest)
        if (content != null) {
            println("debug 1")
            if (content.isEmpty()) {
                httpServletResponse.addHeader("code","1")
                httpServletResponse.status = HttpServletResponse.SC_NO_CONTENT
            }
            else {
                println("upload${File.separator}${getTimestamp(fileTime).time}$fileType")
                FileOutputStream("upload${File.separator}${getTimestamp(fileTime).time}$fileType").use { fos ->
                    fos.write(content)
                    fos.close()
                }
                httpServletResponse.addHeader("code", "0")
                httpServletResponse.status = HttpServletResponse.SC_OK
            }
        }

        val md = MessageDigest.getInstance("MD5")
        md.update(content)
        val digest = md.digest()
        val sb = StringBuffer()
        for (b in digest) {
            sb.append(String.format("%02x", b.toInt() and 0xff))
        }

        println("generated md5: $sb\n")

        httpServletResponse.addHeader("type", type)
        httpServletResponse.addHeader("md5", sb.toString())
        httpServletResponse.writer.write("OK")
        httpServletResponse.writer.flush()
        httpServletResponse.writer.close()
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

    private fun getTimestamp(fileTime: String): Date {
        val c = Calendar.getInstance()

        c.set(Calendar.ZONE_OFFSET, 0)
        c.set(Calendar.YEAR, fileTime.substring(2,4).toInt(16) + 2000)
        c.set(Calendar.MONTH, fileTime.substring(4,6).toInt(16) - 1)
        c.set(Calendar.DAY_OF_MONTH, fileTime.substring(6,8).toInt(16))
        c.set(Calendar.HOUR_OF_DAY, fileTime.substring(8,10).toInt(16))
        c.set(Calendar.MINUTE, fileTime.substring(10,12).toInt(16))
        c.set(Calendar.MILLISECOND, fileTime.substring(12,14).toInt(16))

        println(c.time)
        println(c.timeInMillis)
        return c.time
    }

}
