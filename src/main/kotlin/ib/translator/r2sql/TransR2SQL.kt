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

}