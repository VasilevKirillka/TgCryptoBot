## Как запустить

При открытии директории через IntelliJ IDEA проект должен автоматически распознаться.
У вас должна появится `run configuration CryptoBotApplication` (зеленый треугольник),
если не появился, можно пройти в класс `CryptoBotApplication` и оттуда напрямую вызвать `main` метод.

## О проекте

Сервис, с помощью которого пользователи смогут подписаться на курс криптовалюты в вашем телеграм-боте.
Имя бота в Telegram `SnamCrypto`.

Основные функции приложения:
подписка на конкретную стоимость биткоина;
вывод информации о текущей подписке;
возможность включить или отключить подписку;
отправка уведомлений, если стоимость биткоина становится меньше,чем стоимость подписки.

Поддерживаемые команды:
`/start` - выводит список всех команд. Tакже для удобства выводится виртуальная клавиатура;
`/get_price` - выводит актуальную стоимость биткоина;
`/get_subscription` - выводит текущую подписку;
`/subscribe <стоимость биктоина>` - подписывает пользователя на стоимость биткоина;
`/unsubscribe` - отменяет подписку пользователя.

### База данных

Для удобной работы с postgreSql используется докер-образ,
вам не нужно самостоятельно устанавливать postgreSql.
Выполнив `docker-compose up`, вы запустите postgreSql с предустановленными
настройками (их посмотреть можно в файле `docker-compose.yaml`).
Для запуска докер образа вам понадобится установить `docker` и `docker-compose`

### Spring проект

В данном проекте используется Spring, gradle используется в качестве системы сборки.
Основные зависимости уже указаны, но никто вам не запрещает добавлять новые.

В `application.yml` находятся конфигурируемые параметры нашего приложения
(в частности, там указывается токен вашего бота)

Для получения `telegram.bot.token` и `telegram.bot.username` нужно в Telegram перейти на `@BotFather`
и следовать инструкции по созданию нового бота.
