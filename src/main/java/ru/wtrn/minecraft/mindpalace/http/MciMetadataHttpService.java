package ru.wtrn.minecraft.mindpalace.http;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import ru.wtrn.minecraft.mindpalace.http.model.MciImageMetadata;

import static ru.wtrn.minecraft.mindpalace.config.ModCommonConfigs.MCI_SERVER_URL;

public interface MciMetadataHttpService {
    MciMetadataHttpService INSTANCE = new Retrofit.Builder()
            .baseUrl(MCI_SERVER_URL.get())
            .addConverterFactory(GsonConverterFactory.create(
                    new GsonBuilder()
                            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                            .create()
            ))
            .build()
            .create(MciMetadataHttpService.class);

    @GET("/i/{imageId}/meta.json")
    Call<MciImageMetadata> getImageMetadata(@Path("imageId") long imageId);

    @GET("/i/latest/meta.json")
    Call<MciImageMetadata> getLatestImageMetadata();
}
