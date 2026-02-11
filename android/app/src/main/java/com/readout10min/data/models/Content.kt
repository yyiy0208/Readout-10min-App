package com.readout10min.data.models

import java.time.Instant
import java.util.*
import kotlinx.serialization.Serializable
import com.readout10min.data.serializers.UUIDSerializer
import com.readout10min.data.serializers.InstantSerializer

@Serializable
data class Content(
    @Serializable(with = UUIDSerializer::class) val id: UUID = UUID.randomUUID(),
    val title: String,
    val author: String? = null,
    val source: String? = null,
    val file_url: String,
    val file_name: String,
    val file_type: String,
    val file_size: Int,
    val total_paragraphs: Int = 0,
    val total_words: Int = 0,
    val estimated_duration: Int = 0,
    val difficulty: String = "medium",
    val language: String = "en",
    @Serializable(with = InstantSerializer::class) val created_at: Instant = Instant.now(),
    @Serializable(with = InstantSerializer::class) val updated_at: Instant = Instant.now(),
    val created_by: String
)
