package com.cheercent.xnetty.httpserver.logic.user;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cheercent.xnetty.httpserver.base.XLogic;
import com.cheercent.xnetty.httpserver.base.XLogicConfig;
import com.cheercent.xnetty.httpserver.base.XRedis;
import com.cheercent.xnetty.httpserver.base.XRedis.RedisKey;
import com.cheercent.xnetty.httpserver.conf.DataKey;
import com.cheercent.xnetty.httpserver.conf.RedisConfig;
import com.cheercent.xnetty.httpserver.conf.SearchConfig.SearchType;
import com.cheercent.xnetty.httpserver.model.search.UserSearchModel;
import com.cheercent.xnetty.httpserver.util.CommonUtils;

@XLogicConfig(name = "search", requiredParameters = {
        DataKey.KEYWORDS})
public final class Search extends XLogic {

    @Override
    protected boolean requireSession() {
        return false;
    }

    private String keywords = null;

    private Integer pagenum = null;
    private Integer pagesize = null;

    @Override
    protected String prepare() {
        keywords = this.getString(DataKey.KEYWORDS);
        if (keywords.length() < 1) {
            return errorParameterResult("KEYWORDS_ERROR");
        }

        pagenum = this.getInteger(DataKey.PAGENUM);
        if (pagenum == null || pagenum < 1) {
            pagenum = 1;
        }
        
        pagesize = this.getInteger(DataKey.PAGESIZE);
        if (pagesize == null || pagesize < 10 || pagesize > 100) {
            pagesize = 50;
        }

        return null;
    }

    @Override
    protected String execute() {
        JSONObject resultData = new JSONObject();
        RedisKey redisKey = RedisConfig.SEARCH_TYPE_RESULT.build().append(SearchType.user).append(CommonUtils.md5(keywords)).append(pagesize).append(pagenum);
        String redisData = XRedis.get(redisKey);
        if (redisData == null) {
            UserSearchModel userModel = new UserSearchModel();
            JSONArray userList = userModel.getList(keywords, pagenum, pagesize);
            if (userList == null) {
                return errorInternalResult();
            }
            resultData.put(DataKey.USER, userList);

        } else {
            resultData = JSONObject.parseObject(redisData);
        }

        return this.successResult(resultData);
    }

}
