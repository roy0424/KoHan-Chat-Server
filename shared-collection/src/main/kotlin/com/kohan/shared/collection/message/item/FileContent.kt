package com.kohan.shared.collection.message.item

import com.kohan.shared.enum.message.item.FileType

class FileContent(
    /** 파일 경로 */
    var url: String,
    /** 파일 종류 */
    var type: FileType,
)
