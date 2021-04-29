package cn.wthee.pcrtool.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.wthee.pcrtool.MyApplication

object Dimen {
    /**
     * 10dp
     */
    val cardRadius = 10.dp

    /**
     * 4dp
     */
    val cardElevation = 4.dp

    /**
     * 8dp
     */
    val fabElevation = 8.dp

    /**
     * 40dp
     */
    val fabSize = 40.dp

    /**
     * 24dp
     */
    val fabIconSize = 24.dp

    /**
     * 16dp
     */
    val fabMargin = 16.dp

    /**
     * 70dp
     */
    val fabMarginEnd = 70.dp

    /**
     * 14dp
     */
    val fabSmallMarginEnd = 14.dp

    /**
     * 6dp
     */
    val fabPadding = 6.dp

    /**
     * 3dp
     */
    val smallPadding = 3.dp

    /**
     * 6dp
     */
    val mediuPadding = 6.dp

    /**
     * 12dp
     */
    val largePadding = 12.dp

    /**
     * 48dp
     */
    val menuIconSize = 48.dp

    /**
     * 60dp
     */
    val smallMenuHeight = 70.dp

    /**
     * 130dp
     */
    val largeMenuHeight = 130.dp

    /**
     * 48dp
     */
    val iconSize = 48.dp


    /**
     * 46dp
     */
    val sheetMarginBottom = 46.dp


    /**
     * 42dp
     */
    val smallIconSize = 42.dp

    /**
     * 45dp
     */
    val lineWidth = 45.dp

    /**
     * 3dp
     */
    val lineHeight = 3.dp

    /**
     * 2dp
     */
    val border = 2.dp

    /**
     * 40dp
     */
    val topBarHeight = 48.dp

    /**
     * 根据文字大小显示
     */
    fun getWordWidth(length: Int): Dp {
        return (15 * length).sp2dp
    }
}


/**
 * sp to dp
 */
val Int.sp2dp: Dp
    get() {
        val scale: Float = MyApplication.context.resources.displayMetrics.density
        val px = this.sp.value * scale
        val dpInt = (px / scale + 0.5f).toInt()
        return dpInt.dp
    }
