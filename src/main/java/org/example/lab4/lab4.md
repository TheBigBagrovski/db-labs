
## Программа работы
1. Общее описание:
    - для данной работы необходимо выбрать часть таблиц БД (3+), для которой можно придумать/использовать осмысленные SQL-запросы, необходимые для выполнения пользовательских функций
    - в рамках работы необходимо реализовать две программы: кэширующий прокси и программа для выполнения запросов и измерения результатов
2. Выбор понравившегося способа кэширования:
    - в памяти программы
    - с использованием внешних хранилищ
    - во внешней памяти
3. Реализация выбранного способа
    - преобразование входных запросов
    - выбор ключа для хранения результатов
    - реализация алгоритма поиска сохраненных результатов, которые надо сбросить после внесения изменений в БД
4. Снятие показательных характеристик
    - в программе для формирования потока запросов к БД на чтение/изменение/удаление должна быть возможность настройки соотношения запросов, количества запросов разных типов в потоке и измерения временных характеристик: среднее/минимальное/максимальное время выполнения запроса по типу, необходимо иметь возможность проанализировать эффективность кэширования различных сценариев: преимущественно чтение, преимущественно изменение, преимущественно удаление.
    - измерения можно производить путем простого сравнения отметок текущего времени до и после выполнения запросов
5. Анализ полученных результатов и сравнение реализаций с кэшем и без между собой.
6. Демонстрация результатов преподавателю.

## Ход работы

В рамках данной лабораторной работы на вход БД поступают запросы на чтение (SELECT), добавление (INSERT), изменение (UPDATE) и удаление (DELETE) записей в таблицах personal_data, cart, store.

В модуле стандартной библиотеки Python functools реализован декоратор @lru_cache, дающий возможность кэшировать результат функций, используя стратегию Least Recently Used (LRU, «вытеснение давно неиспользуемых»). Кэш, реализованный посредством стратегии LRU, упорядочивает элементы в порядке их использования. При каждом обращении к записи алгоритм LRU перемещает ее в верхнюю часть кэша. Таким образом, алгоритм может быстро определить запись, которая дольше всех не использовалась, и удалить её.

Кэширование результатов выполнения запроса было реализовано следующим образом:

```
@lru_cache(maxsize=128)
def cache_query(query):
    if query.split(" ")[0] in ["INSERT", "UPDATE", "DELETE"]:
        cur.execute(query)
        cache_query.cache_clear()
    else:
        cur.execute(query)
```

Декоратор lru_cache был импортирован из модуля functools и применён к функции cache_query. Декораторы - это функции-обертки, которые объявляются перед определением функции и позволяют модифицировать её поведение. В данном случае мы сохраняем в памяти результат, полученный для одних и тех же запросов на чтение, и затем возвращаем его при следующем аналогичном запросе.

У декоратора @lru_cache есть атрибут maxsize, определяющий максимальное количество записей до того, как кэш начнет удалять старые элементы. По умолчанию maxsize равен 128.

Если же приходит запрос на изменение (INSERT, UPDATE, DELETE), то сохранённые результаты запросов на чтение становятся неактуальными и приходится очищать кэш, для чего используется метод .cache_clear().

Для выполнения запросов и измерения результатов была написана следующая функция:

```
def execute_queries(is_cached, queries, n):
    max_time = 0
    min_time = sys.maxsize
    sum_time = 0

    for ind in range(n):
        start_time = time()
        query = queries[ind]

        if is_cached:
            cache_query(query)
        else:
            cur.execute(query)

        conn.commit()

        sleep(0.001)

        end_time = time()

        result_time = end_time - start_time
        if max_time < result_time:
            max_time = result_time
        if min_time > result_time:
            min_time = result_time
        sum_time += result_time

    print('\tКоличество выполненных запросов \n\t' + str(n))
    print('\tСреднее время выполнения запроса \n\t' + str(round((sum_time / n), 5)))
    print('\tСуммарное время выполнения запросов \n\t' + str(round(sum_time, 5)))
    print('\tМинимальное время выполнения запроса \n\t' + str(round(min_time, 5)))
    print('\tМаксимальное время выполнения запроса \n\t' + str(round(max_time, 5)))
```

