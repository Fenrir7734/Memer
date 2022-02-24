package com.fenrir.Memer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Settings {
    private static final String PATH = "settings.json";

    private String token;
    private String prefix;
    private String sqlPath;

    public void load() throws JSONException, IOException {
        String content = new String(Files.readAllBytes(Path.of(PATH)));
        JSONObject settingsJSONObject = new JSONObject(content);

        token = settingsJSONObject.getString("token");
        prefix = settingsJSONObject.getString("prefix");

        sqlPath = settingsJSONObject.getString("sql_path");
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
}
