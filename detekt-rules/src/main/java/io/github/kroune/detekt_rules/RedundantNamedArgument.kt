package io.github.kroune.detekt_rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtCallExpression

class RedundantNamedArgument(config: Config) : Rule(
    config,
    "RedundantNamedArgument",
) {
    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val valueArgumentList = expression.valueArgumentList ?: return
        for (arg in valueArgumentList.arguments) {
            if (!arg.isNamed()) continue

            val argName = arg.getArgumentName()?.asName?.asString() ?: continue
            val argExpr = arg.getArgumentExpression() ?: continue
            if (argExpr.text == argName) {
                report(
                    Finding(
                        Entity.from(arg),
                        "Redundant named argument: '$argName = $argName'. Use just '$argName'.",
                    ),
                )
            }
        }
    }
}
