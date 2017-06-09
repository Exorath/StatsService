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

package com.exorath.service.stats.res;

/**
 * Created by toonsev on 6/5/2017.
 */
public class GetTopPlayersReq {
    private String gameId;
    private String statId;
    private int amount;

    public GetTopPlayersReq() {
    }

    public GetTopPlayersReq(String gameId, String statId, int amount) {
        this.gameId = gameId;
        this.statId = statId;
        this.amount = amount;
    }

    public String getGameId() {
        return gameId;
    }

    public String getStatId() {
        return statId;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GetTopPlayersReq that = (GetTopPlayersReq) o;

        if (amount != that.amount) return false;
        if (gameId != null ? !gameId.equals(that.gameId) : that.gameId != null) return false;
        return statId != null ? statId.equals(that.statId) : that.statId == null;

    }

    @Override
    public int hashCode() {
        int result = gameId != null ? gameId.hashCode() : 0;
        result = 31 * result + (statId != null ? statId.hashCode() : 0);
        result = 31 * result + amount;
        return result;
    }
}
