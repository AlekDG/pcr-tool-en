package cn.wthee.pcrtool.ui.character

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cn.wthee.pcrtool.R
import cn.wthee.pcrtool.data.db.view.CharacterInfo
import cn.wthee.pcrtool.data.enums.MainIconType
import cn.wthee.pcrtool.data.enums.getSortType
import cn.wthee.pcrtool.data.model.ChipData
import cn.wthee.pcrtool.data.model.FilterCharacter
import cn.wthee.pcrtool.data.model.isFilter
import cn.wthee.pcrtool.ui.MainActivity.Companion.navViewModel
import cn.wthee.pcrtool.ui.NavViewModel
import cn.wthee.pcrtool.ui.common.*
import cn.wthee.pcrtool.ui.mainSP
import cn.wthee.pcrtool.ui.theme.*
import cn.wthee.pcrtool.utils.*
import cn.wthee.pcrtool.viewmodel.CharacterViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 角色列表
 */
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun CharacterList(
    scrollState: LazyListState,
    toDetail: (Int) -> Unit,
    viewModel: CharacterViewModel = hiltViewModel(),
) {

    //筛选状态
    val filter = navViewModel.filterCharacter.observeAsState()
    // dialog 状态
    val state = rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden
    )
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    //关闭时监听
    if (!state.isVisible && !state.isAnimationRunning) {
        navViewModel.fabMainIcon.postValue(MainIconType.BACK)
        navViewModel.fabOKCilck.postValue(false)
        navViewModel.resetClick.postValue(false)
        keyboardController?.hide()
    }

    filter.value?.let { filterValue ->
        filterValue.starIds =
            GsonUtil.fromJson(mainSP().getString(Constants.SP_STAR_CHARACTER, ""))
                ?: arrayListOf()
    }
    val list = viewModel.getCharacters(filter.value).collectAsState(initial = arrayListOf()).value
    val spanCount = ScreenUtil.getWidth() / (120.dp + Dimen.mediumPadding * 2).value.dp2px

    ModalBottomSheetLayout(
        sheetState = state,
        scrimColor = colorResource(id = if (isSystemInDarkTheme()) R.color.alpha_black else R.color.alpha_white),
        sheetElevation = Dimen.sheetElevation,
        sheetShape = if (state.offset.value == 0f) {
            noShape
        } else {
            Shape.large
        },
        sheetContent = {
            FilterCharacterSheet(navViewModel, coroutineScope, state)
        },
        sheetBackgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (list.isNotEmpty()) {
                LazyVerticalGrid(
                    cells = GridCells.Adaptive(Dimen.characterListCardMinWidth),
                    state = scrollState,
                    contentPadding = PaddingValues(Dimen.mediumPadding)
                ) {
                    items(list) {
                        CharacterItem(
                            it,
                            filter.value!!.starIds.contains(it.id),
                            modifier = Modifier.padding(Dimen.mediumPadding)
                        ) {
                            toDetail(it.id)
                        }
                    }
                    items(2) {
                        CommonSpacer()
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = Dimen.fabMarginEnd, bottom = Dimen.fabMargin),
                horizontalArrangement = Arrangement.End
            ) {
                //回到顶部
                FadeAnimation(visible = scrollState.firstVisibleItemIndex != 0) {
                    FabCompose(
                        iconType = MainIconType.TOP
                    ) {
                        coroutineScope.launch {
                            scrollState.scrollToItem(0)
                        }
                    }
                }
                //重置筛选
                if (filter.value != null && filter.value!!.isFilter()) {
                    FabCompose(
                        iconType = MainIconType.RESET
                    ) {
                        coroutineScope.launch {
                            state.hide()
                        }
                        navViewModel.resetClick.postValue(true)
                    }
                }
                val count = list.size
                // 数量显示&筛选按钮
                FabCompose(
                    iconType = MainIconType.CHARACTER,
                    text = "$count"
                ) {
                    coroutineScope.launch {
                        if (state.isVisible) {
                            navViewModel.fabMainIcon.postValue(MainIconType.BACK)
                            state.hide()
                        } else {
                            navViewModel.fabMainIcon.postValue(MainIconType.OK)
                            state.show()
                        }
                    }
                }
            }

        }

    }
}

/**
 * 角色列表项
 */
