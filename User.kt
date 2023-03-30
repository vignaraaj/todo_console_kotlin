data class User(val userId:Int,val userName:String,val email:String,val password:String){
    constructor(userId:Int,userName:String,email:String) : this(userId,userName,email,"")
}
