package cn.wthee.pcrtool.ui.tool

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import cn.wthee.pcrtool.R
import cn.wthee.pcrtool.data.db.entity.EnemyParameter
import cn.wthee.pcrtool.data.db.view.ClanBattleInfo
import cn.wthee.pcrtool.data.db.view.Enemy
import cn.wthee.pcrtool.data.enums.MainIconType
import cn.wthee.pcrtool.data.enums.getMultipleIcon
import cn.wthee.pcrtool.data.model.ChipData
import cn.wthee.pcrtool.ui.MainActivity.Companion.navViewModel
import cn.wthee.pcrtool.ui.compose.*
import cn.wthee.pcrtool.ui.skill.SkillItem
import cn.wthee.pcrtool.ui.skill.SkillLoopList
import cn.wthee.pcrtool.ui.theme.CardTopShape
import cn.wthee.pcrtool.ui.theme.Dimen
import cn.wthee.pcrtool.ui.theme.Shapes
import cn.wthee.pcrtool.utils.Constants
import cn.wthee.pcrtool.utils.getZhNumberText
import cn.wthee.pcrtool.viewmodel.ClanViewModel
import cn.wthee.pcrtool.viewmodel.SkillViewModel
import com.google.accompanist.pager.*
import kotlinx.coroutines.launch

/**
 * 每月 BOSS 信息列表
 */
@ExperimentalPagerApi
@Composable
fun ClanBattleList(
    toClanBossInfo: (Int, Int) -> Unit,
    clanViewModel: ClanViewModel = hiltNavGraphViewModel()
) {
    clanViewModel.getAllClanBattleData()
    val clanList = clanViewModel.clanInfoList.observeAsState()
    val state = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    navViewModel.loading.postValue(true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.bg_gray))
    ) {
        clanList.value?.let { data ->
            navViewModel.loading.postValue(false)
            LazyColumn(state = state) {
                items(data) {
                    ClanBattleItem(it, toClanBossInfo, type = 0)
                }
                item {
                    Spacer(modifier = Modifier.height(Dimen.sheetMarginBottom))
                }
            }
        }
        //回到顶部
        ExtendedFabCompose(
            iconType = MainIconType.CLAN,
            text = stringResource(id = R.string.tool_clan),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Dimen.fabMarginEnd, bottom = Dimen.fabMargin)
        ) {
            coroutineScope.launch {
                state.scrollToItem(0)
            }
        }
    }

}

/**
 * 图标列表
 * type 0：点击查看详情， 1：点击切换 BOSS
 */
@ExperimentalPagerApi
@Composable
private fun ClanBattleItem(
    clanInfo: ClanBattleInfo,
    toClanBossInfo: ((Int, Int) -> Unit)? = null,
    pagerState: PagerState? = null,
    type: Int
) {
    val section = clanInfo.getAllBossInfo().size
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .padding(Dimen.mediuPadding)
            .fillMaxWidth()
    ) {
        //标题
        Row(modifier = Modifier.padding(bottom = Dimen.mediuPadding)) {
            MainTitleText(text = clanInfo.getDate())
            if (type == 0) {
                MainTitleText(
                    text = stringResource(
                        id = R.string.section,
                        getZhNumberText(section)
                    ),
                    backgroundColor = getSectionTextColor(section),
                    modifier = Modifier.padding(start = Dimen.smallPadding),
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = Dimen.cardElevation, shape = Shapes.large, clip = true)
        ) {
            //图标
            Row(
                modifier = Modifier.padding(Dimen.mediuPadding),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                clanInfo.getUnitIdList(0).forEachIndexed { index, it ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconCompose(data = Constants.UNIT_ICON_URL + it.unitId + Constants.WEBP) {
                            if (type == 0 && toClanBossInfo != null) {
                                toClanBossInfo(clanInfo.clan_battle_id, index)
                            } else {
                                scope.launch {
                                    pagerState?.scrollToPage(index)
                                }
                            }
                        }
                        //多目标提示
                        if (it.targetCount > 1) {
                            Icon(
                                getMultipleIcon(it.targetCount - 1),
                                contentDescription = null,
                                tint = MaterialTheme.colors.primary,
                            )
                        }
                    }

                }
            }
        }
    }
}


