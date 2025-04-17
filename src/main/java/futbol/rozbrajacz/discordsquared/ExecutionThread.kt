package futbol.rozbrajacz.discordsquared

import kotlinx.coroutines.runBlocking
import java.util.concurrent.ConcurrentLinkedQueue

object ExecutionThread {
	private val queue = ConcurrentLinkedQueue<suspend () -> Unit>()

	val thread = Thread {
		runBlocking {
			while(true) {
				try {
					Thread.sleep(Long.MAX_VALUE)
				} catch(_: InterruptedException) {
					while(queue.isNotEmpty())
						queue.poll()()
				}
			}
		}
	}.apply {
		name = "${Reference.MOD_NAME} Execution Thread"
	}

	fun execute(func: suspend () -> Unit) {
		queue.add(func)
		thread.interrupt()
	}
}
