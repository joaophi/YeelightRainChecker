package me.pedro.yeelight.yeelight

enum class OnCFFinish(val action: Int) {
    REVERT_STATE(action = 0),
    KEEP_STATE(action = 1),
    POWER_OFF(action = 2);
}