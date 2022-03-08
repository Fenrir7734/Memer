package com.fenrir.Memer.api;

import com.fenrir.Memer.api.entity.ImageData;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Reddit implements MediaProvider<ImageData> {
    private static final Logger logger = LoggerFactory.getLogger(Reddit.class);
    private static final String[] availableFormats = new String[]{".jpg", ".jpeg", ".png", ".webp"};

    private final ExecutorService httpExecutor = Executors.newFixedThreadPool(10);
    private final HttpClient client = HttpClient.newBuilder()
            .executor(httpExecutor)
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(40))
            .build();
    private final Cache<String, List<ImageData>> memes;

    public Reddit(int refreshTime) {
        this.memes = Caffeine.newBuilder()
                .expireAfterAccess(refreshTime * 2L, TimeUnit.MINUTES)
                .refreshAfterWrite(refreshTime, TimeUnit.MINUTES)
                .build(this::update);
    }

    @Override
    public Optional<ImageData> getMeme(String source, boolean allowNSFW) {
        List<ImageData> memeList = memes.get(source, this::update);
        if (!allowNSFW) {
            memeList = memeList.stream()
                    .filter(v -> !v.isNFSW())
                    .toList();
        }

        if (memeList.isEmpty()) {
            return Optional.empty();
        }

        int index = ThreadLocalRandom.current()
                .nextInt(memeList.size());
        ImageData image = memeList.get(index);
        return Optional.of(image);
    }

    private List<ImageData> update(String source) {
        try {
            HttpResponse<String> response = makeRequest(source);
            if (response.statusCode() == 200) {
                return extractImages(response.body());
            } else {
                logger.warn("Unsuccessful update of memes from reddit. Response status code: {}", response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            logger.error("An error occurred when updating memes from reddit. Subreddit: {}, Error: {}", source, e.getMessage());
        }
        return new ArrayList<>();
    }

    public boolean ping(String source) throws IOException, InterruptedException {
        HttpResponse<String> response = makeRequest(source);
        return response.statusCode() == 200;
    }

    private HttpResponse<String> makeRequest(String source) throws IOException, InterruptedException {
        String url = String.format("https://www.reddit.com/r/%s/top/.json?sort=top&t=day&limit=100", source);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0")
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private List<ImageData> extractImages(String body) {
        JSONArray imageArray = new JSONObject(body).getJSONObject("data")
                .getJSONArray("children");
        List<ImageData> imageData = new ArrayList<>();

        for (int i = 0; i < imageArray.length(); i++) {
            JSONObject data = imageArray.getJSONObject(i)
                    .getJSONObject("data");

            if (checkFormat(data.getString("url"))) {
                ImageData image = new ImageData(
                        "reddit",
                        String.format("https://www.reddit.com%s", data.getString("permalink")),
                        data.getString("url"),
                        data.getString("author"),
                        data.getString("title"),
                        data.getString("subreddit"),
                        data.getBoolean("over_18")
                );
                imageData.add(image);
            }
        }
        return imageData;
    }

    private boolean checkFormat(String string) {
        return Arrays.stream(availableFormats)
                .anyMatch(string::endsWith);
    }

    @Override
    public void shutdown() {
        httpExecutor.shutdown();
    }
}
