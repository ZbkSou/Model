package com.example.bkzhou.modle;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.bkzhou.modle.interfaces.IModelResponse;
import com.example.bkzhou.modle.network.UtilVolley;
import com.example.bkzhou.modle.network.VolleyParams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by bkzhou on 15-9-12.
 */
public class Model {
    private UtilVolley mUtilVolley;

    public Model() {
        mUtilVolley = BaseApplication.getUtilVolley();
    }

    /**
     * GET请求自带参数
     *
     * @param url
     * @param params
     * @param JsonResponse
     */
    public void get(String url, Map<String, String> params, UtilVolley.JsonResponse JsonResponse) {
        mUtilVolley.get(url, params, JsonResponse);
    }

    public void get(String url, UtilVolley.JsonResponse JsonResponse) {
        mUtilVolley.get(url, JsonResponse);
    }

    /**
     * GET请求 Key可重复参数
     *
     * @param url
     * @param params
     * @param JsonResponse
     */
    public void get(String url, VolleyParams params, UtilVolley.JsonResponse JsonResponse) {
        mUtilVolley.get(url, params, JsonResponse);
    }

    public <T> void get(String url, VolleyParams params, final IModelResponse<T> modelResponse, final Class<T> clazz) {
        request(Request.Method.GET, url, null, params, modelResponse, clazz);
    }

    public void post(String url, Map<String, String> params, UtilVolley.JsonResponse JsonResponse) {
        mUtilVolley.post(url, params, JsonResponse);
    }

    /**
     * POST请求 KEY可重复参数
     *
     * @param url
     * @param params
     * @param JsonResponse
     */
    public void post(String url, VolleyParams params, UtilVolley.JsonResponse JsonResponse) {
        mUtilVolley.post(url, params, JsonResponse);
    }

    /**
     * @param url
     * @param params
     * @param modelResponse
     * @param clazz
     * @param <T>
     */
    public <T> void post(String url, Map<String, String> params, final IModelResponse<T> modelResponse, final Class<T> clazz) {
        request(Request.Method.POST, url, params, null, modelResponse, clazz);
    }

    /**
     * @param url
     * @param params
     * @param JsonResponse
     */
    public void put(String url, Map<String, String> params, final UtilVolley.JsonResponse JsonResponse) {
        mUtilVolley.put(url, params, JsonResponse);
    }


    /**
     * @param url
     * @param params
     * @param modelResponse
     * @param clazz
     * @param <T>
     */
    public <T> void put(String url, Map<String, String> params, final IModelResponse<T> modelResponse, final Class<T> clazz) {
        request(Request.Method.PUT, url, params, null, modelResponse, clazz);
    }

    /**
     * @param url
     * @param params
     * @param modelResponse
     * @param clazz
     * @param <T>
     */
    public <T> void put(String url, VolleyParams params, final IModelResponse<T> modelResponse, final Class<T> clazz) {
        request(Request.Method.PUT, url, null, params, modelResponse, clazz);
    }

    public <T> void request(int method, String url, Map<String, String> params, VolleyParams volleyParams, final IModelResponse<T> modelResponse, final Class<T> clazz) {
        mUtilVolley.request(method, url, params, volleyParams, new UtilVolley.JsonResponse() {
            @Override
            public void onSuccess(JSONObject response, JSONArray responseArray) {
                Gson gson = new Gson();
                try {
                    T model = null;
                    ArrayList<T> models = new ArrayList<T>();
                    if (response != null) {
                        model = (T) gson.fromJson(response.toString(), clazz);
                    } else if (responseArray != null) {
                        models = gson.fromJson(responseArray.toString(), new TypeToken<T>() {
                        }.getType());
                    }
                    modelResponse.onSuccess(model, models);
                } catch (Exception ex) {
                    modelResponse.onError(ex.getMessage());
                }
            }

            @Override
            public void onError(VolleyError error) {
                modelResponse.onError(error.getMessage());
            }
        });
    }

    /**
     * put with file param
     *
     * @param url
     * @param params
     * @param fileKey
     * @param file
     * @param modelResponse
     * @param clazz
     * @param <T>
     */
    public <T> void put(String url, Map<String, String> params, String fileKey, File file, final com.example.bkzhou.modle.interfaces.IModelResponse<T> modelResponse, final Class<T> clazz) {
        mUtilVolley.requestFile(Request.Method.PUT, url, params, fileKey, file, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                modelResponse.onError(volleyError.getMessage());
            }
        }, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Gson gson = new Gson();
                try {
                    T model = null;
                    ArrayList<T> models = new ArrayList<T>();
                    if (s.startsWith("{")) {
                        model = (T) gson.fromJson(s, clazz);
                    } else {
                        models = gson.fromJson(s, new TypeToken<T>() {
                        }.getType());
                    }
                    modelResponse.onSuccess(model, models);
                } catch (Exception ex) {
                    modelResponse.onError(ex.getMessage());
                }
            }
        });
    }

    /**
     * 上传多个文件
     * @param method
     * @param url
     * @param errorListener
     * @param successListener
     * @param fileKey
     * @param files
     * @param params
     */
    public void uploadingFiles(int method,String url,Response.ErrorListener errorListener,Response.Listener<String > successListener,String fileKey,List<File> files,VolleyParams params){
        mUtilVolley.requestFiles(method,url,errorListener,successListener,fileKey,files,params);
    }

    /**
     * @param url
     * @param params
     * @param JsonResponse
     */
    public void delete(String url, Map<String, String> params, final UtilVolley.JsonResponse JsonResponse) {
        mUtilVolley.delete(url, params, JsonResponse);
    }

    /**
     * @param url
     * @param ErrorListener
     * @param Lister
     * @param fileKey
     * @param file
     * @param params
     */
    public void postFile(String url, Response.ErrorListener ErrorListener, Response.Listener<String>
            Lister, String fileKey, File file, Map<String, String> params) {
        mUtilVolley.requestFile(url, ErrorListener, Lister, fileKey, file, params);
    }
}
