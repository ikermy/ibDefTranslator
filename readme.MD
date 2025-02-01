# Проект: ibDefTranslator

## Описание
Микросервис компаньон предназначен для перевода авторов и тегов, для проекта Infobot.
Сервис подписывается на канал появления новых сообщений в Kafka. При появлении новых тагов и\или авторов - переводит их на русский язык.

## Структура проекта
- `src/main/kotlin/ib/translator/Main.kt`: Основной файл, содержащий логику прослушивания сообщений из Kafka и выполнения перевода.
- `src/main/kotlin/ib/translator/r2sql/TransR2SQL.kt`: Класс `TransR2SQL`, содержащий методы для перевода текста с помощью сервиса Reverso.
- `src/main/kotlin/ib/translator/translator/Translator.kt`: Класс `Translator`, содержащий методы для перевода авторов и тегов.
- `src/main/resources/logback.xml`: Конфигурация логирования.

## Зависимости
Проект использует следующие зависимости:
- Kotlin
- Gradle
- kotlinx.coroutines
- org.json

## Установка
1. Клонируйте репозиторий:
    ```sh
    git clone https://github.com/ikermy/ibDefTranslator
    ```
2. Перейдите в директорию проекта:
    ```sh
    cd ibDefTranslator
    ```
3. Синхронизируйте проект с Gradle:
    ```sh
    ./gradlew build
    ```

## Конфигурация
Убедитесь, что Kafka запущен и доступен по адресу `localhost:9092`. Настройки Kafka можно изменить в методе `kafkaMessagesFlow` в файле `Main.kt`.

## Запуск
Для запуска проекта выполните следующую команду:
```sh
./gradlew run
```

## Логирование
Логи сохраняются в файл `/var/log/info-bot/ibDefTranslator/ibDefTranslator.log` и выводятся в консоль. Конфигурация логирования находится в файле `logback.xml`.

## Автор

- [kermy](https://github.com/ikermy)

## Вклад

Если вы хотите внести вклад в проект, пожалуйста, создайте форк репозитория и отправьте pull request.

## Лицензия

Этот проект лицензирован под лицензией MIT. Подробности смотрите в файле `LICENSE`.