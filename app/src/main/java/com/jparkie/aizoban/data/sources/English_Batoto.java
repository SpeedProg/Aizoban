package com.jparkie.aizoban.data.sources;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Pair;

import com.jparkie.aizoban.data.caches.CacheManager;
import com.jparkie.aizoban.data.databases.ApplicationContract;
import com.jparkie.aizoban.data.databases.LibraryContract;
import com.jparkie.aizoban.data.databases.QueryManager;
import com.jparkie.aizoban.data.factories.DefaultFactory;
import com.jparkie.aizoban.data.networks.NetworkService;
import com.jparkie.aizoban.models.Chapter;
import com.jparkie.aizoban.models.Manga;
import com.jparkie.aizoban.modules.initial.NetworkModule;
import com.jparkie.aizoban.utils.DatabaseUtils;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.FuncN;
import rx.schedulers.Schedulers;

public class English_Batoto implements Source {
    public static final String TAG = English_Batoto.class.getSimpleName();

    public static final String NAME = "Batoto (EN)";
    public static final String BASE_URL = "www.bato.to";
    public static final String INITIAL_UPDATE_URL = "http://bato.to/search_ajax?order_cond=update&order=desc&p=1";

    private static final Headers REQUEST_HEADERS = constructRequestHeaders();
    private static Headers constructRequestHeaders() {
        Headers.Builder headerBuilder = new Headers.Builder();
        headerBuilder.add("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64)");
        headerBuilder.add("Cookie", "lang_option=English");

        return headerBuilder.build();
    }

    private NetworkService mNetworkService;
    private QueryManager mQueryManager;
    private CacheManager mCacheManager;

    public English_Batoto(NetworkService networkService, QueryManager queryManager, CacheManager cacheManager) {
        mNetworkService = networkService;
        mQueryManager = queryManager;
        mCacheManager = cacheManager;
    }

    @Override
    public Observable<String> getName() {
        return Observable.just(NAME);
    }

    @Override
    public Observable<String> getBaseUrl() {
        return Observable.just(BASE_URL);
    }

    @Override
    public Observable<String> getInitialUpdateUrl() {
        return Observable.just(INITIAL_UPDATE_URL);
    }

    @Override
    public Observable<List<String>> getGenres() {
        List<String> genres = new ArrayList<String>(38);

        genres.add("4-Koma");
        genres.add("Action");
        genres.add("Adventure");
        genres.add("Award Winning");
        genres.add("Comedy");
        genres.add("Cooking");
        genres.add("Doujinshi");
        genres.add("Drama");
        genres.add("Ecchi");
        genres.add("Fantasy");
        genres.add("Gender Bender");
        genres.add("Harem");
        genres.add("Historical");
        genres.add("Horror");
        genres.add("Josei");
        genres.add("Martial Arts");
        genres.add("Mecha");
        genres.add("Medical");
        genres.add("Music");
        genres.add("Mystery");
        genres.add("One Shot");
        genres.add("Psychological");
        genres.add("Romance");
        genres.add("School Life");
        genres.add("Sci-fi");
        genres.add("Seinen");
        genres.add("Shoujo");
        genres.add("Shoujo Ai");
        genres.add("Shounen");
        genres.add("Shounen Ai");
        genres.add("Slice of Life");
        genres.add("Smut");
        genres.add("Sports");
        genres.add("Supernatural");
        genres.add("Tragedy");
        genres.add("Webtoon");
        genres.add("Yaoi");
        genres.add("Yuri");

        return Observable.just(genres);
    }

    @Override
    public Observable<UpdatePageMarker> pullLatestUpdatesFromNetwork(final UpdatePageMarker newUpdate) {
        return mNetworkService
                .getResponse(newUpdate.getNextPageUrl(), NetworkModule.NULL_CACHE_CONTROL, REQUEST_HEADERS)
                .flatMap(new Func1<Response, Observable<String>>() {
                    @Override
                    public Observable<String> call(Response response) {
                        return mNetworkService.mapResponseToString(response);
                    }
                })
                .flatMap(new Func1<String, Observable<UpdatePageMarker>>() {
                    @Override
                    public Observable<UpdatePageMarker> call(String unparsedHtml) {
                        return Observable.just(parseHtmlToLatestUpdates(newUpdate.getNextPageUrl(), unparsedHtml));
                    }
                });
    }

