package com.readout10min.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.readout10min.ui.theme.OnBackground
import com.readout10min.ui.theme.Purple80
import com.readout10min.ui.theme.SurfaceContainer
import com.readout10min.ui.theme.Typography

@Composable
fun ContentLibraryScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
    ) {
        // 屏幕标题
        Text(
            text = "内容库",
            style = Typography.headlineMedium,
            color = OnBackground
        )

        // 内容卡片1
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Sample Article Title",
                    style = Typography.titleMedium,
                    color = OnBackground
                )
                Text(
                    text = "作者：John Doe",
                    style = Typography.bodySmall,
                    color = OnBackground
                )
                Text(
                    text = "预估总时长：60分钟",
                    style = Typography.bodySmall,
                    color = OnBackground
                )
                Text(
                    text = "已读70%",
                    style = Typography.bodySmall,
                    color = OnBackground
                )
                // 简单的进度条占位
                Card(
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceContainer
                    )
                ) {
                    // 这里可以实现实际的进度条
                }
                Button(
                    onClick = {},
                    modifier = Modifier.align(androidx.compose.ui.Alignment.End),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Purple80
                    )
                ) {
                    Text(text = "开始练习")
                }
            }
        }

        // 内容卡片2
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Another Article Example",
                    style = Typography.titleMedium,
                    color = OnBackground
                )
                Text(
                    text = "作者：Jane Smith",
                    style = Typography.bodySmall,
                    color = OnBackground
                )
                Text(
                    text = "预估总时长：45分钟",
                    style = Typography.bodySmall,
                    color = OnBackground
                )
                Text(
                    text = "未读",
                    style = Typography.bodySmall,
                    color = OnBackground
                )
                // 简单的进度条占位
                Card(
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceContainer
                    )
                ) {
                    // 这里可以实现实际的进度条
                }
                Button(
                    onClick = {},
                    modifier = Modifier.align(androidx.compose.ui.Alignment.End),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Purple80
                    )
                ) {
                    Text(text = "开始练习")
                }
            }
        }
    }
}
