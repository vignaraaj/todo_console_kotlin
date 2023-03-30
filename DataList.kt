import java.time.LocalDate
object DataList : Data{
    private val users = arrayListOf<User>()
    private val taskLists = arrayListOf<TaskList>()
    private val tasks = arrayListOf<Task>()
    private val sharedTasks = arrayListOf<Collaborator>()
    private val sharedTaskLists = arrayListOf<Collaborator>()
    override fun addUser(user:User): Boolean {
        return users.add(user)
    }
    override fun addTaskList(taskList : TaskList):Boolean{
        return taskLists.add(taskList)
    }
    override fun addTask(task:Task):Boolean{
        return tasks.add(task)
    }
    override fun addSharedTask(collaborator: Collaborator):Boolean{
        return sharedTasks.add(collaborator)
    }
    override fun addSharedTaskList(collaborator: Collaborator):Boolean{
        return sharedTaskLists.add(collaborator)
    }
    override fun deleteUser(user:User):Boolean{
        return users.remove(user)
    }
    override fun deleteTaskListByTaskListId(taskListId:Int):Boolean{
        return taskLists.removeIf { x -> x.taskListId==taskListId }
    }
    override fun deleteTaskByTaskId(taskId:Int):Boolean{
        return tasks.removeIf { x -> x.taskId==taskId }
    }
    override fun deleteTasksByTaskListId(taskListId:Int):Boolean{
        return tasks.removeIf { x ->  x.taskListId==taskListId }
    }
    override fun deleteSharedTaskListByTaskListId(taskListId : Int):Boolean{
        return sharedTaskLists.removeIf { x ->  x.id == taskListId  }
    }
    override fun deleteSharedTaskListsByUserId(userId :Int):Boolean{
        return sharedTaskLists.removeIf { x -> x.userId==userId }
    }
    override fun deleteSharedTasksByUserId(userId : Int):Boolean{
        return sharedTasks.removeIf { x ->  x.userId==userId }
    }
    override fun deleteSharedTasksByTaskId(taskId : Int):Boolean{
        return sharedTasks.removeIf{ x -> x.id==taskId }
    }
    override fun deleteSharedTaskList(taskListId:Int, userId:Int):Boolean{
        return sharedTaskLists.removeIf{ x -> (x.id==taskListId && x.userId==userId) }
    }
    override fun deleteSharedTask(taskId:Int, userId:Int):Boolean{
        return sharedTasks.removeIf { x ->  (x.id==taskId && x.userId==userId)}
    }
    override fun deleteTaskListsByUserId(userId:Int):Boolean{
        return taskLists.removeIf { x -> x.userId==userId  }
    }
    override fun getUserByEmail(email1 : String?):User?{
        return users.find {x -> x.email==email1}
    }
    override fun getUserName(userId : Int):String?{
        return users.mapNotNull { if(it.userId==userId ) it.userName else null}.singleOrNull()
    }
    override fun getUsers(): ArrayList<User>{
        return users
    }
    override fun getUserCredentials(email:String,password:String): Boolean{
        return !users.none { x -> x.email == email && x.password == password }
    }
    override fun getTaskListsByUserId(userId:Int):ArrayList<TaskList?>{
        return taskLists.filterTo(ArrayList()){ x -> x.userId==userId }
    }
    override fun getSharedTaskListsByUserId(userId : Int): MutableList<TaskList?> {
        return  sharedTaskLists.filter { x -> x.userId==userId}.map{ x -> getTaskListByTaskListId(x.id)}.toMutableList()
    }
    override fun getTaskListByTaskListId(taskListId : Int):TaskList?{
        return taskLists.find { x -> x.taskListId==taskListId }
    }
    override fun getSharedTasksByUserId(userId:Int):MutableList<Task?>{
        return sharedTasks.filter { x -> x.userId==userId }.map{ x -> getTaskByTaskId(x.id)}.toMutableList()
    }
    override fun getTaskByTaskId(taskId : Int):Task?{
        return tasks.find{ x -> x.taskId==taskId  }
    }
    override fun getPassword(userId: Int): String? {
        return users.filter { x -> x.userId==userId  }.map{ x -> x.password}.getOrNull(0)
    }
    override fun getCollaboratorForTaskList(taskListId:Int, userId:Int):Collaborator?{
        return sharedTaskLists.find { x -> (x.id == taskListId && x.userId==userId) }
    }
    override fun getCollaboratorForTask(taskId:Int, userId:Int):Collaborator?{
        return sharedTasks.find{ x -> (x.id==taskId && x.userId==userId) }
    }
    override fun getSharedTaskListIdOfUser(userId:Int):MutableList<Int>{
        return sharedTaskLists.filter { x -> x.userId==userId }.map{ x -> x.id}.toMutableList()
    }
    override fun getSharedTaskIdOfUser(userId : Int):MutableList<Int>{
        return sharedTasks.filter{ x -> x.userId==userId }.map{x->x.id}.toMutableList()
    }
    override fun updateTask(taskId: Int, title: String, description: String, priority: Int, category: Category, startDate: LocalDate, endDate: LocalDate, isCompleted: Int): Boolean {
        for (task in tasks) {
            if (task.taskId == taskId) {
                task.title = title
                task.description = description
                task.priority = priority
                task.isCompleted=isCompleted
                task.category = category
                task.startDate = startDate
                task.endDate = endDate
                return true
            }
        }
        return false
    }
}