@ExperimentalMaterialApi
@Composable
fun CharacterItem(
    character: CharacterInfo,
    loved: Boolean,
    modifier: Modifier = Modifier,
    numberStyle: TextStyle = MaterialTheme.typography.bodySmall,
    size: Dp = Dimen.smallIconSize,
    onClick: () -> Unit
) {
    MainCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column {
            //图片
            Box(contentAlignment = Alignment.BottomEnd) {
                ImageCompose(
                    CharacterIdUtil.getMaxCardUrl(character.id), RATIO
                )
                //位置
                PositionIcon(
                    modifier = Modifier.padding(Dimen.smallPadding),
                    character.position,
                    size = size
                )
            }

            Row(
                modifier = Modifier.padding(
                    start = Dimen.smallPadding,
                    end = Dimen.smallPadding,
                    top = Dimen.smallPadding
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                //限定类型
                CharacterLimitText(
                    characterInfo = character,
                    modifier = Modifier.padding(end = Dimen.smallPadding)
                )
                //名字
                SelectText(
                    selected = loved,
                    text = character.getNameF(),
                    textAlign = TextAlign.Start,
                    margin = 0.dp,
                    padding = Dimen.smallPadding,
                )
            }
            //其它属性
            Row(
                modifier = Modifier
                    .padding(Dimen.smallPadding)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CharacterNumberText(text = character.getFixedAge(), style = numberStyle)
                CharacterNumberText(text = character.getFixedHeight() + "CM", style = numberStyle)
                CharacterNumberText(text = character.getFixedWeight() + "KG", style = numberStyle)
                CharacterPositionText(
                    position = character.position,
                    textAlign = TextAlign.End,
                    textStyle = numberStyle
                )
            }
        }
    }
}

/**
 * 蓝色字体
 */
@Composable
private fun CharacterNumberText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = MaterialTheme.typography.bodySmall,
) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.surfaceVariant,
        style = style,
        modifier = modifier.padding(end = Dimen.smallPadding),
        textAlign = TextAlign.End
    )
}

