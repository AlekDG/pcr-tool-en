package cn.wthee.pcrtool.ui

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.paging.ExperimentalPagingApi
import cn.wthee.pcrtool.MyApplication
import cn.wthee.pcrtool.data.enums.MainIconType
import cn.wthee.pcrtool.database.DatabaseUpdater
import cn.wthee.pcrtool.ui.MainActivity.Companion.navViewModel
import cn.wthee.pcrtool.ui.MainActivity.Companion.r6Ids
import cn.wthee.pcrtool.ui.compose.DownloadCompose
import cn.wthee.pcrtool.ui.compose.FabCompose
import cn.wthee.pcrtool.ui.compose.FadeAnimation
import cn.wthee.pcrtool.ui.theme.Dimen
import cn.wthee.pcrtool.ui.theme.PcrtoolcomposeTheme
import cn.wthee.pcrtool.utils.*
import cn.wthee.pcrtool.viewmodel.NoticeViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

fun Context.mainSP() = MyApplication.context.getSharedPreferences("main", Context.MODE_PRIVATE)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        lateinit var handler: Handler
        lateinit var navViewModel: NavViewModel
        var r6Ids = listOf<Int>()
    }

    @ExperimentalPagingApi
    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    @ExperimentalPagerApi
    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PcrtoolcomposeTheme {
                FadeAnimation {
                    Home()
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
        ActivityHelper.instance.currentActivity = this
        //设置 handler
        setHandler()
        UMengInitializer().create(this)
    }

    //返回拦截
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            navViewModel.loading.postValue(false)
            when (navViewModel.fabMainIcon.value ?: MainIconType.MAIN) {
                MainIconType.MAIN -> {
                    return super.onKeyDown(keyCode, event)
                }
                MainIconType.DOWN -> {
                    navViewModel.fabMainIcon.postValue(MainIconType.MAIN)
                    return true
                }
                else -> {
                    navViewModel.fabMainIcon.postValue(MainIconType.BACK)
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    //刷新页面
    @SuppressLint("RestrictedApi")
    private fun setHandler() {
        //接收消息
        handler = Handler(Looper.getMainLooper(), Handler.Callback {
            viewModelStore.clear()
            recreate()
            when (it.what) {
                //正常更新
                -1, 0 -> {
                    ToastUtil.short(Constants.NOTICE_TOAST_SUCCESS)
                }
                //数据切换
                1 -> {
                    ToastUtil.short(Constants.NOTICE_TOAST_CHANGE_SUCCESS)
                }
            }
            return@Callback true
        })
    }
}

@ExperimentalPagingApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Composable
fun Home() {
    val navController = rememberNavController()
    val actions = remember(navController) { NavActions(navController) }
    navViewModel = hiltNavGraphViewModel()
    val noticeViewModel: NoticeViewModel = hiltNavGraphViewModel()
    val scope = rememberCoroutineScope()
    val loading = navViewModel.loading.observeAsState().value ?: false
    val r6IdList = navViewModel.r6Ids.observeAsState()
    if (r6IdList.value != null) {
        r6Ids = r6IdList.value!!
    }


    Box(modifier = Modifier.fillMaxSize()) {
        //数据库版本检查
        scope.launch {
            DatabaseUpdater.checkDBVersion()
            noticeViewModel.check()
        }
        NavGraph(navController, navViewModel, actions)
        //菜单
        MenuContent(navViewModel, navController)
        Column(modifier = Modifier.align(Alignment.BottomEnd)) {
            DownloadCompose(navViewModel)
            FabMain(
                navController,
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(Dimen.fabMargin)
            )
        }
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(Dimen.fabIconSize)
                    .align(Alignment.Center)
            )
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun FabMain(navController: NavHostController, modifier: Modifier) {
    val icon = navViewModel.fabMainIcon.observeAsState().value ?: MainIconType.MAIN

    FabCompose(icon, modifier = modifier) {
        when (icon) {
            MainIconType.OK -> navViewModel.fabOK.postValue(true)
            MainIconType.CLOSE -> navViewModel.fabClose.postValue(true)
            MainIconType.MAIN -> navViewModel.fabMainIcon.postValue(MainIconType.DOWN)
            MainIconType.DOWN -> navViewModel.fabMainIcon.postValue(MainIconType.MAIN)
            else -> {
                navController.navigateUp()
                navViewModel.loading.postValue(false)
            }
        }
    }
}

