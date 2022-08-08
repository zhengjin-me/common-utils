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

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

/**
 * @version V1.0
 * @Title: SpringBeanUtil
 * @Description:
 * @Author fangzhengjin
 * @Date 2017-10-13 11:17
 */
@Component
class SpringBeanUtils : ApplicationContextAware {

    @Throws(BeansException::class)
    override fun setApplicationContext(applicationcontext: ApplicationContext) {
        ctx = applicationcontext
    }

    companion object {

        @JvmStatic
        private var ctx: ApplicationContext? = null

        @JvmStatic
        private var objectMapper: ObjectMapper? = null

        /**
         * 通过name取得bean对象
         *
         * @param name spring bean name
         * @return spring bean对象
         */
        @JvmStatic
        fun getBean(name: String): Any {
            if (ctx == null) {
                throw NullPointerException("ApplicationContext is null")
            }
            return ctx!!.getBean(name)
        }

        /**
         * 通过类型取得bean对象
         *
         * @param clazz spring bean 类型
         * @return spring bean对象
         */
        @JvmStatic
        fun <T> getBean(clazz: Class<T>): T {
            if (ctx == null) {
                throw NullPointerException("ApplicationContext is null")
            }
            return ctx!!.getBean(clazz)
        }

        /**
         * 通过name 和 类型取得bean对象
         *
         * @param name  spring bean name
         * @param clazz spring bean 类型
         * @return spring bean对象
         */
        @JvmStatic
        fun <T> getBean(name: String, clazz: Class<T>): T {
            if (ctx == null) {
                throw NullPointerException("ApplicationContext is null")
            }
            return ctx!!.getBean(name, clazz)
        }

        @JvmStatic
        fun getObjectMapper(): ObjectMapper {
            if (objectMapper == null) {
                objectMapper = ObjectMapper()
            }
            return objectMapper!!
        }
    }
}
