package io.github.kroune.detekt_rules

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.RequiresAnalysisApi
import dev.detekt.api.Rule
import org.jetbrains.kotlin.analysis.api.analyze
import org.jetbrains.kotlin.analysis.api.resolution.singleFunctionCallOrNull
import org.jetbrains.kotlin.psi.KtCallExpression

class RedundantNamedArgument(config: Config) : Rule(
    config,
    "RedundantNamedArgument",
    url = null,
), RequiresAnalysisApi {

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)
        val valueArgumentList = expression.valueArgumentList ?: return
        analyze(expression) {
            expression.resolveToCall()?.singleFunctionCallOrNull() ?: return@analyze
            for (arg in valueArgumentList.arguments) {
                if (!arg.isNamed()) continue

                val argName = arg.getArgumentName()?.asName?.asString() ?: continue
                val argExpr = arg.getArgumentExpression() ?: continue
                if (argExpr.text == argName) {
                    report(
                        Finding(
                            Entity.from(arg),
                            "Избыточный именованный аргумент: '$argName = $argName'. Оставьте просто '$argName'.",
                        ),
                    )
                }
            }
        }
    }
}
