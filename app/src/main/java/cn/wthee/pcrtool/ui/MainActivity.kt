package cn.wthee.pcrtool.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavHostController
import androidx.work.WorkManager
import cn.wthee.pcrtool.MyApplication.Companion.context
import cn.wthee.pcrtool.R
import cn.wthee.pcrtool.data.enums.MainIconType
import cn.wthee.pcrtool.data.enums.RegionType
import cn.wthee.pcrtool.data.preferences.SettingPreferencesKeys
import cn.wthee.pcrtool.database.*
import cn.wthee.pcrtool.navigation.NavViewModel
import cn.wthee.pcrtool.ui.tool.pvp.PvpFloatService
import cn.wthee.pcrtool.utils.*
import cn.wthee.pcrtool.viewmodel.NoticeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * 本地存储：收藏信息
 */
private const val MAIN_PREFERENCES_NAME = "main"
val Context.dataStoreMain: DataStore<Preferences> by preferencesDataStore(
    name = MAIN_PREFERENCES_NAME,
    produceMigrations = {
        listOf(SharedPreferencesMigration(context, MAIN_PREFERENCES_NAME))
    })

/**
 * 本地存储：版本、设置信息
 */
private const val SETTING_PREFERENCES_NAME = "setting"
val Context.dataStoreSetting: DataStore<Preferences> by preferencesDataStore(
    name = SETTING_PREFERENCES_NAME,
    produceMigrations = {
        listOf(SharedPreferencesMigration(context, SETTING_PREFERENCES_NAME))
    })


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val noticeViewModel: NoticeViewModel by viewModels()

    companion object {
        lateinit var handler: Handler

        lateinit var navViewModel: NavViewModel

        @OptIn(ExperimentalMaterialApi::class)
        lateinit var navSheetState: ModalBottomSheetState

        @SuppressLint("StaticFieldLeak")
        lateinit var navController: NavHostController

        var vibrateOnFlag = true
        var animOnFlag = true
        var dynamicColorOnFlag = true
        var r6Ids = listOf<Int>()
        var regionType = RegionType.CN
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        //加载开屏页面
        installSplashScreen()
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        //用户设置信息
        runBlocking {
            val preferences = dataStoreSetting.data.first()
            vibrateOnFlag = preferences[SettingPreferencesKeys.SP_VIBRATE_STATE] ?: true
            animOnFlag = preferences[SettingPreferencesKeys.SP_ANIM_STATE] ?: true
            dynamicColorOnFlag = preferences[SettingPreferencesKeys.SP_COLOR_STATE] ?: true
            regionType = RegionType.getByValue(
                preferences[SettingPreferencesKeys.SP_DATABASE_TYPE] ?: RegionType.CN.value
            )
        }
        ActivityHelper.instance.currentActivity = this
        //设置 handler
        setHandler()

        setContent {
            PCRToolApp()
        }
    }

    override fun onResume() {
        super.onResume()
        //校验数据库版本
        MainScope().launch {
            DatabaseUpdater.checkDBVersion()
        }
        //更新通知
        noticeViewModel.check()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(context, PvpFloatService::class.java))
        WorkManager.getInstance(context).cancelAllWork()
        val notificationManager: NotificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
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

                MainIconType.CLOSE -> {
                    navViewModel.fabCloseClick.postValue(true)
                    return true
                }

                MainIconType.OK -> {
                    navViewModel.fabOKClick.postValue(true)
                    return true
                }

                else -> {
                    navViewModel.fabMainIcon.postValue(MainIconType.BACK)
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    /**
     * 刷新页面
     * what：0切换数据，1动态色彩，2、3、4数据更新
     */
    @SuppressLint("RestrictedApi")
    private fun setHandler() {
        //接收消息
        handler = Handler(Looper.getMainLooper(), Handler.Callback {
            //结束应用
            if (it.what == 404) {
                val pid: Int = Process.myPid()
                Process.killProcess(pid)
            }
            try {
                //关闭其他数据库连接
                AppBasicDatabase.close()
                //重启应用
                val intent = Intent(this, MainActivity::class.java)
                intent.action = Intent.ACTION_MAIN
                finish()
                startActivity(intent)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    overrideActivityTransition(
                        Activity.OVERRIDE_TRANSITION_CLOSE,
                        android.R.anim.fade_in,
                        android.R.anim.fade_out
                    )
                } else {
                    @Suppress("DEPRECATION")
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
                //数据下载完成，振动提示
                if (it.what > 1) {
                    VibrateUtil(this).done()
                }
            } catch (e: Exception) {
                LogReportUtil.upload(e, Constants.EXCEPTION_DATA_CHANGE)
                ToastUtil.short(getString(R.string.change_failed))
            }
            return@Callback true
        })
    }
}

