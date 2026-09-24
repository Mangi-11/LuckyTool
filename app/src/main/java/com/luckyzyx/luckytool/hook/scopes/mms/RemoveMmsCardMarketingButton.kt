package com.luckyzyx.luckytool.hook.scopes.mms

import androidx.collection.arrayMapOf
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.luckyzyx.luckytool.utils.safeOfNull
import org.json.JSONArray
import org.json.JSONObject
import org.lsposed.lsparanoid.Obfuscate

@Obfuscate
object RemoveMmsCardMarketingButton : YukiBaseHooker() {

    const val ENTITIES: String = "entities"
    const val ACTIONS: String = "actions"
    const val ACTION: String = "action"
    const val MSG_ID: String = "msgId"
    const val DATE: String = "date"

    val SKIP_ACTION = arrayMapOf(
        3 to "openUrlByWebView",
        4 to "openUrlByWebView",
        6 to "openApp",
        12 to "openUrlByWebView",
        23 to "CMCCAction"
    )

    override fun onHook() {
        //Source JSONObject
        JSONObject::class.resolve().apply {
            firstConstructor { parameters(String::class) }.hook {
                after {
                    val js = instance<JSONObject>()
                    safeOfNull {  formatJson(js) }
                }
            }
        }
    }

    fun formatJson(jsonObject: JSONObject) {
        if (!jsonObject.has(ENTITIES)) {
            return
        }
        if (!jsonObject.has(MSG_ID)) {
            return
        }
        if (!jsonObject.has(DATE)) {
            return
        }
        val entitiesArray = jsonObject.optJSONArray(ENTITIES) ?: JSONArray()
        if (entitiesArray.length() > 0) {
            for (i in 0 until entitiesArray.length()) {
                val entity = entitiesArray.getJSONObject(i)
                if (!entity.has(ACTIONS)) {
                    continue
                }
                val actionsArray = entity.optJSONArray(ACTIONS) ?: JSONArray()
                if (actionsArray.length() > 0) {
                    val mockActions = JSONArray()
                    for (j in 0 until actionsArray.length()) {
                        val action = actionsArray.optJSONObject(j) ?: continue
                        val buttonText = action.optString("buttonText")
                        val type = action.optInt(ACTION, -1)
                        if (SKIP_ACTION.keys.contains(type)) {
//                            try {
//                                throw Throwable()
//                            } catch (t: Throwable) {
//                                YLog.debug("$type | $buttonText", t)
//                            }
                            continue
                        }
                        mockActions.put(action)
                    }
                    entity.put(ACTIONS, mockActions)
                }
            }
        }
    }
}