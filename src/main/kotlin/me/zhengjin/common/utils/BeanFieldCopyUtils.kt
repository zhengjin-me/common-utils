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

import org.springframework.beans.BeanUtils
import org.springframework.beans.FatalBeanException
import org.springframework.util.ClassUtils
import java.lang.reflect.Modifier

object BeanFieldCopyUtils {
    /**
     * 对象字段拷贝
     * @param source        源对象
     * @param target        目标对象
     * @param includeFields 拷贝的字段(只有列出的字段会被拷贝)
     */
    fun copyProperties(source: Any, target: Any, vararg includeFields: String) {
        val includeFieldList = if (includeFields.isEmpty()) null else includeFields.toList()
        val actualEditable = target.javaClass
        val targetPds = BeanUtils.getPropertyDescriptors(actualEditable)
        targetPds.forEach {
            val writeMethod = it.writeMethod
            if (writeMethod != null && includeFieldList != null && includeFieldList.contains(it.name)) {
                val sourcePd = BeanUtils.getPropertyDescriptor(source.javaClass, it.name)
                if (sourcePd != null) {
                    val readMethod = sourcePd.readMethod
                    if (readMethod != null && ClassUtils.isAssignable(writeMethod.parameterTypes[0], readMethod.returnType)) {
                        try {
                            if (!Modifier.isPublic(readMethod.declaringClass.modifiers)) {
                                readMethod.isAccessible = true
                            }
                            val value = readMethod.invoke(source)
                            if (!Modifier.isPublic(writeMethod.declaringClass.modifiers)) {
                                writeMethod.isAccessible = true
                            }
                            writeMethod.invoke(target, value)
                        } catch (e: Throwable) {
                            throw FatalBeanException("Could not copy property '${it.name}' from source to target", e)
                        }
                    }
                }
            }
        }
    }
}

fun BeanUtils.copySelectProperties(source: Any, target: Any, vararg includeFields: String) {
    BeanFieldCopyUtils.copyProperties(source, target, *includeFields)
}
