package no.nav.hm.grunndata.index.agreement

import no.nav.hm.grunndata.rapid.dto.AgreementAttachment
import no.nav.hm.grunndata.rapid.dto.AgreementDTO
import no.nav.hm.grunndata.rapid.dto.AgreementPost
import no.nav.hm.grunndata.index.SearchDoc
import java.time.LocalDateTime

data class AgreementDoc(
    override val id: String,
    val identifier: String,
    val title: String,
    val resume: String?,
    val text: String?,
    val reference: String,
    val published: LocalDateTime,
    val expired: LocalDateTime,
    val attachments: List<AgreementAttachment>,
    val posts: List<AgreementPost>,
    val createdBy: String,
    val updatedBy: String,
    val created: LocalDateTime,
    val updated: LocalDateTime
) : SearchDoc

fun AgreementDTO.toDoc() : AgreementDoc = AgreementDoc (
    id = id.toString(), identifier = identifier,
    title = title, resume = resume, text = text,
    reference = reference, published = published, expired = expired, attachments = attachments,
    createdBy = createdBy, updatedBy = updatedBy, created = created, updated = updated,
    posts =  posts )