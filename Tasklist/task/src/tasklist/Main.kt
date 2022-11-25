package tasklist

fun main() {

    val taskList = TaskList("tasklist.json")

    while (true) {
        println("Input an action (add, print, edit, delete, end):")

        when (readln().trim().lowercase()) {
            "add" -> taskList.addTask()
            "print" -> taskList.printTasks()
            "end" -> break
            "delete" -> taskList.deleteTask()
            "edit" -> taskList.editTask()
            else -> println("The input action is invalid")
        }
    }

    taskList.save()
    println("Tasklist exiting!")
}
