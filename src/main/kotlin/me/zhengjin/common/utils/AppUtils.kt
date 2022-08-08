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

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Arrays
import java.util.UUID

object AppUtils {
    private val chars = arrayOf(
        "a", "b", "c", "d", "e", "f",
        "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
        "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
        "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
        "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
        "W", "X", "Y", "Z"
    )

    /**
     *
     * 短8位UUID思想其实借鉴微博短域名的生成方式，但是其重复概率过高，而且每次生成4个，需要随即选取一个。
     * 本算法利用62个可打印字符，通过随机生成32位UUID，由于UUID都为十六进制，所以将UUID分成8组，每4个为一组，然后通过模62操作，结果作为索引取出字符，
     * 这样重复率大大降低。
     * 经测试，在生成一千万个数据也没有出现重复，完全满足大部分需求。
     *
     */
    fun getAppKey(): String {
        val shortBuffer = StringBuffer()
        val uuid = UUID.randomUUID().toString().replace("-", "")
        for (i in 0..7) {
            val str = uuid.substring(i * 4, i * 4 + 4)
            val x = str.toInt(16)
            shortBuffer.append(chars[x % 0x3E])
        }
        return shortBuffer.toString()
    }

    /**
     *
     * 通过appId和内置关键词生成APP Secret
     *
     */
    fun getAppSecret(appId: String, serverName: String): String {
        return try {
            val array = arrayOf(appId, serverName)
            val sb = StringBuffer()
            // 字符串排序
            Arrays.sort(array)
            for (i in array.indices) {
                sb.append(array[i])
            }
            val str = sb.toString()
            val md = MessageDigest.getInstance("SHA-1")
            md.update(str.toByteArray())
            val digest = md.digest()
            val hexStr = StringBuffer()
            var shaHex: String
            for (i in digest.indices) {
                shaHex = Integer.toHexString(digest[i].toInt() and 0xFF)
                if (shaHex.length < 2) {
                    hexStr.append(0)
                }
                hexStr.append(shaHex)
            }
            hexStr.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            throw RuntimeException()
        }
    }
}
