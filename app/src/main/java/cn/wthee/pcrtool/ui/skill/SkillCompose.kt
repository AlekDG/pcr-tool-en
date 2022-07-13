package cn.wthee.pcrtool.ui.skill

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.wthee.pcrtool.BuildConfig
import cn.wthee.pcrtool.R
import cn.wthee.pcrtool.data.db.view.SkillActionText
import cn.wthee.pcrtool.data.enums.MainIconType
import cn.wthee.pcrtool.data.enums.UnitType
import cn.wthee.pcrtool.data.model.SkillDetail
import cn.wthee.pcrtool.ui.common.*
import cn.wthee.pcrtool.ui.theme.*
import cn.wthee.pcrtool.utils.ImageResourceHelper
import cn.wthee.pcrtool.utils.ImageResourceHelper.Companion.ICON_SKILL
import cn.wthee.pcrtool.utils.ToastUtil
import cn.wthee.pcrtool.viewmodel.SkillViewModel
import com.google.accompanist.flowlayout.FlowRow

/**
 * 角色技能列表
 *
 * @param unitId 角色编号
 * @param cutinId 角色特殊编号
 * @param level 等级
 * @param atk 攻击力
 * @param unitType
 */
@Composable
fun SkillCompose(
    unitId: Int,
    cutinId: Int,
    level: Int,
    atk: Int,
    unitType: UnitType,
    toSummonDetail: ((Int, Int) -> Unit)? = null,
    skillViewModel: SkillViewModel = hiltViewModel()
) {
    skillViewModel.getCharacterSkills(level, atk, unitId, cutinId)
    val skillList = skillViewModel.skills.observeAsState().value ?: listOf()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimen.largePadding)
    ) {
        skillList.forEachIndexed { index, skillDetail ->
            SkillItem(
                skillIndex = index,
                skillDetail = skillDetail,
                unitType = unitType,
                toSummonDetail = toSummonDetail
            )
        }
    }
}

/**
 * 技能
 */
@Suppress("RegExpRedundantEscape")
@Composable
fun SkillItem(
    skillIndex: Int,
    skillDetail: SkillDetail,
    unitType: UnitType,
    toSummonDetail: ((Int, Int) -> Unit)? = null
) {
    //是否显示参数判断
    val actionData = skillDetail.getActionInfo()
    try {
        val showCoeIndex = skillDetail.getActionIndexWithCoe()
        actionData.mapIndexed { index, skillActionText ->
            val s = showCoeIndex.filter {
                it.actionIndex == index
            }
            val show = s.isNotEmpty()
            val str = skillActionText.action
            if (show) {
                //系数表达式开始位置
                val startIndex = str.indexOfFirst { ch -> ch == '<' }
                if (startIndex != -1) {
                    var coeExpr = str.substring(startIndex, str.length)
                    Regex("\\{.*?\\}").findAll(skillActionText.action).forEach {
                        if (s[0].type == 0) {
                            coeExpr = coeExpr.replace(it.value, "")
                        } else if (s[0].coe != it.value) {
                            coeExpr = coeExpr.replace(it.value, "")
                        }
                    }
                    skillActionText.action =
                        str.substring(0, startIndex) + coeExpr
                }
            } else {
                skillActionText.action =
                    str.replace(Regex("\\{.*?\\}"), "")
            }
        }
    } catch (_: Exception) {

    }

    //技能类型名
    val type = when (unitType) {
        UnitType.CHARACTER, UnitType.CHARACTER_SUMMON -> getSkillType(skillDetail.skillId)
        UnitType.ENEMY -> if (skillIndex == 0) "连结爆发" else "技能${skillIndex}"
        UnitType.ENEMY_SUMMON -> "技能${skillIndex + 1}"
    }
    val color = getSkillColor(type)
    val name =
        if (unitType == UnitType.ENEMY || unitType == UnitType.ENEMY_SUMMON) type else skillDetail.name
    val url = ImageResourceHelper.getInstance().getUrl(ICON_SKILL, skillDetail.iconType)


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimen.largePadding + Dimen.mediumPadding)
    ) {

        Row {
            //技能图标
            IconCompose(data = url)
            Column(
                modifier = Modifier
                    .padding(horizontal = Dimen.mediumPadding)
                    .heightIn(min = Dimen.iconSize),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                //技能名
                MainText(
                    text = name,
                    color = color,
                    selectable = true,
                    textAlign = TextAlign.Start
                )
                FlowRow {
                    //技能类型
                    CaptionText(
                        text = type + if (skillDetail.isCutin) "(六星)" else "",
                    )
                    //技能等级
                    if (unitType == UnitType.ENEMY || unitType == UnitType.ENEMY_SUMMON) {
                        CaptionText(
                            text = stringResource(id = R.string.skill_level, skillDetail.level),
                            modifier = Modifier.padding(start = Dimen.largePadding)
                        )
                    }
                    //冷却时间
                    if ((unitType == UnitType.ENEMY || unitType == UnitType.ENEMY_SUMMON) && skillDetail.bossUbCooltime > 0.0) {
                        CaptionText(
                            text = stringResource(
                                id = R.string.skill_cooltime,
                                skillDetail.bossUbCooltime
                            ),
                            modifier = Modifier
                                .padding(start = Dimen.largePadding)
                        )
                    }
                    //准备时间
                    if (skillDetail.castTime > 0) {
                        CaptionText(
                            text = stringResource(
                                id = R.string.skill_cast_time,
                                skillDetail.castTime
                            ),
                            modifier = Modifier
                                .padding(start = Dimen.largePadding)
                        )
                    }
                }
            }
        }

        //标签
        val tags = getTags(skillDetail.getActionInfo())
        FlowRow {
            tags.forEach {
                SkillActionTag(it)
            }
        }

        //描述
        if (skillDetail.desc.isNotBlank()) {
            Subtitle1(
                text = skillDetail.desc,
                selectable = true,
                modifier = Modifier.padding(top = Dimen.mediumPadding)
            )
        }

        //动作
        actionData.forEach {
            SkillActionItem(
                skillAction = it,
                unitType = unitType,
                toSummonDetail = toSummonDetail
            )
        }
    }
}

