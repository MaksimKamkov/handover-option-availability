#language: ru
@handover-option-availability
@allure.label.endpoint:api/v1/handover-options/delivery
Функционал: Метод возвращает подробную информацию о доставке товара(ов) по идентификаторам способа получения

  @allure.label.owner:daklimenko
  @allure.label.jira:CS-9363
  @allure.id:6210
  @allure.label.requirement:Сервис_должен_вернуть_interval_delivery_при_низкой_точности_адреса
  Сценарий: Проверка доступности interval-delivery при низкой точности адреса
    И тестировщик авторизовался с помощью токена "realmsmasteropenidconnecttoken/token.txt"
    Пусть тестировщик планирует вызывать метод "api/v2/handover-options/delivery"
    И тело запроса будет "handoveroptionsdelivery/request/9363/9363-request.json"
    И запрос будет содержать следующие заголовки:
      | Accept | application/json |
    Когда автотест делает POST вызов в приложение "handover-option-availability"
    Тогда он получает ответ с кодом "200"
    И тело ответа JSON соответствует ожидаемому результату "handoveroptionsdelivery/response/9363/9363-response.json" по правилу проверки "NON_EXTENSIBLE"