/**
 * 团队战 BOSS 详情
 */
@ExperimentalPagerApi
@Composable
fun ClanBossInfoPager(
    clanId: Int,
    index: Int,
    clanViewModel: ClanViewModel = hiltNavGraphViewModel()
) {
    clanViewModel.getClanInfo(clanId)
    val clanInfo = clanViewModel.clanInfo.observeAsState()
    val pagerState = rememberPagerState(pageCount = 5, initialPage = index)

    clanInfo.value?.let { clanValue ->
        val section = remember {
            mutableStateOf(clanValue.getAllBossInfo().size - 1)
        }
        val bossInfoList = clanValue.getUnitIdList(section.value)
        val enemyIds = arrayListOf<Int>()
        bossInfoList.forEach {
            enemyIds.add(it.enemyId)
        }
        clanViewModel.getAllBossAttr(enemyIds)
        val bossDataList = clanViewModel.allClanBossAttr.observeAsState()

        Column(modifier = Modifier.background(colorResource(id = R.color.bg_gray))) {
            //图标列表
            ClanBattleItem(clanValue, pagerState = pagerState, type = 1)
            //指示器
            HorizontalPagerIndicator(
                pagerState = pagerState,
                activeColor = MaterialTheme.colors.primary,
                inactiveColor = colorResource(id = R.color.alpha_primary),
                spacing = Dimen.iconSize + Dimen.mediuPadding * 2,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
            //阶段选择
            val sectionChipData = arrayListOf<ChipData>()
            for (i in clanValue.getAllBossInfo().indices) {
                sectionChipData.add(
                    ChipData(i, getZhNumberText(i + 1))
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                MainText(text = stringResource(R.string.title_section))
                ChipGroup(
                    sectionChipData,
                    section,
                    modifier = Modifier,
                    type = 2
                )
            }
            //BOSS信息
            HorizontalPager(state = pagerState) { pagerIndex ->
                if (bossDataList.value != null && bossDataList.value!!.isNotEmpty()) {
                    val bossDataValue = bossDataList.value!![pagerIndex]
                    Card(
                        shape = CardTopShape,
                        elevation = 0.dp,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(Dimen.mediuPadding)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(Dimen.smallPadding)
                                .verticalScroll(rememberScrollState())
                        ) {
                            //名称
                            MainText(
                                text = bossDataValue.name,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            //等级
                            MainSubText(
                                text = bossDataValue.level.toString(),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            //属性
                            AttrList(attrs = bossDataValue.attr.Enemy())
                            //技能
                            BossSkillList(pagerIndex, bossDataList.value!!)
                        }
                    }
                }

            }
        }

    }
}

@Composable
private fun BossSkillList(
    index: Int,
    bossList: List<EnemyParameter>,
    skillViewModel: SkillViewModel = hiltNavGraphViewModel()
) {
    skillViewModel.getAllEnemySkill(bossList)
    skillViewModel.getAllSkillLoops(bossList)
    val allSkillList = skillViewModel.allSkills.observeAsState()
    val allLoopData = skillViewModel.allAtkPattern.observeAsState()
    val allIcon = skillViewModel.allIconTypes.observeAsState()


    allSkillList.value?.let { list ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimen.smallPadding)
        ) {
            if (allLoopData.value != null && allIcon.value != null) {
                SkillLoopList(
                    allLoopData.value!![index],
                    allIcon.value!![index]
                )
            }
            list[index].forEach {
                SkillItem(level = it.level, skillDetail = it)
            }
        }
    }
}


/**
 * 获取团队战阶段字体颜色
 */
@Composable
fun getSectionTextColor(section: Int): Color {
    val color = when (section) {
        1 -> R.color.color_rank_2_3
        2 -> R.color.color_rank_4_6
        3 -> R.color.color_rank_7_10
        4 -> R.color.color_rank_11_17
        else -> R.color.color_rank_18
    }
    return colorResource(id = color)
}

