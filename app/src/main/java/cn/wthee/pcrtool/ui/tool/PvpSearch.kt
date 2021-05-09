package cn.wthee.pcrtool.ui.tool

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import cn.wthee.pcrtool.R
import cn.wthee.pcrtool.data.db.dao.PvpDao
import cn.wthee.pcrtool.data.db.entity.PvpLikedData
import cn.wthee.pcrtool.data.db.view.PvpCharacterData
import cn.wthee.pcrtool.data.db.view.getIdStr
import cn.wthee.pcrtool.data.enums.MainIconType
import cn.wthee.pcrtool.data.model.PvpResultData
import cn.wthee.pcrtool.database.AppPvpDatabase
import cn.wthee.pcrtool.database.getRegion
import cn.wthee.pcrtool.service.PvpService
import cn.wthee.pcrtool.ui.MainActivity
import cn.wthee.pcrtool.ui.MainActivity.Companion.navViewModel
import cn.wthee.pcrtool.ui.compose.*
import cn.wthee.pcrtool.ui.theme.Dimen
import cn.wthee.pcrtool.ui.theme.Shapes
import cn.wthee.pcrtool.utils.ActivityHelper
import cn.wthee.pcrtool.utils.CharacterIdUtil
import cn.wthee.pcrtool.utils.ToastUtil
import cn.wthee.pcrtool.utils.fillZero
import cn.wthee.pcrtool.viewmodel.CharacterViewModel
import cn.wthee.pcrtool.viewmodel.PvpViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.gson.JsonArray
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round


/**
 * 竞技场查询
 */
@ExperimentalPagerApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun PvpSearchCompose(
    toResult: (String) -> Unit,
    viewModel: CharacterViewModel = hiltNavGraphViewModel()
) {
    //已选择的id
    val navIds = navViewModel.selectedIds.value
    val selectedIds = remember {
        if (navIds != null && navIds.isNotEmpty()) {
            mutableStateListOf(
                navIds[0],
                navIds[1],
                navIds[2],
                navIds[3],
                navIds[4],
            )
        } else {
            mutableStateListOf(
                PvpCharacterData(),
                PvpCharacterData(),
                PvpCharacterData(),
                PvpCharacterData(),
                PvpCharacterData(),
            )
        }

    }
    navViewModel.selectedIds.postValue(selectedIds.subList(0, 5))

    //获取数据
    viewModel.getAllCharacter()
    val data = viewModel.allPvpCharacterData.observeAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activity = ActivityHelper.instance.currentActivity
    //fixme 滑动距离
    val scrollState = rememberLazyListState()
    val positionIndex = remember {
        mutableStateOf(0)
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.bg_gray))
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colorResource(id = R.color.bg_gray))
        ) {
            //已选择列表
            StaggeredVerticalGrid(
                spanCount = 5, modifier = Modifier
                    .padding(top = Dimen.mediuPadding)
            ) {
                selectedIds.forEach {
                    PvpIconItem(selectedIds = selectedIds, it = it)
                }
            }
            Card(modifier = Modifier.fillMaxWidth(), shape = Shapes.large) {
                data.value?.let { dataValue ->
                    val character0 = dataValue.filter {
                        it.position in 0..299
                    }
                    val character1 = dataValue.filter {
                        it.position in 300..599
                    }
                    val character2 = dataValue.filter {
                        it.position in 600..9999
                    }
                    val spanCount = 5
                    val showIcon = listOf(0, 0, 1, 0, 0)
                    //fixme item 高度
                    val itemHeightPx = (Dimen.iconSize + Dimen.mediuPadding).value.toInt()
                    //站位图标在列表中的位置
                    val positions = arrayListOf(0, 0, 0)
                    val filledCount1 = spanCount - character0.size % spanCount
                    positions[1] =
                        (spanCount + character0.size + filledCount1) / spanCount
                    val filledCount2 = spanCount - character1.size % spanCount
                    positions[0] =
                        ((positions[1] + 1) * spanCount + character1.size + filledCount2) / spanCount
                    //滚动监听
                    when (scrollState.firstVisibleItemIndex) {
                        //后
                        positions[0] -> {
                            positionIndex.value = 0
                        }
                        scrollState.layoutInfo.totalItemsCount - scrollState.layoutInfo.visibleItemsInfo.size -> {
                            positionIndex.value = 0
                        }
                        //中
                        positions[1] -> {
                            positionIndex.value = 1
                        }
                        //前
                        positions[2] -> {
                            positionIndex.value = 2
                        }

                    }
                    Box {
                        //供选择列表
                        /*    Column(modifier = Modifier.verticalScroll(scrollState)) {
                                StaggeredVerticalGrid(spanCount = 5) {
                                    //前
                                    showIcon.forEach {
                                        PlaceHolderItem(it, R.drawable.ic_position_0)
                                    }
                                    character0.forEach {
                                        PvpIconItem(selectedIds, it)
                                    }
                                    for (i in 0 until filledCount1) {
                                        PlaceHolderItem(0)
                                    }
                                    //中
                                    showIcon.forEach {
                                        PlaceHolderItem(it, R.drawable.ic_position_1)
                                    }
                                    character1.forEach {
                                        PvpIconItem(selectedIds, it)
                                    }
                                    for (i in 0 until filledCount2) {
                                        PlaceHolderItem(0)
                                    }
                                    //后
                                    showIcon.forEach {
                                        PlaceHolderItem(it, R.drawable.ic_position_2)
                                    }
                                    character2.forEach {
                                        PvpIconItem(selectedIds, it)
                                    }
                                    for (i in 0 until spanCount) {
                                        PlaceHolderItem(0)
                                    }
                                }
                            }*/

                        LazyVerticalGrid(cells = GridCells.Fixed(spanCount), state = scrollState) {
                            //前
                            itemsIndexed(showIcon) { index, _ ->
                                if (index == 2) {
                                    PvpPositionIcon(R.drawable.ic_position_0)
                                }
                            }
                            items(character0) {
                                PvpIconItem(selectedIds, it)
                            }
                            //中
                            items(filledCount1) {
                                Spacer(modifier = Modifier.height(Dimen.sheetMarginBottom))
                            }
                            itemsIndexed(showIcon) { index, _ ->
                                if (index == 2) {
                                    PvpPositionIcon(R.drawable.ic_position_1)
                                }
                            }
                            items(character1) {
                                PvpIconItem(selectedIds, it)
                            }
                            //后
                            items(filledCount2) {
                                Spacer(modifier = Modifier.height(Dimen.sheetMarginBottom))
                            }
                            itemsIndexed(showIcon) { index, _ ->
                                if (index == 2) {
                                    PvpPositionIcon(R.drawable.ic_position_2)
                                }
                            }
                            items(character2) {
                                PvpIconItem(selectedIds, it)
                            }
                            items(spanCount) {
                                Spacer(modifier = Modifier.height(Dimen.iconSize))
                            }
                        }
                        //指示器
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(
                                    bottom = Dimen.fabSize + Dimen.fabMargin + Dimen.mediuPadding,
                                    start = Dimen.mediuPadding,
                                    end = Dimen.fabMargin
                                )
                        ) {
                            val icons = arrayListOf(
                                R.drawable.ic_position_2,
                                R.drawable.ic_position_1,
                                R.drawable.ic_position_0,
                            )
                            icons.forEachIndexed { index, it ->
                                val modifier = if (positionIndex.value == index) {
                                    Modifier
                                        .padding(Dimen.smallPadding)
                                        .border(
                                            Dimen.border,
                                            MaterialTheme.colors.primary,
                                            CircleShape
                                        )
                                } else {
                                    Modifier.padding(Dimen.smallPadding)
                                }
                                IconCompose(
                                    data = it,
                                    modifier = modifier
                                        .size(Dimen.fabIconSize)
                                        .clip(CircleShape)
                                ) {
                                    positionIndex.value = index
                                    scope.launch {
                                        scrollState.scrollToItem(positions[index])
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }

        //fab
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = Dimen.fabMarginEnd, bottom = Dimen.fabMargin)
        ) {
            //悬浮窗
            FabCompose(
                iconType = MainIconType.PVP_SEARCH_WINDOW,
                modifier = Modifier
                    .padding(end = Dimen.fabSmallMarginEnd)
                    .rotate(180f)
            ) {
                //检查是否已经授予权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(
                        context
                    )
                ) {
                    //若未授权则请求权限
                    val intent = Intent(ACTION_MANAGE_OVERLAY_PERMISSION)
                    intent.data = Uri.parse("package:" + context.packageName)
                    context.startActivity(intent)
                } else {
                    //打开悬浮窗
                    val intent =
                        Intent(activity?.applicationContext, PvpService::class.java)
                    activity?.startService(intent)
                    //退回桌面
                    val home = Intent(Intent.ACTION_MAIN)
                    home.addCategory(Intent.CATEGORY_HOME)
                    context.startActivity(home)
                }
            }
            //查询
            val tip = stringResource(id = R.string.tip_select_5)
            ExtendedFabCompose(
                iconType = MainIconType.PVP_SEARCH,
                text = stringResource(id = R.string.pvp_search)
            ) {
                //查询
                if (selectedIds.contains(PvpCharacterData())) {
                    ToastUtil.short(tip)
                } else {
                    toResult(selectedIds.subList(0, 5).getIdStr())
                }
            }
        }

    }

}