/**
 * 技能动作标签
 */
@Composable
fun SkillActionTag(skillTag: String) {
    MainTitleText(
        text = skillTag,
        textStyle = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(end = Dimen.smallPadding, top = Dimen.mediumPadding)
    )
}

/**
 * 技能动作
 */
@Composable
fun SkillActionItem(
    skillAction: SkillActionText,
    unitType: UnitType,
    toSummonDetail: ((Int, Int) -> Unit)? = null,
) {

    //详细描述
    val mark0 = arrayListOf<SkillIndex>()
    val mark1 = arrayListOf<SkillIndex>()
    val mark2 = arrayListOf<SkillIndex>()
    val mark3 = arrayListOf<SkillIndex>()
    val colors =
        arrayListOf(
            colorGreen,
            if (isSystemInDarkTheme()) colorWhite else Color.Black,
            colorPurple,
            MaterialTheme.colorScheme.primary
        )
    skillAction.action.forEachIndexed { index, c ->
        if (c == '[') {
            mark0.add(SkillIndex(start = index))
        }
        if (c == ']') {
            mark0[mark0.size - 1].end = index
        }
        if (c == '(') {
            mark1.add(SkillIndex(start = index))
        }
        if (c == ')') {
            mark1[mark1.size - 1].end = index
        }
        if (c == '{') {
            mark2.add(SkillIndex(start = index))
        }
        if (c == '}') {
            mark2[mark2.size - 1].end = index
        }
        if (c == '<') {
            mark3.add(SkillIndex(start = index))
        }
        if (c == '>') {
            mark3[mark3.size - 1].end = index
        }
    }
    val map = hashMapOf<Int, ArrayList<SkillIndex>>()
    map[0] = mark0
    map[1] = mark1
    map[2] = mark2
    map[3] = mark3

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(vertical = Dimen.smallPadding)
            .background(MaterialTheme.colorScheme.background)
            .clickable(enabled = BuildConfig.DEBUG) {
                ToastUtil.short(skillAction.actionId.toString())
            }
    ) {
        Box(
            modifier = Modifier
                .width(Dimen.vLineWidth)
                .fillMaxHeight()
                .background(if (isSystemInDarkTheme()) colorGrayWhite else colorGrayWhite1)
        )
        Column(
            modifier = Modifier
                .padding(Dimen.smallPadding)
                .fillMaxWidth()
                .heightIn(min = Dimen.skillActionMinHeight)
        ) {
            //设置字体
            Text(
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp
                ),
                modifier = Modifier.padding(
                    top = Dimen.smallPadding
                ),
                color = MaterialTheme.colorScheme.onSurface,
                text = buildAnnotatedString {
                    skillAction.action.forEachIndexed { index, c ->
                        var added = false
                        for (i in 0..3) {
                            map[i]?.forEach {
                                if (index >= it.start && index <= it.end) {
                                    added = true
                                    withStyle(style = SpanStyle(color = colors[i])) {
                                        append(c)
                                    }
                                    return@forEachIndexed
                                }
                            }
                        }
                        if (!added) {
                            append(c)
                        }
                    }

                }
            )

            if (skillAction.summonUnitId != 0 && toSummonDetail != null) {
                //查看召唤物
                IconTextButton(
                    icon = MainIconType.SUMMON,
                    text = stringResource(R.string.to_summon)
                ) {
                    toSummonDetail(skillAction.summonUnitId, unitType.type)
                }
            }
        }
    }
}


/**
 * 获取标签状态
 */
private fun getTags(data: ArrayList<SkillActionText>): ArrayList<String> {
    val list = arrayListOf<String>()
    data.forEach {
        if (it.tag.isNotEmpty() && !list.contains(it.tag)) {
            list.add(it.tag)
        }
    }
    return list
}

/**
 * 获取技能
 */
private fun getSkillType(skillId: Int): String {
    return when (skillId % 1000) {
        501 -> "EX技能"
        511 -> "EX技能+"
        100 -> "SP连结爆发"
        101 -> "SP技能 1"
        111 -> "SP技能 1+"
        102 -> "SP技能 2"
        112 -> "SP技能 2+"
        103 -> "SP技能 3"
        113 -> "SP技能 3+"
        1, 21 -> "连结爆发"
        11 -> "连结爆发+"
        else -> {
            val skillIndex = skillId % 10 - 1
            if (skillId % 1000 / 10 == 1) {
                "技能 ${skillIndex}+"
            } else {
                "技能 $skillIndex"
            }
        }
    }
}


/**
 * 获取技能名称颜色
 */
@Composable
fun getSkillColor(type: String): Color {
    return when {
        type.contains("连结") -> colorGold
        type.contains("EX") -> colorCopper
        type.contains("1") -> colorPurple
        type.contains("2") -> colorRed
        else -> MaterialTheme.colorScheme.primary
    }
}

private data class SkillIndex(
    var start: Int = 0,
    var end: Int = 0
)