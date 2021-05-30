package cn.wthee.pcrtool.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.paging.ExperimentalPagingApi
import cn.wthee.pcrtool.data.db.view.PvpCharacterData
import cn.wthee.pcrtool.data.enums.MainIconType
import cn.wthee.pcrtool.data.model.FilterCharacter
import cn.wthee.pcrtool.data.model.FilterEquipment
import cn.wthee.pcrtool.ui.character.*
import cn.wthee.pcrtool.ui.compose.StatusBarBox
import cn.wthee.pcrtool.ui.equip.EquipList
import cn.wthee.pcrtool.ui.equip.EquipMainInfo
import cn.wthee.pcrtool.ui.equip.EquipMaterialDeatil
import cn.wthee.pcrtool.ui.home.CharacterList
import cn.wthee.pcrtool.ui.tool.*
import com.google.accompanist.pager.ExperimentalPagerApi
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

object Navigation {
    const val CHARACTER_LIST = "characterList"
    const val CHARACTER_DETAIL = "characterDetail"
    const val CHARACTER_BASIC_INFO = "characterBasicInfo"
    const val UNIT_ID = "unitId"
    const val EQUIP_LIST = "equipList"
    const val EQUIP_ID = "equipId"
    const val EQUIP_DETAIL = "equipDetail"
    const val RANK_EQUIP = "rankEquip"
    const val RANK_COMPARE = "rankCompare"
    const val PICS = "pictures"
    const val MAX_RANK = "maxRank"
    const val LEVEL = "level"
    const val RARITY = "rarity"
    const val UNIQUE_EQUIP_LEVEL = "uniqueEquipLevel"
    const val EQUIP_COUNT = "equipCount"
    const val EQUIP_MATERIAL = "equipMaterial"

    //工具
    const val TOOL_LEADER = "toolLeader"
    const val TOOL_GACHA = "toolGacha"
    const val TOOL_EVENT = "toolEvent"
    const val TOOL_GUILD = "toolGuild"
    const val TOOL_CLAN = "toolClanBattle"
    const val TOOL_CLAN_BOSS_INFO = "toolClanBattleInfo"
    const val TOOL_CLAN_BOSS_ID = "toolClanBattleID"
    const val TOOL_CLAN_BOSS_INDEX = "toolClanBattleIndex"
    const val TOOL_CALENDAR = "toolCalendar"
    const val TOOL_PVP = "toolPvpSearch"
    const val TOOL_PVP_FAVORITE = "toolPvpFavorite"
    const val TOOL_NEWS = "toolNews"
    const val TOOL_NEWS_DETAIL = "toolNewsDetail"
    const val TOOL_NEWS_TITLE = "toolNewsTitle"
    const val TOOL_NEWS_REGION = "toolNewsRegion"
    const val TOOL_NEWS_DATE = "toolNewsDate"
    const val TOOL_NEWS_URL = "toolNewsUrl"
    const val MAIN_SETTINGS = "mainSettings"
    const val APP_NOTICE = "appNotice"
}

