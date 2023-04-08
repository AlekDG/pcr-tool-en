package cn.wthee.pcrtool.viewmodel

import androidx.lifecycle.ViewModel
import cn.wthee.pcrtool.data.db.repository.ExtraEquipmentRepository
import cn.wthee.pcrtool.data.model.FilterExtraEquipment
import cn.wthee.pcrtool.utils.LogReportUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * ex装备 ViewModel
 *
 * @param equipmentRepository
 */
@HiltViewModel
class ExtraEquipmentViewModel @Inject constructor(
    private val equipmentRepository: ExtraEquipmentRepository
) : ViewModel() {

    /**
     * 获取装备列表
     *
     * @param params 装备筛选
     */
    fun getExtraEquips(params: FilterExtraEquipment) = flow {
        try {
            val data = equipmentRepository.getEquipments(params, Int.MAX_VALUE)
            emit(data)
        } catch (e: Exception) {
            LogReportUtil.upload(e, "getExtraEquips#params:$params")
        }
    }

    /**
     * 获取装备信息
     *
     * @param equipId 装备编号
     */
    fun getExtraEquip(equipId: Int) = flow {
        try {
            emit(equipmentRepository.getEquipmentData(equipId))
        } catch (e: Exception) {
            LogReportUtil.upload(e, "getExtraEquip#equipId:$equipId")
        }
    }

    /**
     * 获取装备颜色种类数
     */
    fun getExtraEquipColorNum() = flow {
        emit(equipmentRepository.getEquipColorNum())
    }

    /**
     * 获取装备类别
     */
    fun getExtraEquipCategoryList() = flow {
        emit(equipmentRepository.getEquipCategoryList())
    }

    /**
     * 获取可使用装备的角色列表
     */
    fun getExtraEquipUnitList(category: Int) = flow {
        emit(equipmentRepository.getEquipUnitList(category))
    }

    /**
     * 装备掉落信息
     */
    fun getExtraDropQuestList(equipId: Int) = flow {
        emit(equipmentRepository.getDropQuestList(equipId))
    }

    /**
     * 次要掉落信息
     */
    fun getSubRewardList(questId: Int) = flow {
        emit(equipmentRepository.getSubRewardList(questId))
    }

    /**
     * 冒险区域详情
     */
    fun getTravelQuest(questId: Int) = flow {
        emit(equipmentRepository.getTravelQuest(questId))
    }

    /**
     * ex冒险区域
     */
    fun getTravelAreaList() = flow {
        emit(equipmentRepository.getTravelAreaList())
    }

    /**
     * 获取角色可使用的ex装备列表
     */
    fun getCharacterExtraEquipList(unitId: Int) = flow {
        emit(equipmentRepository.getCharacterExtraEquipList(unitId))
    }

    /**
     * 获取装所有备技能id
     *
     */
    fun getAllEquipSkillIdList() = flow {
        try {
            val data = equipmentRepository.getAllEquipSkillIdList()
            emit(data)
        } catch (_: Exception) {

        }
    }
}