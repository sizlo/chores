package com.timsummertonbrier.chores.database

import io.micronaut.aop.Around
import io.micronaut.aop.InterceptorBean
import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import jakarta.inject.Singleton
import org.jetbrains.exposed.sql.transactions.transaction

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Around
annotation class ExposedTransactional

@Singleton
@InterceptorBean(ExposedTransactional::class)
class ExposedTransactionalInterceptor : MethodInterceptor<Any, Any> {
    override fun intercept(context: MethodInvocationContext<Any, Any>): Any? {
        return transaction {
            context.proceed()
        }
    }
}