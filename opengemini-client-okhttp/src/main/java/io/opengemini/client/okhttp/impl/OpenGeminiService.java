package io.opengemini.client.okhttp.impl;

import io.opengemini.client.api.QueryResult;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * @author Janle
 * @date 2024/5/10 10:38
 */
interface OpenGeminiService {

    String U = "u";
    String P = "p";
    String Q = "q";
    String DB = "db";
    String RP = "rp";
    String PARAMS = "params";
    String PRECISION = "precision";
    String CONSISTENCY = "consistency";
    String EPOCH = "epoch";
    String CHUNK_SIZE = "chunk_size";

    @GET("ping")
    public Call<ResponseBody> ping();

    /**
     * @param username        u: optional The username for authentication
     * @param password        p: optional The password for authentication
     * @param database        db: required The database to write points
     * @param retentionPolicy rp: optional The retention policy to write points.
     *                        If not specified, the autogen retention
     * @param precision       optional The precision of the time stamps (n, u, ms, s, m, h).
     *                        If not specified, n
     * @param consistency     optional The write consistency level required for the write to succeed.
     *                        Can be one of one, any, all, quorum. Defaults to all.
     */
    @POST("write")
    Call<ResponseBody> writePoints(@Query(DB) String database,
                                   @Query(RP) String retentionPolicy, @Query(PRECISION) String precision,
                                   @Query(CONSISTENCY) String consistency, @Body RequestBody batchPoints);

    @GET("query")
    Call<QueryResult> query(@Query(DB) String db,
                            @Query(EPOCH) String epoch, @Query(value = Q, encoded = true) String query);

    @POST("query")
    @FormUrlEncoded
    Call<QueryResult> query(@Query(DB) String db,
                            @Query(EPOCH) String epoch, @Field(value = Q, encoded = true) String query,
                            @Query(value = PARAMS, encoded = true) String params);

    @GET("query")
    Call<QueryResult> query(@Query(DB) String db,
                            @Query(value = Q, encoded = true) String query);

    @POST("query")
    @FormUrlEncoded
    Call<QueryResult> postQuery(@Query(DB) String db,
                                @Field(value = Q, encoded = true) String query);

    @POST("query")
    @FormUrlEncoded
    Call<QueryResult> postQuery(@Query(DB) String db,
                                @Field(value = Q, encoded = true) String query, @Query(value = PARAMS, encoded = true) String params);

    @POST("query")
    @FormUrlEncoded
    Call<QueryResult> postQuery(@Field(value = Q, encoded = true) String query);

    @Streaming
    @GET("query?chunked=true")
    Call<ResponseBody> query(@Query(DB) String db,
                             @Query(RP) String retentionPolicy,
                             @Query(value = Q, encoded = true) String query,
                             @Query(CHUNK_SIZE) int chunkSize);

    @Streaming
    @POST("query?chunked=true")
    @FormUrlEncoded
    Call<ResponseBody> query(@Query(DB) String db,
                             @Query(RP) String retentionPolicy,
                             @Field(value = Q, encoded = true) String query,
                             @Query(CHUNK_SIZE) int chunkSize,
                             @Query(value = PARAMS, encoded = true) String params);

}
