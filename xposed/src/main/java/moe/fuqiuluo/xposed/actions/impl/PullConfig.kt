package moe.fuqiuluo.xposed.actions.impl

import android.content.Context
import android.widget.Toast
import androidx.core.content.edit
import moe.fuqiuluo.xposed.actions.IAction
import moe.fuqiuluo.xposed.helper.DataRequester

import de.robv.android.xposed.XposedBridge.log
import moe.fuqiuluo.xposed.loader.ActionLoader
import mqq.app.MobileQQ
import kotlin.concurrent.thread
import kotlin.system.exitProcess

class PullConfig: IAction {
    companion object {
        @JvmStatic
        var isConfigOk = false
    }

    override fun invoke(ctx: Context) {
        if (MobileQQ.getMobileQQ().qqProcessName != "com.tencent.mobileqq") return

        DataRequester.request(ctx, "init", bodyBuilder = {}, onFailure = {
            GlobalUi.post {
                Toast.makeText(ctx, "请启动Shamrock主进程以初始化服务，进程将退出。", Toast.LENGTH_LONG).show()
            }
            thread {
                Thread.sleep(3000)
                exitProcess(1)
            }
        }) {
            isConfigOk = true
            log("接到APP返回")
            // do something

            val preferences = ctx.getSharedPreferences("shamrock_config", 0)
            preferences.edit {
                putBoolean(  "tablet",    it.getBooleanExtra("tablet", false)) // 强制平板模式
                putInt(      "port",      it.getIntExtra("port", 5700)) // 主动HTTP端口
                putBoolean(  "ws",        it.getBooleanExtra("ws", false)) // 主动WS开关
                putInt(      "ws_port",   it.getIntExtra("port", 5700)) // 主动WS端口
                putBoolean(  "http",      it.getBooleanExtra("http", false)) // HTTP回调开关
                putString(   "http_addr", it.getStringExtra("http_addr")) // WebHook回调地址
                putBoolean(  "ws_client", it.getBooleanExtra("ws_client", false)) // 被动WS开关
                putString(   "ws_addr",   it.getStringExtra("ws_addr")) // 被动WS地址
                putBoolean(  "pro_api",   it.getBooleanExtra("pro_api", false)) // 开发调试API开关
            }

            ActionLoader.runService(ctx)
        }
    }
}