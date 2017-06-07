package com.google.playstore.app.download;

import android.content.Context;
import android.util.Log;

import com.github.yeriomin.playstoreapi.AndroidAppDeliveryData;
import com.github.yeriomin.playstoreapi.BulkDetailsEntry;
import com.github.yeriomin.playstoreapi.DeliveryResponse;
import com.github.yeriomin.playstoreapi.DetailsResponse;
import com.github.yeriomin.playstoreapi.DocV2;
import com.google.playstore.app.download.model.App;
import com.google.playstore.app.download.model.AppBuilder;
import com.google.playstore.app.download.model.ReviewBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Akdeniz Google Play Crawler classes are supposed to be independent from android,
 * so this warpper manages anything android-related and feeds it to the Akdeniz's classes
 * Specifically: credentials via Preferences, downloads via DownloadManager, app details using
 * android PackageInfo
 */
public class PlayStoreApiWrapper {

    private static final String BACKEND_DOCID_SIMILAR_APPS = "similar_apps";
    private static final String BACKEND_DOCID_USERS_ALSO_INSTALLED = "users_also_installed";

    private Context context;

    public PlayStoreApiWrapper(Context context) {
        this.context = context;
        AppBuilder.suffixMil = context.getString(R.string.suffix_million);
        AppBuilder.suffixBil = context.getString(R.string.suffix_billion);
    }

    public App getDetails(String packageId) throws IOException {
        DetailsResponse response = new PlayStoreApiAuthenticator(context).getApi().details(packageId);
        App app = AppBuilder.build(response.getDocV2());
        if (response.hasUserReview()) {
            app.setUserReview(ReviewBuilder.build(response.getUserReview()));
        }
        for (DocV2 doc : response.getDocV2().getChildList()) {
            boolean isSimilarApps = doc.getBackendDocid().contains(BACKEND_DOCID_SIMILAR_APPS);
            boolean isUsersAlsoInstalled = doc.getBackendDocid().contains(BACKEND_DOCID_USERS_ALSO_INSTALLED);
            if (isUsersAlsoInstalled && app.getUsersAlsoInstalledApps().size() > 0) {
                // Two users_also_installed lists are returned, consisting of mostly the same apps
                continue;
            }
            for (DocV2 child : doc.getChildList()) {
                if (isSimilarApps) {
                    app.getSimilarApps().add(AppBuilder.build(child));
                } else if (isUsersAlsoInstalled) {
                    app.getUsersAlsoInstalledApps().add(AppBuilder.build(child));
                }
            }
        }
        return app;
    }

    public List<App> getDetails(List<String> packageIds) throws IOException {
        List<App> apps = new ArrayList<>();
        int i = -1;
        for (BulkDetailsEntry details : new PlayStoreApiAuthenticator(context).getApi().bulkDetails(packageIds).getEntryList()) {
            i++;
            if (!details.hasDoc()) {
                Log.i(this.getClass().getName(), "Empty response for " + packageIds.get(i));
                continue;
            }
            App app = AppBuilder.build(details.getDoc());
            if (!app.isFree()) {
                Log.i(this.getClass().getName(), "Skipping non-free app " + packageIds.get(i));
                continue;
            }
            apps.add(app);
        }
        Collections.sort(apps);
        return apps;
    }

    public AndroidAppDeliveryData purchaseOrDeliver(App app) throws IOException, NotPurchasedException {
        if (app.isFree()) {
            return new PlayStoreApiAuthenticator(context).getApi()
                    .purchase(app.getPackageName(), app.getVersionCode(), app.getOfferType())
                    .getPurchaseStatusResponse()
                    .getAppDeliveryData();
        }
        DeliveryResponse response = new PlayStoreApiAuthenticator(context).getApi().delivery(
                app.getPackageName(), app.getVersionCode(), app.getOfferType());
        if (response.hasAppDeliveryData()) {
            return response.getAppDeliveryData();
        } else {
            throw new NotPurchasedException();
        }
    }
}
