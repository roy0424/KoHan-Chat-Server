package com.kohan.shared.collection.user

import com.kohan.proto.rest.v1.UserProfileOuterClass
import com.kohan.shared.collection.base.BaseCollection
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "user_profile")
data class UserProfileCollection(
    /** User ID */
    @Indexed(unique = true)
    var userId: ObjectId,
    /** Name as seen by other users */
    var nickname: String,
    /** File server request URL*/
    var profileImageFileId: String,
    /** User status message */
    var statusMessage: String? = null,
): BaseCollection() {
    companion object {
        fun to(request: UserProfileOuterClass.UserProfile): UserProfileCollection =
            UserProfileCollection(
                userId = ObjectId(request.userId),
                nickname = request.nickname,
                profileImageFileId = request.profileImageFileId,
            )
    }
}
