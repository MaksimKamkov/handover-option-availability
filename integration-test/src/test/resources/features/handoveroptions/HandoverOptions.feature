#language: ru
@handover-option-availability
@allure.label.endpoint:api/v1/handover-options
Функционал: Метод возвращает краткую информацию о доступных способах получения товаров

  @allure.label.owner:daklimenko
  @allure.label.jira:CS-8586
  @allure.id:2459
  @allure.label.requirement:Сервис_должен_вернуть_все_доступные_partnerBrand_не_зависимо_от_availableDate
  Сценарий: Проверка доступности partnerBrand
    И тестировщик авторизовался с помощью токена "realmsmasteropenidconnecttoken/token.txt"
    Пусть тестировщик планирует вызывать метод "api/v1/handover-options"
    И тело запроса будет "handoveroptions/request/2459/2459-request.json"
    И запрос будет содержать следующие заголовки:
      | Accept | application/json |
    Когда автотест делает POST вызов в приложение "handover-option-availability"
    Тогда он получает ответ с кодом "200"
    И тело ответа JSON соответствует ожидаемому результату "handoveroptions/response/2459/2459-response.json" по правилу проверки "NON_EXTENSIBLE"