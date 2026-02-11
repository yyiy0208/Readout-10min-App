package com.readout10min.data.repositories

import com.readout10min.data.SupabaseClient
import com.readout10min.data.models.Content
import com.readout10min.data.models.Paragraph
import com.readout10min.data.models.Progress
import com.readout10min.data.models.PracticeRecord
import io.github.jan.supabase.postgrest.*

import java.util.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

class ContentRepository {
    private val postgrest = SupabaseClient.client.postgrest
    private var _lastError: String? = null

    val lastError: String?
        get() = _lastError

    suspend fun getContentList(): List<Content> {
        return try {
            _lastError = null
            // 直接调用 select() 得到结果
            val response = postgrest.from("content").select()
            response.decodeList<Content>()
        } catch (e: Exception) {
            e.printStackTrace()
            _lastError = "加载内容列表失败: ${e.message}"
            emptyList()
        }
    }

    suspend fun getContentById(id: UUID): Content? {
        return try {
            _lastError = null
            // 筛选条件写在 select { ... } 闭包内
            val response = postgrest.from("content").select {
                filter {
                    eq("id", id.toString()) // 建议转成 String 确保兼容性
                }
            }
            response.decodeSingleOrNull<Content>()
        } catch (e: Exception) {
            e.printStackTrace()
            _lastError = "加载内容详情失败: ${e.message}"
            null
        }
    }

    suspend fun getParagraphsByContentId(contentId: UUID): List<Paragraph> {
        return try {
            _lastError = null
            val response = postgrest.from("paragraphs").select {
                filter {
                    eq("content_id", contentId.toString())
                }
                order("paragraph_number", order = io.github.jan.supabase.postgrest.query.Order.ASCENDING)
            }
            response.decodeList<Paragraph>()
        } catch (e: Exception) {
            e.printStackTrace()
            _lastError = "加载段落失败: ${e.message}"
            emptyList()
        }
    }

