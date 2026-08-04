package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.CommentDao
import com.example.data.dao.FollowDao
import com.example.data.dao.LikeDao
import com.example.data.dao.MessageDao
import com.example.data.dao.NotificationDao
import com.example.data.dao.PostDao
import com.example.data.dao.ReportDao
import com.example.data.dao.StoryDao
import com.example.data.dao.UserDao
import com.example.data.entity.CommentEntity
import com.example.data.entity.FollowEntity
import com.example.data.entity.LikeEntity
import com.example.data.entity.MessageEntity
import com.example.data.entity.NotificationEntity
import com.example.data.entity.PostEntity
import com.example.data.entity.ReportEntity
import com.example.data.entity.StoryEntity
import com.example.data.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        PostEntity::class,
        CommentEntity::class,
        LikeEntity::class,
        FollowEntity::class,
        NotificationEntity::class,
        ReportEntity::class,
        StoryEntity::class,
        MessageEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao
    abstract fun likeDao(): LikeDao
    abstract fun followDao(): FollowDao
    abstract fun notificationDao(): NotificationDao
    abstract fun reportDao(): ReportDao
    abstract fun storyDao(): StoryDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shaghaf_app_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
