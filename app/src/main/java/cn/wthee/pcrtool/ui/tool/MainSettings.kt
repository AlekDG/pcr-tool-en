package cn.wthee.pcrtool.ui.tool

import android.content.Context
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import cn.wthee.pcrtool.BuildConfig
import cn.wthee.pcrtool.R
import cn.wthee.pcrtool.data.enums.MainIconType
import cn.wthee.pcrtool.ui.MainActivity
import cn.wthee.pcrtool.ui.MainActivity.Companion.animOnFlag
import cn.wthee.pcrtool.ui.MainActivity.Companion.dynamicColorOnFlag
import cn.wthee.pcrtool.ui.MainActivity.Companion.navSheetState
import cn.wthee.pcrtool.ui.MainActivity.Companion.vibrateOnFlag
import cn.wthee.pcrtool.ui.PreviewBox
import cn.wthee.pcrtool.ui.common.*
import cn.wthee.pcrtool.ui.settingSP
import cn.wthee.pcrtool.ui.theme.Dimen
import cn.wthee.pcrtool.utils.BrowserUtil
import cn.wthee.pcrtool.utils.Constants
import cn.wthee.pcrtool.utils.FileUtil
import cn.wthee.pcrtool.utils.VibrateUtil

/**
 * 设置页面
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainSettings() {
    val context = LocalContext.current
    val sp = settingSP(context)
    val region = MainActivity.regionType

    SideEffect {
        //自动删除历史数据
        FileUtil.deleteOldDatabase(context)
    }

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
    Column(
        modifier = Modifier
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
        VibrateSetting(sp, context)
        //- 动画效果
        AnimSetting(sp, context)
        //- 动态色彩，仅 Android 12 及以上可用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S || BuildConfig.DEBUG) {
            ColorSetting(sp, context)
        }
        Spacer(modifier = Modifier.padding(vertical = Dimen.mediumPadding))
        //感谢友链
        MainText(
            text = stringResource(id = R.string.thanks),
            modifier = Modifier.padding(Dimen.largePadding)
        )
        //- 干炸里脊资源
        val dataFromUrl = stringResource(id = R.string.data_from_url)
        SettingItem(
            MainIconType.DATA_SOURCE,
            stringResource(id = R.string.data_from),
            stringResource(id = R.string.data_from_hint),
        ) {
            BrowserUtil.open(context, dataFromUrl)
        }
        //- 静流笔记
        val shizuruUrl = stringResource(id = R.string.shizuru_note_url)
        SettingItem(
            MainIconType.NOTE,
            stringResource(id = R.string.shizuru_note),
            stringResource(id = R.string.shizuru_note_tip),
        ) {
            BrowserUtil.open(context, shizuruUrl)
        }
        //- 竞技场
        val pcrdfansUrl = stringResource(id = R.string.pcrdfans_url)
        SettingItem(
            MainIconType.PVP_SEARCH,
            stringResource(id = R.string.pcrdfans),
            stringResource(id = R.string.pcrdfans_tip),
        ) {
            BrowserUtil.open(context, pcrdfansUrl)
        }
        //- 排行
        val appMediaUrl = stringResource(id = R.string.leader_source_url)
        SettingItem(
            MainIconType.LEADER,
            stringResource(id = R.string.leader_source),
            stringResource(id = R.string.leader_tip),
        ) {
            BrowserUtil.open(context, appMediaUrl)
        }
        CommonSpacer()
    }

}

/**
 * 动态色彩
 */
@Composable
fun ColorSetting(
    sp: SharedPreferences,
    context: Context,
    showSummary: Boolean = true
) {
    val dynamicColorOn = sp.getBoolean(Constants.SP_COLOR_STATE, true)
    val dynamicColorState = remember {
        mutableStateOf(dynamicColorOn)
    }
    dynamicColorOnFlag = dynamicColorState.value
    val dynamicColorSummary =
        stringResource(id = if (dynamicColorState.value) R.string.color_on else R.string.color_off)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                dynamicColorState.value = !dynamicColorState.value
                sp.edit {
                    putBoolean(Constants.SP_COLOR_STATE, dynamicColorState.value)
                }
                VibrateUtil(context).single()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(Dimen.largePadding))
        IconCompose(
            data = MainIconType.COLOR,
            size = Dimen.settingIconSize
        )
        Column(
            modifier = Modifier
                .padding(Dimen.largePadding)
                .weight(1f)
        ) {
            TitleText(text = stringResource(id = R.string.dynamic_color))
            if (showSummary) {
                SummaryText(text = dynamicColorSummary)
            }
        }
        Switch(
            checked = dynamicColorState.value,
            thumbContent = {
                SwitchThumbIcon(dynamicColorState.value)
            },
            onCheckedChange = {
                dynamicColorState.value = it
                sp.edit().putBoolean(Constants.SP_COLOR_STATE, it).apply()
                VibrateUtil(context).single()
                MainActivity.handler.sendEmptyMessage(1)
            })
        Spacer(modifier = Modifier.width(Dimen.largePadding))
    }
}


