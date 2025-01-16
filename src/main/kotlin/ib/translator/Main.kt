package ib.translator

import ib.assembly.traductor.Traductor
import ib.translator.r2sql.TransR2SQL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.time.Duration
import java.util.*


val traductor = Traductor()
val sql = TransR2SQL()
val semaphore = Semaphore(1)

suspend fun translateAuthor(): Unit = coroutineScope {
    try {
        sql.translateAuthor(traductor,
            complete = {
                Traductor.Log.info { "Translation authors completed. Waiting new articles from kafka" }
//                launch(Dispatchers.Default) {
//                    listenToKafka()
//                }
            },
            success = {
                println("Translation success")
                launch(Dispatchers.IO) {
                    translateAuthor()
                }
            },
            error = { Traductor.Log.error { "Translation error: ${it.message}" } }
        )
    } catch (e: Exception) {
        Traductor.Log.error { "Error translateAuthor: ${e.message}" }
    }
}

suspend fun listenToKafka(): Unit = coroutineScope {
    val consumerProps = Properties().apply {
        put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        put(ConsumerConfig.GROUP_ID_CONFIG, "translation-group")
        put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest") // или "latest"
    }

    val consumer = KafkaConsumer<String, String>(consumerProps)
    consumer.subscribe(listOf("onMessage"))

    while (true) {
        val records = consumer.poll(Duration.ofMillis(2000))
        for (record in records) {
            println("Received message: ${record.value()}")
            semaphore.withPermit {
                launch(Dispatchers.IO) {
                    translateAuthor()
                }
            }
        }
        consumer.commitSync() // Помечаем сообщения как прочитанные
    }
}

suspend fun main() {
    println("Start translation")
//    translateAuthor()
    // Прослушивание сообщений из Kafka
    listenToKafka()
}