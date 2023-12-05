package com.timsummertonbrier.chores

import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpRequest
import io.micronaut.views.ModelAndView
import io.micronaut.views.model.ViewModelProcessor
import jakarta.inject.Singleton

@Singleton
class GlobalModelAttributes (
    @Value("\${app.version}") private val appVersion: String,
    @Value("\${app.environment}") private val appEnvironment: String,
    @Value("\${datasources.default.url}") private val jdbcUrl: String,
) : ViewModelProcessor<Map<String, Any>> {

    override fun process(request: HttpRequest<*>, modelAndView: ModelAndView<Map<String, Any>>) {
        val databaseEnvironment = when {
            jdbcUrl.contains("localhost") -> "local"
            jdbcUrl.contains("_dev") -> "dev"
            else -> "prod"
        }

        modelAndView.addToModel(mapOf(
            "buildInfo" to "$appVersion - framework: micronaut - running on: $appEnvironment - database: $databaseEnvironment"
        ))
    }

    private fun ModelAndView<Map<String, Any>>.addToModel(attributes: Map<String, Any>) {
        val enrichedModel = this.model
            .map { it.toMutableMap() }
            .orElse(mutableMapOf())
            .also { it.putAll(attributes) }
        this.setModel(enrichedModel)
    }
}