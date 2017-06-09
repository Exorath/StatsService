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

package com.exorath.service.stats.api;

import com.exorath.service.stats.Service;
import com.exorath.service.stats.res.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.GetRequest;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;

/**
 * Created by toonsev on 6/5/2017.
 */
public class StatsServiceAPI implements Service {
    private static final Gson GSON = new Gson();
    private String address;

    public StatsServiceAPI(String address) {
        this.address = address;
    }

    @Override
    public Success postStat(PostStatReq req) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("amount", req.getAmount());
            HttpRequestWithBody httpRequestWithBody = Unirest.post(url("/games/{gameId}/player/{uuid}/stat/{statId}"))
                    .routeParam("gameId", req.getGameId())
                    .routeParam("uuid", req.getPlayerId())
                    .routeParam("statId", req.getStatId());
            if (req.getPlayerName() != null)
                httpRequestWithBody.queryString("name", req.getPlayerName());
            return
                    GSON.fromJson(httpRequestWithBody.body(body.toString()).asString().getBody(), Success.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new Success(e.getMessage(), -1);
        }
    }

    @Override
    public GetStatAggregateRes getStatAggregate(GetStatAggregateReq req) {
        try {
            GetRequest request = Unirest.get(url("/games/{gameId}/player/{uuid}/stat/{statId}"))
                    .routeParam("gameId", req.getGameId())
                    .routeParam("uuid", req.getPlayerId())
                    .routeParam("statId", req.getStatId());
            if (req.getSince() != null)
                request.queryString("since", req.getSince());
            return GSON.fromJson(request.asString().getBody(), GetStatAggregateRes.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public GetTopPlayersRes getTopWeeklyPlayers(GetTopPlayersReq req) {
        try {
            HttpRequest request = Unirest.get(url("/games/{gameId}/stat/{statId}/top/weekly"))
                    .routeParam("gameId", req.getGameId())
                    .routeParam("statId", req.getStatId())
                    .queryString("amount", req.getAmount());
            return GSON.fromJson(request.asString().getBody(), GetTopPlayersRes.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String url(String endpoint) {
        return address + endpoint;
    }
}
