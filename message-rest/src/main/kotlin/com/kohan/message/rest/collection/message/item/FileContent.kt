package com.kohan.message.rest.collection.message.item

import com.kohan.message.rest.enum.collection.message.item.FileType

class FileContent(
    /** 파일 경로 */
    var url: String,
    /** 파일 종류 */
    var type: FileType,
)