    private UpdatePageMarker parseHtmlToLatestUpdates(String requestUrl, String unparsedHtml) {
        Document parsedDocument = Jsoup.parse(unparsedHtml);

        List<Manga> updatedMangaList = scrapeUpdateMangasFromParsedDocument(parsedDocument);
        updateLibraryInDatabase(updatedMangaList);

        String nextPageUrl = findNextUrlFromParsedDocument(requestUrl, unparsedHtml);
        int lastMangaPostion = updatedMangaList.size();

        return new UpdatePageMarker(nextPageUrl, lastMangaPostion);
    }

    private List<Manga> scrapeUpdateMangasFromParsedDocument(Document parsedDocument) {
        List<Manga> updatedMangaList = new ArrayList<Manga>();

        Elements updatedHtmlBlocks = parsedDocument.select("tr:not([id]):not([class])");
        for (Element currentHtmlBlock : updatedHtmlBlocks) {
            Manga currentlyUpdatedManga = constructMangaFromHtmlBlock(currentHtmlBlock);

            updatedMangaList.add(currentlyUpdatedManga);
        }

        return updatedMangaList;
    }

    private Manga constructMangaFromHtmlBlock(Element htmlBlock) {
        Manga mangaFromHtmlBlock = DefaultFactory.Manga.constructDefault();
        mangaFromHtmlBlock.setSource(NAME);

        Element urlElement = htmlBlock.select("a[href^=http://bato.to]").first();
        Element nameElement = urlElement;
        Element updateElement = htmlBlock.select("td").get(5);

        if (urlElement != null) {
            String fieldUrl = urlElement.attr("href");
            mangaFromHtmlBlock.setUrl(fieldUrl);
        }
        if (nameElement != null) {
            String fieldName = nameElement.text().trim();
            mangaFromHtmlBlock.setName(fieldName);
        }
        if (updateElement != null) {
            long fieldUpdate = parseUpdateFromElement(updateElement);
            mangaFromHtmlBlock.setUpdated(fieldUpdate);
        }

        int updateCount = 1;
        mangaFromHtmlBlock.setUpdateCount(updateCount);

        return mangaFromHtmlBlock;
    }

    private long parseUpdateFromElement(Element updateElement) {
        String updatedDateAsString = updateElement.text();

        try {
            Date specificDate = new SimpleDateFormat("dd MMMMM yyyy - hh:mm a", Locale.ENGLISH).parse(updatedDateAsString);

            return specificDate.getTime();
        } catch (ParseException e) {
            // Do Nothing.
        }

        return DefaultFactory.Manga.DEFAULT_UPDATED;
    }

    private void updateLibraryInDatabase(List<Manga> mangaList) {
        mQueryManager.beginLibraryTransaction();
        try {
            List<Manga> mangaToRemove = new ArrayList<>();
            for (Manga currentManga : mangaList) {
                Manga existingManga = mQueryManager.retrieveManga(NAME, currentManga.getName())
                        .toBlocking()
                        .single();

                if (existingManga != null) {
                    existingManga.setUpdated(currentManga.getUpdated());
                    existingManga.setUpdateCount(currentManga.getUpdateCount());


                    mQueryManager.createManga(existingManga)
                            .toBlocking()
                            .single();
                } else {
                    mangaToRemove.add(currentManga);
                }
            }
            mangaList.removeAll(mangaToRemove);

            mQueryManager.setLibraryTransactionSuccessful();
        } finally {
            mQueryManager.endLibraryTransaction();
        }
    }

    private String findNextUrlFromParsedDocument(String requestUrl, String unparsedHtml) {
        if (!unparsedHtml.contains("No (more) comics found!")) {
            requestUrl = requestUrl.replace("http://bato.to/search_ajax?order_cond=update&order=desc&p=", "");

            return "http://bato.to/search_ajax?order_cond=update&order=desc&p=" + (Integer.valueOf(requestUrl) + 1);
        }

        return DefaultFactory.UpdatePageMarker.DEFAULT_NEXT_PAGE_URL;
    }

    @Override
    public Observable<Manga> pullMangaFromNetwork(final String mangaUrl) {
        String mangaId = mangaUrl.substring(mangaUrl.lastIndexOf("r") + 1);

        return mNetworkService
                .getResponse("http://bato.to/comic_pop?id=" + mangaId, NetworkModule.NULL_CACHE_CONTROL, REQUEST_HEADERS)
                .flatMap(new Func1<Response, Observable<String>>() {
                    @Override
                    public Observable<String> call(Response response) {
                        return mNetworkService.mapResponseToString(response);
                    }
                })
                .flatMap(new Func1<String, Observable<Manga>>() {
                    @Override
                    public Observable<Manga> call(String unparsedHtml) {
                        return Observable.just(parseHtmlToManga(mangaUrl, unparsedHtml));
                    }
                });
    }

