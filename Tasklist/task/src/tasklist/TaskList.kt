package tasklist

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.datetime.LocalDate
import java.io.File
import java.time.LocalTime
import kotlin.math.max

class TaskList(taskFileName: String) {

    private val columns = listOf(
        Column(4, "N"),
        Column(12, "Date"),
        Column(7, "Time"),
        Column(3, "P"),
        Column(3, "D"),
        Column(44, "Task  "),
    )

    private val separatingLine: String = columns.joinToString("+", "+", "+") { "-".repeat(it.width) }

    private val tasks = mutableListOf<Task>()

    private val taskListAdapter = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        .adapter<List<Task?>>(Types.newParameterizedType(List::class.java, Task::class.java))

    private val taskFile = File(taskFileName)

    init {
        if (taskFile.exists()) {
            tasks.addAll((taskListAdapter.fromJson(taskFile.readText()) ?: emptyList()).filterNotNull())
        }
    }

    fun addTask() {

        val priority = readTaskPriority()
        val deadlineDate = readDeadlineDate()
        val deadlineTime = readDeadlineTime()
        val taskLines = readTaskLines()

        if (taskLines.isEmpty()) {
            println("The task is blank")
        } else {
            tasks.add(Task(taskLines, priority, deadlineDate, deadlineTime))
        }
    }

    fun printTasks() {
        if (tasks.isEmpty()) {
            println("No tasks have been input")
        } else {
            buildList {
                add(separatingLine)
                add(columns.joinToString("|", "|", "|") {
                    cellFormat(it.title, it.width)
                })
                add(separatingLine)
                tasks.forEachIndexed { index, task ->
                    val taskLines = task.lines.flatMap { it.chunked(columns.last().width) }
                        .map { cellFormat(it, columns.last().width, center = false) + "|" }
                    add(
                        listOf(
                            index + 1,
                            task.deadlineDate,
                            task.deadlineTime,
                            task.priority.ansiCode,
                            task.dueTag.ansiCode
                        ).zip(columns).joinToString("|", "|", "|") {
                            cellFormat(it.first.toString(), it.second.width)
                        } + taskLines[0]
                    )
                    addAll(
                        taskLines.drop(1).map { line ->
                            columns.dropLast(1).joinToString("|", "|", "|") { cellFormat("", it.width) } + line
                        }
                    )
                    add(separatingLine)
                }
            }.forEach(::println)
        }
    }

    private fun cellFormat(value: String, width: Int, center: Boolean = true): String {
        val spaceStart = if (center) max((width - value.length) / 2, 1) else 0
        val spaceEnd = max(width - spaceStart - value.length, if (center) 1 else 0)
        return " ".repeat(spaceStart) + value + " ".repeat(spaceEnd)
    }

    fun editTask() {
        printTasks()
        if (tasks.isNotEmpty()) {
            val taskNumber = readTaskNumber()
            val taskField = readTaskField()
            val task = tasks[taskNumber]
            tasks[taskNumber] = when (taskField) {
                TaskField.PRIORITY -> task.copy(priority = readTaskPriority())
                TaskField.DATE -> task.copy(deadlineDate = readDeadlineDate())
                TaskField.TIME -> task.copy(deadlineTime = readDeadlineTime())
                TaskField.TASK -> task.copy(lines = readTaskLines())
            }
            println("The task is changed")
        }
    }

    fun deleteTask() {
        printTasks()
        if (tasks.isNotEmpty()) {
            val taskNumber = readTaskNumber()
            tasks.removeAt(taskNumber)
            println("The task is deleted")
        }
    }

    fun save() {
        taskFile.writeText(taskListAdapter.toJson(tasks))
    }

    private fun readTaskPriority(): TaskPriority =
        read("Input the task priority (C, H, N, L):", null) { TaskPriority.valueOf(readln().uppercase()) }

    private fun readDeadlineDate(): String = read("Input the date (yyyy-mm-dd):", "The input date is invalid") {
        val dateParts = readln().split("-")
        require(dateParts.size == 3)
        LocalDate(dateParts[0].toInt(), dateParts[1].toInt(), dateParts[2].toInt()).toString()
    }

    private fun readDeadlineTime(): String = read("Input the time (hh:mm):", "The input time is invalid") {
        val timeParts = readln().split(":")
        require(timeParts.size == 2)
        LocalTime.of(timeParts[0].toInt(), timeParts[1].toInt()).toString()
    }

    private fun readTaskLines(): List<String> {
        println("Input a new task (enter a blank line to end):")

        return generateSequence { readln() }
            .map { it.trim() }
            .takeWhile { it != "" }
            .toList()
    }

    private fun readTaskNumber(): Int = read("Input the task number (1-${tasks.size}):", "Invalid task number") {
        readln().toInt().also { require(it in 1..tasks.size) } - 1
    }

    private fun readTaskField(): TaskField =
        read("Input a field to edit (priority, date, time, task):", "Invalid field") {
            TaskField.valueOf(readln().uppercase())
        }

    private fun <T> read(prompt: String, errorMessage: String?, block: () -> T): T {
        while (true) {
            println(prompt)
            try {
                return block()
            } catch (e: Exception) {
                errorMessage?.also { println(it) }
            }
        }
    }

}
