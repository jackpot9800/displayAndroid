package com.livetv.displayclient.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    // URLs possibles pour l'API
    private static String currentBaseUrl = null;
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (currentBaseUrl == null) {
            throw new IllegalStateException("Server URL not set. Please configure it in Settings.");
        }
        return getClient(currentBaseUrl);
    }
    
    public static Retrofit getClient(String baseUrl) {
        if (retrofit == null || !baseUrl.equals(currentBaseUrl)) {
            currentBaseUrl = baseUrl;
            String apiUrl = currentBaseUrl + "index.php/";
            
            // Ajout d'un intercepteur de logging pour le débogage
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Configuration du client HTTP avec timeouts plus courts pour tester rapidement
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(apiUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
    
    public static void setBaseUrl(String baseUrl) {
        currentBaseUrl = baseUrl;
        resetClient();
    }
    
    public static String getCurrentBaseUrl() {
        return currentBaseUrl;
    }
    
    public static void resetClient() {
        retrofit = null;
    }
}
