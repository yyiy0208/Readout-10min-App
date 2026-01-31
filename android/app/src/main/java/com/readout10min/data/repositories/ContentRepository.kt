package com.readout10min.data.repositories

import com.readout10min.data.SupabaseClient
import com.readout10min.data.models.Content
import com.readout10min.data.models.Paragraph
import com.readout10min.data.models.Progress
import io.github.jan.supabase.postgrest.*

import java.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

class ContentRepository {
    private val postgrest = SupabaseClient.client.postgrest

    suspend fun getContentList(): List<Content> {
        return try {
            // 直接调用 select() 得到结果
            val response = postgrest.from("content").select()
            response.decodeList<Content>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getContentById(id: UUID): Content? {
        return try {
            // 筛选条件写在 select { ... } 闭包内
            val response = postgrest.from("content").select {
                filter {
                    eq("id", id.toString()) // 建议转成 String 确保兼容性
                }
            }
            response.decodeSingleOrNull<Content>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getParagraphsByContentId(contentId: UUID): List<Paragraph> {
        return try {
            val response = postgrest.from("paragraphs").select {
                filter {
                    eq("content_id", contentId.toString())
                }
                order("paragraph_number", order = io.github.jan.supabase.postgrest.query.Order.ASCENDING)
            }
            response.decodeList<Paragraph>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun updateProgress(progress: Progress): Boolean {
        return try {
            // upsert 直接传对象
            postgrest.from("progress").upsert(progress) {
                // 如果需要指定冲突列，新版语法如下
                onConflict = "user_id,content_id" 
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun getRecentContent(userId: UUID): List<Content> {
        return try {
            val progressList = postgrest.from("progress").select {
                filter { eq("user_id", userId.toString()) }
                order("updated_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                limit(3)
            }.decodeList<Progress>()

            val contentIds = progressList.map { it.content_id.toString() }
            if (contentIds.isEmpty()) return emptyList()

            // 使用 filter { `in`(...) }
            val response = postgrest.from("content").select {
                filter {
                    isIn("id", contentIds) // 新版推荐用 isIn 替代 `in`
                }
            }
            response.decodeList<Content>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getRecommendedContent(): List<Content> {
        return try {
            val response = postgrest.from("content").select {
                order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                limit(3)
            }
            response.decodeList<Content>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
