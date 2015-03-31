package com.jparkie.aizoban.data.sources;

import android.database.Cursor;

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
import com.squareup.okhttp.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.FuncN;
import rx.schedulers.Schedulers;

public class English_MangaReader implements Source {
    public static final String TAG = English_MangaReader.class.getSimpleName();

    public static final String NAME = "MangaReader (EN)";
    public static final String BASE_URL = "www.mangareader.net";
    public static final String INITIAL_UPDATE_URL = "http://www.mangareader.net/latest/";

    private NetworkService mNetworkService;
    private QueryManager mQueryManager;
    private CacheManager mCacheManager;

    public English_MangaReader(NetworkService networkService, QueryManager queryManager, CacheManager cacheManager) {
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
        List<String> genres = new ArrayList<String>(37);

        genres.add("Action");
        genres.add("Adventure");
        genres.add("Comedy");
        genres.add("Demons");
        genres.add("Drama");
        genres.add("Ecchi");
        genres.add("Fantasy");
        genres.add("Gender Bender");
        genres.add("Harem");
        genres.add("Historical");
        genres.add("Horror");
        genres.add("Josei");
        genres.add("Magic");
        genres.add("Martial Arts");
        genres.add("Mature");
        genres.add("Mecha");
        genres.add("Military");
        genres.add("Mystery");
        genres.add("One Shot");
        genres.add("Psychological");
        genres.add("Romance");
        genres.add("School Life");
        genres.add("Sci-Fi");
        genres.add("Seinen");
        genres.add("Shoujo");
        genres.add("Shoujoai");
        genres.add("Shounen");
        genres.add("Shounenai");
        genres.add("Slice of Life");
        genres.add("Smut");
        genres.add("Sports");
        genres.add("Super Power");
        genres.add("Supernatural");
        genres.add("Tragedy");
        genres.add("Vampire");
        genres.add("Yaoi");
        genres.add("Yuri");

        return Observable.just(genres);
    }

    @Override
    public Observable<UpdatePageMarker> pullLatestUpdatesFromNetwork(final UpdatePageMarker newUpdate) {
        return mNetworkService
                .getResponse(newUpdate.getNextPageUrl(), NetworkModule.NULL_CACHE_CONTROL, NetworkModule.NULL_HEADERS)
                .flatMap(new Func1<Response, Observable<String>>() {
                    @Override
                    public Observable<String> call(Response response) {
                        return mNetworkService.mapResponseToString(response);
                    }
                })
                .flatMap(new Func1<String, Observable<UpdatePageMarker>>() {
                    @Override
                    public Observable<UpdatePageMarker> call(String unparsedHtml) {
                        return Observable.just(parseHtmlToLatestUpdates(unparsedHtml));
                    }
                });
    }

    private UpdatePageMarker parseHtmlToLatestUpdates(String unparsedHtml) {
        Document parsedDocument = Jsoup.parse(unparsedHtml);

        List<Manga> updatedMangaList = scrapeUpdateMangasFromParsedDocument(parsedDocument);
        updateLibraryInDatabase(updatedMangaList);

        String nextPageUrl = findNextUrlFromParsedDocument(parsedDocument);
        int lastMangaPostion = updatedMangaList.size();

        return new UpdatePageMarker(nextPageUrl, lastMangaPostion);
    }

    private List<Manga> scrapeUpdateMangasFromParsedDocument(Document parsedDocument) {
        List<Manga> updatedMangaList = new ArrayList<Manga>();

        Elements updatedHtmlBlocks = parsedDocument.select("table.updates tr.c2");
        for (Element currentHtmlBlock : updatedHtmlBlocks) {
            Manga currentlyUpdatedManga = constructMangaFromHtmlBlock(currentHtmlBlock);

            updatedMangaList.add(currentlyUpdatedManga);
        }

        return updatedMangaList;
    }

    private Manga constructMangaFromHtmlBlock(Element htmlBlock) {
        Manga mangaFromHtmlBlock = DefaultFactory.Manga.constructDefault();
        mangaFromHtmlBlock.setSource(NAME);

        Element urlElement = htmlBlock.select("a.chapter").first();
        Element nameElement = htmlBlock.select("a.chapter").first();
        Element updateElement = htmlBlock.select("td.c1").first();

        if (urlElement != null) {
            String fieldUrl = "http://www.mangareader.net" + urlElement.attr("href");
            mangaFromHtmlBlock.setUrl(fieldUrl);
        }
        if (nameElement != null) {
            String fieldName = nameElement.text();
            mangaFromHtmlBlock.setName(fieldName);
        }
        if (updateElement != null) {
            long fieldUpdate = parseUpdateFromElement(updateElement);
            mangaFromHtmlBlock.setUpdated(fieldUpdate);
        }

        int updateCount = htmlBlock.select("a.chaptersrec").size();
        mangaFromHtmlBlock.setUpdateCount(updateCount);

        return mangaFromHtmlBlock;
    }