Ключевым замеряемым параметром является среднее время выполнения запроса, однако суммарное время выполнения запросов более наглядно отражает разницу во времени между выполнением запросов с использованием кэширования и без него.

Для удобства сравнения минимальной скорости выполнения запроса была добавлена задержка с помощью функции time.sleep().

Первым было замеряно время необходимое на выполнение 1000 запросов на чтение для таблицы personal_data:

```
number_of_queries = 1000

personal_data_select_queries = dict()
for i in range(number_of_queries):
    ids = set()
    while len(ids) < 100:
        ids.add(random.randint(1, 101))
    personal_data_select_queries[i] = \
        'SELECT * FROM personal_data WHERE id IN (' + str(ids).strip('{}') + ');'

print('SELECT для personal_data')
print('\nisCashed = True')
execute_queries(True, personal_data_select_queries, number_of_queries)
print()
print(cache_query.cache_info())
print('\nisCashed = False')
execute_queries(False, personal_data_select_queries, number_of_queries)
cache_query.cache_clear()
print()
```

В данном случае запросы на чтение отличается друг от друга списком читаемых записей. В данном случае всего есть 101 возможный вариант приходящего запроса.

| Запрос                   | isCashed | N    | AVG     | SUM      | MIN     | MAX     |
| ------------------------ | -------- | ---- | ------- | -------- | ------- | ------- |
| SELECT для personal_data | True     | 1000 | 0.01608 | 16.08296 | 0.01514 | 0.06249 |
| SELECT для personal_data | False    | 1000 | 0.01716 | 17.15638 | 0.01515 | 0.03143 |

Использование кэша позволило выполнить 1000 запросов на чтение на секунду быстрее, чем без него.

Для измерения эффективности использования кэша для функции cache_query был вызван метод .cache_info(), который возвращает:
- hits – количество вызовов, которые @lru_cache вернул непосредственно из памяти, поскольку они присутствовали в кэше
- misses – количество вызовов, которые взяты не из памяти, а были вычислены
- maxsize – это размер кэша, который был определён
- currsize – текущий размер кэша

```
CacheInfo(hits=899, misses=101, maxsize=128, currsize=101)
```

Из этой информации можно сделать вывод, что среди 1000 запросов присутствовал каждый из 101 возможного варианта запроса. Размера кэша хватило на то, чтобы сохранить их все.

Следующим было замеряно время необходимое на выполнение 3000 запросов на чтение для таблицы cart:

```
number_of_queries = 3000

cart_select_queries = dict()
for i in range(number_of_queries):
    ids = set()
    while len(ids) < 100:
        ids.add(random.randint(1, 101))
    cart_select_queries[i] = \
        'SELECT * FROM cart WHERE id IN (' + str(ids).strip('{}') + ');'

print('SELECT для cart')
print('\nisCashed = True')
execute_queries(True, cart_select_queries, number_of_queries)
print()
print(cache_query.cache_info())
print('\nisCashed = False')
execute_queries(False, cart_select_queries, number_of_queries)
cache_query.cache_clear()
print()
```

В данном случае тоже был всего 101 возможный вариант приходящего запроса.

| Запрос                   | isCashed | N    | AVG     | SUM      | MIN     | MAX     |
| ------------------------ | -------- | ---- | ------- | -------- | ------- | ------- |
| SELECT для cart          | True     | 3000 | 0.01745 | 52.36116 | 0.01117 | 0.03616 |
| SELECT для cart          | False    | 3000 | 0.01816 | 54.46832 | 0.00202 | 0.03134 |

На этот раз использование кэша дало преимущество в 2 секунды.

```
CacheInfo(hits=2899, misses=101, maxsize=128, currsize=101)
```

Количетсво попаданий заметно возросло, что является логичным следствием увеличения количества запросов. Размера кэша всё ещё хватает.

Для того чтобы рассмотреть ситуацию, когда количество вариантов запросов на чтение превосходит максимальный размер кэша, был написан следующий код для составления запросов к таблице store:

