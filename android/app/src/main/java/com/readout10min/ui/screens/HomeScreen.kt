package com.readout10min.ui.screens

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.readout10min.ui.theme.BackgroundColor
import com.readout10min.ui.theme.OnBackground
import com.readout10min.ui.theme.Purple80
import com.readout10min.ui.theme.SurfaceColor
import com.readout10min.ui.theme.SurfaceContainer
import com.readout10min.ui.theme.SurfaceVariant
import com.readout10min.ui.theme.Typography
import com.readout10min.ui.theme.White

@Composable
fun HomeScreen() {
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
                        color = Purple80
                    )
                }

                //  stats card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
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
                                    text = "15",
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
                                    text = "0/1",
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
                        color = Purple80
                    )
                }

                //  recommendation card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Sample Article Title",
                            style = Typography.titleMedium,
                            color = OnBackground
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "⏱️ 12 分钟",
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
                            onClick = {},
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Purple80
                            )
                        ) {
                            Text(text = "开始练习")
                        }
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
                        color = Purple80
                    )
                }

                //  recent reading items
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    //  recent item 1
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = SurfaceContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Article 1",
                                    style = Typography.bodyMedium,
                                    color = OnBackground
                                )
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "8 分钟",
                                        style = Typography.bodySmall,
                                        color = OnBackground
                                    )
                                    Text(
                                        text = "70%",
                                        style = Typography.bodySmall,
                                        color = Color(76, 175, 80) // success color
                                    )
                                }
                            }
                        }
                    }

                    //  recent item 2
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = SurfaceContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Article 2",
                                    style = Typography.bodyMedium,
                                    color = OnBackground
                                )
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "10 分钟",
                                        style = Typography.bodySmall,
                                        color = OnBackground
                                    )
                                    Text(
                                        text = "30%",
                                        style = Typography.bodySmall,
                                        color = Color(76, 175, 80) // success color
                                    )
                                }
                            }
                        }
                    }

                    //  recent item 3
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = SurfaceContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Article 3",
                                    style = Typography.bodyMedium,
                                    color = OnBackground
                                )
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "15 分钟",
                                        style = Typography.bodySmall,
                                        color = OnBackground
                                    )
                                    Text(
                                        text = "100%",
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
    }
}

