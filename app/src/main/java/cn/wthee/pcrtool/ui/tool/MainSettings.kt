package cn.wthee.pcrtool.ui.tool

import android.content.SharedPreferences
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.core.content.edit
import cn.wthee.pcrtool.BuildConfig
import cn.wthee.pcrtool.R
import cn.wthee.pcrtool.data.enums.MainIconType
import cn.wthee.pcrtool.data.enums.SettingSwitchType
import cn.wthee.pcrtool.ui.MainActivity
import cn.wthee.pcrtool.ui.MainActivity.Companion.animOnFlag
import cn.wthee.pcrtool.ui.MainActivity.Companion.dynamicColorOnFlag
import cn.wthee.pcrtool.ui.MainActivity.Companion.navSheetState
import cn.wthee.pcrtool.ui.MainActivity.Companion.vibrateOnFlag
import cn.wthee.pcrtool.ui.common.*
import cn.wthee.pcrtool.ui.settingSP
import cn.wthee.pcrtool.ui.theme.CombinedPreviews
import cn.wthee.pcrtool.ui.theme.Dimen
import cn.wthee.pcrtool.ui.theme.PreviewLayout
import cn.wthee.pcrtool.utils.*

/**
 * 设置页面
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainSettings(
    sp: SharedPreferences = settingSP(LocalContext.current)
) {
    val context = LocalContext.current
    val region = MainActivity.regionType

    //调整主按钮图表
    LaunchedEffect(navSheetState.currentValue) {
        if (navSheetState.isVisible) {
            MainActivity.navViewModel.fabMainIcon.postValue(MainIconType.BACK)
        }
    }

    //数据库版本
    val typeName = getRegionName(region)
    val localVersion = sp.getString(
        when (region) {
            2 -> Constants.SP_DATABASE_VERSION_CN
            3 -> Constants.SP_DATABASE_VERSION_TW
            else -> Constants.SP_DATABASE_VERSION_JP
        },
        ""
    )
    val dbVersionGroup = if (localVersion != null) {
        localVersion.split("/")[0]
    } else {
        ""
    }

    //缓存删除确认弹窗
    val openDialog = remember {
        mutableStateOf(false)
    }
    //图片缓存大小
    val imageCacheSize = remember {
        mutableStateOf("")
    }
    LaunchedEffect(openDialog.value) {
        imageCacheSize.value = FileUtil.getCoilDirSize(context)
    }


    Column(
        modifier = Modifier
            .padding(horizontal = Dimen.mediumPadding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(bottom = Dimen.largePadding)
                .fillMaxWidth()
        ) {
            HeaderText(
                text = stringResource(id = R.string.app_name) + " v" + BuildConfig.VERSION_NAME,
                modifier = Modifier.padding(top = Dimen.mediumPadding)
            )
            IconCompose(
                data = R.drawable.ic_logo_large,
                size = Dimen.largeIconSize,
                modifier = Modifier.padding(Dimen.mediumPadding),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
            Subtitle1(
                text = "${typeName}：${dbVersionGroup}"
            )
        }


        //其它设置
        Spacer(modifier = Modifier.padding(vertical = Dimen.mediumPadding))
        MainText(
            text = stringResource(id = R.string.app_setting),
            modifier = Modifier.padding(Dimen.largePadding)
        )
        //- 振动开关
        SettingSwitchCompose(type = SettingSwitchType.VIBRATE, showSummary = true)
        //- 动画效果
        SettingSwitchCompose(type = SettingSwitchType.ANIMATION, showSummary = true)
        //- 动态色彩，仅 Android 12 及以上可用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S || BuildConfig.DEBUG) {
            SettingSwitchCompose(type = SettingSwitchType.DYNAMIC_COLOR, showSummary = true)
        }
        //- 清除图片缓存
        SettingCommonItem(
            iconType = MainIconType.DELETE,
            title = stringResource(id = R.string.clean_image_cache),
            summary = stringResource(id = R.string.tip_clean_image_cache),
            onClick = {
                //清除缓存弹窗
                openDialog.value = true
            }
        ) {
            Subtitle2(
                text = imageCacheSize.value,
                color = MaterialTheme.colorScheme.primary
            )
        }

        //其它相关
        Spacer(modifier = Modifier.padding(vertical = Dimen.mediumPadding))
        MainText(
            text = stringResource(id = R.string.other),
            modifier = Modifier.padding(Dimen.largePadding)
        )
        //- 加入反馈群
        SettingCommonItem(
            iconType = MainIconType.SUPPORT,
            title = stringResource(id = R.string.qq_group),
            summary = stringResource(id = R.string.qq_group_summary),
            onClick = {
                joinQQGroup(context)
            }
        ) {
            Subtitle2(
                text = stringResource(id = R.string.to_join_qq_group),
                color = MaterialTheme.colorScheme.primary
            )
            IconCompose(data = MainIconType.MORE, size = Dimen.fabIconSize)
        }
        //- 模型预览
        SettingCommonItem(
            iconType = MainIconType.PREVIEW_UNIT_SPINE,
            title = stringResource(id = R.string.title_spine),
            summary = stringResource(id = R.string.spine_tip),
            onClick = {
                BrowserUtil.open(Constants.PREVIEW_URL)
            }
        )

        //关于
        Spacer(modifier = Modifier.padding(vertical = Dimen.mediumPadding))
        MainText(
            text = stringResource(id = R.string.about),
            modifier = Modifier.padding(Dimen.largePadding)
        )
        //- 酷安
        val appUrl = stringResource(id = R.string.coolapk_url)
        SettingCommonItem(
            iconType = MainIconType.COOLAPK_APP_STORE,
            title = stringResource(id = R.string.coolapk),
            summary = stringResource(id = R.string.tip_coolapk),
            onClick = {
                BrowserUtil.open(appUrl)
            }
        )
        //- 项目代码
        val gitUrl = stringResource(id = R.string.github_project_url)
        SettingCommonItem(
            iconType = MainIconType.GITHUB_PROJECT,
            title = stringResource(id = R.string.github),
            summary = stringResource(id = R.string.tip_github),
            onClick = {
                BrowserUtil.open(gitUrl)
            }
        )

        //感谢友链
        Spacer(modifier = Modifier.padding(vertical = Dimen.mediumPadding))
        MainText(
            text = stringResource(id = R.string.thanks),
            modifier = Modifier.padding(Dimen.largePadding)
        )
        //- 干炸里脊资源
        val dataFromUrl = stringResource(id = R.string.data_from_url)
        SettingCommonItem(
            iconType = MainIconType.DATA_SOURCE,
            title = stringResource(id = R.string.data_from),
            summary = stringResource(id = R.string.data_from_hint),
            onClick = {
                BrowserUtil.open(dataFromUrl)
            }
        )
        //- 静流笔记
        val shizuruUrl = stringResource(id = R.string.shizuru_note_url)
        SettingCommonItem(
            iconType = MainIconType.NOTE,
            title = stringResource(id = R.string.shizuru_note),
            summary = stringResource(id = R.string.shizuru_note_tip),
            onClick = {
                BrowserUtil.open(shizuruUrl)
            }
        )
        //- 竞技场
        val pcrdfansUrl = stringResource(id = R.string.pcrdfans_url)
        SettingCommonItem(
            iconType = MainIconType.PVP_SEARCH,
            title = stringResource(id = R.string.pcrdfans),
            summary = stringResource(id = R.string.pcrdfans_tip),
            onClick = {
                BrowserUtil.open(pcrdfansUrl)
            }
        )
        //- 排行
        val leaderUrl = stringResource(id = R.string.leader_source_url)
        SettingCommonItem(
            iconType = MainIconType.LEADER,
            title = stringResource(id = R.string.leader_source),
            summary = stringResource(id = R.string.leader_tip),
            onClick = {
                BrowserUtil.open(leaderUrl)
            }
        )
        CommonSpacer()
    }

    if (openDialog.value) {
        AlertDialog(
            title = {
                MainContentText(
                    text = stringResource(id = R.string.confirm_clean_image_cache),
                    textAlign = TextAlign.Start,
                    selectable = true
                )
            },
            modifier = Modifier.padding(start = Dimen.mediumPadding, end = Dimen.mediumPadding),
            onDismissRequest = {
                openDialog.value = false
            },
            containerColor = MaterialTheme.colorScheme.background,
            shape = MaterialTheme.shapes.medium,
            confirmButton = {
                //删除缓存
                MainButton(text = stringResource(R.string.confirm)) {
                    FileUtil.delete(context.filesDir.resolve(Constants.COIL_DIR))
                    openDialog.value = false
                }
            },
            dismissButton = {
                //取消
                SubButton(
                    text = stringResource(id = R.string.cancel)
                ) {
                    openDialog.value = false
                }
            })
    }

}

/**
 * 设置项（带开关）
 *
 * @param type 设置类型 [cn.wthee.pcrtool.data.enums.SettingSwitchType]
 * @param showSummary 是否显示摘要，主页面设置为 false，设置页面设置为 true
 */