@ExperimentalComposeUiApi
@ExperimentalPagingApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun NavGraph(navController: NavHostController, viewModel: NavViewModel, actions: NavActions) {

    NavHost(navController, startDestination = Navigation.CHARACTER_LIST) {

        //首页
        composable(Navigation.CHARACTER_LIST) {
            val scrollState = rememberLazyListState()
            StatusBarBox {
                CharacterList(scrollState, actions.toCharacterDetail)
            }
        }

        //角色属性详情
        composable(
            "${Navigation.CHARACTER_DETAIL}/{${Navigation.UNIT_ID}}",
            arguments = listOf(navArgument(Navigation.UNIT_ID) {
                type = NavType.IntType
            })
        ) {
            val arguments = requireNotNull(it.arguments)
            viewModel.fabMainIcon.postValue(MainIconType.BACK)
            val scrollState = rememberScrollState()
            StatusBarBox {
                CharacterDetail(
                    scrollState,
                    unitId = arguments.getInt(Navigation.UNIT_ID),
                    actions.toEquipDetail,
                    actions.toCharacterBasicInfo,
                    actions.toCharacteRankEquip,
                    actions.toCharacteRankCompare,
                    actions.toCharacteEquipCount,
                    actions.toCharacterPic,
                    viewModel
                )
            }

        }

        //角色资料
        composable(
            "${Navigation.CHARACTER_BASIC_INFO}/{${Navigation.UNIT_ID}}",
            arguments = listOf(navArgument(Navigation.UNIT_ID) {
                type = NavType.IntType
            })
        ) {
            val arguments = requireNotNull(it.arguments)
            val scrollState = rememberScrollState()
            StatusBarBox {
                CharacterBasicInfo(scrollState, unitId = arguments.getInt(Navigation.UNIT_ID))
            }
        }

        //角色图片
        composable(
            "${Navigation.PICS}/{${Navigation.UNIT_ID}}",
            arguments = listOf(navArgument(Navigation.UNIT_ID) {
                type = NavType.IntType
            })
        ) {
            val arguments = requireNotNull(it.arguments)
            StatusBarBox {
                CharacterAllPicture(
                    unitId = arguments.getInt(Navigation.UNIT_ID)
                )
            }
        }

        //装备列表
        composable(Navigation.EQUIP_LIST) {
            StatusBarBox {
                val scrollState = rememberLazyListState()
                EquipList(
                    scrollState,
                    toEquipDetail = actions.toEquipDetail,
                    toEquipMaterial = actions.toEquipMaterail
                )
            }
        }

        //装备详情
        composable(
            "${Navigation.EQUIP_DETAIL}/{${Navigation.EQUIP_ID}}",
            arguments = listOf(navArgument(Navigation.EQUIP_ID) {
                type = NavType.IntType
            })
        ) {
            val arguments = requireNotNull(it.arguments)
            StatusBarBox {
                EquipMainInfo(arguments.getInt(Navigation.EQUIP_ID), actions.toEquipMaterail)
            }
        }

        //装备素材详情
        composable(
            "${Navigation.EQUIP_MATERIAL}/{${Navigation.EQUIP_ID}}",
            arguments = listOf(navArgument(Navigation.EQUIP_ID) {
                type = NavType.IntType
            })
        ) {
            val arguments = requireNotNull(it.arguments)
            StatusBarBox {
                EquipMaterialDeatil(arguments.getInt(Navigation.EQUIP_ID))
            }
        }

        //角色 RANK 装备
        composable(
            "${Navigation.RANK_EQUIP}/{${Navigation.UNIT_ID}}",
            arguments = listOf(navArgument(Navigation.UNIT_ID) {
                type = NavType.IntType
            })
        ) {
            val arguments = requireNotNull(it.arguments)
            StatusBarBox {
                RankEquipList(
                    unitId = arguments.getInt(Navigation.UNIT_ID),
                    toEquipDetail = actions.toEquipDetail,
                    navViewModel = viewModel
                )
            }
        }

        //角色 RANK 对比
        composable(
            "${Navigation.RANK_COMPARE}/{${Navigation.UNIT_ID}}/{${Navigation.MAX_RANK}}/{${Navigation.LEVEL}}/{${Navigation.RARITY}}/{${Navigation.UNIQUE_EQUIP_LEVEL}}",
            arguments = listOf(navArgument(Navigation.UNIT_ID) {
                type = NavType.IntType
            }, navArgument(Navigation.MAX_RANK) {
                type = NavType.IntType
            }, navArgument(Navigation.LEVEL) {
                type = NavType.IntType
            }, navArgument(Navigation.RARITY) {
                type = NavType.IntType
            }, navArgument(Navigation.UNIQUE_EQUIP_LEVEL) {
                type = NavType.IntType
            })
        ) {
            val arguments = requireNotNull(it.arguments)
            StatusBarBox {
                RankCompare(
                    unitId = arguments.getInt(Navigation.UNIT_ID),
                    maxRank = arguments.getInt(Navigation.MAX_RANK),
                    level = arguments.getInt(Navigation.LEVEL),
                    rarity = arguments.getInt(Navigation.RARITY),
                    uniqueEquipLevel = arguments.getInt(Navigation.UNIQUE_EQUIP_LEVEL),
                    navViewModel = viewModel
                )
            }
        }

        //角色装备统计
        composable(
            "${Navigation.EQUIP_COUNT}/{${Navigation.UNIT_ID}}/{${Navigation.MAX_RANK}}",
            arguments = listOf(navArgument(Navigation.UNIT_ID) {
                type = NavType.IntType
            }, navArgument(Navigation.MAX_RANK) {
                type = NavType.IntType
            })
        ) {
            val arguments = requireNotNull(it.arguments)
            StatusBarBox {
                RankEquipCount(
                    unitId = arguments.getInt(Navigation.UNIT_ID),
                    maxRank = arguments.getInt(Navigation.MAX_RANK),
                    actions.toEquipMaterail,
                    navViewModel = viewModel
                )
            }
        }

        //角色排行
        composable(Navigation.TOOL_LEADER) {
            viewModel.fabMainIcon.postValue(MainIconType.BACK)
            val scrollState = rememberLazyListState()
            StatusBarBox {
                LeaderboardList(scrollState)
            }
        }

        //角色卡池
        composable(Navigation.TOOL_GACHA) {
            viewModel.fabMainIcon.postValue(MainIconType.BACK)
            val scrollState = rememberLazyListState()
            StatusBarBox {
                GachaList(scrollState, actions.toCharacterDetail)
            }
        }

        //剧情活动
        composable(Navigation.TOOL_EVENT) {
            viewModel.fabMainIcon.postValue(MainIconType.BACK)
            val scrollState = rememberLazyListState()
            StatusBarBox {
                EventList(scrollState, actions.toCharacterDetail)
            }
        }

        //角色公会
        composable(Navigation.TOOL_GUILD) {
            viewModel.fabMainIcon.postValue(MainIconType.BACK)
            val scrollState = rememberLazyListState()
            StatusBarBox {
                GuildList(scrollState, actions.toCharacterDetail)
            }
        }

        //团队战
        composable(Navigation.TOOL_CLAN) {
            val scrollState = rememberLazyListState()
            StatusBarBox {
                viewModel.fabMainIcon.postValue(MainIconType.BACK)
                ClanBattleList(scrollState, actions.toClanBossInfo)
            }

        }

        //团队战详情
        composable(
            "${Navigation.TOOL_CLAN_BOSS_INFO}/{${Navigation.TOOL_CLAN_BOSS_ID}}/{${Navigation.TOOL_CLAN_BOSS_INDEX}}",
            arguments = listOf(navArgument(Navigation.TOOL_CLAN_BOSS_ID) {
                type = NavType.IntType
            }, navArgument(Navigation.TOOL_CLAN_BOSS_INDEX) {
                type = NavType.IntType
            })
        ) {
            val arguments = requireNotNull(it.arguments)
            StatusBarBox {
                ClanBossInfoPager(
                    arguments.getInt(Navigation.TOOL_CLAN_BOSS_ID),
                    arguments.getInt(Navigation.TOOL_CLAN_BOSS_INDEX)
                )
            }
        }

        //日历活动
        composable(Navigation.TOOL_CALENDAR) {
            viewModel.fabMainIcon.postValue(MainIconType.BACK)
            val scrollState = rememberLazyListState()
            StatusBarBox {
                CalendarCompose(scrollState)
            }
        }

        //竞技场查询
        composable(Navigation.TOOL_PVP) {
            StatusBarBox {
                val scrollState = rememberLazyListState()
                PvpSearchCompose(
                    scrollState, toFavorite = actions.toPvpFavorite,
                )
            }
        }

        //竞技场收藏
        composable(Navigation.TOOL_PVP_FAVORITE) {
            viewModel.fabMainIcon.postValue(MainIconType.BACK)
            val scrollState = rememberLazyListState()
            StatusBarBox {
                PvpFavorites(scrollState, actions.toCharacterDetail, actions.toPvpResearch)
            }
        }

        //设置页面
        composable(Navigation.MAIN_SETTINGS) {
            viewModel.fabMainIcon.postValue(MainIconType.BACK)
            StatusBarBox {
                MainSettings()
            }
        }

        //更新通知
        composable(Navigation.APP_NOTICE) {
            viewModel.fabMainIcon.postValue(MainIconType.BACK)
            val scrollState = rememberLazyListState()
            StatusBarBox {
                NoticeList(scrollState)
            }
        }

        //公告
        composable(
            "${Navigation.TOOL_NEWS}/{${Navigation.TOOL_NEWS_REGION}}",
            arguments = listOf(
                navArgument(Navigation.TOOL_NEWS_REGION) {
                    type = NavType.IntType
                },
            )
        ) {
            val arguments = requireNotNull(it.arguments)
            viewModel.fabMainIcon.postValue(MainIconType.BACK)

            val scrollState = rememberLazyListState()
            val region = arguments.getInt(Navigation.TOOL_NEWS_REGION)

            StatusBarBox {
                NewsList(
                    scrollState,
                    region,
                    actions.toNewsDetail
                )
            }
        }

        //公告详情
        composable(
            "${Navigation.TOOL_NEWS_DETAIL}/{${Navigation.TOOL_NEWS_TITLE}}/{${Navigation.TOOL_NEWS_REGION}}/{${Navigation.TOOL_NEWS_URL}}/{${Navigation.TOOL_NEWS_DATE}}",
            arguments = listOf(
                navArgument(Navigation.TOOL_NEWS_TITLE) {
                    type = NavType.StringType
                },
                navArgument(Navigation.TOOL_NEWS_URL) {
                    type = NavType.StringType
                },
                navArgument(Navigation.TOOL_NEWS_REGION) {
                    type = NavType.IntType
                },
                navArgument(Navigation.TOOL_NEWS_DATE) {
                    type = NavType.StringType
                },
            )
        ) {
            viewModel.fabMainIcon.postValue(MainIconType.BACK)
            val arguments = requireNotNull(it.arguments)
            StatusBarBox {
                NewsDetail(
                    arguments.getString(Navigation.TOOL_NEWS_TITLE) ?: "",
                    arguments.getString(Navigation.TOOL_NEWS_URL) ?: "",
                    arguments.getInt(Navigation.TOOL_NEWS_REGION),
                    arguments.getString(Navigation.TOOL_NEWS_DATE) ?: "",
                )
            }
        }
    }
}

