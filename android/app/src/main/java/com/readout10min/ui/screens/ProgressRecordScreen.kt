package com.readout10min.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.readout10min.ui.theme.OnBackground
import com.readout10min.ui.theme.SurfaceContainer
import com.readout10min.ui.theme.Typography

@Composable
fun ProgressRecordScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
    ) {
        // 屏幕标题
        Text(
            text = "练习记录",
            style = Typography.headlineMedium,
            color = OnBackground
        )

        // 练习日历卡片
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
                    text = "练习日历",
                    style = Typography.titleMedium,
                    color = OnBackground
                )
                Text(
                    text = "2026年1月",
                    style = Typography.bodySmall,
                    color = OnBackground
                )
                // 简单的日历占位
                Card(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceContainer
                    )
                ) {
                    // 这里可以实现实际的日历
                }
            }
        }

        // 统计图表卡片
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
                    text = "练习时长统计",
                    style = Typography.titleMedium,
                    color = OnBackground
                )
                // 简单的图表占位
                Card(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SurfaceContainer
                    )
                ) {
                    // 这里可以实现实际的统计图表
                }
                Text(
                    text = "周一 周二 周三 周四 周五 周六 周日",
                    style = Typography.bodySmall,
                    color = OnBackground
                )
            }
        }
    }
}