@Composable
private fun PvpPositionIcon(iconId: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Dimen.iconSize)
    ) {
        IconCompose(
            data = iconId,
            modifier = Modifier
                .align(
                    Alignment.Center
                )
                .size(Dimen.fabIconSize),
            clickable = false
        )
    }
}


@Composable
fun PvpIconItem(
    selectedIds: SnapshotStateList<PvpCharacterData>,
    it: PvpCharacterData
) {
    val tipSelectLimit = stringResource(id = R.string.tip_select_limit)
    val selected = selectedIds.contains(it)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        val icon = if (it.unitId == 0) {
            R.drawable.unknown_gray
        } else {
            CharacterIdUtil.getMaxIconUrl(
                it.unitId,
                MainActivity.r6Ids.contains(it.unitId)
            )
        }
        //图标
        IconCompose(data = icon) {
            //点击选择或取消选择
            if (selected) {
                var cancelSelectIndex = 0
                selectedIds.forEachIndexed { index, sel ->
                    if (it.position == sel.position) {
                        cancelSelectIndex = index
                    }
                }
                selectedIds[cancelSelectIndex] = PvpCharacterData()
            } else {
                val unSelected = selectedIds.find { it.position == 999 }
                if (unSelected == null) {
                    //选完了
                    ToastUtil.short(tipSelectLimit)
                } else {
                    //可以选择
                    selectedIds[0] = it
                }
            }
            selectedIds.sortByDescending { it.position }
        }
        //位置
        val text =
            if (it != PvpCharacterData()) it.position.toString() else stringResource(id = R.string.unselect)
        Text(
            text = text,
            color = if (selected) MaterialTheme.colors.primary else MaterialTheme.colors.onSurface,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.subtitle2,
            modifier = Modifier.padding(bottom = Dimen.mediuPadding)
        )
    }
}

/**
 * 查询结果页面
 */
