package ib.translator.translator

import ib.assembly.traductor.Traductor
import ib.assembly.traductor.Traductor.TranslateUtils.getTranslate
import ib.translator.r2sql.TransR2SQL
import org.json.JSONObject

class Translator() {
    private val traductor = Traductor()
    private val sql = TransR2SQL()

    private suspend fun performTagTranslation(tagInfo: String): Pair<Int, String?>? {
        val jsonObject = JSONObject(tagInfo)
        val tagId = jsonObject.optInt("id")
        val tagData = jsonObject.optString("tag")

        var tagRu = if (tagData.isNotEmpty()) traductor.translate(tagData).getTranslate() else null
        // Может быть из нескольких символов, разделенных пробелом или "-", меняю на "_" так нужно для Телеги
        tagRu = tagRu?.replace(Regex("[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]"), "")
        tagRu = tagRu?.replace(Regex("[ -]"), "_")?.replaceFirstChar { it.uppercase() }

        if (tagRu != null && tagRu.length > 49) {
            tagRu = tagRu.substring(0, 49) // Костыль для длинных тегов
        }

        return Pair(tagId, tagRu)
    }


    private suspend fun performTranslation(authorInfo: String): Pair<String?, String?>? {
        val jsonObject = JSONObject(authorInfo)
        val authorName = jsonObject.optString("name")
        val authorAbout = jsonObject.optString("about")

        val nameRu = if (authorName.isNotEmpty()) traductor.translate(authorName).getTranslate() else null
        val aboutRu = if (authorAbout.isNotEmpty()) traductor.translate(authorAbout).getTranslate() else null

        if (nameRu == null && aboutRu == null) {
            return null
        }
        return Pair(nameRu, aboutRu)
    }

    suspend fun translateTag(
        error: (Exception) -> Unit,
        success: () -> Unit,
        complete: () -> Unit
    ) {
        val tagInfo = sql.fetchTagInfo()

        if (tagInfo != null && tagInfo != "{\"message\":\"NO_MORE_TRANSLATIONS\"}") {
            val translationResult = performTagTranslation(tagInfo)

            if (translationResult != null) {
                val (tagId, tagRu) = translationResult
                sql.updateTagInfo(tagId, tagRu, error, success, complete)
            } else {
                complete()
            }
        } else {
            complete()
        }
    }


    suspend fun translateAuthor(
        error: (Exception) -> Unit,
        success: () -> Unit,
        complete: () -> Unit
    ) {
        val authorInfo = sql.fetchAuthorInfo()

        if (authorInfo != null && authorInfo != "{\"message\":\"NO_MORE_TRANSLATIONS\"}") {
            val jsonObject = JSONObject(authorInfo)
            val authorId = jsonObject.optInt("id")
            val translationResult = performTranslation(authorInfo)

            if (translationResult != null) {
                val (nameRu, aboutRu) = translationResult
                sql.updateAuthorInfo(authorId, nameRu, aboutRu, error, success, complete)
            } else {
                complete()
            }
        } else {
            complete()
        }
    }


}