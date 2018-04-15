package love.sola.spigot.dailysign.sql


import love.sola.spigot.dailysign.config
import love.sola.spigot.dailysign.utils.getValue
import org.bukkit.entity.Player
import org.intellij.lang.annotations.Language
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.time.*
import java.util.*


private const val PAGE_SIZE = 15

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
                  `id` INT NOT NULL AUTO_INCREMENT,
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
                  `count` INT NOT NULL,
                  `continuous` INT NOT NULL,
                  `highest` INT NOT NULL,
                  PRIMARY KEY (`player_id`)
                )
            """.trimIndent()
        )
    }

    fun createNewUser(player: Player) {
        update("INSERT IGNORE INTO sign_user VALUES(?,?,0,0,0)") {
            it.setString(1, player.uniqueId.toString())
            it.setString(2, player.name)
        }
    }

    fun sign(player: Player, dateTime: LocalDateTime = LocalDateTime.now()): Boolean {
        return update("INSERT INTO daily_sign VALUES(NULL,?,?,DEFAULT,?)") {
            it.setString(1, player.uniqueId.toString())
            it.setString(2, player.name)
            it.setObject(3, dateTime)
        } == 1
    }

    fun querySignInfoOfDay(player: Player, date: LocalDate = LocalDate.now()): SignInfo? {
        return query("SELECT * FROM daily_sign WHERE player_id=? AND sign_time BETWEEN ? AND ?", {
            it.setString(1, player.uniqueId.toString())
            it.setObject(2, date.atStartOfDay())
            it.setObject(3, date.atTime(LocalTime.MAX))
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

    fun querySignInfoOfMonth(player: Player, month: YearMonth): List<SignInfo> {
        return query("SELECT * FROM daily_sign WHERE player_id=? AND sign_time BETWEEN ? AND ?", {
            it.setString(1, player.uniqueId.toString())
            it.setObject(2, month.atDay(1))
            it.setObject(3, month.atEndOfMonth())
        }) {
            val result = arrayListOf<SignInfo>()
            while (it.next()) {
                SignInfo(
                    UUID.fromString(it.getString("player_id")),
                    it.getString("player_name"),
                    it.getString("reward").split(", "),
                    it.getTimestamp("sign_time").toLocalDateTime()
                ).let { result.add(it) }
            }
            result
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

    fun updateUserInfo(player: Player) {
        val info = queryUserInfo(player) ?: return
        info.signCount = countTotalSign(player)
        val continuous = countContinuousSign(player)
        info.continuousSignCount = continuous
        if (continuous > info.highestContinuous) {
            info.highestContinuous = continuous
        }
        update("UPDATE sign_user SET `count`=?, continuous=?, highest=? WHERE player_id=?") {
            it.setString(4, info.playerId.toString())
            it.setLong(1, info.signCount)
            it.setLong(2, info.continuousSignCount)
            it.setLong(3, info.highestContinuous)
        }
    }

    fun updateSignInfo(info: SignInfo): Boolean {
        return update("UPDATE daily_sign SET reward=? WHERE player_id=? AND sign_time=?") {
            it.setString(1, info.rewardedServer.joinToString())
            it.setString(2, info.playerId.toString())
            it.setObject(3, info.time)
        } == 1
    }

    fun queryRankBoard(type: String, page: Int): List<UserInfo> {
        return query("SELECT * FROM sign_user ORDER BY ? DESC LIMIT ?,?", {
            it.setString(1, type)
            it.setInt(2, page * PAGE_SIZE)
            it.setInt(3, PAGE_SIZE)
        }) {
            val infoList = ArrayList<UserInfo>(PAGE_SIZE)
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

    private fun countContinuousSign(player: Player): Long {
        return query("SELECT sign_time FROM daily_sign WHERE player_id=? ORDER BY sign_time DESC", {
            it.setString(1, player.uniqueId.toString())
        }) {
            var expectedDate = LocalDate.now()
            var count = 0L
            while (it.next()) {
                val cursorDate = it.getTimestamp("sign_time").toLocalDateTime().toLocalDate()
                if (expectedDate == cursorDate) {
                    count++
                    expectedDate = expectedDate.minusDays(1)
                } else {
                    break
                }
            }
            return@query count
        }
    }

    private fun countTotalSign(player: Player): Long {
        return query("SELECT count(*) FROM daily_sign WHERE player_id=?", {
            it.setString(1, player.uniqueId.toString())
        }) {
            it.next()
            return@query it.getLong(1)
        }
    }


}
