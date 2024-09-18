package com.kohan.shared.collection.file

import com.kohan.shared.collection.base.BaseCollection
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collation = "file")
class FileCollection(
    /** 업로드 한 파일 이름 */
    var originalFileName: String,
    /** 서버 파일 시스템에 저장된 파일 이름 */
    var fileName: String,
    /** 업로드한 파일의 확장자 */
    var extension: String,
    /** 파일 사이즈 */
    var fileSize: Int,
    /** 업로드한 사람 */
    @Indexed
    var uploadUserKey: ObjectId,
) : BaseCollection()