/**
 * 角色筛选
 */
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
private fun FilterCharacterSheet(
    navViewModel: NavViewModel,
    coroutineScope: CoroutineScope,
    sheetState: ModalBottomSheetState,
    characterViewModel: CharacterViewModel = hiltViewModel()
) {
    val filter = navViewModel.filterCharacter.value ?: FilterCharacter()

    val textState = remember { mutableStateOf(TextFieldValue(text = filter.name)) }
    filter.name = textState.value.text
    //排序类型筛选s
    val sortTypeIndex = remember {
        mutableStateOf(filter.sortType.type)
    }
    filter.sortType = getSortType(sortTypeIndex.value)

    //排序方式筛选
    val sortAscIndex = remember {
        mutableStateOf(if (filter.asc) 0 else 1)
    }
    filter.asc = sortAscIndex.value == 0

    //收藏筛选
    val loveIndex = remember {
        mutableStateOf(if (filter.all) 0 else 1)
    }
    filter.all = loveIndex.value == 0

    //六星筛选
    val r6Index = remember {
        mutableStateOf(if (filter.r6) 1 else 0)
    }
    filter.r6 = r6Index.value == 1

    //位置筛选
    val positionIndex = remember {
        mutableStateOf(filter.positon)
    }
    filter.positon = positionIndex.value

    //攻击类型
    val atkIndex = remember {
        mutableStateOf(filter.atk)
    }
    filter.atk = atkIndex.value

    //公会
    val guildList = characterViewModel.getGuilds().collectAsState(initial = arrayListOf()).value
    val guildIndex = remember {
        mutableStateOf(filter.guild)
    }
    filter.guild = guildIndex.value

    //限定类型
    val typeIndex = remember {
        mutableStateOf(filter.type)
    }
    filter.type = typeIndex.value

    //确认操作
    val ok = navViewModel.fabOKCilck.observeAsState().value ?: false
    val reset = navViewModel.resetClick.observeAsState().value ?: false

    //选择状态
    Column(
        modifier = Modifier
            .padding(start = Dimen.largePadding, end = Dimen.largePadding)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        //点击重置
        if (reset) {
            textState.value = TextFieldValue(text = "")
            sortTypeIndex.value = 0
            sortAscIndex.value = 1
            loveIndex.value = 0
            r6Index.value = 0
            positionIndex.value = 0
            atkIndex.value = 0
            guildIndex.value = 0
            typeIndex.value = 0
            navViewModel.resetClick.postValue(false)
            navViewModel.filterCharacter.postValue(FilterCharacter())
        }
        //点击确认
        if (ok) {
            coroutineScope.launch {
                sheetState.hide()
            }
            navViewModel.filterCharacter.postValue(filter)
            navViewModel.fabOKCilck.postValue(false)
            navViewModel.fabMainIcon.postValue(MainIconType.BACK)
        }
        //角色名搜索
        val keyboardController = LocalSoftwareKeyboardController.current
        OutlinedTextField(
            value = textState.value,
            shape = Shape.medium,
            colors = outlinedTextFieldColors(),
            onValueChange = { textState.value = it },
            textStyle = MaterialTheme.typography.labelLarge,
            leadingIcon = {
                IconCompose(
                    data = MainIconType.CHARACTER.icon,
                    size = Dimen.fabIconSize
                )
            },
            trailingIcon = {
                IconCompose(
                    data = MainIconType.SEARCH.icon,
                    size = Dimen.fabIconSize
                ) {
                    keyboardController?.hide()
                    navViewModel.fabOKCilck.postValue(true)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    navViewModel.fabOKCilck.postValue(true)
                }
            ),
            singleLine = false,
            label = {
                Text(
                    text = stringResource(id = R.string.character_name),
                    style = MaterialTheme.typography.labelLarge
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        //排序类型
        MainText(
            text = stringResource(id = R.string.title_sort),
            modifier = Modifier.padding(top = Dimen.largePadding)
        )
        val sortChipData = arrayListOf(
            ChipData(0, stringResource(id = R.string.sort_date)),
            ChipData(1, stringResource(id = R.string.age)),
            ChipData(2, stringResource(id = R.string.title_height)),
            ChipData(3, stringResource(id = R.string.title_weight)),
            ChipData(4, stringResource(id = R.string.title_position)),
        )
        ChipGroup(
            sortChipData,
            sortTypeIndex,
            modifier = Modifier.padding(Dimen.smallPadding),
        )
        //排序方式
        MainText(
            text = stringResource(id = R.string.sort_asc_desc),
            modifier = Modifier.padding(top = Dimen.largePadding)
        )
        val sortAscChipData = arrayListOf(
            ChipData(0, stringResource(id = R.string.sort_asc)),
            ChipData(1, stringResource(id = R.string.sort_desc)),
        )
        ChipGroup(
            sortAscChipData,
            sortAscIndex,
            modifier = Modifier.padding(Dimen.smallPadding),
        )
        //收藏
        MainText(
            text = stringResource(id = R.string.title_love),
            modifier = Modifier.padding(top = Dimen.largePadding)
        )
        val loveChipData = arrayListOf(
            ChipData(0, stringResource(id = R.string.all)),
            ChipData(1, stringResource(id = R.string.loved)),
        )
        ChipGroup(
            loveChipData,
            loveIndex,
            modifier = Modifier.padding(Dimen.smallPadding),
        )
        //类型
        MainText(
            text = stringResource(id = R.string.title_type),
            modifier = Modifier.padding(top = Dimen.largePadding)
        )
        val typeChipData = arrayListOf(
            ChipData(0, stringResource(id = R.string.all)),
            ChipData(1, stringResource(id = R.string.type_normal)),
            ChipData(2, stringResource(id = R.string.type_limit)),
            ChipData(3, stringResource(id = R.string.type_event_limit)),
        )
        ChipGroup(
            typeChipData,
            typeIndex,
            modifier = Modifier.padding(Dimen.smallPadding),
        )
        //六星
        MainText(
            text = stringResource(id = R.string.title_rarity),
            modifier = Modifier.padding(top = Dimen.largePadding)
        )
        val r6ChipData = arrayListOf(
            ChipData(0, stringResource(id = R.string.all)),
            ChipData(1, stringResource(id = R.string.six_unlock)),
        )
        ChipGroup(
            r6ChipData,
            r6Index,
            modifier = Modifier.padding(Dimen.smallPadding),
        )
        //位置
        MainText(
            text = stringResource(id = R.string.title_position),
            modifier = Modifier.padding(top = Dimen.largePadding)
        )
        val positionChipData = arrayListOf(
            ChipData(0, stringResource(id = R.string.all)),
            ChipData(1, stringResource(id = R.string.position_0)),
            ChipData(2, stringResource(id = R.string.position_1)),
            ChipData(3, stringResource(id = R.string.position_2)),
        )
        ChipGroup(
            positionChipData,
            positionIndex,
            modifier = Modifier.padding(Dimen.smallPadding),
        )
        //攻击类型
        MainText(
            text = stringResource(id = R.string.atk_type),
            modifier = Modifier.padding(top = Dimen.largePadding)
        )
        val atkChipData = arrayListOf(
            ChipData(0, stringResource(id = R.string.all)),
            ChipData(1, stringResource(id = R.string.physical)),
            ChipData(2, stringResource(id = R.string.magic)),
        )
        ChipGroup(
            atkChipData,
            atkIndex,
            modifier = Modifier.padding(Dimen.smallPadding)
        )
        //公会名
        if (guildList.isNotEmpty()) {
            MainText(
                text = stringResource(id = R.string.title_guild),
                modifier = Modifier.padding(top = Dimen.largePadding)
            )
            val guildChipData = arrayListOf(ChipData(0, stringResource(id = R.string.all)))
            guildList.forEachIndexed { index, guildData ->
                guildChipData.add(ChipData(index + 1, guildData.guildName))
            }
            ChipGroup(
                guildChipData,
                guildIndex,
                modifier = Modifier.padding(Dimen.smallPadding),
            )
            CommonSpacer()
        }
    }
}