/**
 * 导航
 */
class NavActions(navController: NavHostController) {
    /**
     * 角色详情
     */
    val toCharacterDetail: (Int) -> Unit = { unitId: Int ->
        navController.navigate("${Navigation.CHARACTER_DETAIL}/${unitId}")
    }

    /**
     * 装备详情
     */
    val toEquipDetail: (Int) -> Unit = { equipId: Int ->
        navController.navigate("${Navigation.EQUIP_DETAIL}/${equipId}")
    }

    /**
     * 装备素材详情
     */
    val toEquipMaterail: (Int) -> Unit = { equipId: Int ->
        navController.navigate("${Navigation.EQUIP_MATERIAL}/${equipId}")
    }

    /**
     * 角色资料
     */
    val toCharacterBasicInfo: (Int) -> Unit = { unitId: Int ->
        navController.navigate("${Navigation.CHARACTER_BASIC_INFO}/${unitId}")
    }

    /**
     * 角色图片
     */
    val toCharacterPic: (Int) -> Unit = { unitId: Int ->
        navController.navigate("${Navigation.PICS}/${unitId}")
    }

    /**
     * 角色 RANK 装备
     */
    val toCharacteRankEquip: (Int) -> Unit = { unitId: Int ->
        navController.navigate("${Navigation.RANK_EQUIP}/${unitId}")
    }

