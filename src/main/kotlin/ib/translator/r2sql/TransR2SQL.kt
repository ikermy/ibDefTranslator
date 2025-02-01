package ib.translator.r2sql

import ib.assembly.sql.R2SQL
import ib.assembly.traductor.Traductor
import ib.assembly.traductor.Traductor.TranslateUtils.getTranslate
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle

class TransR2SQL: R2SQL() {

    suspend fun updateTagInfo(
        tagId: Int,
        tagRu: String?,
        error: (Exception) -> Unit,
        success: () -> Unit,
        complete: () -> Unit
    ) {
        val sqlUpdate = "CALL ib_tr_SetTagWithoutTranslate(:id, :new_value)"
        val connection = connectionFactory.create().awaitSingle()

        return try {
            val result = connection.createStatement(sqlUpdate)
                .bind("id", tagId)
                .apply {
                    if (tagRu != null) bind("new_value", tagRu) else bindNull("new_value", String::class.java)
                }
                .execute()
                .awaitFirstOrNull()

            val resultSet = result?.map { row, _ -> row.get("message", String::class.java) }?.awaitFirstOrNull()
            if (resultSet == "NO_MORE_TRANSLATIONS") {
                complete()
            } else {
                success()
            }
        } catch (e: Exception) {
            error(e)
        } finally {
            connection.close().awaitFirstOrNull()
        }
    }

    suspend fun updateAuthorInfo(
        authorId: Int,
        nameRu: String?,
        aboutRu: String?,
        error: (Exception) -> Unit,
        success: () -> Unit,
        complete: () -> Unit
    ) {
        val sqlUpdate = "CALL ib_tr_SetAuthorWithoutTranslate(:id, :name_ru, :about_ru)"
        val connection = connectionFactory.create().awaitSingle()

        return try {
            val result = connection.createStatement(sqlUpdate)
                .bind("id", authorId)
                .apply {
                    if (nameRu != null) bind("name_ru", nameRu) else bindNull("name_ru", String::class.java)
                    if (aboutRu != null) bind("about_ru", aboutRu) else bindNull("about_ru", String::class.java)
                }
                .execute()
                .awaitFirstOrNull()

            val resultSet = result?.map { row, _ -> row.get("message", String::class.java) }?.awaitFirstOrNull()
            if (resultSet == "NO_MORE_TRANSLATIONS") {
                complete()
            } else {
                success()
            }
        } catch (e: Exception) {
            error(e)
        } finally {
            connection.close().awaitFirstOrNull()
        }
    }

    suspend fun fetchAuthorInfo(): String? {
        val sqlGet = "SELECT ib_tr_GetAuthorWithoutTranslate() AS author_info"
        val connection = connectionFactory.create().awaitSingle()

        return try {
            val authorInfo = connection.createStatement(sqlGet)
                .execute()
                .awaitFirstOrNull()
                ?.map { row, _ -> row.get("author_info", String::class.java) }
                ?.awaitFirstOrNull()
            authorInfo
        } catch (e: Exception) {
            throw e
        } finally {
            connection.close().awaitFirstOrNull()
        }
    }

    suspend fun fetchTagInfo(): String? {
        val sqlGet = "SELECT ib_tr_GetTagWithoutTranslate() AS tag_info"
        val connection = connectionFactory.create().awaitSingle()

        return try {
            val tagInfo = connection.createStatement(sqlGet)
                .execute()
                .awaitFirstOrNull()
                ?.map { row, _ -> row.get("tag_info", String::class.java) }
                ?.awaitFirstOrNull()
            tagInfo
        } catch (e: Exception) {
            throw e
        } finally {
            connection.close().awaitFirstOrNull()
        }
    }

}