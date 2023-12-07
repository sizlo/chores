package com.timsummertonbrier.chores.database

import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.context.event.StartupEvent
import io.micronaut.data.connection.jdbc.advice.DelegatingDataSource
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton
import org.jetbrains.exposed.sql.Database
import javax.sql.DataSource

/**
 * When trying to inject a DataSource Micronaut gives us a proxy to an
 * actual datasource. We have to jump through some hoops to pass the
 * correct object to our Database
 */
@Singleton
class ExposedConfig(
    private val datasource: DataSource,
    private val applicationEventPublisher: ApplicationEventPublisher<DatabaseReadyEvent>
) {

    @EventListener
    @Suppress("UNUSED_PARAMETER")
    fun connectToDatabase(event: StartupEvent) {
        Database.connect(
            (datasource as DelegatingDataSource).targetDataSource
        )
        applicationEventPublisher.publishEvent(DatabaseReadyEvent())
    }
}

class DatabaseReadyEvent