package cn.wthee.pcrtool.ui.home.module

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import cn.wthee.pcrtool.R
import cn.wthee.pcrtool.data.db.entity.NewsTable
import cn.wthee.pcrtool.data.enums.MainIconType
import cn.wthee.pcrtool.data.enums.OverviewType
import cn.wthee.pcrtool.ui.NavActions
import cn.wthee.pcrtool.ui.common.CenterTipText
import cn.wthee.pcrtool.ui.home.Section
import cn.wthee.pcrtool.ui.home.editOverviewMenuOrder
import cn.wthee.pcrtool.ui.tool.NewsItem
import cn.wthee.pcrtool.viewmodel.OverviewViewModel


/**
 * 公告
 */
@Composable
fun NewsSection(
    actions: NavActions,
    isEditMode: Boolean,
    overviewViewModel: OverviewViewModel = hiltViewModel()
) {
    val id = OverviewType.NEWS.id
    //公告列表
    val newsList = overviewViewModel.newOverview.observeAsState().value
    LaunchedEffect(null) {
        overviewViewModel.getNewsOverview()
    }

    Section(
        id = id,
        titleId = R.string.tool_news,
        iconType = MainIconType.NEWS,
        isEditMode = isEditMode,
        onClick = {
            if (isEditMode)
                editOverviewMenuOrder(id)
            else
                actions.toNews()
        }
    ) {
        Column {
            if (newsList == null) {
                for (i in 0 until 3) {
                    NewsItem(
                        news = NewsTable()
                    )
                }
            } else if (newsList.data?.isNotEmpty() == true) {
                newsList.data?.forEach {
                    NewsItem(
                        news = it
                    )
                }
            } else {
                CenterTipText(stringResource(id = R.string.no_data))
            }
        }
    }
}