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

package me.zhengjin.common.autoconfig

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.deser.key.LocalDateTimeKeyDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.time.LocalDateTime
import java.util.TimeZone

@AutoConfiguration
class JacksonAutoConfiguration {

    @Bean
    @Primary
    @ConditionalOnMissingBean(ObjectMapper::class)
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    fun jacksonObjectMapper(builder: Jackson2ObjectMapperBuilder): ObjectMapper {
        val objectMapper = builder.createXmlMapper(false).build<ObjectMapper>()

        // 通过该方法对mapper对象进行设置，所有序列化的对象都将按改规则进行序列化
        // Include.ALWAYS 序列化的时候序列对象的所有属性 默认
        // Include.NON_DEFAULT 属性为默认值不序列化
        // Include.NON_EMPTY 属性为 空（""） 或者为 NULL 都不序列化，则返回的json是没有这个字段的。这样对移动端会更省流量
        // Include.NON_NULL 属性为NULL 不序列化
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS)
        // 默认为ISO8601格式时间
        objectMapper.dateFormat = StdDateFormat().withTimeZone(TimeZone.getTimeZone("Asia/Shanghai"))
        // 关闭反序列化出现未定义属性时 报错
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        // 关闭日期序列化为时间戳的功能
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        // 关闭序列化的时候没有为属性找到getter方法 报错
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)

        // 允许出现特殊字符和转义符
        objectMapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true)
        // 允许出现单引号
        objectMapper.configure(JsonReadFeature.ALLOW_SINGLE_QUOTES.mappedFeature(), true)
        // 字段保留，将null值转为""
//        objectMapper.serializerProvider.setNullValueSerializer(object : JsonSerializer<Any?>() {
//            override fun serialize(o: Any?, jsonGenerator: JsonGenerator,
//                                   serializerProvider: SerializerProvider?) {
//                jsonGenerator.writeString("")
//            }
//        })

        val simpleModule = SimpleModule()
        // json值序列化
        simpleModule.addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer.INSTANCE)
        // json值反序列化
        simpleModule.addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer.INSTANCE)
        // json键序列化
        simpleModule.addKeySerializer(LocalDateTime::class.java, LocalDateTimeSerializer.INSTANCE)
        // json键反序列化
        simpleModule.addKeyDeserializer(LocalDateTime::class.java, LocalDateTimeKeyDeserializer.INSTANCE)
        objectMapper.registerModule(simpleModule)
        return objectMapper
    }
}
