import java.time.LocalDate
import kotlin.random.Random
import kotlinx.coroutines.*
fun getRandomInstance(): Random {
    return Random
}
class Manage {
    companion object {
        //private val data :Data = DataList
        private val data :Data = DataDB
        fun findChoice(input: Int): Entry? {
            return  Entry.values().singleOrNull{ x -> x.entry==input }
        }
        fun findViewTaskListOption(input: Int): ManageTaskList? {
            return ManageTaskList.values().singleOrNull { x -> x.manageTaskList == input }
        }
        fun findTaskListActionType(input :Int):TaskListActions?{
            return TaskListActions.values().singleOrNull { x -> x.taskListAction == input }
        }
        fun findCategoryType(input: Int): Category? {
            return Category.values().singleOrNull { x -> x.category == input }
        }
        fun findRecurringType(input: Int): RecurringTasks? {
            return RecurringTasks.values().singleOrNull { x -> x.period == input }
        }
        fun findViewType(input:Int):ViewTasks?{
            return ViewTasks.values().singleOrNull { x -> x.viewType == input }
        }
        fun findShareType(input:Int):Share?{
            return Share.values().singleOrNull { x -> x.share == input }
        }
        fun  findTaskActionType(input:Int):TaskActions?{
            return TaskActions.values().singleOrNull { x -> x.taskAction == input }
        }
        fun findEditOption(input:Int):Edit?{
            return Edit.values().singleOrNull { x -> x.editOption == input }
        }
        fun findEditTaskListsOption(input:Int):EditTaskLists?{
            return EditTaskLists.values().singleOrNull { x -> x.editTaskListOption == input }
        }
        fun findEditTasksOption(input:Int):EditTasks?{
            return EditTasks.values().singleOrNull { x -> x.editTasksOption == input }
        }
        fun getUserByEmail(email: String?): User? {
            return data.getUserByEmail(email)
        }
        fun signUp(name: String?, email: String?, password: String?): Boolean {
         return data.addUser(User(getRandomInstance().nextInt(999), name!!, email!!, password!!))
        }
        fun login(email: String?,password: String?):User?{
            if(data.getUserCredentials(email!!,password!!)){
                return data.getUserByEmail(email)
            }
            return null
        }
        fun displayUsers(): List<User?> {
            return data.getUsers()
        }
        fun addTaskList(user:User):Boolean {
            return data.addTaskList(TaskList(user.userId,getRandomInstance().nextInt(9999), arrayListOf(), arrayListOf()))
        }
        fun viewTaskList(user: User):ArrayList<TaskList?>{
            return data.getTaskListsByUserId(user.userId)
        }
        fun viewSharedTaskLists(user:User): MutableList<TaskList?> {
            return data.getSharedTaskListsByUserId(user.userId)
        }
        fun viewSharedTasks(user:User):MutableList<Task?>{
            return data.getSharedTasksByUserId(user.userId)
        }
        fun getUserNamesByUserId(userIds : ArrayList<Int>): ArrayList<String?> {
            return userIds.mapTo(ArrayList()) { x -> data.getUserName(x) }
        }
        fun getUserNameByUserId(userId :Int):String? {
            return data.getUserName(userId)
        }
        fun getPassword(userId: Int): String {
            return data.getPassword(userId).toString()
        }
        suspend fun closeAccount(user: User): Boolean {
            val result  = GlobalScope.async{
                val taskLists: List<TaskList?> = data.getTaskListsByUserId(user.userId)
                for (taskList in taskLists) {
                    for (task in taskList!!.tasks) data.deleteSharedTasksByTaskId(task.taskId)
                    data.deleteTasksByTaskListId(taskList.taskListId)
                    data.deleteSharedTaskListByTaskListId(taskList.taskListId)
                }
                data.deleteSharedTasksByUserId(user.userId)
                data.deleteSharedTaskListsByUserId(user.userId)
                data.deleteTaskListsByUserId(user.userId)
                data.deleteUser(user)
                true
            }
            return result.await()
        }
        fun getTaskListByTaskListId(taskListId: Int): TaskList? {
            return data.getTaskListByTaskListId(taskListId)
        }
        fun generateNewTasks(task: Task, recurringNumber: Int): Boolean {
            var recurringNumber = recurringNumber
            val period = task.period
            val days: Int = period.period
            data.addTask(task)
            while (recurringNumber != 0) {
                task.startDate = task.startDate.plusDays(days.toLong())
                task.endDate = task.endDate.plusDays(days.toLong())
                data.addTask(Task(getRandomInstance().nextInt(9999),task.taskListId,task.title,task.description,task.priority,task.startDate,task.endDate,task.category,period,task.collaborator, task.isCompleted))
                recurringNumber--
            }
            return true
        }
        suspend fun deleteTaskList(taskList:TaskList):Boolean{
            val result : Deferred<Boolean> = GlobalScope.async {
                for (task in taskList.tasks) data.deleteSharedTasksByTaskId(task.taskId)
                data.deleteTasksByTaskListId(taskList.taskListId)
                data.deleteSharedTaskListByTaskListId(taskList.taskListId)
                data.deleteTaskListByTaskListId(taskList.taskListId)
                true
            }
            return result.await()
        }
        fun viewTaskOnPriority(tasks: MutableList<Task>): MutableList<Task?> {
            return tasks.sortedByDescending { it.priority } as MutableList<Task?>
        }
        fun viewCurrentTasks(tasks: List<Task>): MutableList<Task?> {
            val currentDate = LocalDate.now()
            return tasks.filter{ x -> (x.startDate.minusDays(1).isBefore(currentDate) && x.endDate.plusDays(1).isAfter(currentDate))}.toMutableList()
        }
        fun viewTasksByCategory(tasks: List<Task>, category: Category): MutableList<Task?> {
            return tasks.filter{ x -> x.category==category}.toMutableList()
        }
        fun getCollaboratorForTaskList(taskListId: Int, userId: Int): Collaborator? {
                return data.getCollaboratorForTaskList(taskListId,userId)
        }
        fun getCollaboratorForTask(taskId: Int, userId: Int): Collaborator? {
            return data.getCollaboratorForTask(taskId,userId)
        }
        fun shareTaskList(taskListId: Int, userId: Int, canEdit: Int): Boolean {
            return data.addSharedTaskList(Collaborator(taskListId, userId, canEdit))
        }
        fun removeShareTaskList(taskListId: Int, userId: Int): Boolean {
            return data.deleteSharedTaskList(taskListId, userId)
        }
        fun shareTask(taskId: Int, userId: Int, canEdit: Int): Boolean {
            return data.addSharedTask(Collaborator(taskId, userId, canEdit))
        }
        fun removeShareTask(taskId: Int, userId: Int): Boolean {
            return data.deleteSharedTask(taskId, userId)
        }
        fun viewTasks(taskListId : Int): MutableList<Task> {
            return data.getTaskListByTaskListId(taskListId)?.tasks ?: arrayListOf()
        }
        fun getTaskByTaskId(taskId :Int):Task?{
            return data.getTaskByTaskId(taskId)
        }
        fun deleteTask(taskId:Int):Boolean{
            return data.deleteTaskByTaskId(taskId)
        }
        fun editTask(task: Task, title: String, description: String, priority: Int, category: Category, startDate: LocalDate, endDate: LocalDate, isCompleted: Int): Boolean {
            return data.updateTask(task.taskId, title, description, priority, category, startDate, endDate, isCompleted)
        }
        fun viewSharedTaskList(userId:Int): MutableList<TaskList?> {
            return data.getSharedTaskListIdOfUser(userId).map{ x -> getTaskListByTaskListId(x) }.toMutableList()
        }
        fun  viewSharedTask(userId:Int):MutableList<Task?>{
            return data.getSharedTaskIdOfUser(userId).map{ x -> getTaskByTaskId(x) }.toMutableList()
        }
        fun getSharedTaskListIdOfUser(userId: Int): List<Int?> {
            return data.getSharedTaskListIdOfUser(userId)
        }
    }

}
