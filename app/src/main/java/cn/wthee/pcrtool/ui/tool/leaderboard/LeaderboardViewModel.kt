package cn.wthee.pcrtool.ui.tool.leaderboard

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.wthee.pcrtool.data.db.repository.UnitRepository
import cn.wthee.pcrtool.data.enums.LeaderboardSortType
import cn.wthee.pcrtool.data.enums.TalentType
import cn.wthee.pcrtool.data.model.FilterLeaderboard
import cn.wthee.pcrtool.data.model.LeaderboardData
import cn.wthee.pcrtool.data.model.ResponseData
import cn.wthee.pcrtool.data.network.ApiRepository
import cn.wthee.pcrtool.ui.LoadState
import cn.wthee.pcrtool.utils.days
import cn.wthee.pcrtool.utils.getToday
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * 页面状态：角色排行
 */
@Immutable
data class LeaderboardUiState(
    val leaderboardResponseData: ResponseData<List<LeaderboardData>>? = null,
    val currentList: List<LeaderboardData> = emptyList(),
    //筛选
    val filterLeader: FilterLeaderboard = FilterLeaderboard(),
    val loadState: LoadState = LoadState.Loading,

    //天赋筛选相关
    val talentType: TalentType = TalentType.ALL,
    val talentUnitMap: HashMap<Int, ArrayList<Int>> = hashMapOf(),
    val openTalentDialog: Boolean = false,
)

/**
 * 角色排行 ViewModel
 *
 * @param apiRepository
 */
@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val apiRepository: ApiRepository,
    private val unitRepository: UnitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    private val day = 30


    init {
        getTalentUnitMap()
        initLeader()
    }

    /**
     * 初始加载排行
     */
    private fun initLeader() {
        viewModelScope.launch {
            val responseData = apiRepository.getLeader()
            _uiState.update {
                it.copy(
                    leaderboardResponseData = responseData,
                    currentList = sortLeaderboardList(
                        FilterLeaderboard(),
                        responseData.data
                    ) ?: emptyList(),
                    loadState = it.loadState.isSuccess(responseData.data != null)
                )
            }
        }
    }

    /**
     * 获取角色基本信息
     *
     * @param unitId 角色编号
     */
    fun getCharacterBasicInfo(unitId: Int) = flow {
        emit(unitRepository.getCharacterBasicInfo(unitId))
    }

    /**
     * 获取排行
     */
    fun refreshLeader(filter: FilterLeaderboard) {
        viewModelScope.launch {
            var data = _uiState.value.leaderboardResponseData?.data
            data = sortLeaderboardList(filter, data)

            //天赋类型
            val talentTypeValue = _uiState.value.talentType.type
            val unitIdList = _uiState.value.talentUnitMap[talentTypeValue] ?: arrayListOf()

            data?.let {
                _uiState.update {
                    it.copy(
                        currentList = if (unitIdList.isNotEmpty()) {
                            data.filter { leader ->
                                unitIdList.contains(leader.unitId)
                            }
                        } else {
                            data
                        }
                    )
                }
            }
        }
    }

    /**
     * 切换天赋类型
     */
    fun changeTalentSelect(type: Int) {
        _uiState.update {
            it.copy(
                talentType = TalentType.getByType(type)
            )
        }
    }

    /**
     * 弹窗状态更新
     */
    fun changeTalentDialog(openDialog: Boolean) {
        _uiState.update {
            it.copy(
                openTalentDialog = openDialog
            )
        }
    }

    /**
     * 获取角色id按天赋分组
     */
    private fun getTalentUnitMap() {
        viewModelScope.launch {
            val map = unitRepository.getTalentUnitMap()
            _uiState.update {
                it.copy(
                    talentUnitMap = map
                )
            }
        }
    }

    private fun sortLeaderboardList(
        filter: FilterLeaderboard,
        list: List<LeaderboardData>?
    ): List<LeaderboardData>? {
        //筛选最近编辑的角色
        var data = list
        if (filter.onlyLast) {
            val today = getToday()
            data = data?.filter {
                it.updateTime == null || today.days(it.updateTime, showDay = false)
                    .toInt() <= day
            }
        }

        //排序，降低露娜塔、主线的权重
        val towerWeight = 0.5
        val questWeight = 0.5
        data = data?.sortedWith { o1, o2 ->
            (if (filter.asc) 1 else -1) * when (filter.sort) {
                LeaderboardSortType.MAIN_QUEST.type -> {
                    //主线
                    val quest = (o1.questScore).compareTo(o2.questScore)
                    if (quest == 0) {
                        (o1.towerScore * towerWeight + o1.pvpScore + o1.clanScore).compareTo(o2.towerScore * towerWeight + o2.pvpScore + o2.clanScore)
                    } else {
                        quest
                    }
                }

                LeaderboardSortType.TOWER.type -> {
                    //露娜塔
                    val tower = (o1.towerScore).compareTo(o2.towerScore)
                    if (tower == 0) {
                        (o1.questScore * questWeight + o1.pvpScore + o1.clanScore).compareTo(o2.questScore * questWeight + o2.pvpScore + o2.clanScore)
                    } else {
                        tower
                    }
                }

                LeaderboardSortType.PVP.type -> {
                    //pvp
                    val pvp = (o1.pvpScore).compareTo(o2.pvpScore)
                    if (pvp == 0) {
                        (o1.questScore * questWeight + o1.towerScore * towerWeight + o1.clanScore).compareTo(
                            o2.questScore * questWeight + o2.towerScore * towerWeight + o2.clanScore
                        )
                    } else {
                        pvp
                    }
                }

                LeaderboardSortType.CLAN_BATTLE.type -> {
                    //公会战
                    val clan = (o1.clanScore).compareTo(o2.clanScore)
                    if (clan == 0) {
                        (o1.questScore * questWeight + o1.towerScore * towerWeight + o1.pvpScore).compareTo(
                            o2.questScore * questWeight + o2.towerScore * towerWeight + o2.pvpScore
                        )
                    } else {
                        clan
                    }
                }

                else -> {
                    //综合
                    val all =
                        (o1.questScore * questWeight + o1.towerScore * towerWeight + o1.pvpScore + o1.clanScore).compareTo(
                            o2.questScore * questWeight + o2.towerScore * towerWeight + o2.pvpScore + o2.clanScore
                        )
                    //综合分数相等时
                    if (all == 0) {
                        val sub =
                            (o1.towerScore * towerWeight + o1.pvpScore + o1.clanScore).compareTo(o1.towerScore * towerWeight + o2.pvpScore + o2.clanScore)
                        //露娜塔、pvp、公会战总分相同时
                        if (sub == 0) {
                            (o1.pvpScore + o1.clanScore).compareTo(o2.pvpScore + o2.clanScore)
                        } else {
                            sub
                        }
                    } else {
                        all
                    }
                }
            }
        }
        return data
    }

}