    /**
     * 角色 RANK 对比
     */
    val toCharacteRankCompare: (Int, Int, Int, Int, Int) -> Unit =
        { unitId: Int, maxRank: Int, level: Int, rarity: Int, uniqueEquipLevel: Int ->
            navController.navigate("${Navigation.RANK_COMPARE}/${unitId}/${maxRank}/${level}/${rarity}/${uniqueEquipLevel}")
        }

    /**
     * 角装备统计
     */
    val toCharacteEquipCount: (Int, Int) -> Unit =
        { unitId: Int, maxRank: Int ->
            navController.navigate("${Navigation.EQUIP_COUNT}/${unitId}/${maxRank}")
        }


    /**
     * 团队战 BOSS
     */
    val toClanBossInfo: (Int, Int) -> Unit = { clanId: Int, index: Int ->
        navController.navigate("${Navigation.TOOL_CLAN_BOSS_INFO}/${clanId}/${index}")
    }

    /**
     * 官方公告详情
     */
    val toNewsDetail: (String, String, Int, String) -> Unit =
        { title: String, url: String, region: Int, date: String ->
            navController.navigate("${Navigation.TOOL_NEWS_DETAIL}/${title}/${region}/${url}/${date}")
        }

    /**
     * 竞技场重新查询
     */
    val toPvpResearch = {
        navController.navigateUp()
    }

    /**
     * 竞技场收藏
     */
    val toPvpFavorite = {
        navController.navigate(Navigation.TOOL_PVP_FAVORITE)
    }
}

/**
 * 导航 ViewModel
 */
@HiltViewModel
class NavViewModel @Inject constructor() : ViewModel() {

    /**
     * fab 图标显示
     */
    val fabMainIcon = MutableLiveData(MainIconType.MAIN)

    /**
     * 确认
     */
    val fabOKCilck = MutableLiveData(false)

    /**
     * 关闭
     */
    val fabCloseClick = MutableLiveData(false)

    /**
     * 选择的 RANK
     */
    val selectRank = MutableLiveData(0)


    /**
     * 下载状态
     * -2: 隐藏
     * -1: 显示加载中
     * >0: 进度
     */
    val downloadProgress = MutableLiveData(-2)

    /**
     * 加载中
     */
    val loading = MutableLiveData(false)

    /**
     * 已六星的角色ID
     */
    val r6Ids = MutableLiveData(listOf<Int>())

    /**
     * 重置
     */
    val resetClick = MutableLiveData(false)

    /**
     * 角色筛选
     */
    var filterCharacter = MutableLiveData(FilterCharacter())

    /**
     * 装备筛选
     */
    var filterEquip = MutableLiveData(FilterEquipment())

    /**
     * 竞技场查询角色
     */
    val selectedPvpData = MutableLiveData<List<PvpCharacterData>>()
    val selectedIds = MutableLiveData<String>()

    var curRank = MutableLiveData(0)
    var targetRank = MutableLiveData(0)
}
