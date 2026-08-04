package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.entity.CommentEntity
import com.example.data.entity.FollowEntity
import com.example.data.entity.LikeEntity
import com.example.data.entity.MessageEntity
import com.example.data.entity.NotificationEntity
import com.example.data.entity.PostEntity
import com.example.data.entity.ReportEntity
import com.example.data.entity.StoryEntity
import com.example.data.entity.UserEntity
import com.example.util.SecurityUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AppRepository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val postDao = db.postDao()
    private val commentDao = db.commentDao()
    private val likeDao = db.likeDao()
    private val followDao = db.followDao()
    private val notificationDao = db.notificationDao()
    private val reportDao = db.reportDao()
    private val storyDao = db.storyDao()
    private val messageDao = db.messageDao()

    // --- USER MANAGEMENT & AUTH ---

    suspend fun registerUser(username: String, passwordRaw: String, displayName: String): Result<UserEntity> {
        val existing = userDao.getUserByUsername(username.trim())
        if (existing != null) {
            return Result.failure(Exception("اسم المستخدم مستعمل بالفعل، اختر اسمًا آخر"))
        }

        val salt = SecurityUtils.generateSalt()
        val hash = SecurityUtils.hashPassword(passwordRaw, salt)
        val newUser = UserEntity(
            username = username.trim(),
            passwordHash = hash,
            salt = salt,
            displayName = displayName.ifBlank { username }
        )
        val id = userDao.insertUser(newUser)
        val created = userDao.getUserById(id)
            ?: return Result.failure(Exception("حدث خطأ أثناء إنشاء الحساب"))
        return Result.success(created)
    }

    suspend fun loginUser(username: String, passwordRaw: String): Result<UserEntity> {
        val user = userDao.getUserByUsername(username.trim())
            ?: return Result.failure(Exception("اسم المستخدم أو كلمة المرور غير صحيحة"))

        if (user.isSuspended) {
            return Result.failure(Exception("تم تعليق هذا الحساب بواسطة إدارة المنصة"))
        }

        val isValid = SecurityUtils.verifyPassword(passwordRaw, user.salt, user.passwordHash)
        return if (isValid) {
            Result.success(user)
        } else {
            Result.failure(Exception("اسم المستخدم أو كلمة المرور غير صحيحة"))
        }
    }

    suspend fun getUserById(userId: Long): UserEntity? = userDao.getUserById(userId)

    fun observeUser(userId: Long): Flow<UserEntity?> = userDao.observeUserById(userId)

    fun searchUsers(query: String): Flow<List<UserEntity>> = userDao.searchUsers(query)

    suspend fun updateUserProfile(userId: Long, newDisplayName: String, newBio: String, newAvatarColor: String) {
        val user = userDao.getUserById(userId) ?: return
        val updated = user.copy(
            displayName = newDisplayName.ifBlank { user.displayName },
            bio = newBio,
            avatarColorHex = newAvatarColor
        )
        userDao.updateUser(updated)
    }

    // --- POSTS & FEED ---

    fun getVideoPosts(): Flow<List<PostEntity>> = postDao.getVideoPosts()

    fun getPhotoPosts(): Flow<List<PostEntity>> = postDao.getPhotoPosts()

    fun getAllPosts(): Flow<List<PostEntity>> = postDao.getAllPosts()

    fun getPostsByUserId(userId: Long): Flow<List<PostEntity>> = postDao.getPostsByUserId(userId)

    fun getPostsByUserIdAndType(userId: Long, type: String): Flow<List<PostEntity>> =
        postDao.getPostsByUserIdAndType(userId, type)

    fun searchPosts(query: String): Flow<List<PostEntity>> = postDao.searchPosts(query)

    suspend fun createPost(
        userId: Long,
        type: String,
        mediaUrl: String,
        thumbnailUrl: String,
        caption: String,
        hashtags: String,
        isFeatured: Boolean = false
    ): Long {
        val post = PostEntity(
            userId = userId,
            type = type,
            mediaUrl = mediaUrl,
            thumbnailUrl = thumbnailUrl,
            caption = caption,
            hashtags = hashtags,
            isFeatured = isFeatured
        )
        return postDao.insertPost(post)
    }

    suspend fun deletePost(postId: Long) {
        commentDao.deleteCommentsForPost(postId)
        postDao.deletePostById(postId)
    }

    // --- LIKES ---

    suspend fun toggleLike(postId: Long, userId: Long): Boolean {
        val isLiked = likeDao.isPostLikedByUser(postId, userId)
        val post = postDao.getPostById(postId)
        if (isLiked) {
            likeDao.deleteLike(postId, userId)
            postDao.updateLikeCount(postId, -1)
            return false
        } else {
            likeDao.insertLike(LikeEntity(postId = postId, userId = userId))
            postDao.updateLikeCount(postId, 1)

            // Send notification to post owner
            if (post != null && post.userId != userId) {
                notificationDao.insertNotification(
                    NotificationEntity(
                        recipientId = post.userId,
                        senderId = userId,
                        type = "LIKE",
                        postId = postId
                    )
                )
            }
            return true
        }
    }

    fun observeIsPostLiked(postId: Long, userId: Long): Flow<Boolean> =
        likeDao.observeIsPostLikedByUser(postId, userId)

    // --- COMMENTS ---

    fun getCommentsForPost(postId: Long): Flow<List<CommentEntity>> =
        commentDao.getCommentsForPost(postId)

    suspend fun addComment(postId: Long, userId: Long, text: String): Long {
        val comment = CommentEntity(postId = postId, userId = userId, text = text)
        val id = commentDao.insertComment(comment)
        postDao.updateCommentCount(postId, 1)

        val post = postDao.getPostById(postId)
        if (post != null && post.userId != userId) {
            notificationDao.insertNotification(
                NotificationEntity(
                    recipientId = post.userId,
                    senderId = userId,
                    type = "COMMENT",
                    postId = postId
                )
            )
        }
        return id
    }

    suspend fun deleteComment(commentId: Long, userId: Long, isAdmin: Boolean): Boolean {
        val comment = commentDao.getCommentById(commentId) ?: return false
        if (comment.userId == userId || isAdmin) {
            commentDao.deleteCommentById(commentId)
            postDao.updateCommentCount(comment.postId, -1)
            return true
        }
        return false
    }

    // --- FOLLOWS ---

    suspend fun toggleFollow(followerId: Long, followingId: Long): Boolean {
        if (followerId == followingId) return false
        val isFollowing = followDao.isFollowing(followerId, followingId)
        if (isFollowing) {
            followDao.deleteFollow(followerId, followingId)
            return false
        } else {
            followDao.insertFollow(FollowEntity(followerId = followerId, followingId = followingId))
            notificationDao.insertNotification(
                NotificationEntity(
                    recipientId = followingId,
                    senderId = followerId,
                    type = "FOLLOW"
                )
            )
            return true
        }
    }

    fun observeIsFollowing(followerId: Long, followingId: Long): Flow<Boolean> =
        followDao.observeIsFollowing(followerId, followingId)

    fun getFollowerCount(userId: Long): Flow<Int> = followDao.getFollowerCount(userId)

    fun getFollowingCount(userId: Long): Flow<Int> = followDao.getFollowingCount(userId)

    // --- NOTIFICATIONS ---

    fun getNotificationsForUser(userId: Long): Flow<List<NotificationEntity>> =
        notificationDao.getNotificationsForUser(userId)

    suspend fun markNotificationsRead(userId: Long) = notificationDao.markAllAsRead(userId)

    // --- REPORTS & ADMIN PANEL ---

    suspend fun reportPost(postId: Long, reporterUserId: Long, reason: String) {
        reportDao.insertReport(
            ReportEntity(
                postId = postId,
                reporterUserId = reporterUserId,
                reason = reason
            )
        )
        postDao.setReportedStatus(postId, true, reason)
    }

    fun getPendingReports(): Flow<List<ReportEntity>> = reportDao.getPendingReports()

    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()

    fun getActiveUserCount(): Flow<Int> = userDao.getActiveUserCount()

    fun getTotalPostsCount(): Flow<Int> = postDao.getTotalPostsCount()

    fun getTotalVideosCount(): Flow<Int> = postDao.getTotalVideosCount()

    fun getPendingReportCount(): Flow<Int> = reportDao.getPendingReportCount()

    suspend fun setUserSuspendedStatus(userId: Long, isSuspended: Boolean) {
        userDao.setUserSuspendedStatus(userId, isSuspended)
    }

    suspend fun deleteUser(userId: Long) {
        userDao.deleteUserById(userId)
    }

    suspend fun resolveReportAndDeletePost(postId: Long) {
        reportDao.setReportStatusForPost(postId, "RESOLVED")
        deletePost(postId)
    }

    suspend fun dismissReport(postId: Long) {
        reportDao.setReportStatusForPost(postId, "REJECTED")
        postDao.setReportedStatus(postId, false, "")
    }

    // --- STORIES (24-Hour Expiration) ---

    fun getActiveStories(): Flow<List<StoryEntity>> {
        val cutoff = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
        return storyDao.getActiveStories(cutoff)
    }

    suspend fun createStory(userId: Long, type: String, mediaUrl: String, caption: String): Long {
        val story = StoryEntity(
            userId = userId,
            type = type,
            mediaUrl = mediaUrl,
            caption = caption,
            createdAt = System.currentTimeMillis()
        )
        return storyDao.insertStory(story)
    }

    suspend fun deleteStory(storyId: Long) {
        storyDao.deleteStory(storyId)
    }

    suspend fun cleanupExpiredStories() {
        val cutoff = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
        storyDao.deleteExpiredStories(cutoff)
    }

    // --- DIRECT MESSAGING ---

    fun getMessagesBetween(user1Id: Long, user2Id: Long): Flow<List<MessageEntity>> =
        messageDao.getMessagesBetween(user1Id, user2Id)

    fun getAllUserMessages(userId: Long): Flow<List<MessageEntity>> =
        messageDao.getAllMessagesForUser(userId)

    suspend fun sendMessage(senderId: Long, receiverId: Long, content: String): Long {
        if (content.isBlank()) return 0L
        val msg = MessageEntity(
            senderId = senderId,
            receiverId = receiverId,
            content = content.trim(),
            timestamp = System.currentTimeMillis()
        )
        val msgId = messageDao.insertMessage(msg)

        // Optionally send a notification for new message
        notificationDao.insertNotification(
            NotificationEntity(
                recipientId = receiverId,
                senderId = senderId,
                type = "COMMENT" // reuse or display as direct message
            )
        )
        return msgId
    }

    suspend fun markMessagesAsRead(currentUserId: Long, otherUserId: Long) {
        messageDao.markMessagesAsRead(currentUserId, otherUserId)
    }

    // --- RELATED CONTENT SUGGESTIONS ---

    fun getRelatedPostsForUser(userId: Long): Flow<List<PostEntity>> {
        return postDao.getAllPosts().map { allPosts ->
            // Filter out posts authored by user or find posts with matching hashtags
            val userPosts = allPosts.filter { it.userId == userId }
            val userHashtags = userPosts.flatMap { post ->
                post.hashtags.split(" ", "#", "،", ",").filter { it.isNotBlank() }
            }.toSet()

            val defaultPopularTags = setOf("تقنية", "فنون", "رسم", "قهوة", "طهي", "شغف")
            val targetTags = if (userHashtags.isNotEmpty()) userHashtags else defaultPopularTags

            allPosts.filter { post ->
                post.userId != userId && targetTags.any { tag -> post.hashtags.contains(tag, ignoreCase = true) }
            }.take(6)
        }
    }
}