```
number_of_queries = 1000

store_select_queries = dict()
for i in range(number_of_queries):
    ids = set()
    while len(ids) < 29:
        ids.add(random.randint(1, 31))
    store_select_queries[i] = \
        'SELECT * FROM store WHERE id IN (' + str(ids).strip('{}') + ');'

print('SELECT для store')
print('\nisCashed = True')
execute_queries(True, store_select_queries, number_of_queries)
print()
print(cache_query.cache_info())
print('\nisCashed = False')
execute_queries(False, store_select_queries, number_of_queries)
cache_query.cache_clear()
print()
```

Кэш-попадания в этом случае происходили гораздо реже, чем в предыдущих двух, что существенно сказалось на времени выполнения запросов, и алгоритм с использованием кэша впервые уступил алгоритму без его использования.

| Запрос                   | isCashed | N    | AVG     | SUM      | MIN     | MAX     |
| ------------------------ | -------- | ---- | ------- | -------- | ------- | ------- |
| SELECT для store         | True     | 1000 | 0.01752 | 17.52461 | 0.00172 | 0.03879 |
| SELECT для store         | False    | 1000 | 0.01733 | 17.33424 | 0.002   | 0.03043 |

Измерение эффективности использования кэша:

```
CacheInfo(hits=257, misses=743, maxsize=128, currsize=128)
```

Впервые кэш был заполнен полностью из-за чего ему пришлось удалять записи, которые дольше всего не использовалась.

Далее был написан код для формирования потока запросов на чтение/добавление/изменение/удаление записей в таблице personal_data. Было замеряно время для различного соотношения запросов на изменение и запросов на чтение в потоке запросов.

Формирование запросов вставки и чтения записей:

```
number_of_insert_queries = 1024
fake = Faker(locale="ru_RU")
insert_select_queries = dict()
for i in range(number_of_insert_queries):
    gender = fake.random.choice(["М", "Ж"])
    if gender == "М":
        first_name = fake.first_name_male()
        last_name = fake.last_name_male()
    else:
        first_name = fake.first_name_female()
        last_name = fake.last_name_female()
    date_of_birth = fake.date_of_birth()
    phone_number = fake.phone_number()

    insert_select_queries[i] = 'INSERT INTO personal_data ' \
                               '(id, first_name, last_name, gender, date_of_birth, phone_number) ' \
                               'VALUES (' + str(1000 + i) + ', \'' + first_name + '\', \'' + \
                               last_name + '\', \'' + gender + '\', \'' + str(date_of_birth) + \
                               '\', \'' + phone_number + '\');'

number_of_select_queries = int(ratio[0] * number_of_insert_queries / ratio[1])
for i in range(number_of_insert_queries, number_of_insert_queries + number_of_select_queries):
    insert_select_queries[i] = 'SELECT * FROM personal_data;'

random_insert_select_queries = dict()

i = 0
while len(insert_select_queries) > 0:
    j = fake.random.choice(list(insert_select_queries.keys()))
    random_insert_select_queries[i] = insert_select_queries[j]
    del insert_select_queries[j]
    i += 1
```

Формирование запросов изменения и чтения записей:

```
number_of_update_queries = 1024
update_select_queries = dict()
for i in range(number_of_update_queries):
    gender = fake.random.choice(["М", "Ж"])

    ids = set()
    while len(ids) < random.randint(1, 21):
        ids.add(random.randint(1000, 1000 + number_of_insert_queries - 1))
    update_select_queries[i] = 'UPDATE personal_data SET gender = \'' + gender + \
                               '\' WHERE id IN (' + str(ids).strip('{}') + ');'

number_of_select_queries = int(ratio[0] * number_of_update_queries / ratio[1])
for i in range(number_of_update_queries, number_of_update_queries + number_of_select_queries):
    update_select_queries[i] = 'SELECT * FROM personal_data;'

random_update_select_queries = dict()

i = 0
while len(update_select_queries) > 0:
    j = fake.random.choice(list(update_select_queries.keys()))
    random_update_select_queries[i] = update_select_queries[j]
    del update_select_queries[j]
    i += 1
```