    private long parseUpdateFromElement(Element updateElement) {
        String updatedDateAsString = updateElement.text();

        if (updatedDateAsString.contains("Today")) {
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            return today.getTimeInMillis();
        } else if (updatedDateAsString.contains("Yesterday")) {
            Calendar yesterday = Calendar.getInstance();
            yesterday.add(Calendar.DATE, -1);
            yesterday.set(Calendar.HOUR_OF_DAY, 0);
            yesterday.set(Calendar.MINUTE, 0);
            yesterday.set(Calendar.SECOND, 0);
            yesterday.set(Calendar.MILLISECOND, 0);

            return yesterday.getTimeInMillis();
        } else {
            try {
                Date specificDate = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).parse(updatedDateAsString);

                return specificDate.getTime();
            } catch (ParseException e) {
                // Do Nothing.
            }
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

    private String findNextUrlFromParsedDocument(Document parsedDocument) {
        Element nextUrlElement = parsedDocument.select("div#sp").select("a:containsOwn(>)").first();;

        if (nextUrlElement != null) {
            return "http://www.mangareader.net" + nextUrlElement.attr("href");
        }

        return DefaultFactory.UpdatePageMarker.DEFAULT_NEXT_PAGE_URL;
    }

    @Override
    public Observable<Manga> pullMangaFromNetwork(final String mangaUrl) {
        return mNetworkService
                .getResponse(mangaUrl, NetworkModule.NULL_CACHE_CONTROL, NetworkModule.NULL_HEADERS)
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

        Elements detailElements = parsedDocument.select("div#mangaproperties").select("td:not([class])");

        Element artistElement = detailElements.get(5);
        Element authorElement = detailElements.get(4);
        Element descriptionElement = parsedDocument.select("div#readmangasum > p").first();
        Elements genreElements = detailElements.get(7).select("a");
        Element statusElement = detailElements.get(3);
        Element thumbnailUrlElement = parsedDocument.select("div#mangaimg img").first();

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
        }
        if (authorElement != null) {
            String fieldAuthor = authorElement.text();
            newManga.setAuthor(fieldAuthor);
        }
        if (descriptionElement != null) {
            String fieldDescription = descriptionElement.text();
            newManga.setDescription(fieldDescription);
        }
        if (genreElements != null) {
            String fieldGenre = "";
            for (int index = 0; index < genreElements.size(); index++) {
                Element genreElement = genreElements.get(index);

                if (index != genreElements.size() - 1) {
                    fieldGenre += genreElement.text() + ", ";
                } else {
                    fieldGenre += genreElement.text();
                }
            }
            newManga.setGenre(fieldGenre);
        }
        if (statusElement != null) {
            boolean fieldCompleted = statusElement.text().contains("Completed");
            newManga.setCompleted(fieldCompleted);
        }
        if (thumbnailUrlElement != null) {
            String fieldThumbnailUrl = thumbnailUrlElement.attr("src");
            newManga.setThumbnailUrl(fieldThumbnailUrl);
        }

        newManga.setInitialized(true);

        mQueryManager.createManga(newManga)
                .toBlocking()
                .single();

        return newManga;
    }

    @Override
    public Observable<List<Chapter>> pullChaptersFromNetwork(final String mangaUrl, final String mangaName) {
        return mNetworkService
                .getResponse(mangaUrl, NetworkModule.NULL_CACHE_CONTROL, NetworkModule.NULL_HEADERS)
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
        int beginIndex = unparsedHtml.indexOf("<table id=\"listing\">");
        int endIndex = unparsedHtml.indexOf("</table>", beginIndex);
        String trimmedHtml = unparsedHtml.substring(beginIndex, endIndex);

        Document parsedDocument = Jsoup.parse(trimmedHtml);

        beginIndex = unparsedHtml.indexOf("<div id=\"latestchapters\">");
        endIndex = unparsedHtml.indexOf("</ul>\n" + "</div>", beginIndex);
        String latestChaptersHtml = unparsedHtml.substring(beginIndex, endIndex);

        List<Chapter> chapterList = scrapeChaptersFromParsedDocument(parsedDocument, latestChaptersHtml);
        chapterList = setSourceForChapterList(chapterList);
        chapterList = setParentInfoForChapterList(chapterList, mangaUrl, mangaName);
        chapterList = setNumberForChapterList(chapterList);

        saveChaptersToDatabase(chapterList, mangaUrl);

        return chapterList;
    }

    private List<Chapter> scrapeChaptersFromParsedDocument(Document parsedDocument, String latestChaptersHtml) {
        List<Chapter> chapterList = new ArrayList<Chapter>();

        Elements chapterElements = parsedDocument.select("tr:not(.table_head)");
        for (Element chapterElement : chapterElements) {
            Chapter currentChapter = constructChapterFromHtmlBlock(chapterElement, latestChaptersHtml);

            chapterList.add(currentChapter);
        }

        return chapterList;
    }

