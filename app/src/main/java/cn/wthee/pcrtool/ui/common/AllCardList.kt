package cn.wthee.pcrtool.ui.common

import android.Manifest
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import cn.wthee.pcrtool.R
import cn.wthee.pcrtool.data.enums.AllPicsType
import cn.wthee.pcrtool.ui.theme.Dimen
import cn.wthee.pcrtool.utils.*
import cn.wthee.pcrtool.viewmodel.AllPicsViewModel

//权限
val permissions = arrayOf(
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
)

//缓存
val loadedPicMap = hashMapOf<String, Drawable?>()


/**
 * 角色所有卡面/剧情故事图片
 */
@Composable
fun AllCardList(
    id: Int,
    allPicsType: AllPicsType,
    picsViewModel: AllPicsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    //角色卡面
    val basicUrls = if (allPicsType == AllPicsType.CHARACTER) {
        picsViewModel.getUniCardList(id).collectAsState(initial = arrayListOf()).value
    } else {
        arrayListOf()
    }
    //剧情活动
    val storyUrls =
        picsViewModel.getStoryList(id, allPicsType.type).collectAsState(initial = null).value

    val checkedPicUrl = remember {
        mutableStateOf("")
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (allPicsType == AllPicsType.CHARACTER) {
                Row(
                    modifier = Modifier
                        .padding(
                            top = Dimen.largePadding,
                            start = Dimen.largePadding,
                            end = Dimen.largePadding
                        )
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MainTitleText(text = "基本")
                    Spacer(modifier = Modifier.weight(1f))
                    MainText(text = basicUrls.size.toString())
                }
                CardGridList(
                    checkedPicUrl = checkedPicUrl,
                    urls = basicUrls
                )
            }
            Row(
                modifier = Modifier
                    .padding(
                        top = Dimen.largePadding,
                        start = Dimen.largePadding,
                        end = Dimen.largePadding
                    )
                    .fillMaxWidth(),
            ) {
                MainTitleText(
                    text = "剧情"
                )
                Spacer(modifier = Modifier.weight(1f))
                if (storyUrls == null) {
                    CircularProgressCompose()
                } else {
                    MainText(text = storyUrls.size.toString())
                }
            }
            if (storyUrls != null) {
                if (storyUrls.isEmpty()) {
                    MainText(
                        text = "暂无剧情信息",
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(
                                Dimen.largePadding
                            )
                    )
                } else {
                    CardGridList(
                        checkedPicUrl = checkedPicUrl,
                        urls = storyUrls
                    )
                }
            }
            CommonSpacer()
        }
    }

    //下载确认
    if (checkedPicUrl.value != "") {
        AlertDialog(
            title = {
                MainText(text = stringResource(R.string.ask_save_image))
            },
            modifier = Modifier.padding(start = Dimen.mediumPadding, end = Dimen.mediumPadding),
            onDismissRequest = {
                checkedPicUrl.value = ""
            },
            containerColor = MaterialTheme.colorScheme.background,
            shape = MaterialTheme.shapes.medium,
            confirmButton = {
                //确认下载
                MainButton(text = stringResource(R.string.save_image)) {
                    loadedPicMap[checkedPicUrl.value]?.let {
                        FileSaveHelper(context).saveBitmap(
                            bitmap = (it as BitmapDrawable).bitmap,
                            displayName = "${getFileName(checkedPicUrl.value)}.jpg"
                        )
                        checkedPicUrl.value = ""
                    }
                }
            },
            dismissButton = {
                //取消
                SubButton(
                    text = stringResource(id = R.string.cancel)
                ) {
                    checkedPicUrl.value = ""
                }
            })
    }
}


@Composable
private fun CardGridList(
    checkedPicUrl: MutableState<String>,
    urls: ArrayList<String>
) {
    val context = LocalContext.current
    val unLoadToast = stringResource(id = R.string.wait_pic_load)
    val spanCount = ScreenUtil.getWidth() / getItemWidth().value.dp2px

    VerticalGrid(spanCount = spanCount) {
        urls.forEach { picUrl ->
            MainCard(
                modifier = Modifier
                    .padding(Dimen.largePadding),
                onClick = {
                    //下载
                    val loaded = loadedPicMap[picUrl] != null
                    if (loaded) {
                        //权限校验
                        checkPermissions(context, permissions) {
                            checkedPicUrl.value = picUrl
                        }
                    } else {
                        ToastUtil.short(unLoadToast)
                    }
                }
            ) {
                //图片
                StoryImageCompose(
                    data = picUrl
                ) {
                    loadedPicMap[picUrl] = it.result.drawable
                }
            }
        }
    }
}

/**
 * 获取文件名
 */
private fun getFileName(url: String): String {
    return try {
        url.split('/').last().split('.')[0]
    } catch (e: Exception) {
        System.currentTimeMillis().toString()
    }
}