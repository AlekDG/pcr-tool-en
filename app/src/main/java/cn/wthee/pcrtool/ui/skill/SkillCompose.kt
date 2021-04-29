package cn.wthee.pcrtool.ui.skill

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import cn.wthee.pcrtool.R
import cn.wthee.pcrtool.data.model.SkillDetail
import cn.wthee.pcrtool.data.model.SkillLoop
import cn.wthee.pcrtool.data.view.SkillActionText
import cn.wthee.pcrtool.ui.compose.IconCompose
import cn.wthee.pcrtool.ui.compose.MainTitleText
import cn.wthee.pcrtool.ui.compose.getGridData
import cn.wthee.pcrtool.ui.theme.Dimen
import cn.wthee.pcrtool.utils.Constants
import cn.wthee.pcrtool.viewmodel.SkillViewModel

/**
 * 角色技能列表
 */
@Composable
fun SkillCompose(
    level: Int,
    atk: Int,
    id: Int,
    skillViewModel: SkillViewModel = hiltNavGraphViewModel()
) {
    skillViewModel.getCharacterSkills(level, atk, id)
    val skillList = skillViewModel.skills.observeAsState().value ?: listOf()

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        skillList.forEach {
            SkillItem(level = level, skillDetail = it)
        }
    }
}

/**
 * 技能
 */
@Composable
fun SkillItem(level: Int, skillDetail: SkillDetail) {

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
    } catch (e: Exception) {

    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Dimen.largePadding, bottom = Dimen.largePadding)
    ) {
        //技能名
        val type = getSkillType(skillDetail.skillId)
        val color = getSkillColor(type)
        Text(
            text = skillDetail.name,
            color = colorResource(color),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = Dimen.largePadding)
        )
        //技能类型
        Text(
            text = type,
            color = colorResource(color),
            style = MaterialTheme.typography.caption,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = Dimen.smallPadding)
        )
        //等级
//        Text(
//            text = "$level",
//            color = MaterialTheme.colors.primary,
//            fontWeight = FontWeight.Bold,
//            style = MaterialTheme.typography.caption
//        )
        //图标、等级、描述
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Dimen.smallPadding)
        ) {
            val url = Constants.SKILL_ICON_URL + skillDetail.iconType + Constants.WEBP
            //技能图标
            IconCompose(data = url, modifier = Modifier.size(Dimen.iconSize))
            //描述
            Text(
                text = skillDetail.desc,
                style = MaterialTheme.typography.subtitle2,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Dimen.mediuPadding)
            )
        }
        val tags = getTags(skillDetail.getActionInfo())
        //标签
        Row {
            tags.forEach {
                SkillActionTag(it)
            }
        }
        //动作
        actionData.forEach {
            SkillActionItem(skillAction = it)
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
        small = true,
        modifier = Modifier.padding(end = Dimen.smallPadding, top = Dimen.mediuPadding)
    )
}

/**
 * 技能动作
 */
@Composable
fun SkillActionItem(skillAction: SkillActionText) {
    //详细描述
    val mark0 = arrayListOf<SkillIndex>()
    val mark1 = arrayListOf<SkillIndex>()
    val mark2 = arrayListOf<SkillIndex>()
    val mark3 = arrayListOf<SkillIndex>()
    val colors =
        arrayListOf(R.color.cool_apk, R.color.black, R.color.color_rank_11_17, R.color.colorPrimary)
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
    //设置字体
    Text(
        style = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 0.5.sp
        ),
        color = colorResource(id = R.color.gray),
        modifier = Modifier.padding(
            top = Dimen.mediuPadding,
            start = Dimen.mediuPadding,
            end = Dimen.mediuPadding
        ),
        text = buildAnnotatedString {
            skillAction.action.forEachIndexed { index, c ->
                var added = false
                for (i in 0..3) {
                    map[i]?.forEach {
                        if (index >= it.start && index <= it.end) {
                            added = true
                            withStyle(style = SpanStyle(color = colorResource(id = colors[i]))) {
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

        })

}

/**
 * 技能循环
 */
@Composable
fun SkillLoopList(unitId: Int) {
    val skillViewModel: SkillViewModel = hiltNavGraphViewModel()
    skillViewModel.getCharacterSkillLoops(unitId)
    val loopData = skillViewModel.atkPattern.observeAsState().value ?: arrayListOf()

    val loops = arrayListOf<SkillLoop>()
    loopData.forEach { ap ->
        if (ap.getBefore().size > 0) {
            loops.add(SkillLoop(stringResource(R.string.before_loop), ap.getBefore()))
        }
        if (ap.getLoop().size > 0) {
            loops.add(SkillLoop(stringResource(R.string.looping), ap.getLoop()))
        }
    }
    Column(modifier = Modifier.padding(Dimen.mediuPadding)) {
        if (loops.isNotEmpty()) {
            loops.forEach {
                SkillLoopItem(loop = it)
            }
        } else {
            Text(text = "")
        }
    }
}

/**
 * 技能循环 item
 */
@Composable
private fun SkillLoopItem(loop: SkillLoop) {
    Column {
        MainTitleText(text = loop.loopTitle)
        SkillLoopIconList(loop.loopList)
    }
}

/**
 * 技能循环图标列表
 */
@Composable
private fun SkillLoopIconList(iconList: List<Int>) {
    val spanCount = 5
    val newList = getGridData(spanCount = spanCount, list = iconList, placeholder = 0)
    Column(Modifier.padding(top = Dimen.mediuPadding)) {
        newList.forEachIndexed { index, i ->
            if (index % spanCount == 0) {
                SkillLoopIconListRow(newList.subList(index, index + spanCount))
            }
        }
    }
}

/**
 * 技能循环图标列表
 */
@Composable
private fun SkillLoopIconListRow(iconList: List<Int>) {
    Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
        iconList.forEach {
            val alpha = if (it == 0) 0f else 1f
            Column(
                modifier = Modifier
                    .padding(Dimen.mediuPadding)
                    .alpha(alpha)
            ) {
                IconCompose(data = Constants.SKILL_ICON_URL + it + Constants.WEBP)
                val type = getSkillType(it)
                Text(
                    text = type,
                    color = colorResource(getSkillColor(type = type)),
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = Dimen.smallPadding)
                )
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
private fun getSkillType(skillId: Int) = when (skillId % 1000) {
    501 -> "EX技能"
    511 -> "EX技能+"
    100 -> "SP连结爆发"
    101 -> "SP技能 1"
    102 -> "SP技能 2"
    103 -> "SP技能 3"
    1, 21 -> "连结爆发"
    11 -> "连结爆发+"
    else -> {
        val skillIndex = skillId % 1000 % 10 - 1
        if (skillId % 1000 / 10 == 1) {
            "技能 ${skillIndex}+"
        } else {
            "技能 $skillIndex"
        }
    }
}


/**
 * 获取技能名称颜色
 */
@Composable
private fun getSkillColor(type: String): Int {
    val color = when {
        type.contains("连结") -> R.color.color_rank_7_10
        type.contains("EX") -> R.color.color_rank_2_3
        else -> R.color.color_rank_4_6
    }
    return color
}

private data class SkillIndex(
    var start: Int = 0,
    var end: Int = 0
)