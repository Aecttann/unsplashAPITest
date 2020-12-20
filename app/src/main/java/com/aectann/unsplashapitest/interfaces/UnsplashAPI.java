package com.aectann.unsplashapitest.interfaces;

import com.aectann.unsplashapitest.POJOs.POJOPhotos;
import com.aectann.unsplashapitest.POJOs.POJOSearchPhotos;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface UnsplashAPI {

//    @GET("photos")
//    Call<List<POJOPhotos>> getPhotos(@Header("Authorization") String ACCESS_KEY);
    @GET("photos/random")
    Call<List<POJOPhotos>> getRandomPhotos(@Header("Authorization") String ACCESS_KEY,
                                           @Query("count") String count);
    @GET("search/photos")
    Call<POJOSearchPhotos> searchPhotos(@Header("Authorization") String ACCESS_KEY,
                                              @Query("query") String query,       //Search terms.
                                              @Query("page") String page,         //Page number to retrieve. (Optional; default: 1)
                                              @Query("per_page") String per_page); //Number of items per page. (Optional; default: 10)

}
