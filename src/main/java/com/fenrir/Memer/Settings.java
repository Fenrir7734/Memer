package com.fenrir.Memer;

import com.fenrir.Memer.api.Imgur;
import com.fenrir.Memer.exceptions.BotInvalidSettingsException;
import com.fenrir.Memer.exceptions.HttpException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Settings {
    private static final Logger logger = LoggerFactory.getLogger(Settings.class);

    private static final String PATH = "settings.json";

    private String token;
    private String imgurClientId;

    private String prefix;
    private String sqlPath;

    private int redditRefresh;
    private int imgurRefresh;

    private List<String> subreddits;
    private List<String> imgurTags;

    public void load() throws JSONException, IOException, BotInvalidSettingsException {
        String content = new String(Files.readAllBytes(Path.of(PATH)));
        JSONObject settingsJSONObject = new JSONObject(content);

        token = settingsJSONObject.getString("token");
        imgurClientId = settingsJSONObject.getString("imgur_client_id");

        prefix = settingsJSONObject.getString("prefix");
        sqlPath = settingsJSONObject.getString("sql_path");

        redditRefresh = settingsJSONObject.getInt("reddit_refresh");
        imgurRefresh = settingsJSONObject.getInt("imgur_refresh");
        boolean imgurRefreshForce = settingsJSONObject.getBoolean("imgur_refresh_force");

        subreddits = parseJSONArray(settingsJSONObject.getJSONArray("subreddits"));
        imgurTags = parseJSONArray(settingsJSONObject.getJSONArray("imgur_tags"));

        try {
            if (imgurRefreshForce) {
                validateImgurClientId(imgurClientId);
            } else {
                validateImgurRefreshTime(imgurClientId, imgurTags.size(), imgurRefresh);
            }
        } catch (IOException | InterruptedException e) {
            logger.error("An error occurred during validation of imgur settings. {}", e.getMessage());
            throw new BotInvalidSettingsException(e.getMessage(), e);
        } catch (HttpException e) {
            logger.error(
                    "An error occurred during validation of imgur settings. {}. Status code: {}",
                    e.getMessage(), e.getStatusCode());
            throw new BotInvalidSettingsException(e.getMessage(), e);
        }
    }

    private List<String> parseJSONArray(JSONArray array) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            String element = array.getString(i);
            list.add(element);
        }
        return list;
    }

    private void validateImgurClientId(String clientId) throws HttpException, IOException, InterruptedException {
        Imgur imgur = new Imgur(clientId, 999);
        imgur.shutdown();
    }

    private void validateImgurRefreshTime(String clientId, int tagsCount, int refreshTime)
            throws HttpException, IOException, InterruptedException, BotInvalidSettingsException {
        Imgur imgur = new Imgur(clientId, refreshTime);
        int rateLimit = imgur.getClientLimit();
        int maxRefreshes = (int) Math.ceil(((31.0 * 24 * 60) / refreshTime) * tagsCount);
        System.out.println(maxRefreshes);
        imgur.shutdown();

        if (maxRefreshes > rateLimit) {
            logger.error("Imgur refresh time is too high. Rate limit for given Client id is {} and " +
                    "maximum number of refreshes with given refresh time will be {}.", rateLimit, maxRefreshes);
            throw new BotInvalidSettingsException("Imgur refresh time is too high.");
        }
    }

    public String getToken() {
        return token;
    }

    public String getImgurClientId() {
        return imgurClientId;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSqlPath() {
        return sqlPath;
    }

    public int getRedditRefresh() {
        return redditRefresh;
    }

    public int getImgurRefresh() {
        return imgurRefresh;
    }

    public List<String> getSubreddits() {
        return subreddits;
    }

    public List<String> getImgurTags() {
        return imgurTags;
    }
}
