package io.github.kroune.detekt_rules

import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class CustomRuleSetProvider : RuleSetProvider {
    override val ruleSetId = RuleSetId("custom-style")

    override fun instance(): RuleSet = RuleSet(
        ruleSetId,
        listOf(
            ::RedundantNamedArgument,
        ),
    )
}
