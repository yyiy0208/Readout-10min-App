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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController
import com.readout10min.data.models.Content
import com.readout10min.data.models.Paragraph
import com.readout10min.data.models.Progress
import com.readout10min.data.repositories.ContentRepository
import com.readout10min.navigation.Screen
import com.readout10min.ui.theme.Purple80
import com.readout10min.ui.theme.Typography
import com.readout10min.ui.theme.White
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.isSystemInDarkTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

// 带有进度信息的内容数据类
data class ContentWithProgress(
    val content: Content,
    val progress: Int, // 进度百分比
    val lastPracticeParagraph: Int? // 最后练习的段落号
)

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val contentRepository = ContentRepository()
    
    // 获取主题颜色
    val isDarkTheme = isSystemInDarkTheme()
    val surfaceContainer = if (isDarkTheme) {
        Color(41, 38, 45) // 使用深色的SurfaceContainerDark
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onBackground = MaterialTheme.colorScheme.onBackground
    
    var recommendedContent by remember { mutableStateOf<List<Content>>(emptyList()) }
    var recentContentWithProgress by remember { mutableStateOf<List<ContentWithProgress>>(emptyList()) }
    var practiceDays by remember { mutableStateOf(0) }
    var todayPracticeCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadTrigger by remember { mutableStateOf(UUID.randomUUID()) }
    
    // 最近练习的段落信息
    var lastPracticeContent by remember { mutableStateOf<Content?>(null) }
    var lastPracticeParagraph by remember { mutableStateOf<Int?>(null) }
    var lastPracticeParagraphCompleted by remember { mutableStateOf<Boolean>(false) }
    var nextParagraphNumber by remember { mutableStateOf<Int?>(null) }
    
    // 模拟用户ID
    val userId = UUID.fromString("00000000-0000-0000-0000-000000000000")
    
    // 加载数据
    LaunchedEffect(key1 = Unit, key2 = reloadTrigger) {
        isLoading = true
        error = null
        try {
            // 获取推荐内容（基于最近一次练习的段落）
            val recommended = withContext(Dispatchers.IO) {
                // 获取用户的所有进度记录
                val progressList = contentRepository.getAllProgress(userId)
                
                if (progressList.isNotEmpty()) {
                    // 按时间排序，获取最近一次的进度记录（列表已按updated_at降序排序，最新的在最前面）
                    val lastProgress = progressList.firstOrNull()
                    if (lastProgress != null) {
                        // 获取对应的内容
                        val contentId = lastProgress.content_id
                        val content = contentRepository.getContentById(contentId)
                        if (content != null) {
                            lastPracticeContent = content
                            lastPracticeParagraph = lastProgress.current_paragraph
                            lastPracticeParagraphCompleted = lastProgress.is_completed
                            
                            // 获取内容的所有段落
                            val paragraphs = contentRepository.getParagraphsByContentId(contentId)
                            val totalParagraphs = paragraphs.size
                            
                            // 如果当前段落已完成，查找下一个段落
                            if (lastProgress.is_completed && lastProgress.current_paragraph < totalParagraphs) {
                                nextParagraphNumber = lastProgress.current_paragraph + 1
                            } else {
                                nextParagraphNumber = lastProgress.current_paragraph
                            }
                            
                            listOf(content)
                        } else {
                            emptyList()
                        }
                    } else {
                        // 没有进度记录，使用默认推荐
                        contentRepository.getRecommendedContent()
                    }
                } else {
                    // 没有进度记录，使用默认推荐
                    contentRepository.getRecommendedContent()
                }
            }
            
            // 检查推荐内容加载是否有错误
            val recommendError = contentRepository.lastError
            if (recommendError != null) {
                error = recommendError
            } else {
                recommendedContent = recommended
            }
            
            // 获取最近阅读
            val recentContentList = withContext(Dispatchers.IO) {
                contentRepository.getRecentContent(userId)
            }
            
            // 检查最近阅读加载是否有错误
            val recentError = contentRepository.lastError
            if (recentError != null && error == null) {
                error = recentError
            } else {
                // 为每个最近阅读的内容计算进度
                val recentWithProgress = withContext(Dispatchers.IO) {
                    recentContentList.mapNotNull { content ->
                        try {
                            // 获取内容的所有段落
                            val paragraphs = contentRepository.getParagraphsByContentId(content.id)
                            val totalParagraphs = paragraphs.size
                            
                            if (totalParagraphs == 0) {
                                ContentWithProgress(content, 0, null)
                            } else {
                                // 获取用户在该内容上的所有进度记录
                                val progressList = contentRepository.getAllProgressByUserIdAndContentId(userId, content.id)
                                
                                if (progressList.isEmpty()) {
                                    ContentWithProgress(content, 0, null)
                                } else {
                                    // 计算已完成的段落数
                                    val completedParagraphs = progressList.filter { it.is_completed == true }.size
                                    val progressPercentage = (completedParagraphs * 100) / totalParagraphs
                                    
                                    // 获取最后练习的段落（使用最后一个进度记录）
                                    val lastProgress = progressList.lastOrNull()
                                    val lastParagraph = lastProgress?.current_paragraph
                                    
                                    ContentWithProgress(content, progressPercentage, lastParagraph)
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            ContentWithProgress(content, 0, null)
                        }
                    }
                }
                recentContentWithProgress = recentWithProgress
            }
            
            // 获取练习天数
            val days = withContext(Dispatchers.IO) {
                contentRepository.getPracticeDays(userId)
            }
            practiceDays = days
            
            // 获取今日练习状态
            val todayStatus = withContext(Dispatchers.IO) {
                contentRepository.getTodayPracticeStatus(userId)
            }
            todayPracticeCount = todayStatus.first
        } catch (e: Exception) {
            e.printStackTrace()
            error = "加载失败: ${e.message}"
        } finally {
            isLoading = false
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部标题栏
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(color = Purple80)
                }
                .padding(16.dp)
        ) {
            Text(
                text = "Readout-10min",
                style = Typography.headlineMedium,
                color = White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // 主内容区域
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 80.dp) // 为底部导航栏留出空间
        ) {
            // 错误信息显示
            if (error != null) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .drawBehind {
                                drawRect(color = Color(255, 221, 221))
                            }
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "错误信息",
                                style = Typography.titleMedium,
                                color = Color(183, 28, 28)
                            )
                            Text(
                                text = error ?: "加载失败",
                                style = Typography.bodyMedium,
                                color = Color(183, 28, 28),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Button(
                                onClick = {
                                    // 重新加载
                                    reloadTrigger = UUID.randomUUID()
                                },
                                modifier = Modifier.padding(top = 12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(183, 28, 28)
                                )
                            ) {
                                Text(text = "重新加载")
                            }
                        }
                    }
                }
            }
            // 练习统计
            item {
                Column {
                    //  section header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "练习统计",
                            style = Typography.titleMedium,
                            color = onBackground
                        )
                        Text(
                            text = "更多",
                            style = Typography.bodySmall,
                            color = Purple80,
                            modifier = Modifier.clickable { navController.navigate(Screen.ProgressRecord.route) }
                        )
                    }

                    // 增加标题与卡片之间的距离
                    Box(modifier = Modifier.height(12.dp))

                    //  stats card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = surfaceContainer
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = practiceDays.toString(),
                                        style = Typography.displayMedium,
                                        color = Purple80
                                    )
                                    Text(
                                        text = "累计练习天数",
                                        style = Typography.bodySmall,
                                        color = onBackground
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(40.dp)
                                        .drawBehind {
                                            drawRect(color = surfaceVariant)
                                        }
                                )
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${todayPracticeCount}/1",
                                        style = Typography.displayMedium,
                                        color = Purple80
                                    )
                                    Text(
                                        text = "今日练习",
                                        style = Typography.bodySmall,
                                        color = onBackground
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 今日推荐
            item {
                Column {
                    //  section header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "今日推荐",
                            style = Typography.titleMedium,
                            color = onBackground
                        )
                        Text(
                            text = "查看全部",
                            style = Typography.bodySmall,
                            color = Purple80,
                            modifier = Modifier.clickable { navController.navigate(Screen.ContentLibrary.route) }
                        )
                    }

                    // 增加标题与卡片之间的距离
                    Box(modifier = Modifier.height(12.dp))

                    if (isLoading) {
                        // 加载中
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Purple80)
                        }
                    } else if (recommendedContent.isNotEmpty()) {
                        //  recommendation card
                        val content = recommendedContent[0]
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = surfaceContainer
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = content.title,
                                    style = Typography.titleMedium,
                                    color = onBackground
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "⏱️ ${content.estimated_duration / 60} 分钟",
                                        style = Typography.bodySmall,
                                        color = onBackground
                                    )
                                }
                                Text(
                                    text = "This article is recommended for you based on your reading history and preferences...",
                                    style = Typography.bodyMedium,
                                    color = onBackground
                                )
                                Button(
                                    onClick = { 
                                        // 根据最近练习的段落状态导航到正确的页面
                                        if (lastPracticeContent != null && nextParagraphNumber != null) {
                                            navController.navigate("${Screen.ReadingPractice.route}/${lastPracticeContent!!.id}?paragraph=${nextParagraphNumber}")
                                        } else {
                                            navController.navigate("${Screen.ReadingPractice.route}/${content.id}")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Purple80
                                    ),
                                    shape = RoundedCornerShape(8.dp) // 调小圆角半径
                                ) {
                                    Text(text = "开始练习")
                                }
                            }
                        }
                    } else {
                        // 无推荐内容
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "暂无推荐内容",
                                style = Typography.bodyMedium,
                                color = onBackground
                            )
                        }
                    }
                }
            }

            // 最近阅读
            item {
                Column {
                    //  section header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "最近阅读",
                            style = Typography.titleMedium,
                            color = onBackground
                        )
                        Text(
                            text = "查看全部",
                            style = Typography.bodySmall,
                            color = Purple80,
                            modifier = Modifier.clickable { navController.navigate(Screen.ContentLibrary.route) }
                        )
                    }

                    // 增加标题与卡片之间的距离
                    Box(modifier = Modifier.height(12.dp))

                    if (isLoading) {
                        // 加载中
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Purple80)
                        }
                    } else if (recentContentWithProgress.isNotEmpty()) {
                        //  recent reading items
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            recentContentWithProgress.forEachIndexed { index, contentWithProgress ->
                                if (index < 3) { // 最多显示3个
                                    val content = contentWithProgress.content
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { 
                                                // 导航到最后练习的段落
                                                if (contentWithProgress.lastPracticeParagraph != null) {
                                                    navController.navigate("${Screen.ReadingPractice.route}/${content.id}?paragraph=${contentWithProgress.lastPracticeParagraph}")
                                                } else {
                                                    navController.navigate("${Screen.ReadingPractice.route}/${content.id}")
                                                }
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = surfaceContainer
                                        ),
                                        elevation = CardDefaults.cardElevation(
                                            defaultElevation = 2.dp
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = content.title,
                                                    style = Typography.bodyMedium,
                                                    color = onBackground
                                                )
                                                Column(
                                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        text = "${content.estimated_duration / 60} 分钟",
                                                        style = Typography.bodySmall,
                                                        color = onBackground
                                                    )
                                                    Text(
                                                        text = "${contentWithProgress.progress}%", // 显示实际进度
                                                        style = Typography.bodySmall,
                                                        color = Color(76, 175, 80) // success color
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // 无最近阅读
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "暂无最近阅读",
                                style = Typography.bodyMedium,
                                color = onBackground
                            )
                        }
                    }
                }
            }
        }

        // 底部导航栏
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRect(color = if (isDarkTheme) Color(41, 38, 45) else surfaceContainer)
                }
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 首页
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { /* 跳转到首页 */ }
                        .padding(8.dp)
                ) {
                    Text(
                        text = "🏠",
                        style = TextStyle(
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        text = "首页",
                        style = Typography.bodySmall,
                        color = Purple80
                    )
                }
                
                // 内容库
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { navController.navigate(Screen.ContentLibrary.route) }
                        .padding(8.dp)
                ) {
                    Text(
                        text = "📚",
                        style = TextStyle(
                            fontSize = 20.sp
                        )
                    )
                    Text(
                            text = "内容库",
                            style = Typography.bodySmall,
                            color = onBackground
                        )
                }
                
                // 练习
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { navController.navigate(Screen.ReadingPractice.route) }
                        .padding(8.dp)
                ) {
                    Text(
                        text = "🔊",
                        style = TextStyle(
                            fontSize = 20.sp
                        )
                    )
                    Text(
                            text = "练习",
                            style = Typography.bodySmall,
                            color = onBackground
                        )
                }
                
                // 记录
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { navController.navigate(Screen.ProgressRecord.route) }
                        .padding(8.dp)
                ) {
                    Text(
                        text = "📊",
                        style = TextStyle(
                            fontSize = 20.sp
                        )
                    )
                    Text(
                            text = "记录",
                            style = Typography.bodySmall,
                            color = onBackground
                        )
                }
            }
        }
    }
}

