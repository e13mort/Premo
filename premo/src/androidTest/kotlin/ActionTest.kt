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
                    println("collected $it")
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

    @Test
    fun multicastActions() = runBlockingTest {
        val action = Action<Int>()
        val results1 = mutableListOf<Int>()
        val results2 = mutableListOf<Int>()
        val job1 = launch {
            action.flow()
                .collect {
                    results1 += it
                    println("receiver1 - $it")
                }
        }
        val job2 = launch {
            action.flow()
                .collect {
                    results2 += it
                    println("receiver2 - $it")
                }
        }

        action.invoke(1)
        action.invoke(2)
        job1.cancel()
        job2.cancel()

        assertEquals(1, results1[0])
        assertEquals(1, results2[0])
        assertEquals(2, results1[1])
        assertEquals(2, results2[1])
    }
}