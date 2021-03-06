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

package com.exorath.service.stats;

import com.exorath.service.commons.portProvider.PortProvider;
import com.exorath.service.stats.res.GetStatAggregateReq;
import com.exorath.service.stats.res.GetTopPlayersReq;
import com.exorath.service.stats.res.PostStatReq;
import com.exorath.service.stats.res.Success;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import spark.Route;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;

/**
 * Created by toonsev on 6/5/2017.
 */
public class Transport {
    private static final Gson GSON = new Gson();

    public static void setup(Service service, PortProvider portProvider) {
        port(portProvider.getPort());
        get("/games/:gameId/player/:uuid/stat/:statId", getGetStatAggregateRoute(service), GSON::toJson);
        post("/games/:gameId/player/:uuid/stat/:statId", getPostStatRoute(service), GSON::toJson);
        get("/games/:gameId/stat/:statId/top/weekly", getGetWeeklyTopRoute(service), GSON::toJson);

    }

    private static Route getGetStatAggregateRoute(Service service) {
        return (req, res) -> {
            Long since = req.queryParams().contains("since") ? Long.valueOf(req.queryParams("since")) : null;
            return service.getStatAggregate(new GetStatAggregateReq(req.params("gameId"), req.params("uuid"), req.params("statId"), since));
        };
    }


    private static Route getGetWeeklyTopRoute(Service service) {
        return (req, res) -> {
            int amount = req.queryParams().contains("amount") ? Integer.valueOf(req.queryParams("amount")) : 3;
            return service.getTopWeeklyPlayers(new GetTopPlayersReq(req.params("gameId"), req.params("statId"), amount));
        };
    }

    private static Route getPostStatRoute(Service service) {
        return (req, res) -> {
            JsonObject body = GSON.fromJson(req.body(), JsonObject.class);
            if (!body.has("amount"))
                return new Success("Body does not have 'amount'", -2);
            String name = req.queryParams().contains("name") ? req.queryParams("name") : null;
            return service.postStat(new PostStatReq(req.params("gameId"), name, req.params("uuid"), req.params("statId"), body.get("amount").getAsInt()));
        };
    }
}
