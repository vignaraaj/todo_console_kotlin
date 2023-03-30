import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.format.DateTimeParseException

fun main() {
        println("TODO APPLICATION")
        while (true) {
            println("1.Signup 2.login 3.Admin 4.Exit")
            val entry = Manage.findChoice(checkType()) ?: continue
            try {
                when (entry) {
                    Entry.SIGNUP -> {
                        print("Enter name : ")
                        val name: String = checkBlank()
                        print("Enter email : ")
                        val email: String = checkBlank()
                        print("Enter password : ")
                        val password: String = checkBlank()
                        val oldUser: User? = Manage.getUserByEmail(email)
                        Utility.userExists(oldUser)
                        if (Manage.signUp(name, email, password)) println("Account added successfully")
                        else println("error occurred while adding new account ")
                    }

                    Entry.LOGIN -> {
                        print("Enter emailId : ")
                        val email1: String = checkBlank()
                        print("Enter password : ")
                        val password1: String = checkBlank()
                        val user: User? = Manage.login(email1, password1)
                        checkNotNull(user)
                        //Utility.checkInvalidLoginConstraint(user)
                        manageTaskList(user)
                    }

                    Entry.DISPLAY_USERS -> {
                        print("Enter adminId : ")
                        val admin: String = checkBlank()
                        print("Enter password : ")
                        val pass: String = checkBlank()
                        Utility.loginAdmin(admin, pass)
                        val userDetails: List<User?> = Manage.displayUsers()
                        Utility.checkEmptyUsers(userDetails)
                        val format = "| %15s  | %20s  |%n"
                        println("--------------------------------------------")
                        System.out.format(format, " User Name ", " Email ")
                        println("--------------------------------------------")
                        for (user in userDetails) System.out.format(format, user?.userName ?: "-", user!!.email)
                        println("--------------------------------------------")
                    }

                    Entry.EXIT -> break
                }
            } catch (e: InvalidDataException) {
                println(e.message)
            } catch (e: AuthenticationException) {
                println(e.message)
            } catch (e: IllegalStateException) {
                println(e.message)
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

fun displayTaskList(taskLists: MutableList<TaskList?>):Boolean{
    if(taskLists.isNullOrEmpty()) { println("There are no taskLists here...")
        return false
    }
    val format = ("| %15s | %15s | %70s | %51s|%n")
    println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------")
    System.out.format(format, "TaskList Id", "UserName ", "Task Id", "Collaborators ")
    println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------")
    for (taskList in taskLists) {
        val  userId = ArrayList<Int>()

        for (collaborator in taskList!!.collaborators){
            userId.add(collaborator.userId)
        }
        val userName = Manage.getUserNamesByUserId(userId)
        System.out.format(format, taskList.taskListId, Manage.getUserNameByUserId(taskList.userId), taskList.tasks, userName)
    }
    println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------")
    return true
}
fun displayTasks(tasks: MutableList<Task?>): Boolean {
    if (tasks.isNullOrEmpty()) { println("There are no tasks here...")
        return false
    }
    val format = "| %6s | %8s | %20s | %8s | %10s | %6s | %10s | %10s | %7s | %45s |%n"
    println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------")
    System.out.format(format, "TaskId", "Title", "Description", "Priority", "Category", "status", "Start Date", "End Date", "Period", "Collaborators")
    println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------")
    for (task in tasks) {
        val userId: ArrayList<Int> = ArrayList()
        for (collaborator in task!!.collaborator) userId.add(collaborator.userId)
        val userName: ArrayList<String?> = Manage.getUserNamesByUserId(userId)
        val status: String = if (task.isCompleted == 1) "completed" else "-"
        System.out.format(format, task.taskId, task.title, task.description, task.priority, task.category, status, task.startDate, task.endDate, task.period, userName)
    }
    println("-----------------------------------------------------------------------------------------------------------------------------------------------------------------")
    return true
}
fun manageTaskList(user:User){
    while(true){
        println("1.Add taskList 2.view taskLists 3.view sharedTaskLists 4.view sharedTasks 5.closeAccount 6.Exit")
        val viewTaskList : ManageTaskList = Manage.findViewTaskListOption(checkType()) ?: continue
        if(viewTaskList == ManageTaskList.EXIT) break
        when(viewTaskList){
            ManageTaskList.ADD_TASK_LIST -> {
                GlobalScope.launch {
                    if (Manage.addTaskList(user)) println("New taskList has been added to your account")
                    else println("An error occurred while creating newTaskList, try again later")
                }
            }
            ManageTaskList.VIEW_TASK_LISTS -> taskListActions(user)
            ManageTaskList.VIEW_SHARED_TASK_LISTS ->
                while(true) {
                    if (displayTaskList(Manage.viewSharedTaskLists(user) as ArrayList<TaskList?>)) {
                        sharedTaskListActions(user)
                    }else break
                }
            ManageTaskList.VIEW_SHARED_TASKS ->
                while(true) {
                    if (displayTasks(Manage.viewSharedTasks(user))) {
                        sharedTasksActions(user)
                    }else break
                }
            ManageTaskList.CLOSE_ACCOUNT -> {
                print("confirm your password : ")
                val password: String = checkBlank()
                try {
                    val originalPassword: String = Manage.getPassword(user.userId)
                    Utility.checkPassWordConstraint(originalPassword, password)
                    runBlocking {
                        if (Manage.closeAccount(user)) {
                            println("Account closed successfully")
                            return@runBlocking
                        } else println("An error occurred while closing the account")
                    }
                } catch (e: InvalidDataException) {
                    println(e.message)
                }
                break
                }
            else -> break
        }
    }
}
fun taskListActions(user:User){
    while(true) {
        if (displayTaskList(Manage.viewTaskList(user))) {
            try {
                println("1.Add task 2.Delete taskList 3.Use taskList 4.view TaskList 5.Share taskList 6.Exit")
                val taskListActions: TaskListActions = Manage.findTaskListActionType(checkType()) ?: continue
                if (taskListActions === TaskListActions.EXIT) break
                print("Enter the taskListId to be operated : ")
                val taskListId: Int = checkType()
                val taskList: TaskList? = Manage.getTaskListByTaskListId(taskListId)
                checkNotNull(taskList)
                //Utility.validTaskListIdConstraint(taskList)
                Utility.checkTaskListBelongsToUser(taskList.userId, user.userId)
                when (taskListActions) {
                    TaskListActions.ADD_TASK -> {
                        print("Enter title : ")
                        val title: String = checkBlank()
                        print("Enter description : ")
                        val description: String = checkBlank()
                        var priority: Int
                        while (true) {
                            try {
                                print("Enter priority (1 to 10): ")
                                priority = checkType()
                                Utility.checkPriorityConstraint(priority)
                                break
                            } catch (e: InvalidDataException) {
                                println(e.message)
                            }
                        }
                        var category: Category?
                        while (true) {
                            try {
                                println("WORK(1),PERSONAL(2),LEARNING(3),SHOPPING(4),OTHER(5)")
                                print("Enter category type (integer only) : ")
                                category = Manage.findCategoryType(checkType())
                                //Utility.checkCategoryConstraint(category)
                                checkNotNull(category)
                                break
                            }catch (e:IllegalStateException){
                                println ("press valid integer to select category type")
                            }
                        }
                        var startDate: LocalDate
                        while (true) {
                            try {
                                print("Enter start date in (yyyy-mm-dd) format : ")
                                val startDate1: String = checkBlank()
                                startDate = Utility.checkStartDateConstraint(startDate1)
                                break
                            } catch (e: DateTimeParseException) {
                                println(e.message)
                            }
                        }
                        var endDate: LocalDate
                        while (true) {
                            try {
                                print("Enter end date in (yyyy-mm-dd) format (should be past the start date or start date itself): ")
                                val endDate1: String = checkBlank()
                                endDate = Utility.checkEndDateConstraint(startDate, endDate1)
                                break
                            } catch (e: DateTimeParseException) {
                                println(e.message)
                            }catch(e:InvalidDataException){
                                println(e.message)
                            }
                        }
                        var period: RecurringTasks?
                        while (true) {
                            try {
                                println("1.DAILY 2.WEEKLY,3.MONTHLY")
                                print("when the task to be recurred (valid integer only) : ")
                                period = Manage.findRecurringType(checkType())
                                //Utility.checkPeriodConstraint(period)
                                checkNotNull(period)
                                break
                            } catch (e: IllegalStateException) {
                                println(e.message)
                            }
                        }
                        var recurringNumber: Int
                        while (true) {
                            try {
                                print("Enter the number of times to be recurred [0 to 50]: ")
                                recurringNumber = checkType()
                                Utility.checkRecurringNumber(recurringNumber)
                                break
                            } catch (e: InvalidDataException) {
                                println(e.message)
                            }
                        }
                        val taskId: Int = getRandomInstance().nextInt(9999)
                        val task = Task(taskId, taskListId, title, description, priority,startDate, endDate,  category!!, period!!, ArrayList(), 0)
                        GlobalScope.launch {
                            if (Manage.generateNewTasks(task, recurringNumber)) println("Task added successfully")
                            else println("An error occurred while adding the task")
                        }
                    }
                    TaskListActions.DELETE_TASK_LIST -> {
                        runBlocking {
                            if (Manage.deleteTaskList(taskList))
                                println("TaskList deleted successfully")
                            else
                               println("An error occurred while deleting the taskList")
                        }
                    }
                    TaskListActions.USE_TASK_LIST -> useTask(user, taskListId)
                    TaskListActions.VIEW_TASK -> viewTask(taskList.tasks)
                    TaskListActions.SHARE_TASK_LIST -> shareTaskList(user.email, taskListId)
                    else -> break
                }
            } catch (e: AuthenticationException) {
                println(e.message)
            } catch (e: InvalidDataException) {
                println(e.message)
            }catch(e : IllegalStateException){
                println(e.message)
            }
        }else break
    }
}
fun sharedTaskListActions(user: User) {
    while (true) {
        val sharedTaskLists: MutableList<TaskList?> = Manage.viewSharedTaskList(user.userId)
        println("TaskLists shared to you")
        if (displayTaskList(sharedTaskLists)) {
            try {
                println("1.Edit shared taskList 2.Delete your copy of a shared taskList 3.view Tasks of shared TaskList 4.Exit")
                val editOption: EditTaskLists = Manage.findEditTaskListsOption(checkType()) ?: continue
                if (editOption == EditTaskLists.EXIT) break
                print("Enter taskListId to be operated : ")
                val taskListId: Int = checkType()
                val taskList : TaskList? = Manage.getTaskListByTaskListId(taskListId)
                checkNotNull(taskList)
                //Utility.validTaskListIdConstraint(taskList)
                val collaboratorForTaskList = Manage.getCollaboratorForTaskList(taskList.taskListId, user.userId)
                checkNotNull(collaboratorForTaskList)
                //Utility.isCollaboratorNull(collaboratorForTaskList)
                when (editOption) {
                    EditTaskLists.EDIT_SHARED_TASK_LIST -> {
                        Utility.canEditStatus(collaboratorForTaskList)
                        print("Enter taskId to be edited : ")
                        val taskId: Int = checkType()
                        val task = Manage.getTaskByTaskId(taskId)
                        //Utility.validTaskIdConstraint(task!!)
                        checkNotNull(task)
                        Utility.checkTaskListHasTask(task, taskListId)
                        val collaboratorForTask = Manage.getCollaboratorForTask(taskId, user.userId)
                        if (collaboratorForTask != null) Utility.canEditStatus(collaboratorForTask)
                        editTask(task)
                    }
                    EditTaskLists.DELETE_SHARED_TASK_LIST -> if (Manage.removeShareTaskList(taskList.taskListId, collaboratorForTaskList.userId)) println("shared copy of taskList deleted successfully")
                    EditTaskLists.VIEW_TASKS_OF_SHARED_TASK_LIST -> displayTasks(taskList.tasks.toMutableList())
                    else -> break
                }
            } catch (e: InvalidDataException) {
                println(e.message)
            } catch (e: AuthenticationException) {
                println(e.message)
            }catch(e : IllegalStateException){
                println(e.message)
            }
        } else break
    }
}
fun sharedTasksActions(user: User) {
    while (true) {
        val sharedTasks: MutableList<Task?> = Manage.viewSharedTask(user.userId)
        println("Tasks shared to you...")
        if (displayTasks(sharedTasks)) {
            try {
                println("1.Edit SharedTask 2.Delete SharedTask 3.Exit")
                val editOption: EditTasks = Manage.findEditTasksOption(checkType()) ?: continue
                if (editOption == EditTasks.EXIT) break
                print("Enter taskId to be operated : ")
                val taskId: Int = checkType()
                val task = Manage.getTaskByTaskId(taskId)
                //Utility.validTaskIdConstraint(task)
                checkNotNull(task)
                val collab = Manage.getCollaboratorForTask(taskId, user.userId)
                //Utility.isCollaboratorNull(collab)
                checkNotNull(collab)
                when (editOption) {
                    EditTasks.EDIT_SHARED_TASK -> {
                        Utility.canEditStatus(collab)
                        editTask(task)
                    }
                    EditTasks.DELETE_SHARED_TASK -> {
                        val collabTaskList = Manage.getCollaboratorForTaskList(task.taskListId, user.userId)
                        val sharedTaskListId: List<Int?> = Manage.getSharedTaskListIdOfUser(user.userId)
                        if (collabTaskList != null) Utility.doTaskListOfTaskShared(sharedTaskListId, task.taskListId, collab.canEdit, collabTaskList.canEdit)
                        if (Manage.removeShareTask(taskId, user.userId)) println("shared copy of task deleted successfully")
                    }
                    else -> break
                }
            } catch (e: InvalidDataException) {
                println(e.message)
            } catch (e: AuthenticationException) {
                println(e.message)
            }
        } else break
    }
}
fun useTask(user:User,taskListId:Int){
    while(true){
        val tasks : MutableList<Task?> = Manage.viewTasks(taskListId).toMutableList()
        if (displayTasks(tasks)) {
        try {
            println("1.delete task 2.edit task 3.share task 4.exit")
            val taskAction: TaskActions = Manage.findTaskActionType(checkType()) ?: continue
            if (taskAction == TaskActions.EXIT) break
            println("Enter the taskId to be operated with : ")
            val taskId: Int = checkType()
            val task: Task? = Manage.getTaskByTaskId(taskId)
            checkNotNull(task)
            //Utility.validTaskIdConstraint(task)
            Utility.checkTaskListHasTask(task, taskListId)
            when (taskAction) {
                TaskActions.DELETE_TASK -> {
                    if (Manage.deleteTask(task.taskId))
                        println("task deleted successfully in all traces")
                    else println("an error occurred in deleting the task")
                }
                TaskActions.EDIT_TASK   -> editTask(task)
                TaskActions.SHARE_TASK  -> shareTask(user.email,taskId)
                else -> break
            }
            }catch(e:IllegalStateException){
                println("Invalid taskId")
            }catch(e:InvalidDataException){
                println(e.message)
            }catch(e:AuthenticationException){
                e.message
            }
        } else break

    }
}
fun editTask(task:Task){
    while (true) {
        println("1.Edit title 2.Edit description 3.Edit priority 4.Edit category 5.edit date 6.Edit completed status 7.Exit")
        val edit: Edit = Manage.findEditOption(checkType()) ?: continue
        if (edit == Edit.Exit) break
        when (edit) {
            Edit.TITLE -> {
                print("Enter updated title : ")
                val editTitle: String = checkBlank()
                task.title = editTitle
            }
            Edit.DESCRIPTION -> {
                print("Enter updated description : ")
                val editDescription: String = checkBlank()
                task.description = editDescription
            }
            Edit.PRIORITY -> {
                var editPriority: Int
                while (true) {
                    try {
                        print("Enter updated priority (1 to 10): ")
                        editPriority = checkType()
                        Utility.checkPriorityConstraint(editPriority)
                        break
                    } catch (e: InvalidDataException) {
                        println(e.message)
                    }
                }
                task.priority = editPriority
            }
            Edit.CATEGORY -> {
                var category: Category?
                while (true) {
                    try {
                        println("WORK(1),PERSONAL(2),LEARNING(3),SHOPPING(4),OTHER(5)")
                        print("Enter updated category type (integer only) : ")
                        category = Manage.findCategoryType(checkType())
                        //Utility.checkCategoryConstraint(category)
                        checkNotNull(category)
                        break
                    } catch (e: InvalidDataException) {
                        println(e.message)
                    }
                }
                task.category = category!!
            }
            Edit.DATE -> {
                var startDate: LocalDate
                while (true) {
                    try {
                        print("Enter start date in (yyyy-mm-dd) format : ")
                        val startDate1: String = checkBlank()
                        startDate = Utility.checkStartDateConstraint(startDate1)
                        break
                    } catch (e: DateTimeParseException) {
                        println(e.message)
                    }
                }
                var endDate: LocalDate
                while (true) {
                    try {
                        print("Enter end date in (yyyy-mm-dd) format (should be past the start date or start date itself): ")
                        val endDate1: String = checkBlank()
                        endDate = Utility.checkEndDateConstraint(startDate, endDate1)
                        break
                    } catch (e: DateTimeParseException) {
                        println(e.message)
                    }
                }
                task.startDate = startDate
                task.endDate = endDate
            }
            Edit.IS_COMPLETED -> {
                var isCompleted: Int
                while (true) {
                    try {
                        print("Press 1 if you completed the task or 0 if not: ")
                        isCompleted = checkType()
                        Utility.checkCompleteStatusConstraint(isCompleted)
                        break
                    } catch (e: InvalidDataException) {
                        println(e.message)
                    }
                }
                task.isCompleted=isCompleted
            }
           else -> break
        }
        if (Manage.editTask(task, task.title, task.description, task.priority, task.category, task.startDate, task.endDate, task.isCompleted))
            println("task edited successfully")
        else println("An error occurred while editing the task")
    }
}
fun shareTaskList(userEmail:String,taskListId:Int){
    while (true) {
        try {
            println("1.Add collaborator 2.Remove collaborator 3.Exit")
            val share: Share = Manage.findShareType(checkType()) ?: continue
            if (share == Share.EXIT) break
            print("Enter email id of collaborator for the taskList : ")
            val collabEmail: String = checkBlank()
            Utility.checkSameAccountCollaboration(userEmail, collabEmail)
            val collabUser = Manage.getUserByEmail(collabEmail)
            checkNotNull(collabUser)
            //Utility.validEmailConstraint(collabUser)
            val collaborator: Collaborator? = Manage.getCollaboratorForTaskList(taskListId, collabUser.userId)
            when (share) {
                Share.ADD_COLLABORATOR -> {
                    Utility.isCollaboratorNotNull(collaborator)
                    var canEdit: String
                    do {
                        print("Can the user edit  ('yes' or 'no') : ")
                        canEdit = checkBlank()
                    } while (canEdit != "yes" && canEdit != "no")
                    val access: Int = if (canEdit == "yes") 1 else 0
                    if (Manage.shareTaskList(taskListId, collabUser.userId, access)) println("Collaborator " + collabUser.userName + " added to the taskList")
                    else println("Error in adding collaborator to the taskList")
                }

                Share.REMOVE_COLLABORATOR -> {
                    checkNotNull(collaborator)
                    //Utility.isCollaboratorNull(collaborator)
                    if (Manage.removeShareTaskList(taskListId,collabUser.userId))
                        println("collaborator " + collabUser.userName + " removed successfully from the taskList")
                    else println("An error occurred while removing collaborator to the taskList")
                }
                else -> break
            }
        } catch (e: InvalidDataException) {
           println(e.message)
        } catch (e: ExistCollaborationException) {
            println(e.message)
        } catch (e: SameAccountException) {
            println(e.message)
        } catch(e:IllegalStateException){
            println("Invalid collaborator mail id")
        }
    }
}
fun shareTask(userEmail: String,taskId:Int){
    while (true) {
        try {
            println("1.Add collaborator 2.Remove collaborator 3.Exit")
            val share: Share = Manage.findShareType(checkType()) ?: continue
            if (share == Share.EXIT) break
            print("Enter email id of collaborator for the task : ")
            val collabEmail: String = checkBlank()
            Utility.checkSameAccountCollaboration(userEmail, collabEmail)
            val collabUser = Manage.getUserByEmail(collabEmail)
            //Utility.validEmailConstraint(collabUser)
            checkNotNull(collabUser)
            val collaborator: Collaborator? = Manage.getCollaboratorForTask(taskId, collabUser.userId)
            when (share) {
                Share.ADD_COLLABORATOR -> {
                    Utility.isCollaboratorNotNull(collaborator)
                    var canEdit: String
                    do {
                        print("Can the user edit  ('yes' or 'no') : ")
                        canEdit = checkBlank()
                    } while (canEdit != "yes" && canEdit != "no")
                    val access: Int = if (canEdit == "yes") 1 else 0
                    if (Manage.shareTask(taskId, collabUser.userId, access))
                        println("Collaborator " + collabUser.userName + " added to the task")
                    else println("Error in adding collaborator to the task")
                }
                Share.REMOVE_COLLABORATOR -> {
                    checkNotNull(collaborator)
                    //Utility.isCollaboratorNull(collaborator)
                    if (Manage.removeShareTask(taskId,collabUser.userId))
                        println("collaborator " + collabUser.userName + " removed successfully")
                    else println("An error occurred while removing collaborator to the task")
                }
                else -> break
            }
        } catch (e: InvalidDataException) {
            println(e.message)
        } catch (e: ExistCollaborationException) {
            println(e.message)
        } catch (e: SameAccountException) {
            println(e.message)
        }catch(e:IllegalStateException){
        println("Invalid collaborator mail id")
        }
    }
}
fun viewTask(tasks:MutableList<Task>){
    while (true) {
        println("1.GroupTasksByCategory 2.DisplayTaskOnPriority 3.Current tasks 4.Exit")
        val viewTasks: ViewTasks = Manage.findViewType(checkType()) ?: continue
        if (viewTasks == ViewTasks.EXIT) break
        when (viewTasks) {
            ViewTasks.CATEGORY_TASKS -> {
                print("WORK(1),PERSONAL(2),LEARNING(3),SHOPPING(4),OTHER(5) \nEnter category type (integer only) : ")
                try {
                    val category: Category? = Manage.findCategoryType(checkType())
                    checkNotNull(category)
                    displayTasks(Manage.viewTasksByCategory(tasks, category))
                } catch (e: IllegalStateException) {
                        println("Enter valid category input...")
                }
            }
            ViewTasks.PRIORITY_TASKS -> displayTasks(Manage.viewTaskOnPriority(tasks))
            ViewTasks.CURRENT_TASKS -> displayTasks(Manage.viewCurrentTasks(tasks))
            else -> break
        }
    }
}
fun checkType(): Int {
    var num = 0
    var flag = true
    while (flag) {
        val str: String = readln()
        try {
            num = str.toInt()
            flag = false
        } catch (e: NumberFormatException) {
            print("Enter an integer : ")
        }
    }
    return num
}
fun checkBlank(): String {
    var name: String
    do {
        name = readln()
    } while (name.isEmpty() || name.trim { it <= ' ' }.isEmpty())
    return name
}