    suspend fun updateProgress(progress: Progress): Boolean {
        return try {
            _lastError = null
            // upsert 直接传对象
            postgrest.from("progress").upsert(progress) {
                // 如果需要指定冲突列，新版语法如下
                onConflict = "user_id,content_id,current_paragraph" 
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _lastError = "更新进度失败: ${e.message}"
            println("更新进度异常: ${e.message}")
            false
        }
    }

    suspend fun getRecentContent(userId: UUID): List<Content> {
        return try {
            _lastError = null
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
            _lastError = "加载最近阅读失败: ${e.message}"
            emptyList()
        }
    }

    suspend fun getRecommendedContent(): List<Content> {
        return try {
            _lastError = null
            val response = postgrest.from("content").select {
                order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                limit(3)
            }
            response.decodeList<Content>()
        } catch (e: Exception) {
            e.printStackTrace()
            _lastError = "加载推荐内容失败: ${e.message}"
            emptyList()
        }
    }

    suspend fun getProgressByUserIdAndContentId(userId: UUID, contentId: UUID): Progress? {
        return try {
            _lastError = null
            val response = postgrest.from("progress").select {
                filter {
                    eq("user_id", userId.toString())
                    eq("content_id", contentId.toString())
                }
            }
            response.decodeSingleOrNull<Progress>()
        } catch (e: Exception) {
            e.printStackTrace()
            _lastError = "加载进度失败: ${e.message}"
            null
        }
    }
    
    suspend fun getAllProgressByUserIdAndContentId(userId: UUID, contentId: UUID): List<Progress> {
        return try {
            _lastError = null
            val response = postgrest.from("progress").select {
                filter {
                    eq("user_id", userId.toString())
                    eq("content_id", contentId.toString())
                }
            }
            response.decodeList<Progress>()
        } catch (e: Exception) {
            e.printStackTrace()
            _lastError = "加载进度失败: ${e.message}"
            emptyList()
        }
    }

    fun clearError() {
        _lastError = null
    }
    
    suspend fun getAllProgress(userId: UUID): List<Progress> {
        return try {
            _lastError = null
            val response = postgrest.from("progress").select {
                filter {
                    eq("user_id", userId.toString())
                }
                order("updated_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            response.decodeList<Progress>()
        } catch (e: Exception) {
            e.printStackTrace()
            _lastError = "加载所有进度失败: ${e.message}"
            emptyList()
        }
    }
    
    suspend fun getPracticeDays(userId: UUID): Int {
        return try {
            _lastError = null
            val practiceRecords = getPracticeRecords(userId)
            // 调试：打印练习记录数量
            println("Practice records count: ${practiceRecords.size}")
            // 调试：打印每条练习记录的日期
            practiceRecords.forEachIndexed { index, record ->
                println("Record $index date: ${record.practice_date}")
            }
            // 计算不同的日期数量
            val dates = practiceRecords.map { 
                val dateString = it.practice_date ?: ""
                // 处理不同的日期格式，无论是空格还是T分隔
                val datePart = if (dateString.contains(" ")) {
                    dateString.substringBefore(" ")
                } else if (dateString.contains("T")) {
                    dateString.substringBefore("T")
                } else {
                    dateString
                }
                datePart
            }
            // 调试：打印提取的日期
            println("Extracted dates: $dates")
            val distinctDates = dates.filter { it.isNotEmpty() }.distinct()
            // 调试：打印去重后的日期
            println("Distinct dates: $distinctDates")
            val practiceDays = distinctDates.size
            // 调试：打印计算的练习天数
            println("Calculated practice days: $practiceDays")
            maxOf(practiceDays, 0)
        } catch (e: Exception) {
            e.printStackTrace()
            _lastError = "计算练习天数失败: ${e.message}"
            0
        }
    }
    
    suspend fun getTodayPracticeStatus(userId: UUID): Pair<Int, Int> {
        return try {
            _lastError = null
            val practiceRecords = getPracticeRecords(userId)
            // 计算今日练习状态
            val today = java.time.LocalDate.now().toString()
            val todayPracticeCount = practiceRecords.count { 
                val dateString = it.practice_date ?: ""
                // 处理不同的日期格式，无论是空格还是T分隔
                val datePart = if (dateString.contains(" ")) {
                    dateString.substringBefore(" ")
                } else if (dateString.contains("T")) {
                    dateString.substringBefore("T")
                } else {
                    dateString
                }
                datePart == today
            }
            // 调试：打印今日练习数量
            println("Today's practice count: $todayPracticeCount")
            Pair(todayPracticeCount, 1) // 返回今日练习数量/1
        } catch (e: Exception) {
            e.printStackTrace()
            _lastError = "获取今日练习状态失败: ${e.message}"
            Pair(0, 1)
        }
    }
    
    suspend fun createPracticeRecord(record: PracticeRecord): Boolean {
        return try {
            _lastError = null
            postgrest.from("practice_records").insert(record)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            _lastError = "创建练习记录失败: ${e.message}"
            false
        }
    }
    
    suspend fun getPracticeRecords(userId: UUID): List<PracticeRecord> {
        return try {
            _lastError = null
            val response = postgrest.from("practice_records").select {
                filter {
                    eq("user_id", userId.toString())
                }
                order("practice_date", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            response.decodeList<PracticeRecord>()
        } catch (e: Exception) {
            e.printStackTrace()
            _lastError = "加载练习记录失败: ${e.message}"
            emptyList()
        }
    }
    
    suspend fun getPracticeRecordsByContentId(userId: UUID, contentId: UUID): List<PracticeRecord> {
        return try {
            _lastError = null
            val response = postgrest.from("practice_records").select {
                filter {
                    eq("user_id", userId.toString())
                    eq("content_id", contentId.toString())
                }
                order("practice_date", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            response.decodeList<PracticeRecord>()
        } catch (e: Exception) {
            e.printStackTrace()
            _lastError = "加载练习记录失败: ${e.message}"
            emptyList()
        }
    }
    
    suspend fun getPracticeStatistics(userId: UUID): Map<String, Any> {
        return try {
            _lastError = null
            val practiceRecords = getPracticeRecords(userId)
            
            // 计算统计信息
            val totalPracticeTime = practiceRecords.sumOf { it.duration }
            val totalPracticeSessions = practiceRecords.size
            val averageAccuracy = if (practiceRecords.isNotEmpty()) {
                practiceRecords.map { it.accuracy }.average()
            } else {
                0.0
            }
            val averageFluency = if (practiceRecords.isNotEmpty()) {
                practiceRecords.map { it.fluency }.average()
            } else {
                0.0
            }
            val averagePronunciation = if (practiceRecords.isNotEmpty()) {
                practiceRecords.map { it.pronunciation_score }.average()
            } else {
                0.0
            }
            
            // 计算练习天数
            val practiceDays = practiceRecords.map { 
                it.practice_date?.substringBefore(" ") ?: ""
            }.distinct().size
            
            mapOf(
                "totalPracticeTime" to totalPracticeTime,
                "totalPracticeSessions" to totalPracticeSessions,
                "averageAccuracy" to averageAccuracy,
                "averageFluency" to averageFluency,
                "averagePronunciation" to averagePronunciation,
                "practiceDays" to practiceDays
            )
        } catch (e: Exception) {
            e.printStackTrace()
            _lastError = "计算练习统计失败: ${e.message}"
            emptyMap()
        }
    }
}
