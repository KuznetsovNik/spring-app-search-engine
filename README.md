# spring-app-search-engine

## Само приложение разделено на три этапа:
>Парсинг сайтов
>>Парсинг всех страниц и добавление всех данных с них в БД
>>>Поисковый запрос

## Технологии:

![Java](https://img.shields.io/badge/java_17-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white)
![MySQL](https://img.shields.io/badge/mysql-%2300f.svg?style=for-the-badge&logo=mysql&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=for-the-badge&logo=Hibernate&logoColor=white)
![Spring](https://img.shields.io/badge/spring_boot_2.6.14-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/-Thymeleaf-brightgreen?style=for-the-badge)
![Maven](https://img.shields.io/badge/-Maven-blue?style=for-the-badge)
![Lombok](https://img.shields.io/badge/-Lombok-red?style=for-the-badge)

## Описание:
    Поисковый движок по сайту - приложение, которое позволяет индексировать страницы и осуществлять по ним быстрый поиск. 

* Во время индексации вся актуальная информация отображается на Dashboard сайта.

![Indexing](https://user-images.githubusercontent.com/125637389/230998777-e16b989a-b30d-4724-a20c-16bbadd9e29a.JPG)

![Dashboard](https://user-images.githubusercontent.com/125637389/230997693-272f82c6-52b3-4f20-a56c-36ca20c43d47.JPG)

* Во вкладке Managment мы можем запустить индексацию, остановить её(если она запущенна, кнопка поменяется) или проиндексировать отдельную страницу.

![Managment](https://user-images.githubusercontent.com/125637389/230999280-102c8bd5-038e-4ea7-9445-28fc1e4fa471.JPG)

![ChangeStatus](https://user-images.githubusercontent.com/125637389/230999289-4935de63-4dcf-48ec-8a1d-e450107e2794.JPG)

* В случае принудительной остановки или возникновении ошибки вся информация также отобразится на вкладке Dashboard

![StopIndexing](https://user-images.githubusercontent.com/125637389/231000114-a76e8a94-1a6c-4a1b-bf17-53311501eea6.JPG)

![Exception](https://user-images.githubusercontent.com/125637389/231000119-d217c91a-c5e2-4dc2-b21f-33ea38ab8a72.JPG)

* Поиск можно проводить по всем сайтам или на отдельных, выбирая варианты на вкладке Search. Также поиск отбирает страницы по релевантности

![daeed28c3e05ed70076890cee2ba87cb](https://user-images.githubusercontent.com/125637389/231004703-3bee0486-478d-43a2-9630-d578ca87e0c0.gif)

## Настройки для запуска:
### Зависимости:
Для успешного скачивания и подключения к проекту зависимостей из GitHub необходимо настроить Maven конфигурацию в файле settings.xml.
Так как для доступа требуется авторизации по токену для получения данных из публичного репозитория, для указания токена, найдите файл settings.xml.

    В Windows он располагается в директории C:/Users/<Имя вашего пользователя>/.m2
    В Linux директория /home/<Имя вашего пользователя>/.m2
    В macOs по адресу /Users/<Имя вашего пользователя>/.m2
    Токен, строка которую надо вставить в тег <value>wtb5axJDFX9Vm_W1Lexg</value> 

и добавьте внутри тега settings текст конфигурации:

    <servers>
      <server>
        <id>skillbox-gitlab</id>
        <configuration>
          <httpHeaders>
           <property>
              <name>Private-Token</name>
             <value>token</value>
            </property>
         </httpHeaders>
       </configuration>
      </server>
    </servers>


❗️Если файла нет, то создайте settings.xml и вставьте в него:

    <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
              xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
     https://maven.apache.org/xsd/settings-1.0.0.xsd">

      <servers>
        <server>
          <id>skillbox-gitlab</id>
          <configuration>
            <httpHeaders>
             <property>
               <name>Private-Token</name>
                <value>token</value>
              </property>
            </httpHeaders>
          </configuration>
        </server>
      </servers>

    </settings>

* После этого, в проекте обновите зависимости (Ctrl+Shift+O / ⌘⇧I) или принудительно обновите данные из pom.xml.

## Запуск приложения:
* Запустить приложение можно из любой IDE
* прописав пути к spring.datasource.username, spring.datasource.password и spring.datasource.url в application.yml или переменных окружения

      spring:
        datasource:
          username: root # имя пользователя
          password: Kimk7FjT # пароль пользователя
          url: jdbc:mysql://localhost:3306/search_engine?useSSL=false&requireSSL=false&allowPublicKeyRetrieval=true
          
* а также изменить сайты для индексации

      indexing-settings:
        sites:
          - url: https://www.tretyakovgallery.ru/
            name: tretyakovgallery.ru
          - url: http://kuprin-lit.ru/
            name: kuprin-lit.ru
    
Запуск из командной строки:
В корне проекта выполнить команду для сборки проекта:
    
    mvn package
    
Перейти в папку target:
   
    cd target
    
Запустить проект:
    
    java -jar <имя_файла.jar>

# Утилиты:
* Использовал для проверки ![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-micro&logo=postman&logoColor=white)

# Статус:
Проект: закончен

В будущем: возможно добавлю покрытие тестами  и упакую в docker ![JUnit](https://img.shields.io/badge/-JUnit-success) ![Docker](https://img.shields.io/badge/-Docker-9cf)

# Контакты:
Создатель Кузнецов Никита, не стесняйтесь обратиться ко мне!

+7(937)777-25-86 | xax_xax@list.ru | Telegram: @Stifler116 

[![codewars](https://www.codewars.com/users/Kamenolom/badges/small)](https://www.codewars.com/users/Kamenolom)
