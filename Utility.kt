import java.time.LocalDate
class Utility {
    companion object {
        private const val adminId = "vkj"
        private const val password = "123"
        fun userExists(user: User?) {
            if (user != null) throw InvalidDataException("  this E-mail has an existing account try different mail id")
        }
//        fun checkInvalidLoginConstraint(user:User?){
//            if(user==null) throw AuthenticationException("Invalid login")
//        }
        fun loginAdmin(admin: String, pass: String) {
            if (!(admin == adminId && pass == password)) throw InvalidDataException("Invalid login")
        }
        fun checkEmptyUsers(userDetails: List<User?>) {
            if (userDetails.isEmpty()) throw InvalidDataException("There are no users yet...")
        }
        fun checkPassWordConstraint(originalPassword: String, password: String) {
            if (originalPassword != password) throw InvalidDataException("Wrong password")
        }
        fun checkTaskListBelongsToUser(userIdInTaskList: Int, userId: Int) {
            if (userIdInTaskList != userId) throw AuthenticationException("You are not authenticated to use this taskList")
        }
//        fun validTaskListIdConstraint(taskList: TaskList?) {
//            if (taskList == null) throw InvalidDataException("taskList doesn't exist")
//        }
        fun checkPriorityConstraint(priority: Int) {
            if (priority > 10 || priority <= 0) throw InvalidDataException("Invalid priority valid from 1 to 10")
        }
//        fun checkCategoryConstraint(category: Category?) {
//            if (category == null) throw InvalidDataException("Invalid input for category press valid only from 1 to 5")
//        }
        fun checkStartDateConstraint(startDate1: String?): LocalDate {
            return LocalDate.parse(startDate1)
        }
        fun checkEndDateConstraint(startDate: LocalDate, endDate1: String?): LocalDate {
            val endDate = LocalDate.parse(endDate1)
            if (startDate.isAfter(endDate)) throw InvalidDataException("end date cant be before start date")
            return endDate
        }
//        fun checkPeriodConstraint(period: RecurringTasks?) {
//            if (period == null) throw InvalidDataException("Invalid input for Recurring tasks press valid input")
//        }
        fun checkRecurringNumber(recurringNumber: Int) {
            if (recurringNumber < 0 || recurringNumber > 50) throw InvalidDataException("recurring Number should be between 0 and 50 (inclusive)")
        }
        fun checkCompleteStatusConstraint(isCompleted: Int) {
            if (isCompleted != 1 && isCompleted != 0) throw InvalidDataException("only valid value is 1 or 0")
        }
//        fun validEmailConstraint(user: User?) {
//            if (user == null) throw InvalidDataException("Collaborator doesn't even have an account")
//        }
        fun checkSameAccountCollaboration(userEmail: String, email: String) {
            if (userEmail == email) throw SameAccountException("You cant be collaborator for your own task.")
        }
//        fun isCollaboratorNull(c: Collaborator?) {
//            if (c == null) throw InvalidDataException("invalid collaborator mail id...")
//        }
        fun isCollaboratorNotNull(c: Collaborator?) {
            if (c != null) throw ExistCollaborationException("The collaboration already exists...")
        }
//        fun validTaskIdConstraint(task:Task){
//            if(task==null) throw InvalidDataException("Invalid taskId")
//        }
        fun checkTaskListHasTask(task: Task, taskListId: Int) {
            if (task.taskListId != taskListId) throw AuthenticationException("the task doesn't belong to this taskList,you are not authenticated...")
        }
        fun canEditStatus(collaborator: Collaborator?){
            if(collaborator!!.canEdit == 0) throw AuthenticationException("You can only view  can't edit ")
        }
        fun doTaskListOfTaskShared(sharedTaskListId: List<Int?>, taskListId: Int, collabTaskEdit: Int, collabTaskListEdit: Int) {
            if (sharedTaskListId.contains(taskListId) && collabTaskListEdit == 1 && collabTaskEdit == 0) throw AuthenticationException(
                "The taskList of this task is also shared to you by deleting this task you could potentially" +
                        " edit the task for which you didn't have access \n try deleting the shared taskList first and delete this shared task"
            )
        }
    }
}