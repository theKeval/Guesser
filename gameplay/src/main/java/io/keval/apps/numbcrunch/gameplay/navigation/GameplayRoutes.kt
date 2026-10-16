package io.keval.apps.numbcrunch.gameplay.navigation

const val HOME_ROUTE = "home"
const val LAUNCH_GATE_ROUTE = "launch_gate"
const val TUTORIAL_ROUTE = "tutorial?firstRun={firstRun}"
const val MANUAL_TUTORIAL_ROUTE = "tutorial?firstRun=false"
const val FIRST_RUN_TUTORIAL_ROUTE = "tutorial?firstRun=true"
const val GAMEPLAY_INFO_ROUTE = "gameplay_info"

/**
 * The Gameplay guide is hidden for now: its Home button and in-game help button are not shown.
 * The screen and its route stay in place; set this to true to bring both entry points back.
 */
const val GAMEPLAY_GUIDE_ENABLED = false
const val ABOUT_ROUTE = "about"
const val PRIVACY_POLICY_ROUTE = "privacy_policy"
const val GAME_ROUTE = "game"
const val SETTINGS_ROUTE = "settings"