    private Manga parseHtmlToManga(String mangaUrl, String unparsedHtml) {
        Document parsedDocument = Jsoup.parse(unparsedHtml);

        Element artistElement = parsedDocument.select("a[href^=http://bato.to/search?artist_name]").first();
        Element descriptionElement = parsedDocument.select("tr").get(5);
        Elements genreElements = parsedDocument.select("img[src=http://bato.to/forums/public/style_images/master/bullet_black.png]");
        Element thumbnailUrlElement = parsedDocument.select("img[src^=http://img.batoto.net/forums/uploads/]").first();

        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<String>();

        selection.append(LibraryContract.Manga.COLUMN_SOURCE + " = ?");
        selectionArgs.add(NAME);
        selection.append(" AND ").append(LibraryContract.Manga.COLUMN_URL + " = ?");
        selectionArgs.add(mangaUrl);

        Manga newManga = mQueryManager.retrieveMangaAsCursor(
                null,
                selection.toString(),
                selectionArgs.toArray(new String[selectionArgs.size()]),
                null,
                null,
                null,
                "1"
        )
                .map(new Func1<Cursor, Manga>() {
                    @Override
                    public Manga call(Cursor cursor) {
                        return DatabaseUtils.toObject(cursor, Manga.class);
                    }
                })
                .filter(new Func1<Manga, Boolean>() {
                    @Override
                    public Boolean call(Manga manga) {
                        return manga != null;
                    }
                })
                .toBlocking()
                .single();

        if (artistElement != null) {
            String fieldArtist = artistElement.text();
            newManga.setArtist(fieldArtist);
            newManga.setAuthor(fieldArtist);
        }
        if (descriptionElement != null) {
            String fieldDescription = descriptionElement.text().substring("Description:".length()).trim();
            newManga.setDescription(fieldDescription);
        }
        if (genreElements != null) {
            String fieldGenres = "";
            for (int index = 0; index < genreElements.size(); index++) {
                String currentGenre = genreElements.get(index).attr("alt");

                if (index < genreElements.size() - 1) {
                    fieldGenres += currentGenre + ", ";
                } else {
                    fieldGenres += currentGenre;
                }
            }
            newManga.setGenre(fieldGenres);
        }
        if (thumbnailUrlElement != null) {
            String fieldThumbnailUrl = thumbnailUrlElement.attr("src");
            newManga.setThumbnailUrl(fieldThumbnailUrl);
        }

        boolean fieldCompleted = unparsedHtml.contains("<td>Complete</td>");
        newManga.setCompleted(fieldCompleted);


        newManga.setInitialized(true);

        mQueryManager.createManga(newManga)
                .toBlocking()
                .single();

        return newManga;
    }

    @Override
    public Observable<List<Chapter>> pullChaptersFromNetwork(final String mangaUrl, final String mangaName) {
        return mNetworkService
                .getResponse(mangaUrl, NetworkModule.NULL_CACHE_CONTROL, REQUEST_HEADERS)
                .flatMap(new Func1<Response, Observable<String>>() {
                    @Override
                    public Observable<String> call(Response response) {
                        return mNetworkService.mapResponseToString(response);
                    }
                })
                .flatMap(new Func1<String, Observable<List<Chapter>>>() {
                    @Override
                    public Observable<List<Chapter>> call(String unparsedHtml) {
                        return Observable.just(parseHtmlToChapters(mangaUrl, mangaName, unparsedHtml));
                    }
                });
    }

    private List<Chapter> parseHtmlToChapters(String mangaUrl, String mangaName, String unparsedHtml) {
        Document parsedDocument = Jsoup.parse(unparsedHtml);

        List<Chapter> chapterList = scrapeChaptersFromParsedDocument(parsedDocument);
        chapterList = setSourceForChapterList(chapterList);
        chapterList = setParentInfoForChapterList(chapterList, mangaUrl, mangaName);
        chapterList = setNumberForChapterList(chapterList);

        saveChaptersToDatabase(chapterList, mangaUrl);

        return chapterList;
    }

    private List<Chapter> scrapeChaptersFromParsedDocument(Document parsedDocument) {
        List<Chapter> chapterList = new ArrayList<Chapter>();

        Elements chapterElements = parsedDocument.select("tr.row.lang_English.chapter_row");
        for (Element chapterElement : chapterElements) {
            Chapter currentChapter = constructChapterFromHtmlBlock(chapterElement);

            chapterList.add(currentChapter);
        }

        return chapterList;
    }

