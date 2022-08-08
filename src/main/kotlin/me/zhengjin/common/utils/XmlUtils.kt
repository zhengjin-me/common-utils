/*
 * MIT License
 *
 * Copyright (c) 2022 ZhengJin Fang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.zhengjin.common.utils

import cn.hutool.core.exceptions.ExceptionUtil
import com.sun.xml.bind.api.JAXBRIContext
import com.sun.xml.bind.marshaller.NamespacePrefixMapper
import org.apache.commons.text.StringEscapeUtils
import org.slf4j.LoggerFactory
import java.io.FileInputStream
import java.io.IOException
import java.io.StringReader
import java.io.StringWriter
import java.util.Base64
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller

object XmlUtils {

    @JvmStatic
    private val logger = LoggerFactory.getLogger(XmlUtils::class.java)!!

    /**
     * 每次在创建JAXBContext实例时，JAXBContext内部都需要维护好Java类和XML之间的映射关系，这个操作十分消耗性能
     * JAXBContext 是线程安全的，但是Marshaller, Unmarshaller, 和 Validator都不是线程安全的。
     * 所以 JAXBContext 可以被缓存供全局使用
     */
    @JvmStatic
    private val jaxbContextCache = mutableMapOf<String, JAXBContext>()

    @JvmStatic
    private fun getJaxbContext(clazz: Class<*>): JAXBContext {
        val className = clazz.name
        if (!jaxbContextCache.containsKey(className)) {
            jaxbContextCache[className] = JAXBRIContext.newInstance(clazz)
        }
        return jaxbContextCache[className]!!
    }

    /**
     * @param clazz                     需要操作的类
     * @param namespacePrefixMapper     命名空间前缀映射
     * @param fragment                  是否忽略标头
     * @param format                    是否生格式化
     */
    @JvmStatic
    private fun getMarshal(clazz: Class<*>, namespacePrefixMapper: NamespacePrefixMapper? = null, fragment: Boolean = true, format: Boolean = false): Marshaller {
        val jaxbContext = getJaxbContext(clazz)
        val marshaller = jaxbContext.createMarshaller()
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8")
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, fragment)
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, format)
        if (namespacePrefixMapper != null) {
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", namespacePrefixMapper)
        }
        return marshaller
    }

    @JvmStatic
    private fun getUnmarshal(clazz: Class<*>): Unmarshaller {
        val context = getJaxbContext(clazz)
        return context.createUnmarshaller()
    }

    /**
     * @param escapeXml                 转义xml字符
     * @param namespacePrefixMapper     命名空间前缀映射
     * @param escapeHtml                转义html字符
     * @param fragment                  是否忽略xml标头
     * @param format                    是否格式化
     */
    @JvmStatic
    @JvmOverloads
    @Throws(Exception::class)
    fun <T : Any> entityToXml(
        entity: T,
        namespacePrefixMapper: NamespacePrefixMapper? = null,
        escapeXml: Boolean = false,
        escapeHtml: Boolean = false,
        fragment: Boolean = true,
        format: Boolean = false
    ): String {
        val marshaller = getMarshal(entity.javaClass, namespacePrefixMapper, fragment, format)
        val sw = StringWriter()
        marshaller.marshal(entity, sw)
        return when {
            escapeXml -> StringEscapeUtils.escapeXml11(sw.toString())
            escapeHtml -> StringEscapeUtils.escapeHtml4(sw.toString())
            else -> sw.toString()
        }
        //        return sw.toString().replace("standalone=\"yes\"", "")
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T> xmlToEntity(clazz: Class<T>, xmlStr: String): T? {
        var reEntity: T? = null
        try {
            val unmarshaller = getUnmarshal(clazz)
            reEntity = unmarshaller.unmarshal(StringReader(xmlStr)) as T
        } catch (e: JAXBException) {
            logger.error("封装Xml到对象失败：" + ExceptionUtil.stacktraceToString(e))
        }

        return reEntity
    }

    /**
     * 将文件转换为Base64字符串
     * @param path 文件路径
     * @return base64字符串
     */
    @JvmStatic
    fun encodeBase64File(path: String): String {
        val inputStream: FileInputStream?
        var data: ByteArray? = null
        try {
            inputStream = FileInputStream(path)
            inputStream.use {
                data = ByteArray(inputStream.available())
                inputStream.read(data)
            }
        } catch (e: IOException) {
            logger.error(e.message)
        }
        // 加密
        return Base64.getEncoder().encodeToString(data!!)
    }

    @JvmStatic
    fun encodeBase64String(s: String) = Base64.getEncoder().encodeToString(s.toByteArray())

    @JvmStatic
    fun encodeBase64String(s: ByteArray) = Base64.getEncoder().encodeToString(s)

    @JvmStatic
    fun decodeBase64String(s: String) = String(Base64.getDecoder().decode(s.toByteArray()))

    /**
     * &amp; (ampersand) 替换为 &amp;amp;
     * &lt; (小于) 替换为 &amp;lt;
     * &gt; (大于) 替换为 &amp;gt;
     * &quot; (双引号) 替换为 &amp;quot;
     */
    @JvmStatic
    fun escapeXml(data: String?): String? {
        if (data.isNullOrBlank()) return data
        return data.replace(">", "&gt;")
            .replace("<", "&lt;")
            .replace("\"", "&quot;")
            .replace("&", "&amp;")
    }
}
