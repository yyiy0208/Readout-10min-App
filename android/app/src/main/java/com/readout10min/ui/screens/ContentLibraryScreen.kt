package com.readout10min.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.readout10min.data.SupabaseClient
import com.readout10min.data.models.Content
import com.readout10min.data.models.Paragraph
import com.readout10min.data.models.Progress
import com.readout10min.data.repositories.ContentRepository
import com.readout10min.navigation.Screen
import com.readout10min.ui.theme.Purple80
import com.readout10min.ui.theme.Typography
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.isSystemInDarkTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentLibraryScreen(navController: NavController) {
    val context = LocalContext.current
    val contentRepository = ContentRepository()
    val searchText = remember { mutableStateOf("") }
    
    // 获取主题颜色
    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onBackground = MaterialTheme.colorScheme.onBackground
    val tertiary = MaterialTheme.colorScheme.tertiary
    val secondary = MaterialTheme.colorScheme.secondary
    val onTertiary = MaterialTheme.colorScheme.onTertiary
    val outline = MaterialTheme.colorScheme.outline
    
    var contentList by remember { mutableStateOf<List<Content>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // 加载数据
    LaunchedEffect(key1 = Unit) {
        isLoading = true
        try {
            val content = withContext(Dispatchers.IO) {
                contentRepository.getContentList()
            }
            contentList = content
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoading = false
        }
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
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "返回首页",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // 主内容区域
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // 添加竖直滚动条
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
            } else if (contentList.isNotEmpty()) {
                // 内容列表
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    contentList.forEach { content ->
                        ContentCard(
                            content = content,
                            navController = navController
                        )
                    }
                }
            } else {
                // 无内容
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                    text = "暂无内容",
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                }
            }
        }
    }
}

// 边框扩展函数
fun Modifier.border(width: Dp, color: Color): Modifier {
    return this.then(
        Modifier.drawBehind {
            val strokeWidth = width.value
            val padding = strokeWidth / 2
            drawRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(padding, padding),
                size = size.copy(
                    width = size.width - strokeWidth,
                    height = size.height - strokeWidth
                ),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
            )
        }
    )
}

// 边框圆角扩展函数
fun Modifier.borderRadius(radius: Dp): Modifier {
    return this.then(
        Modifier.clip(RoundedCornerShape(radius))
    )
}

