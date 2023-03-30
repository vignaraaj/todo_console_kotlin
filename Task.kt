import java.time.LocalDate

data class Task(val taskId:Int, val taskListId:Int, var title:String, var description:String,var priority:Int, var startDate: LocalDate,
                var endDate: LocalDate, var category:Category, val period:RecurringTasks,
                val collaborator:MutableList<Collaborator>, var isCompleted:Int){
    override fun toString(): String {
        return taskId.toString()
    }
}