Формирование запросов удаления и чтения записей:

```
number_of_delete_queries = number_of_insert_queries
delete_select_queries = dict()
ids = set()
for i in range(1000, 1000 + number_of_insert_queries):
    ids.add(i)

for i in range(number_of_delete_queries):
    delete_id = random.choice(list(ids))
    delete_select_queries[i] = 'DELETE FROM personal_data WHERE id = ' + str(delete_id) + ';'
    ids.remove(delete_id)

number_of_select_queries = int(ratio[0] * number_of_delete_queries / ratio[1])
for i in range(number_of_delete_queries, number_of_delete_queries + number_of_select_queries):
    delete_select_queries[i] = 'SELECT * FROM personal_data;'

random_delete_select_queries = dict()

i = 0
while len(delete_select_queries) > 0:
    j = fake.random.choice(list(delete_select_queries.keys()))
    random_delete_select_queries[i] = delete_select_queries[j]
    del delete_select_queries[j]
    i += 1
```

Полученные результаты для удобства представлены в виде таблицы:

| Запрос                            | Ratio | isCashed | N    | AVG     | SUM       | MIN     | MAX     |
| --------------------------------- | ----- | -------- | ---- | ------- | --------- | ------- | ------- |
| INSERT + SELECT для personal_data | 1 к 8 | True     | 9216 | 0.01709 | 157.46307 | 0.00126 | 0.0345  |
| INSERT + SELECT для personal_data | 1 к 8 | False    | 9216 | 0.02035 | 187.50903 | 0.00203 | 0.0679  |
| UPDATE + SELECT для personal_data | 1 к 8 | True     | 9216 | 0.01718 | 158.29257 | 0.0016  | 0.03339 |
| UPDATE + SELECT для personal_data | 1 к 8 | False    | 9216 | 0.01976 | 182.06264 | 0.0011  | 0.03141 |
| DELETE + SELECT для personal_data | 1 к 8 | True     | 9216 | 0.01772 | 163.32003 | 0.00158 | 0.04199 |
| DELETE + SELECT для personal_data | 1 к 8 | False    | 9216 | 0.0202  | 186.15051 | 0.00266 | 0.03121 |
| INSERT + SELECT для personal_data | 1 к 4 | True     | 5120 | 0.01833 |  93.85513 | 0.00141 | 0.03144 |
| INSERT + SELECT для personal_data | 1 к 4 | False    | 5120 | 0.02016 | 103.21953 | 0.00374 | 0.03778 |
| UPDATE + SELECT для personal_data | 1 к 4 | True     | 5120 | 0.0188  |  96.25921 | 0.01511 | 0.03139 |
| UPDATE + SELECT для personal_data | 1 к 4 | False    | 5120 | 0.02129 | 109.00534 | 0.005   | 0.03031 |
| DELETE + SELECT для personal_data | 1 к 4 | True     | 5120 | 0.01911 |  97.84918 | 0.00259 | 0.03109 |
| DELETE + SELECT для personal_data | 1 к 4 | False    | 5120 | 0.02026 | 103.7411  | 0.00343 | 0.03108 |
| INSERT + SELECT для personal_data | 1 к 2 | True     | 3072 | 0.01861 |  57.15657 | 0.00266 | 0.03115 |
| INSERT + SELECT для personal_data | 1 к 2 | False    | 3072 | 0.01997 |  61.36087 | 0.00433 | 0.03087 |
| UPDATE + SELECT для personal_data | 1 к 2 | True     | 3072 | 0.01884 |  57.88466 | 0.01513 | 0.03108 |
| UPDATE + SELECT для personal_data | 1 к 2 | False    | 3072 | 0.02154 |  66.1809  | 0.00177 | 0.03132 |
| DELETE + SELECT для personal_data | 1 к 2 | True     | 3072 | 0.01832 |  56.26881 | 0.00116 | 0.03173 |
| DELETE + SELECT для personal_data | 1 к 2 | False    | 3072 | 0.02083 |  63.98273 | 0.00462 | 0.0307  |
| INSERT + SELECT для personal_data | 1 к 1 | True     | 2048 | 0.01954 |  40.01879 | 0.00311 | 0.03112 |
| INSERT + SELECT для personal_data | 1 к 1 | False    | 2048 | 0.02019 |  41.35782 | 0.01505 | 0.03129 |
| UPDATE + SELECT для personal_data | 1 к 1 | True     | 2048 | 0.01991 |  40.78041 | 0.01509 | 0.02981 |
| UPDATE + SELECT для personal_data | 1 к 1 | False    | 2048 | 0.02112 |  43.25351 | 0.01499 | 0.03118 |
| DELETE + SELECT для personal_data | 1 к 1 | True     | 2048 | 0.0196  |  40.13479 | 0.01516 | 0.03073 |
| DELETE + SELECT для personal_data | 1 к 1 | False    | 2048 | 0.02064 |  42.27206 | 0.00303 | 0.03082 |
| INSERT + SELECT для personal_data | 2 к 1 | True     | 1536 | 0.02001 |  30.73929 | 0.01198 | 0.03148 |
| INSERT + SELECT для personal_data | 2 к 1 | False    | 1536 | 0.02044 |  31.39235 | 0.00441 | 0.03061 |
| UPDATE + SELECT для personal_data | 2 к 1 | True     | 1536 | 0.02069 |  31.77423 | 0.01519 | 0.03119 |
| UPDATE + SELECT для personal_data | 2 к 1 | False    | 1536 | 0.02117 |  32.51883 | 0.01526 | 0.03039 |
| DELETE + SELECT для personal_data | 2 к 1 | True     | 1536 | 0.01991 |  30.58079 | 0.0153  | 0.03019 |
| DELETE + SELECT для personal_data | 2 к 1 | False    | 1536 | 0.02043 |  31.3834  | 0.01528 | 0.03035 |
| INSERT + SELECT для personal_data | 4 к 1 | True     | 1280 | 0.02014 |  25.77708 | 0.01515 | 0.03094 |
| INSERT + SELECT для personal_data | 4 к 1 | False    | 1280 | 0.02028 |  25.9598  | 0.01528 | 0.03041 |
| UPDATE + SELECT для personal_data | 4 к 1 | True     | 1280 | 0.02076 |  26.57839 | 0.01518 | 0.02983 |
| UPDATE + SELECT для personal_data | 4 к 1 | False    | 1280 | 0.02084 |  26.68007 | 0.01521 | 0.03106 |
| DELETE + SELECT для personal_data | 4 к 1 | True     | 1280 | 0.0201  |  25.72587 | 0.00434 | 0.03055 |
| DELETE + SELECT для personal_data | 4 к 1 | False    | 1280 | 0.02028 |  25.95395 | 0.00392 | 0.03114 |
| INSERT + SELECT для personal_data | 8 к 1 | True     | 1152 | 0.0201  |  23.15712 | 0.0152  | 0.02941 |
| INSERT + SELECT для personal_data | 8 к 1 | False    | 1152 | 0.02015 |  23.2113  | 0.00273 | 0.03086 |
| UPDATE + SELECT для personal_data | 8 к 1 | True     | 1152 | 0.02063 |  23.76776 | 0.00471 | 0.03111 |
| UPDATE + SELECT для personal_data | 8 к 1 | False    | 1152 | 0.02079 |  23.95099 | 0.00499 | 0.03053 |
| DELETE + SELECT для personal_data | 8 к 1 | True     | 1152 | 0.02008 |  23.13573 | 0.0058  | 0.0302  |
| DELETE + SELECT для personal_data | 8 к 1 | False    | 1152 | 0.02008 |  23.13648 | 0.01521 | 0.03107 |

В данном случае впервые можно наблюдать огромный скачок в производительности. При соотношении запросов на чтение к запросам на изменение = 8:1 использование кэширования позволило сэкономить порядка 30 секунд, что является колоссальным успехом. Однако чем больше увеличивалось соотношение в пользу запросов на изменение, тем меньше был выигрыш от использования кэширования.