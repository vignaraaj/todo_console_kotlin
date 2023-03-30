import java.time.LocalDate
interface Data {
    fun addUser(user:User): Boolean
    fun addTaskList(taskList : TaskList):Boolean
    fun addTask(task:Task):Boolean
    fun addSharedTask(collaborator: Collaborator):Boolean
    fun addSharedTaskList(collaborator: Collaborator):Boolean
    fun deleteUser(user:User):Boolean
    fun deleteTaskListByTaskListId(taskListId:Int):Boolean
    fun deleteTaskByTaskId(taskId:Int):Boolean
    fun deleteTasksByTaskListId(taskListId:Int):Boolean
    fun deleteSharedTaskListByTaskListId(taskListId : Int):Boolean
    fun deleteSharedTaskListsByUserId(userId :Int):Boolean
    fun deleteSharedTasksByUserId(userId : Int):Boolean
    fun deleteSharedTasksByTaskId(taskId : Int):Boolean
    fun deleteSharedTaskList(taskListId:Int,userId:Int):Boolean
    fun deleteSharedTask(taskId:Int,userId:Int):Boolean
    fun deleteTaskListsByUserId(userId:Int):Boolean
    fun getUserByEmail(email1 : String?):User?
    fun getUserName(userId : Int):String?
    fun getUsers(): ArrayList<User>
    fun getTaskListsByUserId(userId:Int):ArrayList<TaskList?>
    fun getSharedTaskListsByUserId(userId : Int):MutableList<TaskList?>
    fun getTaskListByTaskListId(taskListId : Int):TaskList?
    fun getSharedTasksByUserId(userId:Int): MutableList<Task?>
    fun getTaskByTaskId(taskId : Int):Task?
    fun getPassword(userId: Int): String?
    fun getCollaboratorForTaskList(taskListId:Int,userId:Int):Collaborator?
    fun getCollaboratorForTask(taskId:Int,userId:Int):Collaborator?
    fun getSharedTaskListIdOfUser(userId:Int):MutableList<Int>
    fun getSharedTaskIdOfUser(userId : Int):MutableList<Int>
    fun updateTask(taskId: Int, title: String, description: String, priority: Int, category: Category, startDate: LocalDate, endDate: LocalDate, isCompleted: Int): Boolean
    fun getUserCredentials(email:String,password:String): Boolean
}