@Composable
fun SettingSwitchCompose(
    type: SettingSwitchType,
    showSummary: Boolean
) {
    val context = LocalContext.current
    val sp = settingSP(context)

    val title: String
    val iconType: MainIconType
    val spKey: String
    val summaryOn: String
    val summaryOff: String

    //设置
    when (type) {
        SettingSwitchType.VIBRATE -> {
            title = stringResource(id = R.string.vibrate)
            iconType = MainIconType.VIBRATE
            summaryOn = if (showSummary) stringResource(R.string.vibrate_on) else ""
            summaryOff = if (showSummary) stringResource(R.string.vibrate_off) else ""
            spKey = Constants.SP_VIBRATE_STATE
        }
        SettingSwitchType.ANIMATION -> {
            title = stringResource(id = R.string.animation)
            iconType = MainIconType.ANIMATION
            summaryOn = if (showSummary) stringResource(R.string.animation_on) else ""
            summaryOff = if (showSummary) stringResource(R.string.animation_off) else ""
            spKey = Constants.SP_ANIM_STATE
        }
        SettingSwitchType.DYNAMIC_COLOR -> {
            title = stringResource(id = R.string.dynamic_color)
            iconType = MainIconType.COLOR
            summaryOn = if (showSummary) stringResource(R.string.color_on) else ""
            summaryOff = if (showSummary) stringResource(R.string.color_off) else ""
            spKey = Constants.SP_COLOR_STATE
        }
    }
    val spValue = sp.getBoolean(spKey, true)
    val checkedState = remember {
        mutableStateOf(spValue)
    }

    //更新flag
    SideEffect {
        when (type) {
            SettingSwitchType.VIBRATE -> {
                vibrateOnFlag = checkedState.value
            }
            SettingSwitchType.ANIMATION -> {
                animOnFlag = checkedState.value

            }
            SettingSwitchType.DYNAMIC_COLOR -> {
                dynamicColorOnFlag = checkedState.value
            }
        }
    }

    //摘要
    val summary = if (checkedState.value) summaryOn else summaryOff


    SettingCommonItem(
        iconType = iconType,
        title = title,
        summary = summary,
        onClick = {
            VibrateUtil(context).single()
            checkedState.value = !checkedState.value
            sp.edit {
                putBoolean(spKey, checkedState.value)
            }
        }) {
        Switch(
            checked = checkedState.value,
            thumbContent = {
                SwitchThumbIcon(checkedState.value)
            },
            onCheckedChange = {
                checkedState.value = it
                sp.edit().putBoolean(spKey, it).apply()
                VibrateUtil(context).single()
                //动态色彩变更后，重启应用
                if (type == SettingSwitchType.DYNAMIC_COLOR) {
                    MainActivity.handler.sendEmptyMessage(1)
                }
            }
        )
    }
}