@Composable
fun ContentCard(
    content: Content,
    navController: NavController
) {
    val isExpanded = remember { mutableStateOf(false) }
    val contentRepository = ContentRepository()
    
    // 获取主题颜色
    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onBackground = MaterialTheme.colorScheme.onBackground
    val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer
    val secondaryContainer = MaterialTheme.colorScheme.secondaryContainer
    val tertiary = MaterialTheme.colorScheme.tertiary
    val secondary = MaterialTheme.colorScheme.secondary
    val onTertiary = MaterialTheme.colorScheme.onTertiary
    val outline = MaterialTheme.colorScheme.outline
    
    var paragraphs by remember { mutableStateOf<List<Paragraph>>(emptyList()) }
    var isLoadingParagraphs by remember { mutableStateOf(false) }
    var progressData by remember { mutableStateOf<Map<Int, Pair<Boolean, Boolean>>>(emptyMap()) }
    
    // 模拟用户ID
    val userId = UUID.fromString("00000000-0000-0000-0000-000000000000")
    
    // 加载段落数据
    LaunchedEffect(key1 = Unit) {
        if (paragraphs.isEmpty() || progressData.isEmpty()) {
            isLoadingParagraphs = true
            try {
                val paragraphList = withContext(Dispatchers.IO) {
                    contentRepository.getParagraphsByContentId(content.id)
                }
                paragraphs = paragraphList
                
                // 加载阅读进度
                val progressMap = mutableMapOf<Int, Pair<Boolean, Boolean>>() // (是否存在记录, 是否已完成)
                paragraphList.forEach {
                    progressMap[it.paragraph_number] = Pair(false, false)
                }
                
                // 获取所有进度记录
                val allProgress = withContext(Dispatchers.IO) {
                    contentRepository.getAllProgressByUserIdAndContentId(userId, content.id)
                }
                
                // 为每个段落单独获取实际的阅读进度
                paragraphList.forEach { paragraph ->
                    val progress = allProgress.find { it.current_paragraph == paragraph.paragraph_number }
                    if (progress != null) {
                        progressMap[paragraph.paragraph_number] = Pair(true, progress.is_completed)
                    }
                }
                
                progressData = progressMap
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingParagraphs = false
            }
        }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("${Screen.ReadingPractice.route}/${content.id}") },
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
            // 卡片标题和展开按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = content.title,
                    style = Typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isExpanded.value) "▲" else "▼",
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.clickable { isExpanded.value = !isExpanded.value }
                )
            }

            // 内容元数据
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                content.author?.let {
                    Text(
                        text = "作者：$it",
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "预估总时长：${content.estimated_duration / 60}分钟",
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // 内容状态和进度条
            Column(
                modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 计算文章状态
                val articleStatus = if (progressData.isEmpty()) {
                    "未读"
                } else {
                    val hasCompleted = progressData.values.any { it.first && it.second }
                    val hasInProgress = progressData.values.any { it.first && !it.second }
                    val allCompleted = progressData.values.all { it.first && it.second }
                    
                    when {
                        allCompleted -> "已读完"
                        hasCompleted || hasInProgress -> "阅读中"
                        else -> "未读"
                    }
                }
                
                Text(
                    text = articleStatus,
                    style = Typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                // 计算进度百分比
                val progressPercentage = if (progressData.isEmpty()) {
                    0f
                } else {
                    val completedCount = progressData.values.count { it.first && it.second }
                    completedCount.toFloat() / progressData.size
                }
                
                // 进度条
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .drawBehind {
                            drawRect(color = surfaceVariant)
                        }
                        .borderRadius(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressPercentage)
                            .height(4.dp)
                            .drawBehind {
                                drawRect(color = Purple80)
                            }
                            .borderRadius(2.dp)
                    )
                }
            }

            // 段落列表
                if (isExpanded.value) {
                    Column(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "段落列表",
                            style = Typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        if (isLoadingParagraphs) {
                            Box(
                                modifier = Modifier
                                    .height(100.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Purple80, strokeWidth = 2.dp)
                            }
                        } else if (paragraphs.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp) // 设置一个固定高度
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp) // 增加间距
                                ) {
                                    paragraphs.forEach { paragraph ->
                            val successColor = Color(76, 175, 80) // #4CAF50
                            val primaryColor = Color(104, 84, 141) // 主色调
                            
                            val statusInfo = progressData[paragraph.paragraph_number]
                            
                            // 卡片背景色
                            val cardColor = when {
                                statusInfo?.first == true && statusInfo.second -> tertiaryContainer // 已完成 - 使用主题 tertiaryContainer
                                statusInfo?.first == true && !statusInfo.second -> secondaryContainer // 进行中 - 使用主题 secondaryContainer
                                else -> surfaceContainer // 未开始 - 使用主题背景
                            }
                            
                            // 边框颜色
                            val borderColorValue = when {
                                statusInfo?.first == true && statusInfo.second -> tertiary // 已完成 - 使用主题 tertiary
                                statusInfo?.first == true && !statusInfo.second -> secondary // 进行中 - 使用主题 secondary
                                else -> outline // 未开始 - 使用主题边框色
                            }
                            
                            // 段落编号背景色
                            val numberBackgroundColor = when {
                                statusInfo?.first == true && statusInfo.second -> tertiary // 已完成 - 使用主题 tertiary
                                statusInfo?.first == true && !statusInfo.second -> secondary // 进行中 - 使用主题 secondary
                                else -> surfaceVariant // 未开始 - 使用主题表面变体色
                            }
                            
                            // 段落编号文字色
                            val numberTextColor = when {
                                statusInfo?.first == true -> onTertiary // 已完成和进行中 - 使用主题 onTertiary
                                else -> onBackground // 未开始 - 使用主题文字色
                            }
                            
                            // 状态框背景色
                            val statusBoxColor = when {
                                statusInfo?.first == true && statusInfo.second -> tertiary // 已完成 - 使用主题 tertiary
                                statusInfo?.first == true && !statusInfo.second -> secondary // 进行中 - 使用主题 secondary
                                else -> surfaceVariant // 未开始 - 使用主题表面变体色
                            }
                            
                            // 状态文字色
                            val statusTextColor = when {
                                statusInfo?.first == true -> onTertiary // 已完成和进行中 - 使用主题 onTertiary
                                else -> onBackground // 未开始 - 使用主题文字色
                            }
                            
                            // 段落容器，使用双Box方案实现带圆角的边框
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { navController.navigate("${Screen.ReadingPractice.route}/${content.id}?paragraph=${paragraph.paragraph_number}&paragraphId=${paragraph.id}") }
                            ) {
                                // 外层Box作为边框
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .drawBehind {
                                            drawRect(color = borderColorValue)
                                        }
                                ) {
                                    // 内层Box作为内容容器
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(1.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .drawBehind {
                                                drawRect(color = cardColor)
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // 段落编号
                                            Box(
                                                modifier = Modifier
                                                    .width(24.dp)
                                                    .height(24.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .drawBehind {
                                                        drawRect(color = numberBackgroundColor)
                                                    }
                                                    .align(Alignment.CenterVertically)
                                            ) {
                                                Text(
                                                    text = paragraph.paragraph_number.toString(),
                                                    style = Typography.bodySmall,
                                                    color = numberTextColor,
                                                    modifier = Modifier.align(Alignment.Center)
                                                )
                                            }

                                            // 段落标题
                                            Text(
                                                    text = "段落${paragraph.paragraph_number}",
                                                    style = Typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(start = 12.dp)
                                                )

                                            // 段落时长
                                            Text(
                                                text = "${paragraph.estimated_duration / 60}分钟",
                                                style = Typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onBackground,
                                                modifier = Modifier.padding(horizontal = 12.dp)
                                            )

                                            // 段落状态
                                            val statusInfo = progressData[paragraph.paragraph_number]
                                            val statusText = if (statusInfo?.first == true) {
                                                if (statusInfo.second) {
                                                    "已完成"
                                                } else {
                                                    "进行中"
                                                }
                                            } else {
                                                "未开始"
                                            }
                                            
                                            // 段落状态
                                            Box(
                                                modifier = Modifier
                                                    .padding(4.dp, 2.dp, 4.dp, 2.dp)
                                                    .clip(RoundedCornerShape(9999.dp))
                                                    .drawBehind {
                                                        drawRect(color = statusBoxColor)
                                                    }
                                            ) {
                                                Text(
                                                    text = statusText,
                                                    style = Typography.bodySmall,
                                                    color = statusTextColor,
                                                    modifier = Modifier.padding(8.dp, 4.dp, 8.dp, 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                            }
                        }
                    } else {
                        Text(
                            text = "暂无段落数据",
                            style = Typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

