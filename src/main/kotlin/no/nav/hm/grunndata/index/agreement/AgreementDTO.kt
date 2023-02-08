package no.nav.hm.grunndata.index.agreement

import java.time.LocalDateTime
import java.util.*

data class AgreementDTO(
    val id: UUID,
    val identifier: String,
    val title: String,
    val resume: String?,
    val text: String?,
    val reference: String,
    val published: LocalDateTime,
    val expired: LocalDateTime,
    val attachments: List<AgreementAttachment> = emptyList(),
    val posts: List<AgreementPost> = emptyList(),
    val createdBy:String,
    val updatedBy: String,
    val created: LocalDateTime,
    val updated: LocalDateTime,
)

data class AgreementPost (
    val identifier: String,
    val nr: Int,
    val title: String,
    val description: String,
    val created: LocalDateTime = LocalDateTime.now()
)

data class AgreementAttachment (
    val title: String?,
    val media: List<Media> = emptyList(),
    val description: String?,
)

data class Media (
    val id:   UUID = UUID.randomUUID(),
    val order:  Int=1,
    val type: MediaType = MediaType.IMAGE,
    val uri:    String,
    val text:   String?=null,
    val source: MediaSourceType = MediaSourceType.HMDB
)

enum class MediaSourceType {
    HMDB, GCP, EXTERNALURL
}

enum class MediaType {
    PDF,
    IMAGE,
    VIDEO,
    OTHER
}
