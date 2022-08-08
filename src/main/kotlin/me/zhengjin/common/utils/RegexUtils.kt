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

import cn.hutool.core.convert.Convert

object RegexUtils {
    const val MAIL_REGEX_STR =
        "^(([^<>()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$"
    val MAIL_REGEX = Regex(MAIL_REGEX_STR)

    const val MOBILE_NUMBER_REGEX_STR = "^1\\d{10}\$"
    val MOBILE_NUMBER_REGEX = Regex(MOBILE_NUMBER_REGEX_STR)

    const val NAME_REGEX_STR = "[^a-zA-Z0-9\\u4E00-\\u9FA5\\-_# ]"
    val NAME_REGEX = Regex(NAME_REGEX_STR)

    val ADDRESS_REGEX_STR = "[^a-zA-Z0-9\\u4E00-\\u9FA5\\-_# ]"
    val ADDRESS_REGEX = Regex(ADDRESS_REGEX_STR)

    /**
     * 是否为邮箱
     */
    fun isMailAddress(mailAddress: String) = MAIL_REGEX.matches(mailAddress)

    /**
     * 是否为有效手机号
     */
    fun isMobileNumber(mobile: String) = MOBILE_NUMBER_REGEX.matches(mobile)

    /**
     * 名称清理(全角转半角)
     * 仅允许中英文数字字符
     */
    fun nameCleanUp(address: String) = Convert.toDBC(address).replace(NAME_REGEX, "").trim()

    /**
     * 地址清理(全角转半角)
     * 仅允许中英文字符 _-#字符 与空格
     * 连续重复的_-#字符 与空格会被合并
     */
    fun addressCleanUp(address: String) = Convert.toDBC(address)
        .replace(ADDRESS_REGEX, "")
        .replace(Regex("\\s+"), " ")
        .replace(Regex("[_]+"), "_")
        .replace(Regex("[-]+"), "-")
        .replace(Regex("[#]+"), "#")
        .trim()
}
