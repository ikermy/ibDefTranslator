package ib.translator.r2sql

import ib.assembly.sql.R2SQL
import ib.assembly.traductor.Traductor
import ib.assembly.traductor.Traductor.TranslateUtils.getTranslate
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle

class TransR2SQL: R2SQL() {

    suspend fun translateAuthor(traductor: Traductor,
                                error: (Exception) -> Unit,
                                success: () -> Unit,
                                complete: () -> Unit
    ) {

        val sqlGet = "SELECT ib_tr_GetAuthorWithoutTranslate() AS author_info"
        val sqlUpdate = "CALL ib_tr_SetAuthorWithoutTranslate(:id, :name_ru, :about_ru)"
        val connection = connectionFactory.create().awaitSingle()

        return try {
            val authorInfo = connection.createStatement(sqlGet)
                .execute()
                .awaitFirstOrNull()
                ?.map { row, _ -> row.get("author_info", String::class.java) }
                ?.awaitFirstOrNull()

            if (authorInfo != null && authorInfo != "NO_MORE_TRANSLATIONS") {
                val parts = authorInfo.split("::")
                if (parts.size == 3) {
                    val authorId = parts[0].toInt()
                    val authorName = parts[1]
                    val authorAbout = parts[2]

                    val nameRu = if (authorName.isNotEmpty()) traductor.translate(authorName).getTranslate() else null
                    val aboutRu = if (authorAbout.isNotEmpty()) traductor.translate(authorAbout).getTranslate() else null

                    if (nameRu != null || aboutRu != null) {
                        connection.createStatement(sqlUpdate)
                            .bind("id", authorId)
                            .apply {
                                if (nameRu != null) bind("name_ru", nameRu) else bindNull("name_ru", String::class.java)
                                if (aboutRu != null) bind("about_ru", aboutRu) else bindNull("about_ru", String::class.java)
                            }
                            .execute()
                            .awaitFirstOrNull()

                        return success()
                    }
                }
            }
            complete()
        } catch (e: Exception) {
            error(e)
        } finally {
            connection.close().awaitFirstOrNull()
        }
    }

    suspend fun translateTag(traductor: Traductor,
                                error: (Exception) -> Unit,
                                success: () -> Unit,
                                complete: () -> Unit
    ) {

        val sqlGet = "SELECT ib_tr_GetTagWithoutTranslate() AS tag_info"
        val sqlUpdate = "CALL ib_tr_SetTagWithoutTranslate(:id, :new_value)"
        val connection = connectionFactory.create().awaitSingle()

        return try {
            val outTag = connection.createStatement(sqlGet)
                .execute()
                .awaitFirstOrNull()
                ?.map { row, _ -> row.get("tag_info", String::class.java) }
                ?.awaitFirstOrNull()

            if (outTag != null && outTag != "NO_MORE_TRANSLATIONS") {
                val parts = outTag.split("::")
                if (parts.size == 2) {
                    val tagId = parts[0].toInt()
                    val tagData = parts[1]

                    var tagRu = if (tagData.isNotEmpty()) traductor.translate(tagData).getTranslate() else null
                    // Может быть из нескольких символов разделенных пробелом или "-", меняю на "_" так нужно для Телеги
                    tagRu = tagRu?.replace(Regex("[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]"), "")
                    tagRu = tagRu?.replace(Regex("[ -]"), "_")?.replaceFirstChar { it.uppercase() }

                    if (tagRu != null) {
                        if (tagRu.length > 49) tagRu = tagRu.substring(0, 49) // костыль для длинных тегов

                        connection.createStatement(sqlUpdate)
                            .bind("id", tagId)
                            .apply {
                                bind("new_value", tagRu)
                            }
                            .execute()
                            .awaitFirstOrNull()

                        return success()
                    }
                }
            }
            complete()
        } catch (e: Exception) {
            error(e)
        } finally {
            connection.close().awaitFirstOrNull()
        }
    }

}