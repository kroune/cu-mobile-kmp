package io.github.kroune.cumobile.baseline

/**
 * UI anchor identifiers shared between the app (where they're applied via
 * `Modifier.testTag`) and the baseline-profile generator (where they're
 * queried via UiAutomator `By.res(...)` thanks to `testTagsAsResourceId`).
 *
 * These strings form the tiny contract the generator needs to exercise happy
 * paths without knowing about concrete Compose APIs. Changing UI layout is
 * free; renaming an anchor means regenerating the baseline profile.
 */
object BaselineTestTags {
    const val TAB_HOME = "bp_tab_home"
    const val TAB_TASKS = "bp_tab_tasks"
    const val TAB_COURSES = "bp_tab_courses"
    const val TAB_FILES = "bp_tab_files"

    const val LOGIN_SWITCH_BFF = "bp_login_switch_bff"
    const val LOGIN_BFF_COOKIE_FIELD = "bp_login_bff_cookie_field"
    const val LOGIN_BFF_COOKIE_SUBMIT = "bp_login_bff_cookie_submit"

    const val FIRST_COURSE_CARD = "bp_first_course_card"
    const val FIRST_TASK_CARD = "bp_first_task_card"
}
