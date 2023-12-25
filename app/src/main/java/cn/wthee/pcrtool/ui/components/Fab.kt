package cn.wthee.pcrtool.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerDefaults
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cn.wthee.pcrtool.R
import cn.wthee.pcrtool.data.enums.MainIconType
import cn.wthee.pcrtool.data.enums.RankSelectType
import cn.wthee.pcrtool.ui.theme.CombinedPreviews
import cn.wthee.pcrtool.ui.theme.Dimen
import cn.wthee.pcrtool.ui.theme.PreviewLayout
import cn.wthee.pcrtool.ui.theme.colorWhite
import cn.wthee.pcrtool.ui.theme.defaultSpring
import cn.wthee.pcrtool.ui.theme.defaultTween
import cn.wthee.pcrtool.utils.VibrateUtil
import cn.wthee.pcrtool.utils.simpleDateFormatUTC
import kotlinx.coroutines.launch

/**
 * 通用悬浮按钮
 *
 * @param hasNavBarPadding 适配导航栏
 * @param extraContent 不为空时，将替换text内容
 * @param iconScale 图标缩放，非ImageVector才生效
 */
@Composable
fun MainSmallFab(
    iconType: Any,
    modifier: Modifier = Modifier,
    text: String = "",
    hasNavBarPadding: Boolean = true,
    extraContent: (@Composable () -> Unit)? = null,
    iconScale: ContentScale = ContentScale.FillWidth,
    vibrate: Boolean = true,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var mModifier = if (hasNavBarPadding) {
        modifier.navigationBarsPadding()
    } else {
        modifier
    }
    val isTextFab = text != "" && extraContent == null

    if (isTextFab) {
        mModifier = mModifier.padding(horizontal = Dimen.textFabMargin)
    }

    SmallFloatingActionButton(
        onClick = {
            if (vibrate) {
                VibrateUtil(context).single()
            }
            onClick()
        },
        shape = CircleShape,
        modifier = mModifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = (if (isTextFab) {
                Modifier.padding(start = Dimen.largePadding)
            } else {
                Modifier.padding(start = 0.dp)
            }).animateContentSize(defaultTween())
        ) {

            if (extraContent == null) {
                MainIcon(
                    data = iconType,
                    size = Dimen.fabIconSize,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    contentScale = iconScale
                )

                Text(
                    text = text,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                    modifier = if (isTextFab) {
                        Modifier.padding(start = Dimen.mediumPadding, end = Dimen.largePadding)
                    } else {
                        Modifier
                    }
                        .widthIn(max = Dimen.fabTextMaxWidth),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                extraContent()
            }
        }
    }

}

/**
 * 切换
 * @param width 宽度
 */
@Composable
fun SelectTypeFab(
    modifier: Modifier = Modifier,
    openDialog: Boolean,
    changeDialog: (Boolean) -> Unit,
    icon: MainIconType,
    tabs: List<String>,
    type: Int,
    width: Dp = Dimen.dataChangeWidth,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    paddingValues: PaddingValues = PaddingValues(
        start = Dimen.fabMargin,
        end = Dimen.fabMarginEnd,
        top = Dimen.fabMargin,
        bottom = Dimen.fabMargin,
    ),
    changeSelect: (Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()


    //切换
    SmallFloatingActionButton(
        modifier = modifier
            .animateContentSize(defaultSpring())
            .padding(paddingValues)
            .padding(
                start = Dimen.mediumPadding,
                end = Dimen.textFabMargin,
                top = Dimen.mediumPadding,
            )
            .navigationBarsPadding(),
        shape = if (openDialog) MaterialTheme.shapes.medium else CircleShape,
        onClick = {
            VibrateUtil(context).single()
            if (!openDialog) {
                changeDialog(true)
            }
        },
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = if (openDialog) {
                Dimen.popupMenuElevation
            } else {
                Dimen.fabElevation
            }
        ),
    ) {
        if (openDialog) {
            Column(
                modifier = Modifier.widthIn(max = width),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //选择
                tabs.forEachIndexed { index, tab ->
                    SelectText(
                        selected = type == index,
                        text = tab,
                        textStyle = MaterialTheme.typography.titleLarge,
                        selectedColor = selectedColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimen.mediumPadding)
                    ) {
                        coroutineScope.launch {
                            changeSelect(index)
                        }
                        changeDialog(false)
                    }
                }
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = Dimen.largePadding)
            ) {
                MainIcon(
                    data = icon, tint = selectedColor,
                    size = Dimen.fabIconSize
                )
                Text(
                    text = tabs[type],
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                    color = selectedColor,
                    modifier = Modifier.padding(
                        start = Dimen.mediumPadding, end = Dimen.largePadding
                    )
                )
            }

        }
    }
}

