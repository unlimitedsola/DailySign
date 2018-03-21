package love.sola.spigot.dailysign.sql

class UserInfo(
    val username: String,
    var signCount: Int,
    var continuousSignCount: Int,
    var highestContinuous: Int
)
