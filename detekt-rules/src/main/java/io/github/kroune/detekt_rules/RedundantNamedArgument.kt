package io.github.kroune.detekt_rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.singleFunctionCallOrNull
import org.jetbrains.kotlin.analysis.api.resolution.symbol
import org.jetbrains.kotlin.psi.KtCallExpression

class RedundantNamedArgument(config: Config) : Rule(
    config,
    "RedundantNamedArgument",
),
    RequiresAnalysisApi {
    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val arguments = expression.valueArgumentList?.arguments ?: return
        if (arguments.size != 1) return

        val arg = arguments.single()
        if (!arg.isNamed()) return

        val argName = arg.getArgumentName()?.asName?.asString() ?: return
        val argExpr = arg.getArgumentExpression() ?: return
        if (argExpr.text != argName) return

        val isDefinitelyMultiParam = analyze(expression) {
            val symbol = expression.resolveToCall()
                ?.singleFunctionCallOrNull()
                ?.symbol
            symbol != null && symbol.valueParameters.size != 1
        }

        if (!isDefinitelyMultiParam) {
            report(
                Finding(
                    Entity.from(arg),
                    "Redundant named argument: '$argName = $argName'. Use just '$argName'.",
                ),
            )
        }
    }
}
