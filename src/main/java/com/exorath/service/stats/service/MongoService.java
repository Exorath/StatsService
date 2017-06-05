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
import com.exorath.service.stats.res.GetStatAggregateReq;
import com.exorath.service.stats.res.GetStatAggregateRes;
import com.exorath.service.stats.res.PostStatReq;
import com.exorath.service.stats.res.Success;
import com.mongodb.MongoClient;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Arrays;

/**
 * Created by toonsev on 6/5/2017.
 */
public class MongoService implements Service {
    private MongoCollection<Document> statsCollection;

    public MongoService(MongoClient client, String databaseName) {
        MongoDatabase db = client.getDatabase(databaseName);
        statsCollection = db.getCollection("stats");
        statsCollection.createIndex(new Document("uuid", 1).append("time", 1).append("statId", 1).append("gameId", 1).append("flavorId", 1));
    }

    public Success postStat(PostStatReq req) {
        try {
            statsCollection.insertOne(
                    new Document("uuid", req.getPlayerId())
                            .append("time", System.currentTimeMillis())
                            .append("gameId", req.getGameId())
                            .append("statId", req.getStatId())
                            .append("amount", req.getAmount()));
            return new Success(true);
        } catch (Exception e) {
            e.printStackTrace();
            return new Success(e.getMessage(), -1);
        }
    }

    public GetStatAggregateRes getStatAggregate(GetStatAggregateReq req) {
        Document innerMatch = new Document("statId", req.getStatId()).append("gameId", req.getGameId()).append("uuid", req.getPlayerId());
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
