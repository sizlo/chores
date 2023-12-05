package com.timsummertonbrier.chores.domain

import io.micronaut.serde.annotation.Serdeable

interface Trigger {
    val triggerType: TriggerType
}

@Serdeable
data class FixedDelayTrigger(val daysBetween: Int) : Trigger {
    override val triggerType = TriggerType.FIXED_DELAY
}

@Serdeable
data class WeeklyTrigger(val dayOfWeek: Int) : Trigger {
    override val triggerType = TriggerType.WEEKLY
}

@Serdeable
data class MonthlyTrigger(val dayOfMonth: Int) : Trigger {
    override val triggerType = TriggerType.MONTHLY
}

@Serdeable
data class YearlyTrigger(val monthOfYear: Int, val dayOfMonth: Int) : Trigger {
    override val triggerType = TriggerType.YEARLY
}

@Serdeable
data class OneOffTrigger(val dummyProperty: String = "data classes must have at least one property") : Trigger {
    override val triggerType = TriggerType.ONE_OFF
}

@Serdeable
data class TriggerRequest(
    val triggerType: TriggerType? = null,
    val daysBetween: Int? = null,
    val dayOfWeek: Int? = null,
    val dayOfMonth: Int? = null,
    val monthOfYear: Int? = null,
)

enum class TriggerType {
    FIXED_DELAY,
    WEEKLY,
    MONTHLY,
    YEARLY,
    ONE_OFF,
}