@Composable
fun PvpSearchResult(
    idString: String,
    viewModel: PvpViewModel = hiltNavGraphViewModel()
) {
    val ids = JsonArray()
    for (id in idString.split("-")) {
        if (id.isNotBlank()) {
            ids.add(id.toInt())
        }
    }
    val dao = AppPvpDatabase.getInstance().getPvpDao()
    val region = getRegion()
    viewModel.getPVPData(ids)
    viewModel.getLikedList(idString, region, 0)
    navViewModel.loading.postValue(true)
    val result = viewModel.pvpResult.observeAsState()
    val liked = viewModel.liked.observeAsState()
    val likedList = arrayListOf<String>()
    liked.value?.let {
        it.forEach { data ->
            likedList.add(data.atks)
        }
    }

    Box(Modifier.fillMaxSize()) {
        if (result.value != null) {
            navViewModel.loading.postValue(false)
            if (result.value!!.message == "success") {
                if (result.value!!.data!!.isNotEmpty()) {
                    val list = result.value!!.data!!.sortedByDescending { it.up }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colorResource(id = R.color.bg_gray))
                    ) {
                        //防守
                        Row(
                            modifier = Modifier
                                .padding(top = Dimen.mediuPadding)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ids.forEach {
                                IconCompose(
                                    data = CharacterIdUtil.getMaxIconUrl(
                                        it.asInt,
                                        MainActivity.r6Ids.contains(it.asInt)
                                    ),
                                    modifier = Modifier.padding(end = Dimen.smallPadding)
                                )
                            }
                        }
                        //展示查询结果
                        LazyColumn {
                            itemsIndexed(items = list) { index, item ->
                                PvpAtkTeam(likedList, index + 1, item, region, dao, viewModel)
                            }
                            item {
                                Spacer(modifier = Modifier.height(Dimen.sheetMarginBottom))
                            }
                        }
                    }
                } else {
                    MainText(
                        text = stringResource(id = R.string.pvp_no_data),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            } else {
                MainText(
                    text = stringResource(id = R.string.data_get_error),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

/**
 * 查询结果
 */
@Composable
private fun PvpAtkTeam(
    likedList: List<String>,
    i: Int,
    item: PvpResultData,
    region: Int,
    dao: PvpDao,
    viewModel: PvpViewModel
) {
    val scope = rememberCoroutineScope()
    val liked = likedList.contains(item.atk)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimen.mediuPadding)
    ) {
        MainTitleText(
            text = stringResource(id = R.string.team_no, i.toString().fillZero()),
            modifier = Modifier.padding(bottom = Dimen.mediuPadding)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = Dimen.cardElevation, shape = Shapes.large, clip = true)
        ) {
            val upRatio = if (item.up == 0) 0 else {
                round(item.up * 1.0 / (item.up + item.down) * 100).toInt()
            }
            Column(modifier = Modifier.padding(Dimen.mediuPadding)) {
                //点赞信息
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Dimen.smallPadding)
                ) {
                    MainContentText(
                        text = "${upRatio}%",
                        color = MaterialTheme.colors.primary,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth(0.2f)
                    )
                    MainContentText(
                        text = item.up.toString(),
                        color = colorResource(id = R.color.cool_apk),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth(0.2f)
                    )
                    MainContentText(
                        text = item.down.toString(),
                        color = colorResource(id = R.color.color_rank_18),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth(0.2f)
                    )
                }
                //队伍角色图标
                //进攻
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(0.8f)) {
                        item.getIdList(0).forEach {
                            IconCompose(
                                data = CharacterIdUtil.getMaxIconUrl(
                                    it,
                                    MainActivity.r6Ids.contains(it)
                                ),
                                modifier = Modifier.padding(end = Dimen.largePadding)
                            )
                        }
                    }
                    //点击收藏
                    Box(modifier = Modifier.weight(0.2f)) {
                        Icon(
                            imageVector = if (liked) MainIconType.LOVE_FILL.icon else MainIconType.LOVE_LINE.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clickable {
                                    scope.launch {
                                        if (dao.getLiked(item.atk, item.def, region, 0) != null) {
                                            //已收藏，取消收藏
                                            dao.delete(item.atk, item.def, region)
                                        } else {
                                            //未收藏，添加收藏
                                            val simpleDateFormat =
                                                SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS") // HH:mm:ss
                                            val date = Date(System.currentTimeMillis())
                                            dao.insert(
                                                PvpLikedData(
                                                    item.id,
                                                    item.atk,
                                                    item.def,
                                                    simpleDateFormat.format(date),
                                                    region
                                                )
                                            )
                                        }
                                        viewModel.getLikedList(item.def, region, 0)
                                    }
                                }
                        )
                    }
                }
            }
        }

    }
}
