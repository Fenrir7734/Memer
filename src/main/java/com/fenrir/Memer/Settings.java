package com.fenrir.Memer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Settings {
    private static final String PATH = "settings.json";

    private String token;
    private String prefix;
    private String sqlPath;
    private List<String> subreddits;
    private List<String> imgurTags;

    public void load() throws JSONException, IOException {
        String content = new String(Files.readAllBytes(Path.of(PATH)));
        JSONObject settingsJSONObject = new JSONObject(content);

        token = settingsJSONObject.getString("token");
        prefix = settingsJSONObject.getString("prefix");

        sqlPath = settingsJSONObject.getString("sql_path");
        subreddits = parseJSONArray(settingsJSONObject.getJSONArray("subreddits"));
        imgurTags = parseJSONArray(settingsJSONObject.getJSONArray("imgur_tags"));
    }

    public List<String> parseJSONArray(JSONArray array) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            String element = array.getString(i);
            list.add(element);
        }
        return list;
    }

    public String getToken() {
        return token;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSqlPath() {
        return sqlPath;
    }

    public List<String> getSubreddits() {
        return subreddits;
    }

    public List<String> getImgurTags() {
        return imgurTags;
    }
}
