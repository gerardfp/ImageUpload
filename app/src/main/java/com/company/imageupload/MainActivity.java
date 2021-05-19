package com.company.imageupload;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.company.imageupload.databinding.ActivityMainBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public class MainActivity extends AppCompatActivity {

    private MainViewModel mainViewModel;
    private ActivityMainBinding binding;

    static final String baseUrl = "https://yopi.herokuapp.com/";

    public static class MainViewModel extends ViewModel {

        MutableLiveData<String> avatarUri = new MutableLiveData<>();

        public void upload(byte[] bytes) {

            MultipartBody.Part body = MultipartBody.Part.createFormData("avatar", "avatar",
                    RequestBody.create(MediaType.parse("image/*"), bytes));


            ApiService.api.upload(body).enqueue(new Callback<ApiService.Respuesta>() {
                @Override
                public void onResponse(Call<ApiService.Respuesta> call, Response<ApiService.Respuesta> response) {
                    Log.e("ABCD", "upload ok  " + response.body().imgUrl);
                    avatarUri.postValue(response.body().imgUrl);
                }

                @Override
                public void onFailure(Call<ApiService.Respuesta> call, Throwable t) {
                    Log.e("ABCD", "upload failure " + t.getMessage());

                }
            });
        }
    }

    static class ApiService {
        public static Api api = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(Api.class);

        public static class Respuesta {
            public String imgUrl;
        }

        public interface Api {
            @Multipart
            @POST("upload")
            Call<Respuesta> upload(@Part MultipartBody.Part file);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((binding = ActivityMainBinding.inflate(getLayoutInflater())).getRoot());

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        mainViewModel.avatarUri.observe(this, uri -> Glide.with(this).load(baseUrl + uri).into(binding.foto));
        binding.foto.setOnClickListener(v -> galeria.launch("image/*"));
    }

    ActivityResultLauncher<String> galeria = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
        try {
            mainViewModel.upload(uri2Bytes(uri));
        } catch (IOException e) {
            // no se pudo leer la foto
        }
    });

    byte[] uri2Bytes(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for(int len; (len = inputStream.read(buffer)) != -1;) byteBuffer.write(buffer, 0, len);
        return byteBuffer.toByteArray();
    }
}