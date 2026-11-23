package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppLanguage
import com.example.model.CommissioningState
import com.example.model.ControlLayout
import com.example.model.EngineVariant
import com.example.model.GameUiState
import com.example.model.GraphicsQuality
import com.example.model.MissionId
import com.example.model.TooltipGuideState
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "jcb_4cx_save_v1")

class GamePersistence(private val context: Context) {
    private val keySaveJson = stringPreferencesKey("save_json")

    suspend fun save(state: GameUiState) {
        val obj = JSONObject().apply {
            put("language", state.language.name)
            put("soundEnabled", state.soundEnabled)
            put("graphicsQuality", state.graphicsQuality.name)
            put("controlLayout", state.controlLayout.name)
            put("currentLevel", state.currentLevel)
            put("xp", state.xp)
            put("sandboxDrivingUnlocked", state.sandboxDrivingUnlocked)
            put("installedParts", JSONArray(state.installedParts.toList()))
            put("completedMissions", JSONArray(state.completedMissions.map { it.name }))
            put("unlockedRewards", JSONArray(state.unlockedRewards.toList()))
            put("commissioning", JSONObject().apply {
                val c = state.commissioning
                put("engineOilFilled", c.engineOilFilled)
                put("hydraulicOilFilled", c.hydraulicOilFilled)
                put("coolantAdded", c.coolantAdded)
                put("dieselAdded", c.dieselAdded)
                put("batteryChecked", c.batteryChecked)
                put("engineStarted", c.engineStarted)
                put("warningLightsChecked", c.warningLightsChecked)
                put("steeringTested", c.steeringTested)
                put("gearsTested", c.gearsTested)
                put("loaderTested", c.loaderTested)
                put("excavatorTested", c.excavatorTested)
                put("stabilizersTested", c.stabilizersTested)
                put("finalInspectionDone", c.finalInspectionDone)
            })
            put("micro", JSONObject().apply {
                val m = state.micro
                put("selectedVariant", m.selectedVariant.name)
                put("installedMicroParts", JSONArray(m.installedMicroParts.toList()))
                put("crankSpinVerified", m.crankSpinVerified)
                put("bearingClearanceVerified", m.bearingClearanceVerified)
                put("customManualSpecs", JSONObject(m.customManualSpecs))
            })
            put("tooltipGuide", JSONObject().apply {
                val t = state.tooltipGuide
                put("autoContextualEnabled", t.autoContextualEnabled)
                put("workshopIntroShown", t.workshopIntroShown)
                put("microIntroShown", t.microIntroShown)
            })
        }
        context.dataStore.edit { prefs ->
            prefs[keySaveJson] = obj.toString()
        }
    }

    suspend fun loadInto(base: GameUiState): GameUiState {
        val prefs = context.dataStore.data.first()
        val raw = prefs[keySaveJson] ?: return base
        return try {
            val obj = JSONObject(raw)
            val lang = runCatching { AppLanguage.valueOf(obj.optString("language", "EN")) }.getOrDefault(AppLanguage.EN)
            val gfx = runCatching { GraphicsQuality.valueOf(obj.optString("graphicsQuality", "MEDIUM")) }.getOrDefault(GraphicsQuality.MEDIUM)
            val ctrl = runCatching { ControlLayout.valueOf(obj.optString("controlLayout", "JOYSTICK")) }.getOrDefault(ControlLayout.JOYSTICK)

            val partsArr = obj.optJSONArray("installedParts") ?: JSONArray()
            val parts = buildSet {
                for (i in 0 until partsArr.length()) add(partsArr.getString(i))
            }

            val missionsArr = obj.optJSONArray("completedMissions") ?: JSONArray()
            val missions = buildSet {
                for (i in 0 until missionsArr.length()) {
                    runCatching { MissionId.valueOf(missionsArr.getString(i)) }.getOrNull()?.let { add(it) }
                }
            }

            val rewardsArr = obj.optJSONArray("unlockedRewards") ?: JSONArray()
            val rewards = buildSet {
                for (i in 0 until rewardsArr.length()) add(rewardsArr.getString(i))
            }

            val cObj = obj.optJSONObject("commissioning")
            val comm = if (cObj != null) {
                CommissioningState(
                    engineOilFilled = cObj.optBoolean("engineOilFilled"),
                    hydraulicOilFilled = cObj.optBoolean("hydraulicOilFilled"),
                    coolantAdded = cObj.optBoolean("coolantAdded"),
                    dieselAdded = cObj.optBoolean("dieselAdded"),
                    batteryChecked = cObj.optBoolean("batteryChecked"),
                    engineStarted = cObj.optBoolean("engineStarted"),
                    warningLightsChecked = cObj.optBoolean("warningLightsChecked"),
                    steeringTested = cObj.optBoolean("steeringTested"),
                    gearsTested = cObj.optBoolean("gearsTested"),
                    loaderTested = cObj.optBoolean("loaderTested"),
                    excavatorTested = cObj.optBoolean("excavatorTested"),
                    stabilizersTested = cObj.optBoolean("stabilizersTested"),
                    finalInspectionDone = cObj.optBoolean("finalInspectionDone")
                )
            } else base.commissioning

            val mObj = obj.optJSONObject("micro")
            val microState = if (mObj != null) {
                val variant = runCatching { EngineVariant.valueOf(mObj.optString("selectedVariant", "JCB_444_DIESELMAX")) }
                    .getOrDefault(EngineVariant.JCB_444_DIESELMAX)
                val mPartsArr = mObj.optJSONArray("installedMicroParts") ?: JSONArray()
                val mParts = buildSet {
                    for (i in 0 until mPartsArr.length()) add(mPartsArr.getString(i))
                }
                val specsObj = mObj.optJSONObject("customManualSpecs")
                val customSpecs = buildMap {
                    if (specsObj != null) {
                        val keys = specsObj.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            put(k, specsObj.optString(k))
                        }
                    }
                }
                base.micro.copy(
                    selectedVariant = variant,
                    installedMicroParts = mParts,
                    crankSpinVerified = mObj.optBoolean("crankSpinVerified"),
                    bearingClearanceVerified = mObj.optBoolean("bearingClearanceVerified"),
                    customManualSpecs = customSpecs
                )
            } else base.micro

            val tObj = obj.optJSONObject("tooltipGuide")
            val tooltipGuide = if (tObj != null) {
                TooltipGuideState(
                    autoContextualEnabled = tObj.optBoolean("autoContextualEnabled", true),
                    workshopIntroShown = tObj.optBoolean("workshopIntroShown", parts.isNotEmpty()),
                    microIntroShown = tObj.optBoolean("microIntroShown", microState.installedMicroParts.isNotEmpty())
                )
            } else {
                TooltipGuideState(
                    autoContextualEnabled = true,
                    workshopIntroShown = parts.isNotEmpty(),
                    microIntroShown = microState.installedMicroParts.isNotEmpty()
                )
            }

            base.copy(
                language = lang,
                soundEnabled = obj.optBoolean("soundEnabled", true),
                graphicsQuality = gfx,
                controlLayout = ctrl,
                currentLevel = obj.optInt("currentLevel", 1).coerceIn(1, 10),
                xp = obj.optInt("xp", 0),
                sandboxDrivingUnlocked = obj.optBoolean("sandboxDrivingUnlocked", false),
                installedParts = parts,
                completedMissions = missions,
                unlockedRewards = rewards,
                commissioning = comm,
                micro = microState,
                tooltipGuide = tooltipGuide
            )
        } catch (_: Throwable) {
            base
        }
    }
}
