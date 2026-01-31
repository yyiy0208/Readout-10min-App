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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.readout10min.data.models.Progress
import com.readout10min.data.repositories.ContentRepository
import com.readout10min.navigation.Screen
import com.readout10min.ui.theme.OnBackground
import com.readout10min.ui.theme.Purple80
import com.readout10min.ui.theme.SurfaceContainer
import com.readout10min.ui.theme.SurfaceVariant
import com.readout10min.ui.theme.Typography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID

@Composable
fun ProgressRecordScreen(navController: NavController) {
    val context = LocalContext.current
    val contentRepository = ContentRepository()
    
    // 状态管理
    val currentMonth = remember { mutableStateOf("2026年1月") }
    val selectedPeriod = remember { mutableStateOf("week") }
    var progressList by remember { mutableStateOf<List<Progress>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    val reloadTrigger = remember { mutableStateOf(UUID.randomUUID()) }
    
    // 模拟用户ID
    val userId = UUID.fromString("00000000-0000-0000-0000-000000000000")
    
    // 加载进度数据
    LaunchedEffect(key1 = Unit, key2 = reloadTrigger.value) {
        isLoading = true
        try {
            // 这里应该从Supabase获取用户的进度数据
            // 由于我们还没有实现获取所有进度的方法，暂时使用空列表
            // 后续可以在ContentRepository中添加getAllProgress(userId)方法
            progressList = emptyList()
            error = null
        } catch (e: Exception) {
            e.printStackTrace()
            error = "加载失败，请重试"
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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回首页",
                    tint = OnBackground
                )
            }
        }

        // 主内容区域
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
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
                        color = OnBackground,
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
                // 练习日历卡片
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .drawBehind {
                            drawRect(color = SurfaceContainer)
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
                            color = OnBackground
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
                                color = OnBackground,
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
                                        .clickable { /* 切换到上月 */ }
                                        .drawBehind {
                                            drawRect(color = Color(231, 224, 235))
                                        }
                                        .align(Alignment.CenterVertically)
                                ) {
                                    Text(
                                        text = "‹",
                                        style = TextStyle(
                                            fontSize = 18.sp,
                                            color = OnBackground
                                        ),
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }
                                // 下月按钮
                                Box(
                                    modifier = Modifier
                                        .width(32.dp)
                                        .height(32.dp)
                                        .clickable { /* 切换到下月 */ }
                                        .drawBehind {
                                            drawRect(color = Color(231, 224, 235))
                                        }
                                        .align(Alignment.CenterVertically)
                                ) {
                                    Text(
                                        text = "›",
                                        style = TextStyle(
                                            fontSize = 18.sp,
                                            color = OnBackground
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
                            // 上月日期
                            item {
                                CalendarDay(day = "30", isOtherMonth = true)
                            }
                            item {
                                CalendarDay(day = "31", isOtherMonth = true)
                            }
                            
                            // 当月日期
                            for (day in 1..31) {
                                item {
                                    when (day) {
                                        2, 5, 8, 12, 16 -> CalendarDay(day = day.toString(), hasPractice = true)
                                        19 -> CalendarDay(day = day.toString(), isToday = true)
                                        else -> CalendarDay(day = day.toString())
                                    }
                                }
                            }
                            
                            // 下月日期
                            for (day in 1..3) {
                                item {
                                    CalendarDay(day = day.toString(), isOtherMonth = true)
                                }
                            }
                        }
                    }
                }

                // 统计图表卡片
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .drawBehind {
                            drawRect(color = SurfaceContainer)
                        }
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 卡片标题
                        Text(
                            text = "2026年练习时长统计",
                            style = Typography.titleMedium,
                            color = OnBackground
                        )
                        
                        // 周期切换按钮
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { selectedPeriod.value = "week" },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedPeriod.value == "week") Purple80 else Color(231, 224, 235),
                                    contentColor = if (selectedPeriod.value == "week") Color.White else OnBackground
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "周",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            Button(
                                onClick = { selectedPeriod.value = "month" },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedPeriod.value == "month") Purple80 else Color(231, 224, 235),
                                    contentColor = if (selectedPeriod.value == "month") Color.White else OnBackground
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "月",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                            Button(
                                onClick = { selectedPeriod.value = "year" },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedPeriod.value == "year") Purple80 else Color(231, 224, 235),
                                    contentColor = if (selectedPeriod.value == "year") Color.White else OnBackground
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "年",
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                        
                        // 柱状图
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .drawBehind {
                                    drawRect(color = Color(231, 224, 235))
                                }
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                // 柱状图数据
                                val barHeights = when (selectedPeriod.value) {
                                    "week" -> listOf(0.6f, 0.8f, 0.4f, 0.9f, 0.5f, 0.7f, 0.65f)
                                    "month" -> listOf(0.7f, 0.65f, 0.8f, 0.75f, 0.9f, 0.85f, 0.75f)
                                    "year" -> listOf(0.6f, 0.7f, 0.8f, 0.75f, 0.9f, 0.85f, 0.8f)
                                    else -> listOf(0.6f, 0.8f, 0.4f, 0.9f, 0.5f, 0.7f, 0.65f)
                                }
                                
                                // 柱状图标签
                                val barLabels = when (selectedPeriod.value) {
                                    "week" -> listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
                                    "month" -> listOf("第1周", "第2周", "第3周", "第4周", "第5周", "", "")
                                    "year" -> listOf("1月", "2月", "3月", "4月", "5月", "6月", "7月")
                                    else -> listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
                                }
                                
                                // 绘制柱状图
                                barHeights.forEachIndexed { index, height ->
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .width(20.dp)
                                                .height(((height * 150).toInt()).dp)
                                                .drawBehind {
                                                    drawRect(color = Purple80)
                                                }
                                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        )
                                        Text(
                                            text = barLabels[index],
                                            style = TextStyle(
                                                fontSize = 10.sp,
                                                color = Color(73, 69, 78)
                                            ),
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                }
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
    hasPractice: Boolean = false
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
            .clickable { /* 日期点击事件 */ }
    ) {
        Text(
            text = day,
            style = TextStyle(
                fontSize = 14.sp,
                color = when {
                    isOtherMonth -> Color(203, 196, 207)
                    isToday || hasPractice -> Color.White
                    else -> OnBackground
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
