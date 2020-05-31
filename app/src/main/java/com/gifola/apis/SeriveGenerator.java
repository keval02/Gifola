package com.gifola.apis;

import androidx.annotation.NonNull;

import com.gifola.constans.StringConverterFactory;

import java.io.File;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class SeriveGenerator {

    public static AdminAPI getAPIClass() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiURLs.BASE_URL)
                .client(getRequestHeader())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(StringConverterFactory.create())
                .build();
        return retrofit.create(AdminAPI.class);
    }

    @NonNull
    public static MultipartBody.Part prepareFilePart(String partName, String fileUri) {
        File file = new File(fileUri);
        String mimeType = URLConnection.guessContentTypeFromName(file.getName());
        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), file);
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }


    private static OkHttpClient getRequestHeader() {

        OkHttpClient httpClient = new OkHttpClient().newBuilder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

        return httpClient;
    }

}
