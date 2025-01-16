import ib.assembly.traductor.Traductor
import ib.translator.sql
import ib.translator.traductor
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
        var test = false
        try {
            sql.translateAuthor(
                traductor,
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