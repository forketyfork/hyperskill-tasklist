package tasklist

import kotlinx.datetime.*

data class Task(
    val lines: List<String>,
    val priority: TaskPriority,
    val deadlineDate: String,
    val deadlineTime: String
) {
    val dueTag: DueTag
        get() = Clock.System.now()
            .toLocalDateTime(TimeZone.of("UTC+0"))
            .date
            .daysUntil(deadlineDate.toLocalDate())
            .let {
                when {
                    it < 0 -> DueTag.O
                    it == 0 -> DueTag.T
                    else -> DueTag.I
                }
            }

}
