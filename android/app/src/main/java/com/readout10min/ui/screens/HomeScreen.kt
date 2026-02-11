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
import com.readout10min.data.repositories.ContentRepository
import com.readout10min.navigation.Screen
import com.readout10min.ui.theme.BackgroundColor
import com.readout10min.ui.theme.OnBackground
import com.readout10min.ui.theme.Purple80
import com.readout10min.ui.theme.SurfaceColor
import com.readout10min.ui.theme.SurfaceContainer
import com.readout10min.ui.theme.SurfaceVariant
import com.readout10min.ui.theme.Typography
import com.readout10min.ui.theme.White
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val contentRepository = ContentRepository()
    
    var recommendedContent by remember { mutableStateOf<List<Content>>(emptyList()) }
    var recentContent by remember { mutableStateOf<List<Content>>(emptyList()) }
    var practiceDays by remember { mutableStateOf(0) }
    var todayPracticeCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadTrigger by remember { mutableStateOf(UUID.randomUUID()) }
    
    // 模拟用户ID
    val userId = UUID.fromString("00000000-0000-0000-0000-000000000000")
    
    // 加载数据
    LaunchedEffect(key1 = Unit, key2 = reloadTrigger) {
        isLoading = true
        error = null
        try {
            // 获取推荐内容
            val recommended = withContext(Dispatchers.IO) {
                contentRepository.getRecommendedContent()
            }
            
            // 检查推荐内容加载是否有错误
            val recommendError = contentRepository.lastError
            if (recommendError != null) {
                error = recommendError
            } else {
                recommendedContent = recommended
            }
            
            // 获取最近阅读
            val recent = withContext(Dispatchers.IO) {
                contentRepository.getRecentContent(userId)
            }
            
            // 检查最近阅读加载是否有错误
            val recentError = contentRepository.lastError
            if (recentError != null && error == null) {
                error = recentError
            } else {
                recentContent = recent
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 错误信息显示
            if (error != null) {
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
            // 练习统计
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
                        color = OnBackground
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
                        containerColor = SurfaceContainer
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
                                    color = OnBackground
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(40.dp)
                                    .drawBehind {
                                        drawRect(color = SurfaceVariant)
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
                                    color = OnBackground
                                )
                            }
                        }
                    }
                }
            }

            // 今日推荐
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
                        color = OnBackground
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
                            containerColor = SurfaceContainer
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
                                color = OnBackground
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "⏱️ ${content.estimated_duration} 分钟",
                                    style = Typography.bodySmall,
                                    color = OnBackground
                                )
                            }
                            Text(
                                text = "This article is recommended for you based on your reading history and preferences...",
                                style = Typography.bodyMedium,
                                color = OnBackground
                            )
                            Button(
                                onClick = { 
                                    navController.navigate("${Screen.ReadingPractice.route}/${content.id}") 
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
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
                            color = OnBackground
                        )
                    }
                }
            }

            // 最近阅读
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
                        color = OnBackground
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
                } else if (recentContent.isNotEmpty()) {
                    //  recent reading items
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        recentContent.forEachIndexed { index, content ->
                            if (index < 3) { // 最多显示3个
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { 
                                            navController.navigate("${Screen.ReadingPractice.route}/${content.id}") 
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = SurfaceContainer
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
                                                color = OnBackground
                                            )
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "${content.estimated_duration} 分钟",
                                                    style = Typography.bodySmall,
                                                    color = OnBackground
                                                )
                                                Text(
                                                    text = "70%", // 模拟进度
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
                            color = OnBackground
                        )
                    }
                }
            }
        }

        // 底部导航栏
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
                        color = OnBackground
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
                        color = OnBackground
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
                        color = OnBackground
                    )
                }
            }
        }
    }
}

