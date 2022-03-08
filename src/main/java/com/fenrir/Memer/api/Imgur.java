package com.fenrir.Memer.api;

import com.fenrir.Memer.api.entity.ImageData;
import com.fenrir.Memer.exceptions.HttpException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class Imgur implements MediaProvider<ImageData> {
    private static final Logger logger = LoggerFactory.getLogger(Imgur.class);
    private static final String[] availableFormats = new String[]{"image/jpeg", "image/png"};

    private final ExecutorService httpExecutor = Executors.newFixedThreadPool(10);
    private final HttpClient client = HttpClient.newBuilder()
            .executor(httpExecutor)
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(40))
            .build();
    private final Cache<String, List<ImageData>> memes;

    private final String authorizationHeaderValue;
    private long remainingRateLimit;

    public Imgur(String clientId, int refreshTime) throws IOException, InterruptedException, HttpException {
        this.memes = Caffeine.newBuilder()
                .expireAfterAccess(refreshTime * 2L, TimeUnit.MINUTES)
                .refreshAfterWrite(refreshTime, TimeUnit.MINUTES)
                .build(this::update);
        this.authorizationHeaderValue = String.format("Client-ID %s", clientId);
        this.remainingRateLimit = getClientRemainingRateLimit();
        logger.info("Imgur remaining rate limit set to {}", this.remainingRateLimit);
    }

    public int getClientRemainingRateLimit() throws IOException, InterruptedException, HttpException {
        HttpResponse<String> response = requestCredits();
        if (response.statusCode() == 200) {
            return extractRemainingRateLimit(response.body());
        } else {
            String error = extractError(response.body());
            throw new HttpException(error, response.statusCode());
        }
    }

    public int getClientLimit() throws IOException, InterruptedException, HttpException {
        HttpResponse<String> response = requestCredits();
        if (response.statusCode() == 200) {
            return extractClientLimit(response.body());
        }else {
            String error = extractError(response.body());
            throw new HttpException(error, response.statusCode());
        }
    }

    private HttpResponse<String> requestCredits() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.imgur.com/3/credits"))
                .header("User-Agent", "Mozilla/5.0")
                .header("Authorization", authorizationHeaderValue)
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
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

    private synchronized List<ImageData> update(String source) {
        try {
            if (remainingRateLimit <= 0) {
                logger.warn("Could not update memes from imgur because remaining rate limit is equal {}", remainingRateLimit);
                remainingRateLimit = getClientRemainingRateLimit();
                List<ImageData> memeList = memes.getIfPresent(source);
                return memeList != null ? memeList : new ArrayList<>();
            }

            HttpResponse<String> response = makeRequest(source);
            if (response.statusCode() == 200) {
                List<ImageData> memesData = extractMemes(response.body(), source);
                Optional<Integer> optionalRateLimit = extractRemainingRateLimit(response.headers());
                optionalRateLimit.ifPresent(v -> remainingRateLimit = v);
                return memesData;
            } else {
                logger.warn("Unsuccessful update of memes from imgur. Response status code: {}", response.statusCode());
            }
        } catch (IOException | InterruptedException | HttpException e) {
            logger.error("An error occurred when updating memes from imgur. Tag: {}, Error: {}", source, e.getMessage());
        }
        return new ArrayList<>();
    }

    private HttpResponse<String> makeRequest(String tag) throws IOException, InterruptedException {
        String url = String.format("https://api.imgur.com/3/gallery/t/%s/viral/day/1?showViral=true", tag);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0")
                .header("Authorization", authorizationHeaderValue)
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private List<ImageData> extractMemes(String body, String tag) {
        List<ImageData> imageData = new ArrayList<>();
        JSONArray imageArray = new JSONObject(body)
                .getJSONObject("data")
                .getJSONArray("items");

        for (int i = 0; i < imageArray.length(); i++) {
            JSONObject item = imageArray.getJSONObject(i);
            List<ImageData> imageDataFromItem = extractImageData(item, tag);
            imageData.addAll(imageDataFromItem);
        }
        return imageData;
    }

    private List<ImageData> extractImageData(JSONObject postData, String tag) {
        String postURL = postData.getString("link");
        List<String> imagesURLs = extractImagesURLs(postData);
        String author = postData.getString("account_url");
        String title = postData.getString("title");
        boolean nsfw = postData.getBoolean("nsfw");

        List<ImageData> imageData = new ArrayList<>();
        for (String imageURL : imagesURLs) {
            ImageData image = new ImageData("imgur", postURL, imageURL, author, title, tag, nsfw);
            imageData.add(image);
        }

        return imageData;
    }

    private List<String> extractImagesURLs(JSONObject postData) {
        List<String> urls;
        if (postData.has("images")) {
            JSONArray imagesArray = postData.getJSONArray("images");
            urls = extractImagesURLFromJSONArray(imagesArray);
        } else {
            urls = new ArrayList<>();
            Optional<String> urlOptional = extractImageURLFromJSONObject(postData);
            urlOptional.ifPresent(urls::add);
        }
        return urls;
    }

    private List<String> extractImagesURLFromJSONArray(JSONArray imagesData) {
        List<String> urls = new ArrayList<>();
        for (int i = 0; i < imagesData.length(); i++) {
            JSONObject imageData = imagesData.getJSONObject(i);
            String type = imageData.getString("type");
            if (checkFormat(type)) {
                urls.add(imageData.getString("link"));
            }
        }
        return urls;
    }

    private Optional<String> extractImageURLFromJSONObject(JSONObject postData) {
        String url = postData.getString("link");
        return checkFormat(url) ? Optional.of(url) : Optional.empty();
    }

    private boolean checkFormat(String string) {
        return Arrays.asList(availableFormats)
                .contains(string);
    }

    private Optional<Integer> extractRemainingRateLimit(HttpHeaders headers) {
        Optional<String> optionalRateLimit = headers.firstValue("x-ratelimit-clientremaining");
        if (optionalRateLimit.isEmpty()) {
            return Optional.empty();
        }
        int rateLimit = Integer.parseInt(optionalRateLimit.get());
        return Optional.of(rateLimit);
    }

    private int extractRemainingRateLimit(String body) {
        JSONObject data = new JSONObject(body);
        return data.getJSONObject("data")
                .getInt("ClientRemaining");
    }

    private int extractClientLimit(String body) {
        JSONObject data = new JSONObject(body);
        return data.getJSONObject("data")
                .getInt("ClientLimit");
    }

    private String extractError(String body) {
        JSONObject data = new JSONObject(body);
        return data.getJSONObject("data")
                .getString("error");
    }

    @Override
    public void shutdown() {
        httpExecutor.shutdown();
    }
}
