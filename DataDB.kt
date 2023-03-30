import java.sql.*
import java.time.LocalDate
object DataDB : Data {

    private const val url = "jdbc:sqlite:/Users/vigna-pt6743/Documents/databases/todo.db"
    private val conn: Connection = DriverManager.getConnection(url)

    override fun addUser(user: User): Boolean {
        return try {
            conn.autoCommit = false
            val userId = user.userId
            val name = user.userName
            val email = user.email
            val password = user.password
            val preparedStatement1 = conn.prepareStatement("insert into user values(?,?,?)")
            val preparedStatement2 = conn.prepareStatement("insert into userCredentials values(?,?,?)")
            preparedStatement1.setInt(1, userId)
            preparedStatement1.setString(2, name)
            preparedStatement1.setString(3, email)
            preparedStatement1.executeUpdate()
            preparedStatement1.close()
            preparedStatement2.setInt(1, userId)
            preparedStatement2.setString(2, email)
            preparedStatement2.setString(3, password)
            preparedStatement2.executeUpdate()
            preparedStatement2.close()
            conn.commit()
            true
        } catch (e: Exception) {
            try {
                conn.rollback()
            } catch (ex: SQLException) {
                throw RuntimeException(ex)
            }
            false
        } finally {
            try {
                conn.autoCommit = true
            } catch (e: SQLException) {
                throw RuntimeException(e)
            }
        }
    }
    override fun deleteUser(user: User): Boolean {
        return try {
            conn.autoCommit = false
            val preparedStatement = conn.prepareStatement("delete from user where id=?")
            preparedStatement.setInt(1, user.userId)
            preparedStatement.executeUpdate()
            preparedStatement.close()
            val preparedStatement1 = conn.prepareStatement("delete from userCredentials where userId=?")
            preparedStatement1.setInt(1, user.userId)
            preparedStatement1.executeUpdate()
            preparedStatement1.close()
            conn.commit()
            true
        } catch (e: Exception) {
            try {
                conn.rollback()
            } catch (ex: SQLException) {
                throw RuntimeException(ex)
            }
            false
        } finally {
            try {
                conn.autoCommit = true
            } catch (e: SQLException) {
                throw RuntimeException(e)
            }
        }
    }
    override fun getUserCredentials(email:String,password:String): Boolean {
        return try {
            val selectSQL = "select * from userCredentials where email=? and password=?"
            val preparedStatement = conn.prepareStatement(selectSQL)
            preparedStatement.setString(1,email)
            preparedStatement.setString(2,password)
            preparedStatement.executeQuery()
           true
        } catch (e: Exception) {
           false
        }
    }
    override fun getUsers(): ArrayList<User> {
        return try {
            val users: ArrayList<User> = ArrayList()
            val selectSQL = "select * from user"
            val preparedStatement = conn.prepareStatement(selectSQL)
            val rs = preparedStatement.executeQuery()
            while (rs.next()) {
                val email = rs.getString("email")
                val name = rs.getString("name")
                val userId = rs.getInt("id")
                users.add(User(userId,name,email))
            }
            users
        } catch (e: Exception) {
            ArrayList()
        }
    }
    override fun getUserByEmail(email1: String?): User? {
        return try {
            val selectSQL = "select * from user where email=?"
            val preparedStatement = conn.prepareStatement(selectSQL)
            preparedStatement.setString(1, email1)
            val rs = preparedStatement.executeQuery()
            val mail = rs.getString("email")
            val name = rs.getString("name")
            val userId = rs.getInt("id")
            preparedStatement.close()
            User(userId,name,mail)
        } catch (e: Exception) {
            null
        }
    }
    override fun getPassword(userId: Int): String? {
        return try {
            val selectSQL = "select * from userCredentials where userId=?"
            val preparedStatement = conn.prepareStatement(selectSQL)
            preparedStatement.setInt(1, userId)
            val rs = preparedStatement.executeQuery()
            val password = rs.getString("password")
            preparedStatement.close()
            password
        } catch (e: Exception) {
            null
        }
    }
    override fun getUserName(userId: Int): String? {
        return try {
            val selectSQL = "select * from user where id=?"
            val preparedStatement = conn.prepareStatement(selectSQL)
            preparedStatement.setInt(1, userId)
            val rs = preparedStatement.executeQuery()
            val name = rs.getString("name")
            preparedStatement.close()
            name
        } catch (e: Exception) {
            null
        }
    }
    override fun addTaskList(taskList: TaskList): Boolean {
        return try {
            val insertSQL = "insert into taskList values(?,?)"
            val preparedStatement = conn.prepareStatement(insertSQL)
            preparedStatement.setInt(1, taskList.taskListId)
            preparedStatement.setInt(2, taskList.userId)
            preparedStatement.executeUpdate()
            preparedStatement.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    override fun deleteTaskListByTaskListId(taskListId: Int): Boolean {
        return try {
            val deleteSQL = " delete from taskList where taskListId=?"
            val preparedStatement = conn.prepareStatement(deleteSQL)
            preparedStatement.setInt(1, taskListId)
            preparedStatement.executeUpdate()
            preparedStatement.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    override fun getTaskListsByUserId(userId: Int): ArrayList<TaskList?> {
        return try {
            val taskLists: ArrayList<TaskList?> = ArrayList()
            val selectSQL = "select * from taskList where userId=?"
            val preparedStatement = conn.prepareStatement(selectSQL)
            preparedStatement.setInt(1, userId)
            val rs = preparedStatement.executeQuery()
            while (rs.next()) {
                val taskListId = rs.getInt("taskListId")
                val tasks = getTasksByTaskListId(taskListId)
                val collaborators: MutableList<Collaborator> = ArrayList()
                val selectSQL1 = "select * from sharedTaskList where taskListId=?"
                val preparedStatement1 = conn.prepareStatement(selectSQL1)
                preparedStatement1.setInt(1, taskListId)
                val rs1 = preparedStatement1.executeQuery()
                while (rs1.next()) {
                    val userId1 = rs1.getInt("userId")
                    val canEdit = rs1.getInt("canEdit")
                    collaborators.add(Collaborator(taskListId, userId1, canEdit))
                }
                preparedStatement1.close()
                taskLists.add(TaskList(userId, taskListId, tasks as MutableList<Task>, collaborators))
            }
            preparedStatement.close()
            taskLists
        } catch (e: Exception) {
            ArrayList()
        }
    }
    override fun getSharedTaskListsByUserId(userId: Int): MutableList<TaskList?> {
        return try {
            val sharedTaskList: MutableList<TaskList?> = ArrayList()
            val sharedTaskListIds:MutableList<Int> = getSharedTaskListIdOfUser(userId)
            for (i in sharedTaskListIds) sharedTaskList.add(getTaskListByTaskListId(i))
            sharedTaskList
        } catch (e: Exception) {
            ArrayList()
        }
    }
    override fun getSharedTasksByUserId(userId: Int): MutableList<Task?> {
        return try {
            val sharedTask: MutableList<Task?> = ArrayList()
            val sharedTaskIds:MutableList<Int> = getSharedTaskIdOfUser(userId)
            for (i in sharedTaskIds) sharedTask.add(getTaskByTaskId(i))
            sharedTask
        } catch (e: Exception) {
            ArrayList()
        }
    }
    override fun deleteTaskListsByUserId(userId: Int): Boolean {
        return try {
            val deleteSQL = " delete from taskList where userId=?"
            val preparedStatement = conn.prepareStatement(deleteSQL)
            preparedStatement.setInt(1, userId)
            preparedStatement.executeUpdate()
            preparedStatement.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    override fun getTaskListByTaskListId(taskListId: Int): TaskList? {
        return try {
            val selectSQL = "select * from taskList where taskListId=?"
            val preparedStatement = conn.prepareStatement(selectSQL)
            preparedStatement.setInt(1, taskListId)
            val rs = preparedStatement.executeQuery()
            val userId = rs.getInt("userId")
            val tasks = getTasksByTaskListId(taskListId)
            val collaborators: MutableList<Collaborator> = ArrayList()
            val selectSQL1 = "select * from sharedTaskList where taskListId=?"
            val preparedStatement1 = conn.prepareStatement(selectSQL1)
            preparedStatement1.setInt(1, taskListId)
            val rs1 = preparedStatement1.executeQuery()
            while (rs1.next()) {
                val userId1 = rs1.getInt("userId")
                val canEdit = rs1.getInt("canEdit")
                collaborators.add(Collaborator(taskListId, userId1, canEdit))
            }
            preparedStatement1.close()
            preparedStatement.close()
            TaskList(userId, taskListId, tasks as MutableList<Task>, collaborators)
        } catch (e: Exception) {
            null
        }
    }
    override fun addTask(task: Task): Boolean {
        return try {
            val insertSQl1 = "insert into task values(?,?,?,?,?,?,?,?,?,?)"
            val taskId = task.taskId
            val taskListId = task.taskListId
            val title = task.title
            val description = task.description
            val startDate = task.startDate
            val endDate = task.endDate
            val priority = task.priority
            val category = task.category.toString()
            val period = task.period.toString()
            val isCompleted: Int = task.isCompleted
            val preparedStatement = conn.prepareStatement(insertSQl1)
            preparedStatement.setInt(1, taskId)
            preparedStatement.setInt(2, taskListId)
            preparedStatement.setString(3, title)
            preparedStatement.setString(4, description)
            preparedStatement.setString(5, startDate.toString())
            preparedStatement.setString(6, endDate.toString())
            preparedStatement.setInt(7, priority)
            preparedStatement.setString(8, category)
            preparedStatement.setString(9, period)
            preparedStatement.setInt(10, isCompleted)
            preparedStatement.executeUpdate()
            preparedStatement.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    override fun getTaskByTaskId(taskId: Int): Task? {
        return try {
            val selectSQL = "select * from task where taskId=? "
            val preparedStatement = conn.prepareStatement(selectSQL)
            preparedStatement.setInt(1, taskId)
            val rs = preparedStatement.executeQuery()
            val taskListId = rs.getInt("taskListId")
            val title = rs.getString("title")
            val description = rs.getString("description")
            val startDate = LocalDate.parse(rs.getString("startDate"))
            val endDate = LocalDate.parse(rs.getString("endDate"))
            val priority = rs.getInt("priority")
            val category = Category.valueOf(rs.getString("category"))
            val period = RecurringTasks.valueOf(rs.getString("period"))
            val sharedUsers: MutableList<Collaborator> = ArrayList()
            val preparedStatement1 = conn.prepareStatement("select * from sharedTask where taskId=?")
            preparedStatement1.setInt(1, taskId)
            val rs1 = preparedStatement1.executeQuery()
            while (rs1.next()) {
                val userId = rs1.getInt("userId")
                val canEdit = rs1.getInt("canEdit")
                sharedUsers.add(Collaborator(taskId, userId, canEdit))
            }
            preparedStatement1.close()
            val isCompleted = rs.getInt("isCompleted")
            preparedStatement.close()
            Task(taskId, taskListId, title, description, priority,startDate, endDate,  category, period, sharedUsers, isCompleted)
        } catch (e: Exception) {
            null
        }
    }
    private fun getTasksByTaskListId(taskListId: Int): List<Task>? {
        return try {
            val tasks: MutableList<Task> = ArrayList()
            val selectSQL = "select * from task where taskListId =?"
            val preparedStatement = conn.prepareStatement(selectSQL)
            preparedStatement.setInt(1, taskListId)
            val rs = preparedStatement.executeQuery()
            while (rs.next()) {
                val taskId = rs.getInt("taskId")
                val title = rs.getString("title")
                val description = rs.getString("description")
                val startDate = LocalDate.parse(rs.getString("startDate"))
                val endDate = LocalDate.parse(rs.getString("endDate"))
                val priority = rs.getInt("priority")
                val category = Category.valueOf(rs.getString("category"))
                val period = RecurringTasks.valueOf(rs.getString("period"))
                val isCompleted = rs.getInt("isCompleted")
                val sharedUsers: MutableList<Collaborator> = ArrayList()
                val preparedStatement1 = conn.prepareStatement("select * from sharedTask where taskId=?")
                preparedStatement1.setInt(1, taskId)
                val rs1 = preparedStatement1.executeQuery()
                while (rs1.next()) {
                    val userId = rs1.getInt("userId")
                    val canEdit = rs1.getInt("canEdit")
                    sharedUsers.add(Collaborator(taskId, userId, canEdit))
                }
                preparedStatement1.close()
                tasks.add(Task(taskId, taskListId, title, description, priority,startDate, endDate, category, period, sharedUsers, isCompleted))
            }
            preparedStatement.close()
            tasks
        } catch (e: Exception) {
            null
        }
    }
    override fun addSharedTask(collaborator: Collaborator): Boolean {
        return try {
            val insertSQL = "insert into sharedTask values(?,?,?)"
            val preparedStatement = conn.prepareStatement(insertSQL)
            preparedStatement.setInt(1, collaborator.id)
            preparedStatement.setInt(2, collaborator.userId)
            preparedStatement.setInt(3, collaborator.canEdit)
            preparedStatement.executeUpdate()
            preparedStatement.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    override fun addSharedTaskList(collaborator: Collaborator): Boolean {
        return try {
            val insertSQL = "insert into sharedTaskList values(?,?,?)"
            val preparedStatement = conn.prepareStatement(insertSQL)
            preparedStatement.setInt(1, collaborator.id)
            preparedStatement.setInt(2, collaborator.userId)
            preparedStatement.setInt(3, collaborator.canEdit)
            preparedStatement.executeUpdate()
            preparedStatement.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    override fun deleteTaskByTaskId(taskId: Int): Boolean {
        return try {
            val deleteSQl = "delete from task where taskId=?"
            val preparedStatement = conn.prepareStatement(deleteSQl)
            preparedStatement.setInt(1, taskId)
            preparedStatement.executeUpdate()
            preparedStatement.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    override fun deleteTasksByTaskListId(taskListId: Int): Boolean {
        return try {
            val deleteSQl = "delete from task where taskListId=?"
            val preparedStatement = conn.prepareStatement(deleteSQl)
            preparedStatement.setInt(1, taskListId)
            preparedStatement.executeUpdate()
            preparedStatement.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    override fun deleteSharedTask(taskId: Int, userId: Int): Boolean {
        return try {
            val deleteSQL = " delete from sharedTask where taskId=? and userId=?"
            val preparedStatement = conn.prepareStatement(deleteSQL)
            preparedStatement.setInt(1, taskId)
            preparedStatement.setInt(2, userId)
            preparedStatement.executeUpdate()
            preparedStatement.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    override fun deleteSharedTasksByUserId(userId: Int): Boolean {
        return try {
            val deleteSQL = " delete from sharedTask where  userId=?"
            val preparedStatement = conn.prepareStatement(deleteSQL)
            preparedStatement.setInt(1, userId)
            preparedStatement.executeUpdate()
            preparedStatement.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    override fun deleteSharedTasksByTaskId(taskId: Int): Boolean {
        return try {
            val deleteSQL = " delete from sharedTask where taskId=?"
            val preparedStatement = conn.prepareStatement(deleteSQL)
            preparedStatement.setInt(1, taskId)
            preparedStatement.executeUpdate()
            preparedStatement.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    override fun getSharedTaskIdOfUser(userId: Int): MutableList<Int> {
        return try {
            val sharedTaskIds: MutableList<Int> = ArrayList()
            val selectSQL = "select * from sharedTask where userId=?"
            val preparedStatement = conn.prepareStatement(selectSQL)
            preparedStatement.setInt(1, userId)
            val rs = preparedStatement.executeQuery()
            while (rs.next()) {
                val taskId = rs.getInt("taskId")
                sharedTaskIds.add(taskId)
            }
            preparedStatement.close()
            sharedTaskIds
        } catch (e: Exception) {
            ArrayList()
        }
    }
    override fun getCollaboratorForTask(taskId: Int, userId: Int): Collaborator? {
        return try {
            val selectSQL = "select * from sharedTask where userId=? and taskId=?"
            val preparedStatement = conn.prepareStatement(selectSQL)
            preparedStatement.setInt(1, userId)
            preparedStatement.setInt(2, taskId)
            val rs = preparedStatement.executeQuery()
            val canEdit = rs.getInt("canEdit")
            preparedStatement.close()
            Collaborator(taskId, userId, canEdit)
        } catch (e: Exception) {
            null
        }
    }
    override fun getSharedTaskListIdOfUser(userId: Int): MutableList<Int> {
        return try {
            val sharedTaskList: MutableList<Int> = ArrayList()
            val selectSQL = "select * from sharedTaskList where userId=?"
            val preparedStatement = conn.prepareStatement(selectSQL)
            preparedStatement.setInt(1, userId)
            val rs = preparedStatement.executeQuery()
            while (rs.next()) {
                val taskListId = rs.getInt("taskListId")
                sharedTaskList.add(taskListId)
            }
            preparedStatement.close()
            sharedTaskList
        } catch (e: Exception) {
            ArrayList()
        }
    }
    override fun updateTask(taskId: Int, title: String, description: String, priority: Int, category: Category, startDate: LocalDate, endDate: LocalDate, isCompleted: Int): Boolean {
        return try {
            val updateSQL = "update task set title=?,description=?,priority=?,category=?,startDate=?,endDate=?,isCompleted=? where taskId=?"
            val preparedStatement = conn.prepareStatement(updateSQL)
            preparedStatement.setString(1, title)
            preparedStatement.setString(2, description)
            preparedStatement.setInt(3, priority)
            preparedStatement.setString(4, category.toString())
            preparedStatement.setString(5, startDate.toString())
            preparedStatement.setString(6, endDate.toString())
            preparedStatement.setInt(7, isCompleted)
            preparedStatement.setInt(8, taskId)
            preparedStatement.executeUpdate()
            preparedStatement.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    override fun deleteSharedTaskListByTaskListId(taskListId: Int): Boolean {
        return try {
            val deleteSQL = "delete from sharedTaskList where taskListId=?"
            val preparedStatement = conn.prepareStatement(deleteSQL)
            preparedStatement.setInt(1, taskListId)
            preparedStatement.executeUpdate()
            preparedStatement.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    override fun deleteSharedTaskList(taskListId: Int, userId: Int): Boolean {
        return try {
            val deleteSQL = "delete from sharedTaskList where taskListId=? and userId=?"
            val preparedStatement = conn.prepareStatement(deleteSQL)
            preparedStatement.setInt(1, taskListId)
            preparedStatement.setInt(2, userId)
            preparedStatement.executeUpdate()
            preparedStatement.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    override fun deleteSharedTaskListsByUserId(userId: Int): Boolean {
        return try {
            val deleteSQL = "delete from sharedTaskList where userId=?"
            val preparedStatement = conn.prepareStatement(deleteSQL)
            preparedStatement.setInt(1, userId)
            preparedStatement.executeUpdate()
            preparedStatement.close()
            true
        } catch (e: Exception) {
            false
        }
    }
    override fun getCollaboratorForTaskList(taskListId: Int, userId: Int): Collaborator? {
        return try {
            val selectSQL = "select * from sharedTaskList where userId=? and taskListId=?"
            val preparedStatement = conn.prepareStatement(selectSQL)
            preparedStatement.setInt(1, userId)
            preparedStatement.setInt(2, taskListId)
            val rs = preparedStatement.executeQuery()
            val canEdit = rs.getInt("canEdit")
            preparedStatement.close()
            Collaborator(taskListId, userId, canEdit)
        } catch (e: Exception) {
            null
        }
    }
}