    private Chapter constructChapterFromHtmlBlock(Element chapterElement) {
        Chapter newChapter = DefaultFactory.Chapter.constructDefault();

        Element urlElement = chapterElement.select("a[href^=http://bato.to/read/").first();
        Element nameElement = urlElement;
        Element dateElement = chapterElement.select("td").get(4);

        if (urlElement != null) {
            String fieldUrl = urlElement.attr("href");
            newChapter.setUrl(fieldUrl);
        }
        if (nameElement != null) {
            String fieldName = nameElement.text().trim();
            newChapter.setName(fieldName);
        }
        if (dateElement != null) {
            long fieldDate = parseDateFromElement(dateElement);
            newChapter.setDate(fieldDate);
        }

        return newChapter;
    }

    private long parseDateFromElement(Element dateElement) {
        String dateAsString = dateElement.text();

        try {
            Date specificDate = new SimpleDateFormat("dd MMMMM yyyy - hh:mm a", Locale.ENGLISH).parse(dateAsString);

            return specificDate.getTime();
        } catch (ParseException e) {
            // Do Nothing.
        }

        return DefaultFactory.Chapter.DEFAULT_DATE;
    }

    private List<Chapter> setSourceForChapterList(List<Chapter> chapterList) {
        for (Chapter currentChapter : chapterList) {
            currentChapter.setSource(NAME);
        }

        return chapterList;
    }

    private List<Chapter> setParentInfoForChapterList(List<Chapter> chapterList, String parentUrl, String parentName) {
        for (Chapter currentChapter : chapterList) {
            currentChapter.setParentUrl(parentUrl);
            currentChapter.setParentName(parentName);
        }

        return chapterList;
    }

    private List<Chapter> setNumberForChapterList(List<Chapter> chapterList) {
        Collections.reverse(chapterList);
        for (int index = 0; index < chapterList.size(); index++) {
            chapterList.get(index).setNumber(index + 1);
        }

        return chapterList;
    }

    private void saveChaptersToDatabase(List<Chapter> chapterList, String parentUrl) {
        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<String>();

        selection.append(ApplicationContract.Chapter.COLUMN_SOURCE + " = ?");
        selectionArgs.add(NAME);
        selection.append(" AND ").append(ApplicationContract.Chapter.COLUMN_PARENT_URL + " = ?");
        selectionArgs.add(parentUrl);

        mQueryManager.beginApplicationTransaction();
        try {
            mQueryManager.deleteAllChapter(selection.toString(), selectionArgs.toArray(new String[selectionArgs.size()]))
                    .toBlocking()
                    .single();

            for (Chapter currentChapter : chapterList) {
                mQueryManager.createChapter(currentChapter)
                        .toBlocking()
                        .single();
            }

            mQueryManager.setApplicationTransactionSuccessful();
        } finally {
            mQueryManager.endApplicationTransaction();
        }
    }