/**
 * 选择日期范围组件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerCompose(
    dateRangePickerState: DateRangePickerState,
    dateRange: DateRange,
    openDialog: Boolean,
    changeDialog: (Boolean) -> Unit,
    changeRange: (DateRange) -> Unit,
) {
    val context = LocalContext.current

    //更新日期
    LaunchedEffect(
        dateRangePickerState.selectedStartDateMillis,
        dateRangePickerState.selectedEndDateMillis
    ) {
        //日期字符串处理
        val startDate = dateRangePickerState.selectedStartDateMillis?.simpleDateFormatUTC
        var endDate = dateRangePickerState.selectedEndDateMillis?.simpleDateFormatUTC
        if (endDate != null) {
            endDate = endDate.replace("00:00:00", "23:59:59")
        }


        changeRange(
            DateRange(
                startDate = startDate ?: "",
                endDate = endDate ?: ""
            )
        )
    }

    //日期选择布局
    SmallFloatingActionButton(
        modifier = Modifier
            .widthIn(max = Dimen.itemMaxWidth)
            .animateContentSize(defaultSpring())
            .padding(
                start = Dimen.fabMargin,
                end = Dimen.fabMargin,
                bottom = Dimen.fabMargin * 2 + Dimen.fabSize,
                top = Dimen.largePadding
            )
            .padding(start = Dimen.textFabMargin, end = Dimen.textFabMargin)
            .imePadding(),
        shape = if (openDialog) MaterialTheme.shapes.medium else CircleShape,
        onClick = {
            //点击展开布局
            if (!openDialog) {
                VibrateUtil(context).single()
                changeDialog(true)
            }
        },
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = if (openDialog) {
                Dimen.popupMenuElevation
            } else {
                Dimen.fabElevation
            }
        ),
    ) {
        if (openDialog) {
            //日期选择
            DateRangePicker(
                modifier = Modifier.padding(Dimen.smallPadding),
                state = dateRangePickerState,
                showModeToggle = true,
                title = {},
                headline = {
                    DateRangePickerDefaults.DateRangePickerHeadline(
                        dateRangePickerState,
                        remember { DatePickerFormatter() },
                        modifier = Modifier.padding(Dimen.smallPadding)
                    )
                }
            )
        } else {
            //fab
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = Dimen.largePadding)
            ) {
                MainIcon(
                    data = if (dateRange.hasFilter()) {
                        MainIconType.DATE_RANGE_PICKED
                    } else {
                        MainIconType.DATE_RANGE_NONE
                    },
                    size = Dimen.fabIconSize
                )
                Text(
                    text = if (dateRange.hasFilter()) {
                        stringResource(id = R.string.picked_date)
                    } else {
                        stringResource(id = R.string.pick_date)
                    },
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(
                        start = Dimen.mediumPadding, end = Dimen.largePadding
                    )
                )
            }
        }
    }
}

/**
 * 选择RANK范围
 */