/**
 * 设置子项
 *
 * @param iconType      图标
 * @param iconSize      图标大小
 * @param title         标题
 * @param summary       摘要
 * @param titleColor    标题颜色
 * @param summaryColor  摘要颜色
 * @param onClick       点击事件
 * @param extraContent  右侧额外内容
 */
@Composable
fun SettingCommonItem(
    iconType: Any,
    iconSize: Dp = Dimen.settingIconSize,
    title: String,
    summary: String,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    summaryColor: Color = MaterialTheme.colorScheme.outline,
    padding: Dp = Dimen.largePadding,
    colorFilter: ColorFilter? = ColorFilter.tint(MaterialTheme.colorScheme.primary),
    onClick: () -> Unit,
    extraContent: (@Composable RowScope.() -> Unit)? = null
) {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = {
                VibrateUtil(context).single()
                onClick()
            })
    ) {
        Spacer(modifier = Modifier.width(padding))
        IconCompose(
            data = iconType,
            size = iconSize,
            colorFilter = colorFilter,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(padding),
            verticalArrangement = Arrangement.Center
        ) {
            Subtitle1(text = title, color = titleColor)
            if (summary != "") {
                Subtitle2(
                    text = summary,
                    modifier = Modifier.padding(top = Dimen.mediumPadding),
                    color = summaryColor
                )
            }
        }
        extraContent?.let {
            extraContent()
        }
        Spacer(modifier = Modifier.width(padding))
    }
}

/**
 * switch 选中图标
 */
@Composable
private fun SwitchThumbIcon(checked: Boolean) {
    Icon(
        imageVector = if (checked) MainIconType.OK.icon else MainIconType.CLOSE.icon,
        contentDescription = "",
        modifier = Modifier.size(SwitchDefaults.IconSize)
    )
}


@CombinedPreviews
@Composable
private fun SettingPreview() {
    PreviewLayout {
        SettingSwitchCompose(SettingSwitchType.VIBRATE, true)
        SettingCommonItem(
            iconType = MainIconType.SUPPORT,
            title = stringResource(id = R.string.qq_group),
            summary = stringResource(id = R.string.qq_group_summary),
            onClick = {}
        )
    }
}