    @Override
    public Observable<String> pullImageUrlsFromNetwork(final String chapterUrl) {
        final List<String> temporaryCachedImageUrls = new ArrayList<String>();

        return mCacheManager.getImageUrlsFromDiskCache(chapterUrl)
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends String>>() {
                    @Override
                    public Observable<? extends String> call(Throwable throwable) {
                        return mNetworkService
                                .getResponse(chapterUrl, NetworkModule.NULL_CACHE_CONTROL, null)
                                .flatMap(new Func1<Response, Observable<String>>() {
                                    @Override
                                    public Observable<String> call(Response response) {
                                        return mNetworkService.mapResponseToString(response);
                                    }
                                })
                                .flatMap(new Func1<String, Observable<List<String>>>() {
                                    @Override
                                    public Observable<List<String>> call(String unparsedHtml) {
                                        return Observable.just(parseHtmlToPageUrls(unparsedHtml));
                                    }
                                })
                                .flatMap(new Func1<List<String>, Observable<String>>() {
                                    @Override
                                    public Observable<String> call(List<String> pageUrls) {
                                        return Observable.from(pageUrls.toArray(new String[pageUrls.size()]));
                                    }
                                })
                                .buffer(5)
                                .concatMap(new Func1<List<String>, Observable<? extends List<String>>>() {
                                    @Override
                                    public Observable<? extends List<String>> call(List<String> batchedPageUrls) {
                                        List<Observable<String>> imageUrlObservables = new ArrayList<Observable<String>>();
                                        for (String pageUrl : batchedPageUrls) {
                                            Observable<String> temporaryObservable = mNetworkService
                                                    .getResponse(pageUrl, NetworkModule.NULL_CACHE_CONTROL, null)
                                                    .flatMap(new Func1<Response, Observable<String>>() {
                                                        @Override
                                                        public Observable<String> call(Response response) {
                                                            return mNetworkService.mapResponseToString(response);
                                                        }
                                                    })
                                                    .flatMap(new Func1<String, Observable<String>>() {
                                                        @Override
                                                        public Observable<String> call(String unparsedHtml) {
                                                            return Observable.just(parseHtmlToImageUrl(unparsedHtml));
                                                        }
                                                    })
                                                    .subscribeOn(Schedulers.io());

                                            imageUrlObservables.add(temporaryObservable);
                                        }

                                        return Observable.zip(imageUrlObservables, new FuncN<List<String>>() {
                                            @Override
                                            public List<String> call(Object... args) {
                                                List<String> imageUrls = new ArrayList<String>();
                                                for (Object uncastImageUrl : args) {
                                                    imageUrls.add(String.valueOf(uncastImageUrl));
                                                }

                                                return imageUrls;
                                            }
                                        });
                                    }
                                })
                                .concatMap(new Func1<List<String>, Observable<String>>() {
                                    @Override
                                    public Observable<String> call(List<String> batchedImageUrls) {
                                        return Observable.from(batchedImageUrls.toArray(new String[batchedImageUrls.size()]));
                                    }
                                })
                                .doOnNext(new Action1<String>() {
                                    @Override
                                    public void call(String imageUrl) {
                                        temporaryCachedImageUrls.add(imageUrl);
                                    }
                                })
                                .doOnCompleted(mCacheManager.putImageUrlsToDiskCache(chapterUrl, temporaryCachedImageUrls));
                    }
                })
                .onBackpressureBuffer();
    }

    private List<String> parseHtmlToPageUrls(String unparsedHtml) {
        Document parsedDocument = Jsoup.parse(unparsedHtml);

        List<String> pageUrlList = new ArrayList<String>();

        Elements pageUrlElements = parsedDocument.getElementById("page_select").getElementsByTag("option");
        for (Element pageUrlElement : pageUrlElements) {
            pageUrlList.add(pageUrlElement.attr("value"));
        }

        return pageUrlList;
    }

    private String parseHtmlToImageUrl(String unparsedHtml) {
        int beginIndex = unparsedHtml.indexOf("<img id=\"comic_page\"");
        int endIndex = unparsedHtml.indexOf("</a>", beginIndex);
        String trimmedHtml = unparsedHtml.substring(beginIndex, endIndex);

        Document parsedDocument = Jsoup.parse(trimmedHtml);

        Element imageElement = parsedDocument.getElementById("comic_page");

        return imageElement.attr("src");
    }

    private static String INITIAL_DATABASE_URL_1 = "http://bato.to/comic_pop?id=1";
    private static String INITIAL_DATABASE_URL_2 = "http://bato.to/search_ajax?order_cond=views&order=desc&p=1";

    private static AtomicInteger mCounter = new AtomicInteger(1);

    @Override
    public Observable<String> recursivelyConstructDatabase(final String url) {
        return mNetworkService
                .getResponse(url, NetworkModule.NULL_CACHE_CONTROL, REQUEST_HEADERS)
                .flatMap(new Func1<Response, Observable<String>>() {
                    @Override
                    public Observable<String> call(Response response) {
                        return mNetworkService.mapResponseToString(response);
                    }
                })
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String unparsedHtml) {
                        return Observable.just(parseEnglish_Batoto(unparsedHtml));
                    }
                });
    }

    private String parseEnglish_Batoto(String unparsedHtml) {
        if (!unparsedHtml.equals("wtf?")) {
            Document parsedDocument = Jsoup.parse(unparsedHtml);

            Manga newManga = new Manga();

            Element temporaryElementOne = parsedDocument.getElementsByTag("a").first();
            Element temporaryElementTwo = parsedDocument.select("a[href^=http://bato.to/forums/forum/]").first();
            Element temporaryElementThree = parsedDocument.select("img[src^=http://img.batoto.net/forums/uploads/]").first();
            Elements temporaryElementsFour = parsedDocument.select("img[src=http://bato.to/forums/public/style_images/master/bullet_black.png]");

            String fieldSource = English_Batoto.NAME;
            newManga.setSource(fieldSource);

            String fieldUrl = "http://bato.to" + temporaryElementOne.attr("href");
            newManga.setUrl(fieldUrl);

            String fieldName = temporaryElementTwo.text();
            int startIndex = "Go to ".length();
            int endIndex = fieldName.lastIndexOf(" Forums!");
            newManga.setName(fieldName.substring(startIndex, endIndex));

            String fieldThumbnailUrl = temporaryElementThree.attr("src");
            newManga.setThumbnailUrl(fieldThumbnailUrl);

            String fieldGenres = "";
            for (int index = 0; index < temporaryElementsFour.size(); index++) {
                String currentGenre = temporaryElementsFour.get(index).attr("alt");

                if (index < temporaryElementsFour.size() - 1) {
                    fieldGenres += currentGenre + ", ";
                } else {
                    fieldGenres += currentGenre;
                }
            }
            newManga.setGenre(fieldGenres);

            boolean fieldIsCompleted = unparsedHtml.contains("<td>Complete</td>");
            newManga.setCompleted(fieldIsCompleted);

            mQueryManager.createManga(newManga)
                    .toBlocking()
                    .single();
        }

        return "http://bato.to/comic_pop?id=" + mCounter.incrementAndGet();
    }

    private String parseEnglish_Batoto_Views(String unparsedHtml) {
        if (!unparsedHtml.contains("No (more) comics found!")) {
            Document parsedDocument = Jsoup.parse(unparsedHtml);

            List<Pair<String, ContentValues>> updateList = new ArrayList<Pair<String, ContentValues>>();
            Elements mangaElements = parsedDocument.select("tr:not([id]):not([class])");
            for (Element mangaElement : mangaElements) {
                Element temporaryElementOne = mangaElement.select("a[href^=http://bato.to]").first();
                Element temporaryElementTwo = mangaElement.select("td").get(3);
                String temporaryString = temporaryElementTwo.text();

                String fieldUrl = temporaryElementOne.attr("href");

                String fieldView = null;
                if (temporaryString.contains("m")) {
                    temporaryString = temporaryString.replace("m", "");

                    int viewsAsNumber = (int)(Double.valueOf(temporaryString) * 1000000);
                    fieldView = String.valueOf(viewsAsNumber);
                } else if (temporaryString.contains("k")) {
                    temporaryString = temporaryString.replace("k", "");

                    int viewsAsNumber = (int)(Double.valueOf(temporaryString) * 1000);
                    fieldView = String.valueOf(viewsAsNumber);
                } else {
                    int viewsAsNumber = (int)(Double.valueOf(temporaryString) * 1);
                    fieldView = String.valueOf(viewsAsNumber);
                }

                ContentValues fieldRanking = new ContentValues(1);
                fieldRanking.put(LibraryContract.Manga.COLUMN_RANK, fieldView);

                updateList.add(Pair.create(fieldUrl, fieldRanking));
            }

            mQueryManager.beginLibraryTransaction();
            try {
                for (Pair<String, ContentValues> currentUpdate : updateList) {
                    mQueryManager.updateManga(currentUpdate.second, LibraryContract.Manga.COLUMN_URL + " = ?", new String[]{currentUpdate.first})
                            .toBlocking()
                            .single();
                }

                mQueryManager.setLibraryTransactionSuccessful();
            } finally {
                mQueryManager.endLibraryTransaction();
            }

            return "http://bato.to/search_ajax?order_cond=views&order=desc&p=" + mCounter.incrementAndGet();
        }

        return null;
    }

    public void reorderEnglish_Batoto_Rankings() {
        List<Manga> mangaList = mQueryManager.retrieveAllMangaAsStream(
                null,
                LibraryContract.Manga.COLUMN_SOURCE + " = ?",
                new String[]{NAME},
                null,
                null,
                LibraryContract.Manga.COLUMN_RANK + " DESC",
                null
        )
                .toList()
                .toBlocking()
                .single();

        for (int index = 0; index < mangaList.size(); index++) {
            mangaList.get(index).setRank(index + 1);
        }

        mQueryManager.beginLibraryTransaction();
        try {
            for (Manga currentManga : mangaList) {
                mQueryManager.createManga(currentManga)
                        .toBlocking()
                        .single();
            }
            mQueryManager.setLibraryTransactionSuccessful();
        } finally {
            mQueryManager.endLibraryTransaction();
        }
    }
}
