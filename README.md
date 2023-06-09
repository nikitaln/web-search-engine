# Поисковый движок на Spring Boot

Проект реализован на основе Spring Boot и сборщика Maven с использованием Mysql, JPA и Hibernate. Приложение индексирует сайт(ы), чтобы потом находить наиболее релевантрные страницы по любому поисковому запросу.

## Настройка для запуска

**1. Клонирование проекта на личный ПК**

```
git clone https://github.com/nikitaln/web-search-engine.git
```

**2. Создание Mysql таблицы**

```
create database search_engine
```

**3. Изменение mysql username и password в соответствии с вашей установкой**

+ открыть `application.yml`
+ изменить `spring:datasourse:username` и `spring:datasourse:password`

**4. Запуск приложения**

Приложение запускается на локальном порту http://localhost:8080