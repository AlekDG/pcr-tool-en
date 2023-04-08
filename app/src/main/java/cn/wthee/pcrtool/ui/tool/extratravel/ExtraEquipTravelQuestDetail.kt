package cn.wthee.pcrtool.ui.tool.extratravel

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import cn.wthee.pcrtool.R
import cn.wthee.pcrtool.data.db.view.ExtraEquipQuestData
import cn.wthee.pcrtool.ui.common.CommonGroupTitle
import cn.wthee.pcrtool.ui.common.CommonSpacer
import cn.wthee.pcrtool.ui.common.IconCompose
import cn.wthee.pcrtool.ui.common.SelectText
import cn.wthee.pcrtool.ui.common.VerticalGrid
import cn.wthee.pcrtool.ui.theme.CombinedPreviews
import cn.wthee.pcrtool.ui.theme.Dimen
import cn.wthee.pcrtool.ui.theme.PreviewLayout
import cn.wthee.pcrtool.ui.theme.colorWhite
import cn.wthee.pcrtool.utils.ImageRequestHelper
import cn.wthee.pcrtool.utils.intArrayList
import cn.wthee.pcrtool.viewmodel.ExtraEquipmentViewModel

/**
 * ex冒险区域详情
 */
@Composable
fun ExtraEquipTravelQuestDetail(
    questId: Int,
    toExtraEquipDetail: (Int) -> Unit,
    extraEquipmentViewModel: ExtraEquipmentViewModel = hiltViewModel()
) {
    val questData =
        extraEquipmentViewModel.getTravelQuest(questId).collectAsState(initial = null).value

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        questData?.let {
            TravelQuestItem(
                selectedId = 0,
                questData = questData,
                toExtraEquipDetail = toExtraEquipDetail
            )
        }
    }
}


/**
 * 区域详情信息、ex装备掉落信息
 * @param selectedId 0：无选中装备（区域详情信息）；非0：查看ex装备掉落信息时
 */
@Composable
fun TravelQuestItem(
    selectedId: Int,
    questData: ExtraEquipQuestData,
    extraEquipmentViewModel: ExtraEquipmentViewModel = hiltViewModel(),
    toExtraEquipDetail: ((Int) -> Unit)? = null
) {
    val subRewardList =
        extraEquipmentViewModel.getSubRewardList(questData.travelQuestId).collectAsState(
            initial = arrayListOf()
        ).value

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        //标题
        TravelQuestHeader(questData, showTitle = selectedId == 0)
        //掉落
        subRewardList.forEach { subRewardData ->
            ExtraEquipGroup(
                subRewardData.category,
                subRewardData.categoryName,
                subRewardData.subRewardIds.intArrayList,
                subRewardData.subRewardDrops.intArrayList,
                selectedId,
                toExtraEquipDetail
            )
        }
        CommonSpacer()
    }

}

/**
 * 装备掉落分组、角色适用装备分组
 */
@Composable
fun ExtraEquipGroup(
    category: Int,
    categoryName: String,
    equipIdList: List<Int>,
    dropOddsList: List<Int> = arrayListOf(),
    selectedId: Int,
    toExtraEquipDetail: ((Int) -> Unit)?
) {
    val containsSelectedId = equipIdList.contains(selectedId)

    CommonGroupTitle(
        iconData = ImageRequestHelper.getInstance()
            .getUrl(
                ImageRequestHelper.ICON_EXTRA_EQUIPMENT_CATEGORY,
                category
            ),
        iconSize = Dimen.smallIconSize,
        titleStart = categoryName,
        titleEnd = equipIdList.size.toString(),
        modifier = Modifier.padding(Dimen.mediumPadding),
        backgroundColor = if (containsSelectedId) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        textColor = if (containsSelectedId) {
            colorWhite
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    )

    ExtraEquipRewardIconGrid(equipIdList, dropOddsList, selectedId, toExtraEquipDetail)

}


/**
 * 带选中标记的ex装备图标
 */
@Composable
private fun ExtraEquipRewardIconGrid(
    equipIdList: List<Int>,
    dropOddList: List<Int>,
    selectedId: Int,
    toExtraEquipDetail: ((Int) -> Unit)?
) {
    VerticalGrid(
        modifier = Modifier.padding(
            start = Dimen.commonItemPadding,
            end = Dimen.commonItemPadding
        ),
        itemWidth = Dimen.iconSize,
        contentPadding = Dimen.mediumPadding
    ) {
        equipIdList.forEachIndexed { index, equipId ->
            val selected = selectedId == equipId
            Column(
                modifier = Modifier
                    .padding(bottom = Dimen.mediumPadding)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconCompose(
                    data = ImageRequestHelper.getInstance()
                        .getUrl(ImageRequestHelper.ICON_EXTRA_EQUIPMENT, equipId),
                    onClick = if (toExtraEquipDetail != null) {
                        {
                            toExtraEquipDetail(equipId)
                        }
                    } else {
                        null
                    }
                )
                if (dropOddList.isNotEmpty()) {
                    SelectText(
                        selected = selected,
                        text = stringResource(
                            id = R.string.ex_equip_drop_odd,
                            dropOddList[index] / 10000f
                        )
                    )
                }

            }
        }
    }
}


@CombinedPreviews
@Composable
private fun ExtraEquipSubGroupPreview() {
    PreviewLayout {
        ExtraEquipGroup(
            1,
            "selected",
            arrayListOf(1, 2, 3, 4),
            arrayListOf(1000, 20000, 3333, 444444),
            2
        ) { }
        ExtraEquipGroup(
            1,
            "normal",
            arrayListOf(1, 2, 3, 4),
            arrayListOf(1000, 20000, 3333, 444444),
            5
        ) { }
    }
}