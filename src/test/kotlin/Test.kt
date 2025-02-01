import ib.translator.translator.Translator
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class Test {
    @Test
    fun Starter() {
        runBlocking {
            assertTrue(TranslatorTest(), "TranslatorTest failed")
        }
    }

    suspend fun TranslatorTest(): Boolean {
        val trans = Translator()
        var test = false
        try {
            trans.translateAuthor(
                complete = {
                    test = true
                },
                success = {
                    test = true
                },
                error = {
                    test = false
                }
            )
        } catch (e: Exception) {
            return false
        }
        return test
    }
}