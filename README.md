
# Exchange Rates Web REST API

This is a web REST API that return JSON objects of currencies USD, EUR, RUB to KZT.
## Authors

- [@beketm](https://github.com/beketm)


## API Reference

#### Get currencies for last 10 days

```http
  GET https://spring-boot-exchange-rates.herokuapp.com/last10days
```
Sample Response:
```json
{
    "2022-04-01":{
        "EUR":525.762329,
        "USD":475.963837,
        "RUB":5.54978991
    },
    "2022-04-02":{
        "EUR":526.315796,
        "USD":476.417328,
        "RUB":5.55598783
    },
    "2022-04-03":{
        "EUR":526.315796,
        "USD":476.417328,
        "RUB":5.65492535
    },
    "2022-04-04":{
        "EUR":513.083618,
        "USD":467.508179,
        "RUB":5.5824132
    },
    "2022-04-05":{
        "EUR":509.164978,
        "USD":466.853394,
        "RUB":5.55802584
    },
    "2022-04-06":{
        "EUR":506.842377,
        "USD":465.116272,
        "RUB":5.66447067
    },
    "2022-04-07":{
        "EUR":493.827148,
        "USD":454.545441,
        "RUB":5.73641205
    },
    "2022-04-08":{
        "EUR":482.625488,
        "USD":443.852631,
        "RUB":5.53057575
    },
    "2022-04-09":{
        "EUR":482.625488,
        "USD":443.852631,
        "RUB":5.53057575
    },
    "2022-04-10":{
        "EUR":483.091797,
        "USD":443.852631,
        "RUB":5.21387291
    }
}
```

#### Get exchange rates by specific date

```http
  GET https://spring-boot-exchange-rates.herokuapp.com/rates-by-date/?date=YYYY-MM-DD
```
Sample Response for 2022-04-09
```json
{
    "EUR": 482.625488,
    "success": 1.0,
    "USD": 443.852631,
    "RUB": 5.53057575
}
```

#### See if currencies rates have change by more than 10%. If they have not changed it will return null,otherwise  change coefficient 

```http
  GET https://spring-boot-exchange-rates.herokuapp.com/rates-change
```
Sample Response if there was **no** change by more than 10%
```json
{
    "EUR": null,
    "RUB": null,
    "USD": null,
    "success": 1.0
}
```

Sample Response if there was change by more than 10%
```json
{
    "EUR": null,
    "RUB": 0.1285,
    "USD": 0.2369,
    "success": 1.0
}
```


