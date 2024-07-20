package me.ykrank.s1next.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import me.ykrank.s1next.BuildConfig
import java.util.concurrent.Executor
import java.util.concurrent.Executors

interface AppDatabaseManager {
    val executors: Executor
    fun getOrBuildDb(): AppDatabase

    fun close()
}

class AppDatabaseManagerImpl(applicationContext: Context) : AppDatabaseManager {

    val builder = Room.databaseBuilder(
        applicationContext,
        AppDatabase::class.java, BuildConfig.DB_NAME,
    )   // 数据库升级版本7是在2.1.0-5（2018/12），因此之前旧版本直接破坏性重建可以接受
        .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5, 6)

        // TRUNCATE模式，可以方便备份
        .setJournalMode(RoomDatabase.JournalMode.TRUNCATE).apply {
            if (!BuildConfig.DEBUG) {
                // TODO: 数据库操作禁止在主线程
                this.allowMainThreadQueries()
            }
        }

    @Volatile
    var database: AppDatabase? = null

    override val executors: Executor = Executors.newSingleThreadExecutor()

    override fun getOrBuildDb(): AppDatabase {
        return database ?: synchronized(this) {
            database ?: builder.build().also { database = it }
        }
    }

    override fun close() {
        database?.close()
        database = null
    }

}