/**
 * 动画效果
 */
@Composable
fun AnimSetting(
    sp: SharedPreferences,
    context: Context,
    showSummary: Boolean = true
) {
    val animOn = sp.getBoolean(Constants.SP_ANIM_STATE, true)
    val animState = remember {
        mutableStateOf(animOn)
    }
    animOnFlag = animState.value
    val animSummary =
        stringResource(id = if (animState.value) R.string.animation_on else R.string.animation_off)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                animState.value = !animState.value
                sp.edit {
                    putBoolean(Constants.SP_ANIM_STATE, animState.value)
                }
                VibrateUtil(context).single()
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(Dimen.largePadding))
        IconCompose(
            data = MainIconType.ANIMATION,
            size = Dimen.settingIconSize
        )
        Column(
            modifier = Modifier
                .padding(Dimen.largePadding)
                .weight(1f)
        ) {
            TitleText(text = stringResource(id = R.string.animation))
            if (showSummary) {
                SummaryText(text = animSummary)
            }
        }
        Switch(
            checked = animState.value,
            thumbContent = {
                SwitchThumbIcon(animState.value)
            }, onCheckedChange = {
                animState.value = it
                sp.edit().putBoolean(Constants.SP_ANIM_STATE, it).apply()
                VibrateUtil(context).single()
            }
        )
        Spacer(modifier = Modifier.width(Dimen.largePadding))
    }
}

/**
 * 振动反馈
 */
@Composable
fun VibrateSetting(
    sp: SharedPreferences,
    context: Context,
    showSummary: Boolean = true
) {
    val vibrateOn = sp.getBoolean(Constants.SP_VIBRATE_STATE, true)
    val vibrateState = remember {
        mutableStateOf(vibrateOn)
    }
    vibrateOnFlag = vibrateState.value
    val vibrateSummary =
        stringResource(id = if (vibrateState.value) R.string.vibrate_on else R.string.vibrate_off)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                VibrateUtil(context).single()
                vibrateState.value = !vibrateState.value
                sp.edit {
                    putBoolean(Constants.SP_VIBRATE_STATE, vibrateState.value)
                }
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(Dimen.largePadding))
        IconCompose(
            data = MainIconType.VIBRATE,
            size = Dimen.settingIconSize
        )
        Column(
            modifier = Modifier
                .padding(Dimen.largePadding)
                .weight(1f)
        ) {
            TitleText(text = stringResource(id = R.string.vibrate))
            if (showSummary) {
                SummaryText(text = vibrateSummary)
            }
        }
        Switch(
            checked = vibrateState.value,
            thumbContent = {
                SwitchThumbIcon(vibrateState.value)
            },
            onCheckedChange = {
                vibrateState.value = it
                sp.edit().putBoolean(Constants.SP_VIBRATE_STATE, it).apply()
                VibrateUtil(context).single()
            }
        )
        Spacer(modifier = Modifier.width(Dimen.largePadding))
    }
}

/**
 * 设置项
 */
@Composable
private fun SettingItem(
    iconType: MainIconType,
    title: String,
    summary: String,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = {
                VibrateUtil(context).single()
                onClick()
            })
    ) {
        Spacer(modifier = Modifier.width(Dimen.largePadding))
        IconCompose(
            data = iconType,
            size = Dimen.settingIconSize
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(Dimen.largePadding)
        ) {
            TitleText(text = title)
            SummaryText(text = summary)
        }
        Spacer(modifier = Modifier.width(Dimen.mediumPadding))
    }
}

/**
 *文本标题
 */
@Composable
private fun TitleText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        color = MaterialTheme.colorScheme.onSurface
    )
}

/**
 * 摘要
 */
@Composable
private fun SummaryText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.padding(top = Dimen.mediumPadding)
    )
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

@Preview
@Composable
private fun MainSettingsPreview() {
    PreviewBox(1) {
        MainSettings()
    }
}

@Preview
@Composable
private fun MainSettingsDarkPreview() {
    PreviewBox(2) {
        MainSettings()
    }
}