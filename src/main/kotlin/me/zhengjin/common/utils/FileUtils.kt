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

import org.slf4j.LoggerFactory
import org.springframework.core.io.DefaultResourceLoader
import java.io.File
import java.io.InputStream

object FileUtils {
    @JvmStatic
    private val logger = LoggerFactory.getLogger(FileUtils::class.java)!!

    // JAR包外部文件优先使用
    @JvmStatic
    fun getFile(filePath: String): File? {
        return try {
            val template = File(filePath.removePrefix("classpath:"))
            return if (template.exists()) {
                template
            } else null
        } catch (ignore: Exception) {
            logger.error("load file $filePath error")
            null
        }
    }

    // JAR包内外文件使用
    @JvmStatic
    fun getFileAsInputStream(filePath: String): InputStream? {
        return try {
            val file = getFile(filePath)
            if (file != null && file.exists()) {
                return file.inputStream()
            }
            val resource = DefaultResourceLoader().getResource(filePath)
            if (resource.exists()) {
                resource.inputStream
            } else null
        } catch (ignore: Exception) {
            logger.error("load file $filePath by inputStream error")
            null
        }
    }
}
