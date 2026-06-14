package com.novelreader.data.model

// 翻页方式枚举
enum class PageMode(val displayName: String) {
    VERTICAL_SCROLL("上下滚屏"),    // 滚动模式
    HORIZONTAL_PAGE("左右翻页")     // 滑动或点击翻页，点击中间弹出菜单
}

data class ReadingSettings(
    val fontSize: Float = 18f,
    val lineHeight: Float = 1.8f,
    val letterSpacing: Float = 0f,
    val paragraphSpacing: Float = 12f,
    val backgroundColor: Long = 0xFFFFFFFF,
    val textColor: Long = 0xFF333333,
    val isNightMode: Boolean = false,
    val pageMode: PageMode = PageMode.VERTICAL_SCROLL
) {
    companion object {
        // 背景颜色选项
        val BG_WHITE = 0xFFFFFFFF
        val BG_CREAM = 0xFFF5F5DC
        val BG_GREEN = 0xFFC7EDCC
        val BG_DARK = 0xFF2D2D2D

        // 夜间模式颜色
        val NIGHT_BG = 0xFF1A1A1A
        val NIGHT_TEXT = 0xFFE0E0E0
        val NIGHT_SECONDARY = 0xFF999999

        // 主题色
        val PRIMARY = 0xFF1565C0
        val PRIMARY_LIGHT = 0xFF64B5F6
    }
}
