# StatsService
The statsService is responsible for keeping track of numeric stats of games


### Endpoints

#### /games/{gameId}/player/{uuid}/stat/{statId}?since=1496675655742 [GET] - Get an agregated stat response

**Response**
```json
{
"amount": 215
}
```

#### /games/{gameId}/player/{uuid}/stat/{statId} [POST] - Add a stat entry
This will automatically be logged at the current time of the receiving service.

**Request**
```json
{
"amount": 5
}
```

**Response**
```json
{
"Success": true
}
```

## Environment
| Name | Value |
| --------- | --- |
| MONGO_URI | {mongo_uri} |
| DB_NAME | {db name to store data} |