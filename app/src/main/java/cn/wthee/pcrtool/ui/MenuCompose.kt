package cn.wthee.pcrtool.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import cn.wthee.pcrtool.R
import cn.wthee.pcrtool.data.enums.MainIconType
import cn.wthee.pcrtool.database.DatabaseUpdater
import cn.wthee.pcrtool.ui.compose.ExtendedFabCompose
import cn.wthee.pcrtool.ui.compose.FabCompose
import cn.wthee.pcrtool.ui.compose.MenuAnimation
import cn.wthee.pcrtool.ui.compose.defaultTween
import cn.wthee.pcrtool.ui.theme.Dimen
import cn.wthee.pcrtool.ui.theme.Shapes
import cn.wthee.pcrtool.utils.VibrateUtil
import cn.wthee.pcrtool.utils.addToClip
import cn.wthee.pcrtool.utils.vibrate
import cn.wthee.pcrtool.viewmodel.NoticeViewModel
import kotlinx.coroutines.launch

/**
 * 菜单
 */
@ExperimentalAnimationApi
@Composable
fun MenuContent(
    viewModel: NavViewModel,
    actions: NavActions,
    noticeViewModel: NoticeViewModel = hiltNavGraphViewModel()
) {
    val fabMainIcon = viewModel.fabMainIcon.observeAsState().value ?: MainIconType.OK
    val coroutineScope = rememberCoroutineScope()
    val updateApp = noticeViewModel.updateApp.observeAsState().value ?: false

    val backgroundAnim = animateFloatAsState(
        targetValue = if (fabMainIcon == MainIconType.DOWN) 1f else 0f,
        animationSpec = defaultTween()
    )

    Box(modifier = Modifier
        .fillMaxSize()
        .alpha(backgroundAnim.value)
        .background(colorResource(id = R.color.alpha_black))
        .clickable(enabled = backgroundAnim.value == 1f) {
            viewModel.fabMainIcon.postValue(MainIconType.MAIN)
        }
    ) {
        MenuAnimation(visible = fabMainIcon == MainIconType.DOWN) {
            Column(verticalArrangement = Arrangement.Bottom) {
                Row(modifier = Modifier.height(Dimen.largeMenuHeight)) {
                    //卡池
                    MenuItem(
                        text = stringResource(id = R.string.tool_gacha),
                        iconType = MainIconType.GACHA,
                        modifier = Modifier
                            .weight(0.382f)
                            .height(Dimen.largeMenuHeight)
                    ) {
                        actions.toGacha()
                    }
                    //团队战
                    MenuItem(
                        text = stringResource(id = R.string.tool_clan),
                        iconType = MainIconType.CLAN,
                        modifier = Modifier
                            .weight(0.382f)
                            .height(Dimen.largeMenuHeight)
                    ) {
                        actions.toClanBattleList()
                    }
                    //剧情活动
                    Column(modifier = Modifier.weight(0.618f)) {
                        MenuItem(
                            text = stringResource(id = R.string.tool_event),
                            iconType = MainIconType.EVENT,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.5f)
                        ) {
                            actions.toEventStory()
                        }
                        MenuItem(
                            text = stringResource(id = R.string.tool_guild),
                            iconType = MainIconType.GUILD,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.5f)
                        ) {
                            actions.toGuildList()
                        }
                    }
                }

                Row(modifier = Modifier.height(Dimen.largeMenuHeight)) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.5f)
                    ) {
                        //日历
                        MenuItem(
                            text = stringResource(id = R.string.tool_calendar),
                            iconType = MainIconType.CALENDAR,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.3f)
                        ) {
                            actions.toCalendar()
                        }
                        //官网公告
                        MenuItem(
                            text = stringResource(id = R.string.tool_news),
                            iconType = MainIconType.NEWS,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.3f)

                        ) {
                            actions.toNews()
                        }
                    }

                    //竞技场
                    MenuItem(
                        text = stringResource(id = R.string.tool_pvp),
                        iconType = MainIconType.PVP_SEARCH,
                        modifier = Modifier
                            .weight(0.5f)
                            .fillMaxHeight()
                    ) {
                        actions.toPvpSearch()
                    }
                }

                Row(modifier = Modifier.height(Dimen.largeMenuHeight)) {
                    //排行
                    MenuItem(
                        text = stringResource(id = R.string.tool_leader),
                        iconType = MainIconType.LEADER,
                        modifier = Modifier
                            .weight(0.382f)
                            .fillMaxHeight()
                    ) {
                        actions.toLeaderboard()
                    }
                    //装备
                    MenuItem(
                        text = stringResource(id = R.string.tool_equip),
                        iconType = MainIconType.EQUIP,
                        modifier = Modifier
                            .weight(0.618f)
                            .fillMaxHeight()
                    ) {
                        actions.toEquipList()
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.382f)
                    ) {
                        //通知
                        MenuItem(
                            backgroundColor = if (updateApp == 1) colorResource(id = R.color.cool_apk) else MaterialTheme.colors.primary,
                            text = stringResource(id = if (updateApp == 1) R.string.to_update else R.string.app_notice),
                            iconType = if (updateApp == 1) MainIconType.APP_UPDATE else MainIconType.NOTICE,
                            modifier = Modifier
                                .weight(0.618f)
                                .fillMaxWidth()
                        ) {
                            actions.toNotice()
                        }
                        //设置
                        MenuItem(
                            text = stringResource(id = R.string.setting),
                            iconType = MainIconType.SETTING,
                            modifier = Modifier
                                .weight(0.382f)
                                .fillMaxWidth()
                        ) {
                            actions.toSettings()
                        }
                    }

                }
                Row(
                    modifier = Modifier
                        .padding(
                            top = Dimen.mediuPadding,
                            bottom = Dimen.fabMargin,
                            end = Dimen.fabMarginEnd
                        )
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    val qqGroup = stringResource(R.string.qq_group)
                    val tip = stringResource(R.string.copy_qq_tip)
                    //群
                    FabCompose(
                        iconType = MainIconType.GROUP,
                        modifier = Modifier.padding(end = Dimen.fabSmallMarginEnd)
                    ) {
                        addToClip(qqGroup, tip)
                    }
                    //数据版本切换
                    ExtendedFabCompose(
                        iconType = MainIconType.CHANGE_DATA,
                        text = stringResource(id = R.string.change_db)
                    ) {
                        coroutineScope.launch {
                            viewModel.fabMainIcon.postValue(MainIconType.MAIN)
                            DatabaseUpdater.changeType()
                        }
                    }
                }
            }
        }
    }
}

/**
 * 菜单项
 */
@Composable
fun MenuItem(
    backgroundColor: Color = MaterialTheme.colors.primary,
    text: String,
    iconType: MainIconType,
    modifier: Modifier,
    action: () -> Unit
) {
    val context = LocalContext.current

    Card(
        backgroundColor = backgroundColor,
        contentColor = MaterialTheme.colors.onSurface,
        modifier = modifier
            .padding(Dimen.mediuPadding)
            .shadow(elevation = Dimen.cardElevation, shape = Shapes.large, clip = true)
            .clickable(onClick = action.vibrate {
                VibrateUtil(context).single()
            })
    ) {
        Box {
            Text(
                text = text,
                color = MaterialTheme.colors.onPrimary,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(Dimen.mediuPadding)
                    .align(Alignment.TopStart)
            )
            Icon(
                iconType.icon,
                contentDescription = null,
                tint = MaterialTheme.colors.onPrimary,
                modifier = Modifier
                    .size(Dimen.iconSize)
                    .padding(Dimen.mediuPadding)
                    .align(Alignment.BottomEnd)
            )
        }
    }
}
