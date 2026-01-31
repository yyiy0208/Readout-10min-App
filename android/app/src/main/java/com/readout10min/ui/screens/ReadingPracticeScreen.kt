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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.readout10min.data.models.Paragraph
import com.readout10min.data.repositories.ContentRepository
import com.readout10min.navigation.Screen
import com.readout10min.ui.theme.OnBackground
import com.readout10min.ui.theme.Purple80
import com.readout10min.ui.theme.SurfaceContainer
import com.readout10min.ui.theme.SurfaceVariant
import com.readout10min.ui.theme.Typography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.min
import java.util.UUID

@Composable
fun ReadingPracticeScreen(navController: NavController, contentId: UUID?, isNavBarVisible: androidx.compose.runtime.MutableState<Boolean>) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val contentRepository = ContentRepository()
    
    // 状态管理
    val activePanel = remember { mutableStateOf<String?>(null) }
    val isControlsVisible = remember { mutableStateOf(true) }
    val fontSize = remember { mutableStateOf(16.sp) }
    val lineHeight = remember { mutableStateOf(24.sp) }
    val backgroundColor = remember { mutableStateOf(Color.White) }
    val progress = remember { mutableStateOf(0.3f) } // 0.0 to 1.0
    
    var paragraphs by remember { mutableStateOf<List<Paragraph>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // 加载段落数据
    LaunchedEffect(key1 = contentId) {
        if (contentId != null) {
            isLoading = true
            try {
                val paragraphList = withContext(Dispatchers.IO) {
                    contentRepository.getParagraphsByContentId(contentId)
                }
                paragraphs = paragraphList
            } catch (e: Exception) {
                e.printStackTrace()
                error = "加载失败，请重试"
            } finally {
                isLoading = false
            }
        }
    }
    
    // 根据进度计算当前显示的段落
    val currentParagraphIndex = remember(progress.value, paragraphs.size) {
        if (paragraphs.isEmpty()) 0 else {
            val index = (progress.value * paragraphs.size).toInt()
            minOf(index, paragraphs.size - 1)
        }
    }
    val currentParagraph = if (paragraphs.isNotEmpty() && currentParagraphIndex < paragraphs.size) {
        paragraphs[currentParagraphIndex].text
    } else {
        ""
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
                        drawRect(color = SurfaceContainer)
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
                        tint = OnBackground
                    )
                }
            }
        }

        // 阅读区域
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(16.dp)
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
                        color = OnBackground,
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
            } else if (paragraphs.isEmpty()) {
                // 无内容
                Text(
                    text = "暂无内容",
                    style = Typography.bodyMedium,
                    color = OnBackground,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                // 正常显示内容
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // 文章内容
                    Text(
                        text = currentParagraph,
                        style = Typography.bodyLarge.copy(
                            fontSize = fontSize.value,
                            lineHeight = lineHeight.value
                        ),
                        color = OnBackground,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // 段落指示器
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        paragraphs.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .width(8.dp)
                                    .height(8.dp)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .drawBehind {
                                        drawRect(
                                            color = if (index == currentParagraphIndex) Purple80 else SurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                    .clickable {
                                        progress.value = index.toFloat() / (paragraphs.size - 1)
                                    }
                            )
                        }
                    }
                }
            }
        }

        // 进度条 - 固定在最下面
        if (!isLoading && error == null && paragraphs.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .drawBehind {
                        drawRect(color = SurfaceVariant.copy(alpha = 0.5f))
                    }
                    .clip(RoundedCornerShape(9999.dp))
                    .clickable {
                        // 简单实现：点击进度条时随机改变进度
                        val randomProgress = (0..100).random() / 100f
                        progress.value = randomProgress
                    }
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
        if (isControlsVisible.value && !isLoading && error == null && paragraphs.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        drawRect(color = SurfaceContainer)
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
                            containerColor = if (activePanel.value == "font") Purple80 else Color(231, 224, 235).copy(alpha = 0.8f),
                            contentColor = if (activePanel.value == "font") Color.White else OnBackground
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
                            containerColor = if (activePanel.value == "spacing") Purple80 else Color(231, 224, 235).copy(alpha = 0.8f),
                            contentColor = if (activePanel.value == "spacing") Color.White else OnBackground
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
                            containerColor = if (activePanel.value == "background") Purple80 else Color(231, 224, 235).copy(alpha = 0.8f),
                            contentColor = if (activePanel.value == "background") Color.White else OnBackground
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
                            drawRect(color = SurfaceContainer)
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Button(
                            onClick = { 
                                fontSize.value = 14.sp
                            },
                            modifier = Modifier.weight(1f).padding(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (fontSize.value == 14.sp) Purple80 else Color(231, 224, 235).copy(alpha = 0.8f),
                                contentColor = if (fontSize.value == 14.sp) Color.White else OnBackground
                            )
                        ) {
                            Text(text = "小")
                        }
                        Button(
                            onClick = { 
                                fontSize.value = 16.sp
                            },
                            modifier = Modifier.weight(1f).padding(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (fontSize.value == 16.sp) Purple80 else Color(231, 224, 235).copy(alpha = 0.8f),
                                contentColor = if (fontSize.value == 16.sp) Color.White else OnBackground
                            )
                        ) {
                            Text(text = "中")
                        }
                        Button(
                            onClick = { 
                                fontSize.value = 18.sp
                            },
                            modifier = Modifier.weight(1f).padding(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (fontSize.value == 18.sp) Purple80 else Color(231, 224, 235).copy(alpha = 0.8f),
                                contentColor = if (fontSize.value == 18.sp) Color.White else OnBackground
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
                            drawRect(color = SurfaceContainer)
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Button(
                            onClick = { 
                                lineHeight.value = 20.sp
                            },
                            modifier = Modifier.weight(1f).padding(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (lineHeight.value == 20.sp) Purple80 else Color(231, 224, 235).copy(alpha = 0.8f),
                                contentColor = if (lineHeight.value == 20.sp) Color.White else OnBackground
                            )
                        ) {
                            Text(text = "缩小")
                        }
                        Button(
                            onClick = { 
                                lineHeight.value = 24.sp
                            },
                            modifier = Modifier.weight(1f).padding(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (lineHeight.value == 24.sp) Purple80 else Color(231, 224, 235).copy(alpha = 0.8f),
                                contentColor = if (lineHeight.value == 24.sp) Color.White else OnBackground
                            )
                        ) {
                            Text(text = "标准")
                        }
                        Button(
                            onClick = { 
                                lineHeight.value = 28.sp
                            },
                            modifier = Modifier.weight(1f).padding(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (lineHeight.value == 28.sp) Purple80 else Color(231, 224, 235).copy(alpha = 0.8f),
                                contentColor = if (lineHeight.value == 28.sp) Color.White else OnBackground
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
                            drawRect(color = SurfaceContainer)
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
