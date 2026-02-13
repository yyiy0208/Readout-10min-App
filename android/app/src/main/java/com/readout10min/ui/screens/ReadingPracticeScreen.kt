package com.readout10min.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.readout10min.data.models.Paragraph as DataParagraph
import com.readout10min.data.repositories.ContentRepository
import com.readout10min.navigation.Screen
import com.readout10min.ui.theme.Purple80
import com.readout10min.ui.theme.Typography
import androidx.compose.material3.MaterialTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.min
import java.util.UUID

@Composable
fun ReadingPracticeScreen(navController: NavController, contentId: UUID?, paragraphNumber: Int?, paragraphId: UUID?, isNavBarVisible: androidx.compose.runtime.MutableState<Boolean>) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val contentRepository = ContentRepository()
    
    // 获取主题颜色
    val surfaceContainer = MaterialTheme.colorScheme.surfaceContainer
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onBackground = MaterialTheme.colorScheme.onBackground
    val background = MaterialTheme.colorScheme.background
    
    // 状态管理
    val activePanel = remember { mutableStateOf<String?>(null) }
    val isControlsVisible = remember { mutableStateOf(true) }
    val fontSize = remember { mutableStateOf<Float>(16f) }
    val lineHeight = remember { mutableStateOf<Float>(24f) }
    val backgroundColor = remember { mutableStateOf(background) }
    val progress = remember { mutableStateOf(0.0f) } // 0.0 to 1.0
    
    var paragraphs by remember { mutableStateOf<List<DataParagraph>>(emptyList()) }
    var currentParagraph by remember { mutableStateOf<DataParagraph?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()
    
    // 监听滚动状态，更新进度
    LaunchedEffect(key1 = scrollState.value) {
        if (currentParagraph != null && contentId != null) {
            val maxScroll = scrollState.maxValue
            if (maxScroll > 0) {
                progress.value = scrollState.value.toFloat() / maxScroll.toFloat()
                
                // 当滚动到接近末尾时，标记为已完成
                if (progress.value > 0.95 && !isLoading) {
                    // 更新进度为已完成
                    val userId = UUID.fromString("00000000-0000-0000-0000-000000000000")
                    val progressData = com.readout10min.data.models.Progress(
                        user_id = userId,
                        content_id = contentId,
                        current_paragraph = currentParagraph!!.paragraph_number,
                        is_completed = true // 标记为已完成
                    )
                    withContext(Dispatchers.IO) {
                        contentRepository.updateProgress(progressData)
                    }
                    
                    // 创建练习记录
                    val practiceRecord = com.readout10min.data.models.PracticeRecord(
                        user_id = userId,
                        paragraph_id = currentParagraph!!.id,
                        content_id = contentId,
                        practice_date = java.time.LocalDateTime.now().toString(),
                        duration = currentParagraph!!.estimated_duration, // estimated_duration 已经是秒
                        accuracy = 0.0f, // 暂时使用默认值
                        fluency = 0.0f, // 暂时使用默认值
                        pronunciation_score = 0.0f // 暂时使用默认值
                    )
                    withContext(Dispatchers.IO) {
                        contentRepository.createPracticeRecord(practiceRecord)
                    }
                }
            }
        }
    }
    
    // 加载段落数据
    LaunchedEffect(key1 = contentId, key2 = paragraphId) {
        if (contentId != null) {
            isLoading = true
            try {
                val paragraphList = withContext(Dispatchers.IO) {
                    contentRepository.getParagraphsByContentId(contentId)
                }
                
                // 加载所有段落
                paragraphs = paragraphList
                
                // 如果指定了 paragraphId，定位到该段落
                if (paragraphId != null) {
                    val targetParagraph = paragraphList.find { it.id == paragraphId }
                    if (targetParagraph != null) {
                        currentParagraph = targetParagraph
                        progress.value = 0f
                        
                        // 自动记录进度
                        val userId = UUID.fromString("00000000-0000-0000-0000-000000000000")
                        val progressData = com.readout10min.data.models.Progress(
                            user_id = userId,
                            content_id = contentId,
                            current_paragraph = targetParagraph.paragraph_number,
                            is_completed = false // 默认为 false，只有用户读完该段落才设置为 true
                        )
                        withContext(Dispatchers.IO) {
                            println("开始更新进度: contentId=$contentId, paragraphNumber=${targetParagraph.paragraph_number}")
                            val result = contentRepository.updateProgress(progressData)
                            println("更新进度结果: $result")
                            if (!result) {
                                println("更新进度失败: ${contentRepository.lastError}")
                            }
                        }
                    } else if (paragraphNumber != null && paragraphList.isNotEmpty()) {
                        // 如果找不到指定的段落，但有段落编号，定位到对应编号的段落
                        val targetParagraph = paragraphList.find { it.paragraph_number == paragraphNumber }
                        if (targetParagraph != null) {
                            currentParagraph = targetParagraph
                            progress.value = 0f
                        }
                    }
                } else if (paragraphNumber != null && paragraphList.isNotEmpty()) {
                    // 如果没有指定 paragraphId，但有段落编号，定位到对应编号的段落
                    val targetParagraph = paragraphList.find { it.paragraph_number == paragraphNumber }
                    if (targetParagraph != null) {
                        currentParagraph = targetParagraph
                        progress.value = 0f
                    }
                } else if (paragraphList.isNotEmpty()) {
                    // 加载第一个段落
                    currentParagraph = paragraphList[0]
                    progress.value = 0f
                }
            } catch (e: Exception) {
                e.printStackTrace()
                error = "加载失败，请重试"
            } finally {
                isLoading = false
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(color = backgroundColor.value)
            }
    ) {
        // 顶部导航栏
        if (isControlsVisible.value) {
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
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // 阅读区域
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // 直接使用Box作为内容容器，移除左右点击区域
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        if (!isLoading && error == null) {
                            isControlsVisible.value = !isControlsVisible.value
                            isNavBarVisible.value = !isNavBarVisible.value
                        }
                    }
            ) {
                if (isLoading) {
                    // 加载中
                    CircularProgressIndicator(color = Purple80, modifier = Modifier.align(Alignment.Center))
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
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(
                            onClick = { 
                                // 重新加载
                                lifecycleOwner.lifecycleScope.launch {
                                    if (contentId != null) {
                                        isLoading = true
                                        try {
                                            val paragraphList = withContext(Dispatchers.IO) {
                                                contentRepository.getParagraphsByContentId(contentId)
                                            }
                                            paragraphs = paragraphList
                                            error = null
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            error = "加载失败，请重试"
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Purple80
                            )
                        ) {
                            Text(text = "重新加载")
                        }
                    }
                } else if (currentParagraph == null) {
                    // 无内容
                    Text(
                        text = "暂无内容",
                        style = Typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    // 正常显示内容
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 文章内容
                        val displayText = currentParagraph?.text ?: ""
                        
                        Text(
                                text = displayText,
                                style = Typography.bodyLarge.copy(
                                    fontSize = fontSize.value.sp,
                                    lineHeight = lineHeight.value.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .verticalScroll(scrollState),
                                softWrap = true,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Clip,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Start
                            )
                    }
                }
            }
        }

        // 进度条 - 固定在最下面
        if (!isLoading && error == null && currentParagraph != null) {
            Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .drawBehind {
                            drawRect(color = surfaceVariant.copy(alpha = 0.5f))
                        }
                        .clip(RoundedCornerShape(9999.dp))
                ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.value)
                        .height(6.dp)
                        .drawBehind {
                            drawRect(color = Purple80)
                        }
                        .clip(RoundedCornerShape(9999.dp))
                )
            }
        }
        


        // 底部控制栏
        if (isControlsVisible.value && !isLoading && error == null && currentParagraph != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawRect(color = surfaceContainer)
                    }
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 字体按钮
                    Button(
                        onClick = { 
                            activePanel.value = if (activePanel.value == "font") null else "font"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activePanel.value == "font") Purple80 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                            contentColor = if (activePanel.value == "font") Color.White else MaterialTheme.colorScheme.onBackground
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "字体",
                            style = Typography.bodySmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                    }
                    
                    // 行间距按钮
                    Button(
                        onClick = { 
                            activePanel.value = if (activePanel.value == "spacing") null else "spacing"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activePanel.value == "spacing") Purple80 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                            contentColor = if (activePanel.value == "spacing") Color.White else MaterialTheme.colorScheme.onBackground
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "行间距",
                            style = Typography.bodySmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                    }
                    
                    // 背景色按钮
                    Button(
                        onClick = { 
                            activePanel.value = if (activePanel.value == "background") null else "background"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (activePanel.value == "background") Purple80 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                            contentColor = if (activePanel.value == "background") Color.White else MaterialTheme.colorScheme.onBackground
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "背景色",
                            style = Typography.bodySmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                    }
                }
            }
            
            // 字体选项面板
            if (activePanel.value == "font") {
                Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawRect(color = surfaceContainer)
                    }
                    .padding(16.dp)
            ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Button(
                            onClick = { 
                                fontSize.value = 14f
                            },
                            modifier = Modifier.weight(1f).padding(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (fontSize.value == 14f) Purple80 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                contentColor = if (fontSize.value == 14f) Color.White else MaterialTheme.colorScheme.onBackground
                            )
                        ) {
                            Text(text = "小")
                        }
                        Button(
                            onClick = { 
                                fontSize.value = 16f
                            },
                            modifier = Modifier.weight(1f).padding(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (fontSize.value == 16f) Purple80 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                contentColor = if (fontSize.value == 16f) Color.White else MaterialTheme.colorScheme.onBackground
                            )
                        ) {
                            Text(text = "中")
                        }
                        Button(
                            onClick = { 
                                fontSize.value = 18f
                            },
                            modifier = Modifier.weight(1f).padding(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (fontSize.value == 18f) Purple80 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                contentColor = if (fontSize.value == 18f) Color.White else MaterialTheme.colorScheme.onBackground
                            )
                        ) {
                            Text(text = "大")
                        }
                    }
                }
            }
            
            // 行间距选项面板
            if (activePanel.value == "spacing") {
                Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawRect(color = surfaceContainer)
                    }
                    .padding(16.dp)
            ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Button(
                            onClick = { 
                                lineHeight.value = 20f
                            },
                            modifier = Modifier.weight(1f).padding(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (lineHeight.value == 20f) Purple80 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                contentColor = if (lineHeight.value == 20f) Color.White else MaterialTheme.colorScheme.onBackground
                            )
                        ) {
                            Text(text = "缩小")
                        }
                        Button(
                            onClick = { 
                                lineHeight.value = 24f
                            },
                            modifier = Modifier.weight(1f).padding(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (lineHeight.value == 24f) Purple80 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                contentColor = if (lineHeight.value == 24f) Color.White else MaterialTheme.colorScheme.onBackground
                            )
                        ) {
                            Text(text = "标准")
                        }
                        Button(
                            onClick = { 
                                lineHeight.value = 28f
                            },
                            modifier = Modifier.weight(1f).padding(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (lineHeight.value == 28f) Purple80 else surfaceVariant.copy(alpha = 0.8f),
                                contentColor = if (lineHeight.value == 28f) Color.White else onBackground
                            )
                        ) {
                            Text(text = "增加")
                        }
                    }
                }
            }
            
            // 背景色选项面板
            if (activePanel.value == "background") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawRect(color = surfaceContainer)
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(30.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .drawBehind {
                                    drawRect(color = if (backgroundColor.value == Color.White) Purple80 else Color(203, 196, 207))
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .drawBehind {
                                        drawRect(color = Color.White)
                                    }
                                    .clickable { 
                                        backgroundColor.value = Color.White
                                    }
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(30.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .drawBehind {
                                    drawRect(color = if (backgroundColor.value == Color(255, 251, 235)) Purple80 else Color(203, 196, 207))
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .drawBehind {
                                        drawRect(color = Color(255, 251, 235))
                                    }
                                    .clickable { 
                                        backgroundColor.value = Color(255, 251, 235)
                                    }
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(30.dp)
                                .clip(RoundedCornerShape(15.dp))
                                .drawBehind {
                                    drawRect(color = if (backgroundColor.value == Color(18, 18, 18)) Purple80 else Color(203, 196, 207))
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .drawBehind {
                                        drawRect(color = Color(18, 18, 18))
                                    }
                                    .clickable { 
                                        backgroundColor.value = Color(18, 18, 18)
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}
