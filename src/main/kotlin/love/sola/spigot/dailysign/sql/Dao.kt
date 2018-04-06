package love.sola.spigot.dailysign.sql


import love.sola.spigot.dailysign.config
import love.sola.spigot.dailysign.utils.getValue
import org.bukkit.entity.Player
import org.intellij.lang.annotations.Language
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*


class Dao {

    private val sqlConfig = config.getConfigurationSection("sql")
    private val url: String by sqlConfig
    private val username: String by sqlConfig
    private val password: String by sqlConfig

    init {
        createTable()
    }

    private val connection: Connection
        get() = DriverManager.getConnection(url, username, password)

    private fun <R> query(
        @Language("MySQL") sql: String,
        preExec: (ps: PreparedStatement) -> Unit = {},
        postExec: (rs: ResultSet) -> R
    ): R {
        return connection.use {
            it.prepareStatement(sql).apply(preExec).executeQuery().let(postExec)
        }
    }

    private fun execute(@Language("MySQL") sql: String, preExec: (ps: PreparedStatement) -> Unit = {}): Boolean {
        return connection.use {
            it.prepareStatement(sql).apply(preExec).execute()
        }
    }

    private fun update(@Language("MySQL") sql: String, preExec: (ps: PreparedStatement) -> Unit = {}): Int {
        return connection.use {
            it.prepareStatement(sql).apply(preExec).executeUpdate()
        }
    }

    private fun createTable() {
        //language=MySQL
        execute(
            """
                CREATE TABLE IF NOT EXISTS `daily_sign` (
                  `id` INT(11) NOT NULL AUTO_INCREMENT,
                  `player_id` CHAR(36) NOT NULL,
                  `player_name` VARCHAR(20) NOT NULL,
                  `reward` VARCHAR(255) NOT NULL DEFAULT '',
                  `sign_time` DATETIME NOT NULL,
                  PRIMARY KEY (`id`)
                )
            """.trimIndent()
        )
        //language=MySQL
        execute(
            """
                CREATE TABLE IF NOT EXISTS `sign_user` (
                  `player_id` CHAR(36) NOT NULL,
                  `player_name` VARCHAR(20) NOT NULL,
                  `count` INT(11) NOT NULL,
                  `continuous` INT(11) NOT NULL,
                  `highest` INT(11) NOT NULL,
                  PRIMARY KEY (`player_id`)
                )
            """.trimIndent()
        )
    }

    fun createNewUser(player: Player): Int {
        return update("INSERT IGNORE INTO sign_user VALUES(?,?,0,0,0)") {
            it.setString(1, player.uniqueId.toString())
            it.setString(2, player.name)
        }
    }

    fun signByOffset(player: Player, offset: Long): Int {
        return sign(player, LocalDateTime.now().plusDays(offset))
    }

    fun sign(player: Player, dateTime: LocalDateTime = LocalDateTime.now()): Int {
        return update("INSERT INTO daily_sign VALUES(NULL,?,?,DEFAULT,?)") {
            it.setString(1, player.uniqueId.toString())
            it.setString(2, player.name)
            it.setObject(3, dateTime)
        }
    }

    fun querySignInfoYesterday(player: Player): SignInfo? {
        return querySignInfoByOffset(
            player,
            -1
        )
    }

    fun querySignInfoByOffset(player: Player, offset: Long): SignInfo? {
        return querySignInfo(
            player,
            LocalDate.now().plusDays(offset)
        )
    }

    fun querySignInfo(player: Player, date: LocalDate = LocalDate.now()): SignInfo? {
        return query("SELECT * FROM daily_sign WHERE player_id=? AND sign_time BETWEEN ? AND ?", {
            it.setString(1, player.uniqueId.toString())
            it.setObject(2, date.atStartOfDay())
            it.setObject(3, date.plusDays(1).atStartOfDay())
        }) {
            return@query if (it.next()) {
                SignInfo(
                    UUID.fromString(it.getString("player_id")),
                    it.getString("player_name"),
                    it.getString("reward").split(", "),
                    it.getTimestamp("sign_time").toLocalDateTime()
                )
            } else {
                null
            }
        }
    }

    fun queryUserInfo(player: Player): UserInfo? {
        return query("SELECT * FROM sign_user WHERE player_id=?", {
            it.setString(1, player.uniqueId.toString())
        }) {
            return@query if (it.next()) {
                UserInfo(
                    UUID.fromString(it.getString("player_id")),
                    it.getString("player_name"),
                    it.getLong("count"),
                    it.getLong("continuous"),
                    it.getLong("highest")
                )
            } else {
                null
            }
        }
    }

    fun updateUserInfo(info: UserInfo): Boolean {
        return update("UPDATE sign_user SET `count`=?, continuous=?, highest=? WHERE player_id=?") {
            it.setString(4, info.player_id.toString())
            it.setLong(1, info.signCount)
            it.setLong(2, info.continuousSignCount)
            it.setLong(3, info.highestContinuous)
        } == 1
    }

    fun updateSignInfo(info: SignInfo): Boolean {
        return update("UPDATE daily_sign SET reward=? WHERE player_id=? AND sign_time=?") {
            it.setString(1, info.rewardedServer.joinToString())
            it.setString(2, info.playerId.toString())
            it.setObject(3, info.time)
        } == 1
    }

    fun queryRankBoard(type: String, offset: Int, count: Int): List<UserInfo> {
        return query("SELECT * FROM sign_user ORDER BY ? DESC LIMIT ?,?", {
            it.setString(1, type)
            it.setInt(2, offset)
            it.setInt(3, count)
        }) {
            val infoList = ArrayList<UserInfo>(count)
            while (it.next()) {
                infoList.add(
                    UserInfo(
                        UUID.fromString(it.getString("player_id")),
                        it.getString("player_name"),
                        it.getLong("count"),
                        it.getLong("continuous"),
                        it.getLong("highest")
                    )
                )
            }
            return@query infoList
        }
    }

    fun recountContinuous(player: Player): Long {
        return query("SELECT sign_time FROM daily_sign WHERE player_id=? ORDER BY sign_time DESC", {
            it.setString(1, player.uniqueId.toString())
        }) {
            if (!it.next()) return@query 0
            var lastDate = it.getTimestamp("sign_time").toLocalDateTime().toLocalDate()
            var nextDate: LocalDate
            var count = 1L
            while (it.next()) {
                nextDate = it.getTimestamp("sign_time").toLocalDateTime().toLocalDate()
                if (lastDate > nextDate.plusDays(1)) {
                    return@query count
                } else {
                    if (lastDate != nextDate) count++
                    lastDate = nextDate
                }
            }
            return@query -1
        }
    }


}
