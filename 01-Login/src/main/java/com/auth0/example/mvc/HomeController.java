package com.auth0.example.mvc;

import com.auth0.Tokens;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Iterator;
import java.util.Map;

@SuppressWarnings("unused")
@Controller
public class HomeController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${api.server}")
    private String server;

    @RequestMapping(value = "/portal/home", method = RequestMethod.GET)
    protected String home(final Map<String, Object> model, final Principal principal, final HttpSession session) {
        logger.info("Home page");
        Tokens tokens = (Tokens) session.getAttribute("tokens");
        if (principal == null || tokens == null) {
            return "redirect:/logout";
        }
	    logger.info("Principal is " + principal.getName());
        logger.info("Token is " + tokens.getAccessToken());
        try {
            HttpResponse<JsonNode> response = Unirest.get(server + "/api/v1/customer/eth_address")
                    .header("content-type", "application/json")
                    .header("authorization", "Bearer " + tokens.getAccessToken())
                    .asJson();
            logger.info("Response is " + response.getBody());
            if (!response.getBody().getObject().isNull("addresses")) {
                JSONArray array = response.getBody().getObject().getJSONArray("addresses");
                if (!array.isNull(0)) {
                    JSONObject map = array.getJSONObject(0);
                    Iterator<?> it = map.keys();
                    String key = it.hasNext() ? (String)it.next() : null;
                    model.put("address", map.optString(key));
                } else {
                    model.put("address", "");
                }
            } else {
                model.put("address", response.getBody().toString());
            }
            response = Unirest.get(server + "/api/v1/customer/withdrawal_account")
                    .header("content-type", "application/json")
                    .header("authorization", "Bearer " + tokens.getAccessToken())
                    .asJson();
            logger.info("Response is " + response.getBody());
            if (!response.getBody().getObject().isNull("accounts")) {
                JSONArray array = response.getBody().getObject().getJSONArray("accounts");
                if (!array.isNull(0)) {
                    JSONObject map = array.getJSONObject(0);
                    model.put("wa", map);
                } else {
                    model.put("wa", new JSONObject());
                }
            } else {
                model.put("wa", response.getBody().toString());
            }
            HttpResponse<String> error = Unirest.get(server + "/api/v1/customer/transactions")
                    .header("content-type", "application/json")
                    .header("authorization", "Bearer " + tokens.getAccessToken())
                    .asString();
            logger.info("Response is " + error.getStatusText());
            model.put("txs", error.getBody());
            model.put("userId", principal.getName());
        } catch (UnirestException e) {
            logger.error("Exception in API call ", e);
            return "redirect:error";
        }
        return "home";
    }

}
