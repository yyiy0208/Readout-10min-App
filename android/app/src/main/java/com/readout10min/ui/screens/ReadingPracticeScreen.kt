package com.readout10min.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
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
fun ReadingPracticeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
    ) {
        // 屏幕标题
        Text(
            text = "朗读练习",
            style = Typography.headlineMedium,
            color = OnBackground
        )

        // 文章内容卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = SurfaceContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Sample Article Title",
                    style = Typography.titleLarge,
                    color = OnBackground
                )
                Text(
                    text = "This is the first paragraph of the sample article. It contains some text that will be split into readable paragraphs for the user to practice reading.\n\nThis is the second paragraph. It continues with more sample text to provide sufficient content for reading practice.\n\nThis is the third paragraph. It adds more context and information to the sample article.\n\nThis is the fourth and final paragraph. It concludes the sample article with some final thoughts.",
                    style = Typography.bodyLarge,
                    color = OnBackground,
                    lineHeight = Typography.bodyLarge.lineHeight
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
            }
        }

        // 控制按钮区域
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
                    text = "控制选项",
                    style = Typography.titleMedium,
                    color = OnBackground
                )
                // 这里可以添加实际的控制按钮
                Text(
                    text = "字体 行间距 背景色",
                    style = Typography.bodySmall,
                    color = OnBackground
                )
            }
        }
    }
}
