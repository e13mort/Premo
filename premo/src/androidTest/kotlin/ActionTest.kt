import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import me.dmdev.premo.Action
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("EXPERIMENTAL_API_USAGE")
class ActionTest {

    @Test
    fun skipEmissionWhileReceiverIsBusy() = runBlockingTest {
        val action = Action<Int>()
        val results = mutableListOf<Int>()
        val job = launch {
            action.flow()
                .collect {
                    results += it
                    delay(5000)
                }
        }

        action.invoke(1)
        action.invoke(2)
        advanceTimeBy(6000)
        action.invoke(3)
        job.cancel()

        assertEquals(2, results.count())
        assertFalse { results.contains(2) }
    }
}