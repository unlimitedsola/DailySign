package love.sola.spigot.dailysign.sql


import love.sola.spigot.dailysign.config
import love.sola.spigot.dailysign.utils.getValue
import org.intellij.lang.annotations.Language
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
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

    fun <R> query(
        @Language("MySQL") sql: String,
        preExec: (ps: PreparedStatement) -> Unit = {},
        postExec: (rs: ResultSet) -> R
    ): R {
        return connection.use {
            it.prepareStatement(sql).apply(preExec).executeQuery().let(postExec)
        }
    }

    fun execute(@Language("MySQL") sql: String, preExec: (ps: PreparedStatement) -> Unit = {}): Boolean {
        return connection.use {
            it.prepareStatement(sql).apply(preExec).execute()
        }
    }

    fun update(@Language("MySQL") sql: String, preExec: (ps: PreparedStatement) -> Unit = {}): Int {
        return connection.use {
            it.prepareStatement(sql).apply(preExec).executeUpdate()
        }
    }

    private fun createTable() {
        //language=MySQL
        execute(
            """
                CREATE TABLE IF NOT EXISTS `dailysign` (
                  `id` INT(11) NOT NULL AUTO_INCREMENT,
                  `username` VARCHAR(20) NOT NULL,
                  `reward` VARCHAR(255) NOT NULL DEFAULT '',
                  `signtime` INT(11) NOT NULL,
                  PRIMARY KEY (`id`)
                )
            """.trimIndent()
        )
        //language=MySQL
        execute(
            """
                CREATE TABLE IF NOT EXISTS `signuser` (
                  `username` VARCHAR(255) NOT NULL,
                  `count` INT(11) NOT NULL,
                  `continuous` INT(11) NOT NULL,
                  `highest` INT(11) NOT NULL,
                  PRIMARY KEY (`username`)
                )
            """.trimIndent()
        )
    }

    fun createNewUser(player: String): Int {
        return update("INSERT IGNORE INTO signuser VALUES(?,0,0,0)") {
            it.setString(1, player)
        }
    }

    fun signNow(player: String): Int {
        return sign(player, System.currentTimeMillis())
    }

    fun signByOffset(player: String, offset: Int): Int {
        return sign(player, System.currentTimeMillis() + offset * 1000 * 3600 * 24)
    }

    fun sign(player: String, time: Long): Int {
        return update("INSERT INTO dailysign VALUES(NULL,?,DEFAULT,?)") {
            it.setString(1, player)
            it.setInt(2, (time / 1000).toInt())
        }
    }

    fun querySignInfoYesterday(player: String): SignInfo? {
        return querySignInfo(
            player,
            (System.currentTimeMillis() / 1000 / 3600 / 24).toInt() - 1
        )
    }

    fun querySignInfoByOffset(player: String, offset: Int): SignInfo? {
        return querySignInfo(
            player,
            (System.currentTimeMillis() / 1000 / 3600 / 24).toInt() + offset
        )
    }

    fun querySignInfo(player: String, date: Int = (System.currentTimeMillis() / 1000 / 3600 / 24).toInt()): SignInfo? {
        return query("SELECT * FROM dailysign WHERE username=? AND signtime BETWEEN ? AND ?", {
            it.setString(1, player)
            it.setInt(2, date * 3600 * 24)
            it.setInt(3, (date + 1) * 3600 * 24)
        }) {
            return@query if (it.next()) {
                SignInfo(
                    it.getString("username"),
                    it.getString("reward"),
                    it.getInt("signtime")
                )
            } else {
                null
            }
        }
    }

    fun queryUserInfo(player: String): UserInfo? {
        return query("SELECT * FROM signuser WHERE username=?", {
            it.setString(1, player)
        }) {
            return@query if (it.next()) {
                UserInfo(
                    it.getString("username"),
                    it.getInt("count"),
                    it.getInt("continuous"),
                    it.getInt("highest")
                )
            } else {
                null
            }
        }
    }

    fun updateUserInfo(info: UserInfo): Boolean {
        return update("UPDATE signuser SET `count`=?, continuous=?, highest=? WHERE username=?") {
            it.setString(4, info.username)
            it.setInt(1, info.signCount)
            it.setInt(2, info.continuousSignCount)
            it.setInt(3, info.highestContinuous)
        } == 1
    }

    fun updateRewarded(info: SignInfo): Boolean {
        return update("UPDATE dailysign SET reward=? WHERE username=? AND signtime=?") {
            it.setString(1, info.server)
            it.setString(2, info.username)
            it.setInt(3, info.time)
        } == 1
    }

    fun queryRankBoard(type: String, offset: Int, count: Int): List<UserInfo> {
        return query("SELECT * FROM signuser ORDER BY ? DESC LIMIT ?,?", {
            it.setString(1, type)
            it.setInt(2, offset)
            it.setInt(3, count)
        }) {
            val infoList = ArrayList<UserInfo>(count)
            while (it.next()) {
                infoList.add(
                    UserInfo(
                        it.getString("username"),
                        it.getInt("count"),
                        it.getInt("continuous"),
                        it.getInt("highest")
                    )
                )
            }
            return@query infoList
        }
    }

    fun recountContinuous(player: String): Int {
        return query("SELECT signtime FROM dailysign WHERE username=? ORDER BY signtime DESC", {
            it.setString(1, player)
        }) {
            if (!it.next()) return@query 0
            var lastDate = it.getInt("signtime") / 3600 / 24
            var nextDate: Int
            var count = 1
            while (it.next()) {
                nextDate = it.getInt("signtime") / 3600 / 24
                if (lastDate - nextDate > 1) {
                    return@query count
                } else {
                    if (lastDate != nextDate) count++
                    lastDate = nextDate
                }
            }
            return@query -1
        }
    }

    fun signAll(player: String) {
        return query("SELECT signtime FROM dailysign WHERE username=? ORDER BY signtime DESC", {
            it.setString(1, player)
        }) {
            if (!it.next()) return@query
            var lastDate = it.getInt("signtime") / 3600 / 24
            var nextDate: Int
            while (it.next()) {
                nextDate = it.getInt("signtime") / 3600 / 24
                while (lastDate - nextDate > 1) {
                    --lastDate
                    update("INSERT INTO dailysign VALUES(NULL,?,DEFAULT,?)") {
                        it.setString(1, player)
                        it.setInt(2, lastDate * 3600 * 24)
                    }
                }
                lastDate = nextDate
            }
        }
    }

    fun querySignCount(player: String): Int {
        return query("SELECT COUNT(signtime) FROM dailysign WHERE username=?", {
            it.setString(1, player)
        }) {
            return@query if (!it.next()) -1 else it.getInt(1)
        }
    }


}
