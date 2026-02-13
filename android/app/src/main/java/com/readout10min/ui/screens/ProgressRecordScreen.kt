package com.readout10min.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.readout10min.data.models.Progress
import com.readout10min.data.repositories.ContentRepository
import com.readout10min.navigation.Screen
import com.readout10min.ui.theme.Purple80
import com.readout10min.ui.theme.Typography
import androidx.compose.material3.MaterialTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min
import kotlin.math.max
import java.util.Date
import java.util.UUID

@Composable
fun ProgressRecordScreen(navController: NavController) {
    val context = LocalContext.current
    val contentRepository = ContentRepository()
    
    // 获取主题颜色
    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onBackground = MaterialTheme.colorScheme.onBackground
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    
    // 状态管理
    val currentMonth = remember { 
        val today = java.time.LocalDate.now()
        mutableStateOf("${today.year}年${today.monthValue}月") 
    }
    val selectedPeriod = remember { mutableStateOf("week") }
    val currentWeek = remember { mutableStateOf(java.time.LocalDate.now()) }
    val currentMonthDate = remember { mutableStateOf(java.time.LocalDate.now()) }
    val currentYear = remember { mutableStateOf(java.time.LocalDate.now()) }
    var practiceDates by remember { mutableStateOf<Set<String>>(emptySet()) }
    var practiceRecords by remember { mutableStateOf<List<com.readout10min.data.models.PracticeRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val reloadTrigger = remember { mutableStateOf(UUID.randomUUID()) }
    
    // 新增状态变量
    val selectedDate = remember { mutableStateOf("") }
    var isLoadingSelectedDate by remember { mutableStateOf(false) }
    var selectedDatePracticeRecords by remember { mutableStateOf<List<com.readout10min.data.models.PracticeRecord>>(emptyList()) }
    var contentMap by remember { mutableStateOf<Map<UUID, String>>(emptyMap()) }
    
    // 模拟用户ID
    val userId = UUID.fromString("00000000-0000-0000-0000-000000000000")
    
    // 加载进度数据和练习记录
    LaunchedEffect(key1 = Unit, key2 = reloadTrigger.value) {
        isLoading = true
        try {
            // 从Supabase获取用户的练习记录
            val practiceData = withContext(Dispatchers.IO) {
                contentRepository.getPracticeRecords(userId)
            }
            
            // 如果没有数据，添加模拟数据
                val finalPracticeData = if (practiceData.isNullOrEmpty()) {
                    // 添加模拟练习记录
                    val mockRecords = mutableListOf<com.readout10min.data.models.PracticeRecord>()
                    val today = java.time.LocalDate.now()
                    
                    // 今天的练习记录
                    mockRecords.add(
                        com.readout10min.data.models.PracticeRecord(
                            id = UUID.randomUUID(),
                            user_id = userId,
                            paragraph_id = UUID.randomUUID(),
                            content_id = UUID.randomUUID(),
                            practice_date = today.toString() + " 10:00:00",
                            duration = 600, // 10分钟
                            accuracy = 0.95f,
                            fluency = 0.9f,
                            pronunciation_score = 0.92f
                        )
                    )
                    
                    // 今天的第二个练习记录
                    mockRecords.add(
                        com.readout10min.data.models.PracticeRecord(
                            id = UUID.randomUUID(),
                            user_id = userId,
                            paragraph_id = UUID.randomUUID(),
                            content_id = UUID.randomUUID(),
                            practice_date = today.toString() + " 14:30:00",
                            duration = 480, // 8分钟
                            accuracy = 0.92f,
                            fluency = 0.88f,
                            pronunciation_score = 0.9f
                        )
                    )
                    
                    // 昨天的练习记录
                    mockRecords.add(
                        com.readout10min.data.models.PracticeRecord(
                            id = UUID.randomUUID(),
                            user_id = userId,
                            paragraph_id = UUID.randomUUID(),
                            content_id = UUID.randomUUID(),
                            practice_date = today.minusDays(1).toString() + " 15:30:00",
                            duration = 900, // 15分钟
                            accuracy = 0.9f,
                            fluency = 0.85f,
                            pronunciation_score = 0.88f
                        )
                    )
                    
                    // 上个月的练习记录
                    mockRecords.add(
                        com.readout10min.data.models.PracticeRecord(
                            id = UUID.randomUUID(),
                            user_id = userId,
                            paragraph_id = UUID.randomUUID(),
                            content_id = UUID.randomUUID(),
                            practice_date = today.minusMonths(1).toString() + " 09:00:00",
                            duration = 1200, // 20分钟
                            accuracy = 0.88f,
                            fluency = 0.85f,
                            pronunciation_score = 0.86f
                        )
                    )
                    
                    // 下个月的练习记录
                    mockRecords.add(
                        com.readout10min.data.models.PracticeRecord(
                            id = UUID.randomUUID(),
                            user_id = userId,
                            paragraph_id = UUID.randomUUID(),
                            content_id = UUID.randomUUID(),
                            practice_date = today.plusMonths(1).toString() + " 11:00:00",
                            duration = 720, // 12分钟
                            accuracy = 0.92f,
                            fluency = 0.88f,
                            pronunciation_score = 0.9f
                        )
                    )
                    
                    // 下下个月的练习记录
                    mockRecords.add(
                        com.readout10min.data.models.PracticeRecord(
                            id = UUID.randomUUID(),
                            user_id = userId,
                            paragraph_id = UUID.randomUUID(),
                            content_id = UUID.randomUUID(),
                            practice_date = today.plusMonths(2).toString() + " 14:00:00",
                            duration = 900, // 15分钟
                            accuracy = 0.9f,
                            fluency = 0.87f,
                            pronunciation_score = 0.89f
                        )
                    )
                    
                    mockRecords
                } else {
                    practiceData
                }
            
            practiceRecords = finalPracticeData
            
            // 提取练习日期
            val dates = finalPracticeData.mapNotNull { 
                val dateString = it?.practice_date ?: ""
                // 处理不同的日期格式，无论是空格还是T分隔
                if (dateString.contains(" ")) {
                    dateString.substringBefore(" ")
                } else if (dateString.contains("T")) {
                    dateString.substringBefore("T")
                } else {
                    null
                }
            }?.toSet() ?: emptySet()
            practiceDates = dates
            
            error = null
        } catch (e: Exception) {
            e.printStackTrace()
            error = "加载失败，请重试"
            
            // 即使出错也添加模拟数据
            val mockRecords = mutableListOf<com.readout10min.data.models.PracticeRecord>()
            val today = java.time.LocalDate.now()
            
            // 今天的练习记录
            mockRecords.add(
                com.readout10min.data.models.PracticeRecord(
                    id = UUID.randomUUID(),
                    user_id = userId,
                    paragraph_id = UUID.randomUUID(),
                    content_id = UUID.randomUUID(),
                    practice_date = today.toString() + " 10:00:00",
                    duration = 600, // 10分钟
                    accuracy = 0.95f,
                    fluency = 0.9f,
                    pronunciation_score = 0.92f
                )
            )
            
            // 今天的第二个练习记录
            mockRecords.add(
                com.readout10min.data.models.PracticeRecord(
                    id = UUID.randomUUID(),
                    user_id = userId,
                    paragraph_id = UUID.randomUUID(),
                    content_id = UUID.randomUUID(),
                    practice_date = today.toString() + " 14:30:00",
                    duration = 480, // 8分钟
                    accuracy = 0.92f,
                    fluency = 0.88f,
                    pronunciation_score = 0.9f
                )
            )
            
            // 昨天的练习记录
            mockRecords.add(
                com.readout10min.data.models.PracticeRecord(
                    id = UUID.randomUUID(),
                    user_id = userId,
                    paragraph_id = UUID.randomUUID(),
                    content_id = UUID.randomUUID(),
                    practice_date = today.minusDays(1).toString() + " 15:30:00",
                    duration = 900, // 15分钟
                    accuracy = 0.9f,
                    fluency = 0.85f,
                    pronunciation_score = 0.88f
                )
            )
            
            practiceRecords = mockRecords
            
            // 提取练习日期
            val dates = mockRecords.mapNotNull { 
                val dateString = it?.practice_date ?: ""
                // 处理不同的日期格式，无论是空格还是T分隔
                if (dateString.contains(" ")) {
                    dateString.substringBefore(" ")
                } else if (dateString.contains("T")) {
                    dateString.substringBefore("T")
                } else {
                    null
                }
            }?.toSet() ?: emptySet()
            practiceDates = dates
        } finally {
            isLoading = false
        }
    }
    
    // 监听选中日期变化，加载当日练习记录
    LaunchedEffect(key1 = selectedDate.value) {
        if (selectedDate.value.isNotEmpty()) {
            isLoadingSelectedDate = true
            try {
                // 从practiceRecords中筛选出当日的练习记录
                val selectedDateRecords = practiceRecords.filter { record ->
                    val recordDate = record.practice_date ?: ""
                    val recordDatePart = if (recordDate.contains(" ")) {
                        recordDate.substringBefore(" ")
                    } else if (recordDate.contains("T")) {
                        recordDate.substringBefore("T")
                    } else {
                        ""
                    }
                    recordDatePart == selectedDate.value
                }
                selectedDatePracticeRecords = selectedDateRecords
                
                // 加载相关的文章信息
                val newContentMap = mutableMapOf<UUID, String>()
                selectedDateRecords.forEach { record ->
                    record.content_id?.let { contentId ->
                        try {
                            val content = withContext(Dispatchers.IO) {
                                contentRepository.getContentById(contentId)
                            }
                            content?.title?.let {
                                newContentMap[contentId] = it
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                contentMap = newContentMap
            } catch (e: Exception) {
                e.printStackTrace()
                selectedDatePracticeRecords = emptyList()
                contentMap = emptyMap()
            } finally {
                isLoadingSelectedDate = false
            }
        }
    }
    
    // 格式化时间函数
    fun formatDateTime(dateTimeString: String?): String {
        if (dateTimeString == null) return "未知"
        
        try {
            // 处理包含T的时间格式，如 2026-02-11T21:18:39.769874+00
            val cleanedString = dateTimeString.replace(" ", "T")
            val dateTime = java.time.LocalDateTime.parse(
                cleanedString, 
                java.time.format.DateTimeFormatter.ISO_DATE_TIME
            )
            return dateTime.format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")
            )
        } catch (e: Exception) {
            // 如果解析失败，尝试其他格式
            try {
                // 处理空格分隔的时间格式，如 2026-02-11 21:18:39
                val parts = dateTimeString.split(" ")
                if (parts.size >= 2) {
                    return parts[1].substringBefore(".")
                }
            } catch (e2: Exception) {
                // 所有解析都失败，返回原始字符串
                return dateTimeString
            }
            return dateTimeString
        }
    }
    
    // 获取文章名
    fun getContentTitle(contentId: UUID?): String {
        if (contentId == null) return "未知文章"
        
        return contentMap[contentId] ?: "未知文章"
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部导航栏
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(color = surfaceContainer)
                }
                .padding(12.dp)
        ) {
            // 返回按钮
            IconButton(
                onClick = { navController.navigateUp() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回首页",
                    tint = onBackground
                )
            }
        }

        // 主内容区域
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(remember { ScrollState(0) }),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading) {
                // 加载中
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Purple80)
                }
            } else if (error != null) {
                // 错误信息
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = error ?: "加载失败",
                        style = Typography.bodyMedium,
                        color = onBackground,
                        modifier = Modifier.padding(16.dp)
                    )
                    Button(
                        onClick = { 
                            // 重新加载
                            reloadTrigger.value = UUID.randomUUID()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Purple80
                        )
                    ) {
                        Text(text = "重新加载")
                    }
                }
            } else {
                // 累积练习天数卡片
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .drawBehind {
                            drawRect(color = surfaceContainer)
                        }
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 卡片标题
                        Text(
                            text = "练习统计",
                            style = Typography.titleMedium,
                            color = onBackground
                        )
                        
                        // 累积练习天数
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "累积练习天数",
                                style = Typography.bodyMedium,
                                color = onBackground
                            )
                            Text(
                                text = "${practiceDates.size}天",
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Purple80
                                )
                            )
                        }
                    }
                }

                // 练习日历卡片
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .drawBehind {
                            drawRect(color = surfaceContainer)
                        }
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 卡片标题
                        Text(
                            text = "练习日历",
                            style = Typography.titleMedium,
                            color = onBackground
                        )
                        
                        // 日历头部
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentMonth.value,
                                style = Typography.bodyMedium,
                                color = onBackground,
                                fontWeight = FontWeight.Medium
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // 上月按钮
                                Box(
                                    modifier = Modifier
                                        .width(32.dp)
                                        .height(32.dp)
                                        .clickable {
                                            // 切换到上月
                                            try {
                                                // 使用更简单的日期处理方式
                                                val parts = currentMonth.value.split("年", "月")
                                                if (parts.size >= 2) {
                                                    val year = parts[0].toInt()
                                                    val month = parts[1].toInt()
                                                    
                                                    var newYear = year
                                                    var newMonth = month - 1
                                                    if (newMonth < 1) {
                                                        newMonth = 12
                                                        newYear -= 1
                                                    }
                                                    
                                                    currentMonth.value = "${newYear}年${newMonth}月"
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                // 直接使用当前日期作为备选方案
                                                val currentDate = java.time.LocalDate.now().minusMonths(1)
                                                currentMonth.value = "${currentDate.year}年${currentDate.monthValue}月"
                                            }
                                        }
                                        .drawBehind {
                                            drawRect(color = surfaceVariant)
                                        }
                                        .align(Alignment.CenterVertically)
                                ) {
                                    Text(
                                        text = "‹",
                                        style = TextStyle(
                                            fontSize = 18.sp,
                                            color = onBackground
                                        ),
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                                // 下月按钮
                                Box(
                                    modifier = Modifier
                                        .width(32.dp)
                                        .height(32.dp)
                                        .clickable {
                                            // 切换到下月
                                            try {
                                                // 使用更简单的日期处理方式
                                                val parts = currentMonth.value.split("年", "月")
                                                if (parts.size >= 2) {
                                                    val year = parts[0].toInt()
                                                    val month = parts[1].toInt()
                                                    
                                                    var newYear = year
                                                    var newMonth = month + 1
                                                    if (newMonth > 12) {
                                                        newMonth = 1
                                                        newYear += 1
                                                    }
                                                    
                                                    currentMonth.value = "${newYear}年${newMonth}月"
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                // 直接使用当前日期作为备选方案
                                                val currentDate = java.time.LocalDate.now().plusMonths(1)
                                                currentMonth.value = "${currentDate.year}年${currentDate.monthValue}月"
                                            }
                                        }
                                        .drawBehind {
                                            drawRect(color = surfaceVariant)
                                        }
                                        .align(Alignment.CenterVertically)
                                ) {
                                    Text(
                                        text = "›",
                                        style = TextStyle(
                                            fontSize = 18.sp,
                                            color = onBackground
                                        ),
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                            }
                        }
                        
                        // 星期标题
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            listOf("日", "一", "二", "三", "四", "五", "六").forEach {
                                Text(
                                    text = it,
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        color = Color(73, 69, 78),
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.weight(1f).textAlign(Alignment.CenterHorizontally)
                                )
                            }
                        }
                        
                        // 日期网格
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            modifier = Modifier.height(200.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalArrangement = Arrangement.SpaceAround
                        ) {
                            try {
                                // 解析当前月份
                                val parts = currentMonth.value.split("年", "月")
                                if (parts.size >= 2) {
                                    val year = parts[0].toInt()
                                    val month = parts[1].toInt()
                                    val currentDate = java.time.LocalDate.of(year, month, 1)
                                    val daysInMonth = java.time.YearMonth.of(year, month).lengthOfMonth()
                                    val firstDayOfMonth = java.time.LocalDate.of(year, month, 1)
                                    val dayOfWeek = firstDayOfMonth.dayOfWeek.value // 1-7, 1=Monday
                                    val today = java.time.LocalDate.now()
                                    
                                    // 上月日期
                                    val prevMonth = currentDate.minusMonths(1)
                                    val daysInPrevMonth = java.time.YearMonth.of(prevMonth.year, prevMonth.monthValue).lengthOfMonth()
                                    val startDay = max(1, daysInPrevMonth - dayOfWeek + 1)
                                    for (day in startDay until daysInPrevMonth + 1) {
                                        item {
                                            CalendarDay(
                                                day = day.toString(), 
                                                isOtherMonth = true,
                                                onClick = { /* 不处理其他月份的点击 */ }
                                            )
                                        }
                                    }
                                    
                                    // 当月日期
                                    for (day in 1..daysInMonth) {
                                        item {
                                            val dateString = "${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
                                            val hasPractice = practiceDates.contains(dateString)
                                            val isToday = today.year == year && today.monthValue == month && today.dayOfMonth == day
                                            
                                            CalendarDay(
                                                day = day.toString(),
                                                hasPractice = hasPractice,
                                                isToday = isToday,
                                                onClick = {
                                                    // 点击日期时更新选中的日期并加载当日练习记录
                                                    selectedDate.value = dateString
                                                }
                                            )
                                        }
                                    }
                                    
                                    // 下月日期
                                    val remainingDays = 42 - (dayOfWeek + daysInMonth - 1)
                                    for (day in 1..min(7, remainingDays)) {
                                        item {
                                            CalendarDay(
                                                day = day.toString(), 
                                                isOtherMonth = true,
                                                onClick = { /* 不处理其他月份的点击 */ }
                                            )
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                // 如果日期解析失败，显示默认日历
                                for (i in 1..35) {
                                    item {
                                        CalendarDay(
                                            day = "", 
                                            isOtherMonth = true,
                                            onClick = { /* 不处理点击 */ }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 当日练习段落展示卡片
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .drawBehind {
                            drawRect(color = surfaceContainer)
                        }
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 卡片标题
                        Text(
                            text = if (selectedDate.value.isNotEmpty()) {
                                "${selectedDate.value} 练习段落"
                            } else {
                                "点击日历日期查看练习段落"
                            },
                            style = Typography.titleMedium,
                            color = onBackground
                        )
                        
                        // 当日练习段落列表
                        if (isLoadingSelectedDate) {
                            // 加载中
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Purple80)
                            }
                        } else if (selectedDatePracticeRecords.isNotEmpty()) {
                            // 显示练习段落列表
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                selectedDatePracticeRecords.forEachIndexed { index, record ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .drawBehind {
                                                drawRect(color = surfaceVariant)
                                            }
                                            .padding(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // 段落编号
                                                Text(
                                                    text = "段落 ${index + 1}",
                                                    style = TextStyle(
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = onBackground
                                                    )
                                                )
                                                
                                                // 文章名
                                                Text(
                                                    text = "文章: ${getContentTitle(record.content_id)}",
                                                    style = TextStyle(
                                                        fontSize = 12.sp,
                                                        color = onSurfaceVariant
                                                    )
                                                )
                                                
                                                // 练习时间
                                                Text(
                                                    text = "练习时间: ${formatDateTime(record.practice_date)}",
                                                    style = TextStyle(
                                                        fontSize = 12.sp,
                                                        color = onSurfaceVariant
                                                    )
                                                )
                                                
                                                // 练习时长
                                                Text(
                                                    text = "练习时长: ${record.duration / 60}分钟",
                                                    style = TextStyle(
                                                        fontSize = 12.sp,
                                                        color = onSurfaceVariant
                                                    )
                                                )
                                        }
                                    }
                                }
                            }
                        } else if (selectedDate.value.isNotEmpty()) {
                            // 当日无练习记录
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "当日无练习记录",
                                    style = Typography.bodyMedium,
                                    color = onSurfaceVariant
                                )
                            }
                        } else {
                            // 未选择日期
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "请点击日历上的日期查看当日练习段落",
                                    style = Typography.bodyMedium,
                                    color = onSurfaceVariant,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 日历日期组件
@Composable
fun CalendarDay(
    day: String,
    isOtherMonth: Boolean = false,
    isToday: Boolean = false,
    hasPractice: Boolean = false,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .width(36.dp)
            .height(36.dp)
            .drawBehind {
                when {
                    isToday -> drawRect(color = Color(211, 187, 253))
                    hasPractice -> drawRect(color = Color(99, 91, 112))
                    else -> Unit
                }
            }
            .clickable { onClick() }
    ) {
        Text(
            text = day,
            style = TextStyle(
                fontSize = 14.sp,
                color = when {
                    isOtherMonth -> Color(203, 196, 207)
                    isToday || hasPractice -> Color.White
                    else -> MaterialTheme.colorScheme.onBackground
                },
                fontWeight = when {
                    isToday -> FontWeight.SemiBold
                    hasPractice -> FontWeight.Medium
                    else -> FontWeight.Normal
                }
            ),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}



// 扩展函数：居中对齐文本
fun Modifier.textAlign(alignment: Alignment.Horizontal): Modifier {
    return this.then(
        Modifier.fillMaxWidth()
    )
}
