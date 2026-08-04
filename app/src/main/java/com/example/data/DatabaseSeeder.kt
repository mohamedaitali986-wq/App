package com.example.data

import com.example.data.entity.CommentEntity
import com.example.data.entity.LikeEntity
import com.example.data.entity.MessageEntity
import com.example.data.entity.NotificationEntity
import com.example.data.entity.PostEntity
import com.example.data.entity.StoryEntity
import com.example.data.entity.UserEntity
import com.example.util.SecurityUtils

/**
 * Handles seeding initial data on first app startup if the database is empty.
 * Ensures the admin account and starter Arabic content are created securely.
 */
object DatabaseSeeder {

    suspend fun seedIfNeeded(database: AppDatabase) {
        val userDao = database.userDao()
        val postDao = database.postDao()
        val commentDao = database.commentDao()
        val likeDao = database.likeDao()
        val notificationDao = database.notificationDao()
        val storyDao = database.storyDao()
        val messageDao = database.messageDao()

        val sampleVideoUrls = listOf(
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4"
        )

        val existingUsers = userDao.getUserByUsername("admin")
        if (existingUsers != null) {
            // Database is already seeded, update admin displayName if needed
            if (existingUsers.displayName != "شغف") {
                userDao.updateUser(existingUsers.copy(displayName = "شغف"))
            }
            // Ensure any old placeholder video URLs are updated to real working MP4 URLs
            val videoPosts = postDao.getAllVideoPostsOnce()
            videoPosts.forEachIndexed { index, post ->
                if (!post.mediaUrl.startsWith("http://") && !post.mediaUrl.startsWith("https://")) {
                    val realUrl = sampleVideoUrls[index % sampleVideoUrls.size]
                    postDao.updatePost(post.copy(mediaUrl = realUrl))
                }
            }
            return
        }

        // 1. Seed Admin User
        val adminSalt = SecurityUtils.generateSalt()
        val adminHash = SecurityUtils.hashPassword("Admin@1234", adminSalt)
        val adminUser = UserEntity(
            username = "admin",
            passwordHash = adminHash,
            salt = adminSalt,
            displayName = "شغف",
            bio = "الحساب الرسمي لإدارة منصة شغف للفيديوهات والقصص",
            avatarColorHex = "#FF2A55",
            isAdmin = true
        )
        val adminId = userDao.insertUser(adminUser)

        // 2. Seed Arabic Content Creators
        val creator1Salt = SecurityUtils.generateSalt()
        val creator1Hash = SecurityUtils.hashPassword("UserPass#1", creator1Salt)
        val creator1 = UserEntity(
            username = "sara_art",
            passwordHash = creator1Hash,
            salt = creator1Salt,
            displayName = "سارة العلي",
            bio = "فنانة رقمية ومصممة واجهات 🎨 | أشارككم كواليس الرسم والتصميم",
            avatarColorHex = "#8A2BE2"
        )
        val saraId = userDao.insertUser(creator1)

        val creator2Salt = SecurityUtils.generateSalt()
        val creator2Hash = SecurityUtils.hashPassword("UserPass#2", creator2Salt)
        val creator2 = UserEntity(
            username = "tariq_tech",
            passwordHash = creator2Hash,
            salt = creator2Salt,
            displayName = "طارق المعرفة",
            bio = "مستكشف تقني ومراجعة أحدث التطبيقات والهواتف الذكية 📱✨",
            avatarColorHex = "#00D2FF"
        )
        val tariqId = userDao.insertUser(creator2)

        val creator3Salt = SecurityUtils.generateSalt()
        val creator3Hash = SecurityUtils.hashPassword("UserPass#3", creator3Salt)
        val creator3 = UserEntity(
            username = "amira_vlogs",
            passwordHash = creator3Hash,
            salt = creator3Salt,
            displayName = "أميرة الفن والطهي",
            bio = "وصفات سريعة وفيديوهات قصيرة من حياتي اليومية 🍳🌿",
            avatarColorHex = "#FF9900"
        )
        val amiraId = userDao.insertUser(creator3)

        // 3. Seed Short Videos and Photo Posts
        val post1 = PostEntity(
            userId = tariqId,
            type = "VIDEO",
            mediaUrl = sampleVideoUrls[0],
            thumbnailUrl = "thumb_tech",
            caption = "أهم ٣ ميزات جديدة في التحديث الأخير للذكاء الاصطناعي! ما رأيكم بها؟ 🔥📱",
            hashtags = "#تقنية #برمجة #ذكاء_اصطناعي #شغف",
            likeCount = 24,
            commentCount = 3,
            isFeatured = true
        )
        val post1Id = postDao.insertPost(post1)

        val post2 = PostEntity(
            userId = saraId,
            type = "VIDEO",
            mediaUrl = sampleVideoUrls[1],
            thumbnailUrl = "thumb_art",
            caption = "رسم وتصميم شعار تطبيق جديد باستخدام التابلت خلال ٣٠ ثانية 🎨✨",
            hashtags = "#تصميم #فنون #رسم #إبداع",
            likeCount = 42,
            commentCount = 5,
            isFeatured = true
        )
        val post2Id = postDao.insertPost(post2)

        val post3 = PostEntity(
            userId = amiraId,
            type = "VIDEO",
            mediaUrl = sampleVideoUrls[2],
            thumbnailUrl = "thumb_food",
            caption = "طريقة تحضير القهوة المختصة بأسرع وألذ خطوة ☕🌿",
            hashtags = "#قهوة #طهي #وصفات #شغف_يومي",
            likeCount = 18,
            commentCount = 2
        )
        val post3Id = postDao.insertPost(post3)

        val post4 = PostEntity(
            userId = saraId,
            type = "PHOTO",
            mediaUrl = "sample_photo_art.jpg",
            thumbnailUrl = "",
            caption = "لوحة رقمية جديدة بعنوان 'هدوء الليل' 🌌 تحية لكل محبي الفنون",
            hashtags = "#فن_رقمي #رسم #فن",
            likeCount = 31,
            commentCount = 4
        )
        val post4Id = postDao.insertPost(post4)

        val post5 = PostEntity(
            userId = adminId,
            type = "PHOTO",
            mediaUrl = "sample_photo_admin.jpg",
            thumbnailUrl = "",
            caption = "مرحباً بكم جميعاً في منصة شغف العربية للفيديوهات والصور! يسعدنا انضمامكم 🚀✨",
            hashtags = "#منصة_شغف #ترحيب #فيديوهات_قصيرة",
            likeCount = 89,
            commentCount = 6,
            isFeatured = true
        )
        val post5Id = postDao.insertPost(post5)

        // 4. Seed Comments
        commentDao.insertComment(
            CommentEntity(
                postId = post1Id,
                userId = saraId,
                text = "مراجعة ممتازة ومفيدة جداً! شكراً لك طارق 👏"
            )
        )
        commentDao.insertComment(
            CommentEntity(
                postId = post1Id,
                userId = amiraId,
                text = "الميزة الثانية أعجبتني كثيراً ✨"
            )
        )
        commentDao.insertComment(
            CommentEntity(
                postId = post2Id,
                userId = tariqId,
                text = "إبداع وتناسق ألوان خيالي يا سارة!"
            )
        )

        // 5. Seed Likes
        likeDao.insertLike(LikeEntity(postId = post1Id, userId = saraId))
        likeDao.insertLike(LikeEntity(postId = post1Id, userId = amiraId))
        likeDao.insertLike(LikeEntity(postId = post2Id, userId = tariqId))

        // 6. Seed Notifications
        notificationDao.insertNotification(
            NotificationEntity(
                recipientId = tariqId,
                senderId = saraId,
                type = "LIKE",
                postId = post1Id
            )
        )
        notificationDao.insertNotification(
            NotificationEntity(
                recipientId = tariqId,
                senderId = amiraId,
                type = "COMMENT",
                postId = post1Id
            )
        )
        notificationDao.insertNotification(
            NotificationEntity(
                recipientId = saraId,
                senderId = tariqId,
                type = "FOLLOW"
            )
        )

        // 7. Seed 24-Hour Stories
        storyDao.insertStory(
            StoryEntity(
                userId = saraId,
                type = "PHOTO",
                mediaUrl = "sample_photo_art.jpg",
                caption = "يوميات الرسم والتصميم! 🎨✨"
            )
        )
        storyDao.insertStory(
            StoryEntity(
                userId = tariqId,
                type = "VIDEO",
                mediaUrl = sampleVideoUrls[0],
                caption = "تجربة سريعة للذكاء الاصطناعي 📱🚀"
            )
        )
        storyDao.insertStory(
            StoryEntity(
                userId = amiraId,
                type = "PHOTO",
                mediaUrl = "sample_photo_food.jpg",
                caption = "قهوة الصباح الهادئة ☕🌿"
            )
        )

        // 8. Seed Direct Messages
        messageDao.insertMessage(
            MessageEntity(
                senderId = tariqId,
                receiverId = saraId,
                content = "أهلاً سارة! تصميمك الأخير كان ممتازاً جداً 👏",
                timestamp = System.currentTimeMillis() - 3600000
            )
        )
        messageDao.insertMessage(
            MessageEntity(
                senderId = saraId,
                receiverId = tariqId,
                content = "شكراً جزيلاً لك يا طارق! يسرني أنه أعجبك ✨",
                timestamp = System.currentTimeMillis() - 1800000
            )
        )
        messageDao.insertMessage(
            MessageEntity(
                senderId = amiraId,
                receiverId = saraId,
                content = "سارة هل يمكنني استشارتك في اختيار ألوان الشعار الجديد؟",
                timestamp = System.currentTimeMillis() - 900000
            )
        )
    }
}
