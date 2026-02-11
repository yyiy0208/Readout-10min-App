package com.readout10min.data.models

import java.util.UUID
import kotlinx.serialization.Serializable
import com.readout10min.data.serializers.UUIDSerializer

@Serializable
data class PracticeRecord(
    @Serializable(with = UUIDSerializer::class) val id: UUID? = null,
    @Serializable(with = UUIDSerializer::class) val user_id: UUID,
    @Serializable(with = UUIDSerializer::class) val paragraph_id: UUID,
    @Serializable(with = UUIDSerializer::class) val content_id: UUID,
    val practice_date: String? = null, // 暂时使用字符串存储日期，后续可以添加日期序列化器
    val duration: Int = 0, // 实际朗读时间（秒）
    val accuracy: Float = 0.0f, // 准确率
    val fluency: Float = 0.0f, // 流利度
    val pronunciation_score: Float = 0.0f, // 发音分数
    val recording_url: String? = null,
    val notes: String? = null,
    val created_at: String? = null // 暂时使用字符串存储日期
)
