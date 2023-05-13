package cn.wthee.pcrtool.ui.tool

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import cn.wthee.pcrtool.data.enums.UnitType
import cn.wthee.pcrtool.data.model.CharacterProperty
import cn.wthee.pcrtool.ui.components.MainText
import cn.wthee.pcrtool.ui.skill.SkillItem
import cn.wthee.pcrtool.ui.theme.Dimen
import cn.wthee.pcrtool.viewmodel.CharacterViewModel
import cn.wthee.pcrtool.viewmodel.EnemyViewModel
import cn.wthee.pcrtool.viewmodel.ExtraEquipmentViewModel
import cn.wthee.pcrtool.viewmodel.SkillViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AllSkillList(
    toSummonDetail: ((Int, Int, Int, Int, Int) -> Unit)? = null,
    skillViewModel: SkillViewModel = hiltViewModel(),
    characterViewModel: CharacterViewModel = hiltViewModel(),
    enemyViewModel: EnemyViewModel = hiltViewModel(),
    extraEquipmentViewModel: ExtraEquipmentViewModel = hiltViewModel(),
) {
    val allCharacter =
        characterViewModel.getAllCharacter().collectAsState(initial = arrayListOf()).value
    val bossIds = enemyViewModel.getAllBossIds().collectAsState(initial = arrayListOf()).value
    val exEquipSkillIds = extraEquipmentViewModel.getAllEquipSkillIdList()
        .collectAsState(initial = arrayListOf()).value


    val ids = arrayListOf<Int>()
    allCharacter.forEach {
        ids.add(it.unitId)
    }
    ids.addAll(bossIds)

    val skills = skillViewModel.getCharacterSkills(201, 1000, ids.distinct())
        .collectAsState(initial = arrayListOf()).value
    val equipSkills = skillViewModel.getExtraEquipPassiveSkills(exEquipSkillIds).collectAsState(
        initial = arrayListOf()
    ).value


    Column(
        modifier = Modifier
            .padding(Dimen.largePadding)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(pageCount = 2) { index ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    MainText(text = "$index：${if (index == 0) skills.size else equipSkills.size}")
                }
                items(
                    items = if (index == 0) skills else equipSkills,
                    key = {
                        it.skillId
                    }
                ) { skillDetail ->
                    var error = false
                    skillDetail.getActionInfo().forEach { action ->
                        if (action.action.contains("?")) {
                            error = true
                            return@forEach
                        }
                    }
                    if (error) {
                        SkillItem(
                            skillDetail = skillDetail,
                            unitType = UnitType.CHARACTER,
                            toSummonDetail = toSummonDetail,
                            property = CharacterProperty(100, 1, 1)
                        )
                    }
                }
            }
        }
    }


}