    private Chapter constructChapterFromHtmlBlock(Element chapterElement, String latestChaptersHtml) {
        Chapter newChapter = DefaultFactory.Chapter.constructDefault();

        Element urlElement = chapterElement.select("a").first();
        Element nameElement = chapterElement.select("a").first();
        Element dateElement = chapterElement.select("td").get(1);

        if (urlElement != null) {
            String fieldUrl = "http://www.mangareader.net" + urlElement.attr("href");
            newChapter.setUrl(fieldUrl);
        }
        if (nameElement != null) {
            String fieldName = nameElement.text();
            newChapter.setName(fieldName);
        }
        if (dateElement != null) {
            long fieldDate = parseDateFromElement(dateElement);
            newChapter.setDate(fieldDate);
        }

        boolean fieldNew = latestChaptersHtml.contains(newChapter.getName());
        newChapter.setNew(fieldNew);

        return newChapter;
    }

    private long parseDateFromElement(Element dateElement) {
        String dateAsString = dateElement.text();

        try {
            Date date = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH).parse(dateAsString);

            return date.getTime();
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
                                .getResponse(chapterUrl, NetworkModule.NULL_CACHE_CONTROL, NetworkModule.NULL_HEADERS)
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
                                                    .getResponse(pageUrl, NetworkModule.NULL_CACHE_CONTROL, NetworkModule.NULL_HEADERS)
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
        int beginIndex = unparsedHtml.indexOf("<div id=\"selectpage\">");
        int endIndex = unparsedHtml.indexOf("</div>", beginIndex);
        String trimmedHtml = unparsedHtml.substring(beginIndex, endIndex);

        Document parsedDocument = Jsoup.parse(trimmedHtml);

        List<String> pageUrlList = new ArrayList<String>();

        Elements pageUrlElements = parsedDocument.getElementById("pageMenu").getElementsByTag("option");
        for (Element pageUrlElement : pageUrlElements) {
            pageUrlList.add("http://www.mangareader.net" + pageUrlElement.attr("value"));
        }

        return pageUrlList;
    }

    private String parseHtmlToImageUrl(String unparsedHtml) {
        int beginIndex = unparsedHtml.indexOf("<div id=\"imgholder\">");
        int endIndex = unparsedHtml.indexOf("</div>", beginIndex);
        String trimmedHtml = unparsedHtml.substring(beginIndex, endIndex);

        Document parsedDocument = Jsoup.parse(unparsedHtml);

        Element imageElement = parsedDocument.getElementById("img");

        return imageElement.attr("src");
    }

    private static String INITIAL_DATABASE_URL = "http://www.mangareader.net/popular";

    @Override
    public Observable<String> recursivelyConstructDatabase(final String url) {
        return mNetworkService
                .getResponse(url, NetworkModule.NULL_CACHE_CONTROL, NetworkModule.NULL_HEADERS)
                .flatMap(new Func1<Response, Observable<String>>() {
                    @Override
                    public Observable<String> call(Response response) {
                        return mNetworkService.mapResponseToString(response);
                    }
                })
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String unparsedHtml) {
                        return Observable.just(parseEnglish_MangaReader(unparsedHtml));
                    }
                });
    }

    private static AtomicInteger mCounter = new AtomicInteger(0);

    private String parseEnglish_MangaReader(String unparsedHtml) {
        Document parsedDocument = Jsoup.parse(unparsedHtml);

        List<Manga> mangaList = new ArrayList<Manga>();
        Elements mangaElements = parsedDocument.select("div.mangaresultinner");
        for (Element mangaElement : mangaElements) {
            Manga newManga = new Manga();

            Element temporaryElementOne = mangaElement.select("a").first();
            Element temporaryElementTwo = mangaElement.select("div.imgsearchresults").first();
            Element temporaryElementThree = mangaElement.select("div.manga_genre").first();

            String fieldSource = English_MangaReader.NAME;
            newManga.setSource(fieldSource);

            String fieldUrl = "http://www.mangareader.net" + temporaryElementOne.attr("href");
            newManga.setUrl(fieldUrl);

            String fieldName = temporaryElementOne.text();
            newManga.setName(fieldName);

            String fieldThumbnailUrl = temporaryElementTwo.attr("style").replace("background-image:url('", "").replace("')", "");
            newManga.setThumbnailUrl(fieldThumbnailUrl);

            String fieldGenres = temporaryElementThree.text();
            newManga.setGenre(fieldGenres);

            boolean fieldIsCompleted = mangaElement.html().contains("Published. (Completed)");
            newManga.setCompleted(fieldIsCompleted);

            int fieldRank = mCounter.incrementAndGet();
            newManga.setRank(fieldRank);

            mangaList.add(newManga);
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

        Element nextUrlElement = parsedDocument.select("div#sp").select("a:containsOwn(>)").first();
        if (nextUrlElement != null) {
            return "http://www.mangareader.net" + nextUrlElement.attr("href");
        }

        return null;
    }
}