@Composable
fun RankRangePickerCompose(
    rank0: Int,
    rank1: Int,
    maxRank: Int,
    openDialog: MutableState<Boolean>,
    type: RankSelectType = RankSelectType.DEFAULT,
    updateRank: (Int, Int) -> Unit
) {
    val context = LocalContext.current

    val rankList = arrayListOf<Int>()
    for (i in maxRank downTo 1) {
        rankList.add(i)
    }

    //选择
    val selectIndex0 = remember {
        mutableIntStateOf(maxRank - rank0)
    }
    val selectIndex1 = remember {
        mutableIntStateOf(maxRank - rank1)
    }
    LaunchedEffect(selectIndex0.intValue, selectIndex1.intValue) {
        updateRank(maxRank - selectIndex0.intValue, maxRank - selectIndex1.intValue)
    }

    //关闭监听
    BackHandler(openDialog.value) {
        openDialog.value = false
    }


    //选择布局
    SmallFloatingActionButton(
        modifier = Modifier
            .widthIn(max = Dimen.itemMaxWidth)
            .animateContentSize(defaultSpring())
            .padding(
                start = Dimen.fabMarginSecondLineEnd,
                end = Dimen.fabMarginSecondLineEnd,
                bottom = Dimen.fabMarginLargeBottom
            ),
        shape = if (openDialog.value) MaterialTheme.shapes.medium else CircleShape,
        onClick = {
            //点击展开布局
            if (!openDialog.value) {
                VibrateUtil(context).single()
                openDialog.value = true
            }
        },
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = if (openDialog.value) {
                Dimen.popupMenuElevation
            } else {
                Dimen.fabElevation
            }
        ),
    ) {
        if (openDialog.value) {
            Column(
                modifier = Modifier
                    .padding(
                        horizontal = Dimen.mediumPadding,
                        vertical = Dimen.largePadding
                    )
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Bottom
            ) {
                //当前
                MainText(text = stringResource(id = R.string.cur_rank))
                RankSelectItem(
                    selectIndex = selectIndex0,
                    rankList = rankList,
                    targetType = RankSelectType.DEFAULT,
                    currentRank = maxRank - selectIndex0.intValue
                )
                //目标
                MainText(
                    text = stringResource(id = R.string.target_rank),
                    modifier = Modifier.padding(top = Dimen.largePadding)
                )
                RankSelectItem(
                    selectIndex = selectIndex1,
                    rankList = rankList,
                    targetType = type,
                    currentRank = maxRank - selectIndex0.intValue
                )
            }
        } else {
            //fab
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = Dimen.largePadding)
            ) {
                MainIcon(
                    data = MainIconType.RANK_SELECT,
                    size = Dimen.fabIconSize
                )
                Text(
                    text = stringResource(id = R.string.rank_select),
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(
                        start = Dimen.mediumPadding, end = Dimen.largePadding
                    )
                )
            }
        }
    }

}

/**
 * RANK 选择器
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RankSelectItem(
    selectIndex: MutableState<Int>,
    rankList: List<Int>,
    targetType: RankSelectType,
    currentRank: Int
) {
    val context = LocalContext.current

    VerticalGrid(
        itemWidth = Dimen.rankTextWidth,
        contentPadding = Dimen.mediumPadding
    ) {

        rankList.filter {
            targetType == RankSelectType.DEFAULT ||
                    (targetType == RankSelectType.LIMIT && it >= currentRank)
        }.forEachIndexed { index, rank ->
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {

                val rankColor = getRankColor(rank = rank)
                val selected = selectIndex.value == index


                ElevatedFilterChip(
                    selected = selected,
                    onClick = {
                        VibrateUtil(context).single()
                        selectIndex.value = index
                    },
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        selectedContainerColor = rankColor
                    ),
                    label = {
                        CaptionText(
                            text = rankFillBlank(rank),
                            color = if (selected) colorWhite else rankColor
                        )
                    }
                )
            }
        }

    }
}

/**
 * 填充空格
 */
private fun rankFillBlank(rank: Int): String {
    return when (rank) {
        in 0..9 -> "0$rank"
        else -> "$rank"
    }
}


@CombinedPreviews
@Composable
private fun FabComposePreview() {
    PreviewLayout {
        Row {
            MainSmallFab(iconType = MainIconType.ANIMATION) {

            }
            MainSmallFab(iconType = MainIconType.ANIMATION, text = "fab") {

            }
        }
    }
}
