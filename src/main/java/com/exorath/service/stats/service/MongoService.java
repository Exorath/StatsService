/*
 * Copyright 2017 Exorath
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.exorath.service.stats.service;

import com.exorath.service.stats.Service;
import com.exorath.service.stats.res.*;
import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Updates.inc;

/**
 * Created by toonsev on 6/5/2017.
 */
public class MongoService implements Service {
    private static final Long WEEK_MILLIS = TimeUnit.DAYS.toMillis(7);
    private MongoCollection<Document> weeklyCollection;
    private MongoCollection<Document> totalCollection;
    private MongoCollection<Document> statsCollection;

    public MongoService(MongoClient client, String databaseName) {
        MongoDatabase db = client.getDatabase(databaseName);
        weeklyCollection = db.getCollection("weekly");
        weeklyCollection.createIndex(new Document("uuid", 1).append("statId", 1).append("gameId", 1).append("amount", -1));
        totalCollection = db.getCollection("total");
        totalCollection.createIndex(new Document("uuid", 1).append("statId", 1).append("gameId", 1).append("amount", -1));
        statsCollection = db.getCollection("stats");
        statsCollection.createIndex(new Document("uuid", 1).append("time", 1).append("statId", 1).append("gameId", 1).append("flavorId", 1));
    }

    public Success postStat(PostStatReq req) {
        try {
            statsCollection.insertOne(
                    getBase(req.getPlayerId(), req.getGameId(), req.getStatId())
                            .append("time", System.currentTimeMillis())
                            .append("amount", req.getAmount()));
            weeklyCollection.updateOne(getBase(req.getPlayerId(), req.getGameId(), req.getStatId()).append("week", getWeek()), inc("amount", req.getAmount()), new UpdateOptions().upsert(true));
            totalCollection.updateOne(getBase(req.getPlayerId(), req.getGameId(), req.getStatId()), inc("amount", req.getAmount()), new UpdateOptions().upsert(true));
            return new Success(true);
        } catch (Exception e) {
            e.printStackTrace();
            return new Success(e.getMessage(), -1);
        }
    }

    @Override
    public GetTopPlayersRes getTopWeeklyPlayers(GetTopPlayersReq req) {
        FindIterable<Document> iterable = weeklyCollection.find(new Document("gameId", req.getGameId()).append("statId", req.getStatId()).append("week", getWeek()))
                .limit(req.getAmount())
                .sort(new Document("amount", -1));
        List<TopPlayer> topPlayers = new ArrayList<>();
        for (Document document : iterable)
            topPlayers.add(new TopPlayer(document.getString("uuid"), document.getInteger("amount")));
        return new GetTopPlayersRes(topPlayers);
    }

    private Document getBase(String uuid, String gameId, String statId) {
        return new Document("uuid", uuid)
                .append("gameId", gameId)
                .append("statId", statId);
    }

    private long getWeek() {
        return new Double(Math.floor(System.currentTimeMillis() / WEEK_MILLIS.doubleValue())).longValue();
    }

    public GetStatAggregateRes getStatAggregate(GetStatAggregateReq req) {
        Document innerMatch = getBase(req.getPlayerId(), req.getGameId(), req.getStatId());
        if (req.getSince() != null)
            innerMatch.append("time", new Document("$gt", req.getSince()));
        Document matchDoc = new Document("$match", innerMatch);
        Document groupDoc = new Document("$group", new Document("_id", "$uuid").append("total", new Document("$sum", "$amount")));
        AggregateIterable<Document> aggregateIterable = statsCollection.aggregate(Arrays.asList(matchDoc, groupDoc));
        Document result = aggregateIterable.first();
        if (result == null)
            return new GetStatAggregateRes(0);
        return new GetStatAggregateRes(result.getInteger("total"));
    }
}
