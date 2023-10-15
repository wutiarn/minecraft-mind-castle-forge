package ru.wtrn.minecraft.mindpalace.http;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import ru.wtrn.minecraft.mindpalace.config.ModCommonConfigs;
import ru.wtrn.minecraft.mindpalace.http.model.MciImageMetadata;

import java.util.concurrent.TimeUnit;

import static ru.wtrn.minecraft.mindpalace.config.ModClientConfigs.MCI_CONNECT_TIMEOUT_SECONDS;
import static ru.wtrn.minecraft.mindpalace.config.ModClientConfigs.MCI_READ_TIMEOUT_SECONDS;
import static ru.wtrn.minecraft.mindpalace.config.ModCommonConfigs.MCI_SERVER_URL;

public interface MciHttpService {
    OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                Request request = chain.request().newBuilder()
                        .header("MCI-Shared-Secret", ModCommonConfigs.MCI_SECRET.get())
                        .build();
                return chain.proceed(request);
            })
            .connectTimeout(MCI_CONNECT_TIMEOUT_SECONDS.get(), TimeUnit.SECONDS)
            .readTimeout(MCI_READ_TIMEOUT_SECONDS.get(), TimeUnit.SECONDS)
            .build();
    MciHttpService INSTANCE = new Retrofit.Builder()
            .baseUrl(MCI_SERVER_URL.get())
            .client(HTTP_CLIENT)
            .addConverterFactory(GsonConverterFactory.create(new Gson()))
            .build()
            .create(MciHttpService.class);

    @GET("/i/{imageId}/meta.json")
    Call<MciImageMetadata> getImageMetadata(@Path("imageId") long imageId, @Header("Authorization") String token);

    @GET("/i/latest/meta.json")
    Call<MciImageMetadata> getLatestImageMetadata(@Header("Authorization") String token);
}
