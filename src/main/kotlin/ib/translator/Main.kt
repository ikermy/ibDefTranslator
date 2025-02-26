package ib.translator

import ib.assembly.traductor.Traductor
import ib.translator.translator.Translator
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.util.*
import java.time.Duration


val semaphore = Semaphore(1)

fun kafkaMessagesFlow(): Flow<ConsumerRecord<String, String>> = flow {
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
        val records = consumer.poll(Duration.ofMillis(10_000))
        for (record in records) {
            emit(record)
        }
        consumer.commitSync() // Помечаем сообщения как прочитанные
    }
}

@OptIn(InternalCoroutinesApi::class)
suspend fun listenToKafkaWithFlow() = coroutineScope {
    kafkaMessagesFlow().collect(object : FlowCollector<ConsumerRecord<String, String>> {
        override suspend fun emit(value: ConsumerRecord<String, String>) {
            println("Published new article: ${value.value()}")
            withContext(Dispatchers.IO) {
            semaphore.withPermit {
                translate()
            }
            }
        }
    })
}


suspend fun translate () =  coroutineScope {
    val trans = Translator()
    var flag = true
    while (flag) {
        // Перевод авторов
        trans.translateAuthor(
            error = {
                Traductor.Log.error { "Translation authors error: ${it.message}" }
                flag = false
            },
            success = {
                println("Translation success")
            },
            complete = {
                Traductor.Log.info { "Translation authors completed. Waiting new articles from kafka" }
                flag = false
            }
        )
        delay(1000)
//        sql.translateAuthor(traductor,
//            complete = {
//                Traductor.Log.info { "Translation authors completed. Waiting new articles from kafka" }
//                flag = false
//            },
//            success = {
//                println("Translation success")
//            },
//            error = { Traductor.Log.error { "Translation authors error: ${it.message}" } }
//        )
    }
    // Перевод тегов
    flag = true
    while (flag) {
        trans.translateTag(
            error = {
                Traductor.Log.error { "Translation tags error: ${it.message}" }
                flag = false
            },
            success = {
                println("Translation success")
            },
            complete = {
                Traductor.Log.info { "Translation tags completed. Waiting new articles from kafka" }
                flag = false
            }
        )
        delay(1000)
    }
}

suspend fun main() {
    println("Start translation")
    // Прослушивание сообщений из Kafka
    listenToKafkaWithFlow()
}