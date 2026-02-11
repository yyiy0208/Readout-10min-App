package com.readout10min.data.models

import java.util.*
import kotlinx.serialization.Serializable
import com.readout10min.data.serializers.UUIDSerializer

@Serializable
data class Progress(
    @Serializable(with = UUIDSerializer::class) val user_id: UUID,
    @Serializable(with = UUIDSerializer::class) val content_id: UUID,
    val current_paragraph: Int = 1,
    val is_completed: Boolean = false
)
