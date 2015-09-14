package com.example.bkzhou.modle.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by bkzhou on 15-9-12.
 */
public class UtilVolley {
        private final static  String TAG = "UtilVolley";

        private Context mContext;

        /**
         * 请求队列 Volley
         */
        private RequestQueue mQueue;
        /**
         * Cookie
         */
        private String mCookies;
        private String mUserAgent;
        private HashMap<String, String> mHeader;
        /**
         * 图片加载
         *
         * @param context
         */
        private ImageLoader mImageLoader;

        private BitmapLruCache bitmapLruCache;


        /**
         * 请求重试时间Timeout
         */
        private static final int MY_SOCKET_TIMEOUT_MS = 60000 * 5;
        /**
         * 图片缓存大小 20MB
         */
        private static final int MAX_CACHE_SIZE = (int) (Runtime.getRuntime().maxMemory() / (8 * 1024));

        /**
         * 网络出错时是否显示错误Toast 默认不显示
         */
        private static boolean needShowToast = false;

        private static Toast mErrorToast = null;


        public UtilVolley() {
        }


        public UtilVolley(Context context) {
            mContext = context;
            if (context == null) {
                Log.e(TAG, " init error: context is null");
            }
            mContext = context;
            if (mQueue == null) {
                mQueue = Volley.newRequestQueue(context);
            }

            if (mImageLoader == null) {
                bitmapLruCache = new BitmapLruCache(MAX_CACHE_SIZE);
                mImageLoader = new ImageLoader(mQueue, bitmapLruCache);
            }
//        mErrorToast = Toast.makeText(mContext, "网络错误", Toast.LENGTH_SHORT);
            if (Build.VERSION.SDK_INT < 19) {
                WebView webView = new WebView(mContext);
                WebSettings settings = webView.getSettings();

                mUserAgent = settings.getUserAgentString();
                webView = null;
                settings = null;
            }


            mHeader = new HashMap<String, String>();
        }

        /*
            添加header
         */
        public void addHeader(String key, String value) {
            mHeader.put(key, value);
        }

        /**
         * 发送同步请求
         */
        public JSONObject syncGet(String url, final Map<String, String> mheader) {
            RequestFuture<JSONObject> future = RequestFuture.newFuture();
            JsonObjectRequest request = new JsonObjectRequest(url, null, future, future) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    if (mCookies != null && mCookies.length() > 0) {
                        headers.put("Cookie", mCookies);
                    }
                    if (mUserAgent != null) {
                        headers.put("User-Agent", mUserAgent);
                    }
                    mergeHeader(headers);
                    if (mheader != null) {
                        Iterator iter = mheader.entrySet().iterator();
                        while (iter.hasNext()) {
                            Map.Entry entry = (Map.Entry) iter.next();
                            String key = (String) entry.getKey();
                            String val = (String) entry.getValue();
                            Log.d("key", "key = " + key);
                            Log.d("value", "value = " + val);
                            headers.put(key, val);
                        }
                        Log.d("headers", "headers = " + headers.toString());

                    }

                    return headers;
                }
            };


            mQueue.add(request);
            try {
                JSONObject result = future.get();
                return result;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * 取消所有请求
         */
        public void cancelAllRequest() {
            mQueue.cancelAll("all");
        }

        /**
         * 将缓存图片释放掉
         */
        public void releaseCache() {
            if (bitmapLruCache != null) {
                Log.d(TAG, " release Cache ");
                bitmapLruCache.release();
            }
        }

        /**
         * 设置默认Cookie 保持登陆状态
         */
        public void setCookie(String cookie) {
            mCookies = cookie;
        }

        public String getCookie() {
            return mCookies;
        }

        public void clearCookie() {
            mCookies = null;
        }

        public void setPreUserAgent(String preUserAgent) {
            mUserAgent = preUserAgent + mUserAgent;
        }

        /**
         * 取消或显示错误Toast
         *
         * @param need
         */
        public void setShowErrorToast(boolean need) {
            needShowToast = need;
        }

        public ImageLoader getImageLoader() {
            return mImageLoader;
        }

        /**
         * Json 返回值
         *
         * @author ihaveu
         */
        public interface JsonResponse {
            public void onSuccess(JSONObject response, JSONArray responseArray);

            public void onError(VolleyError error);
        }

        /**
         * String 返回值
         *
         * @author ihaveu
         */
        public interface StringResponse {
            public void onSuccess(String response);

            public void onError(VolleyError error);
        }

        /**
         * Get
         * url中附带参数
         *
         * @param url
         * @param jsonResponse
         */
        public void get(String url, final JsonResponse jsonResponse) {
            Log.d(TAG, "get from:" + url);
            request(Request.Method.GET, url, null, null, jsonResponse);
        }

        /**
         * Get
         * 自带参数
         *
         * @param url
         * @param params
         * @param jsonResponse
         */
        public void get(String url, Map<String, String> params, final JsonResponse jsonResponse) {
            StringBuilder newUrl = new StringBuilder();
            newUrl.append(url);
            if (params != null) {
                if (url.indexOf("?") < 0) {
                    newUrl.append("?");
                }
                for (String key : params.keySet()) {
                    newUrl.append(key);
                    newUrl.append("=");
                    newUrl.append(params.get(key));
                    newUrl.append("&");
                }
                newUrl.deleteCharAt(newUrl.length() - 1);
            }
            request(Request.Method.GET, newUrl.toString(), params, jsonResponse);
        }

        /**
         * PUT
         *
         * @param url
         * @param jsonResponse
         */
        public void put(String url, Map<String, String> request, final JsonResponse jsonResponse) {
            request(Request.Method.PUT, url, request, jsonResponse);
        }

        /**
         * Post
         *
         * @param url
         * @param jsonResponse
         */
        public void post(String url, Map<String, String> request, final JsonResponse jsonResponse) {
            request(Request.Method.POST, url, request, jsonResponse);
        }

        /**
         * Delete
         *
         * @param url
         * @param request
         * @param jsonResponse
         */
        public void delete(String url, VolleyParams request, final JsonResponse jsonResponse) {
            request(Request.Method.DELETE, url, request, jsonResponse);
        }

        /**
         * ***********Key可重复参数*************************************************************************************************
         */

        public void get(String url, VolleyParams params, final JsonResponse jsonResponse) {
            StringBuilder newUrl = new StringBuilder();
            newUrl.append(url);
            if (params != null) {
                if (url.indexOf("?") < 0) {
                    newUrl.append("?");
                }
                newUrl.append(params.getParamsString());
            }
            request(Request.Method.GET, newUrl.toString(), params, jsonResponse);
        }

        /**
         * Post
         *
         * @param url
         * @param jsonResponseom/products.json?filter=true&keyword=长袖衬衫&where[unsold_count][gt]=0&order[published_at]=desc&where[target][]=男
         */
        public void post(String url, VolleyParams request, final JsonResponse jsonResponse) {
            request(Request.Method.POST, url, request, jsonResponse);
        }

        /**
         * Delete
         *
         * @param url
         * @param request
         * @param jsonResponse
         */
        public void delete(String url, Map<String, String> request, final JsonResponse jsonResponse) {
            request(Request.Method.DELETE, url, request, jsonResponse);
        }


        /**
         * 请求
         *
         * @param request
         */
        public void request(Request request) {
            request.setRetryPolicy(new DefaultRetryPolicy(MY_SOCKET_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mQueue.add(request);
        }

        /**
         * 请求数据
         * 参数不可重复
         *
         * @param method
         * @param url
         * @param params       Map
         * @param jsonResponse
         */
        private void request(int method, String url, final Map<String, String> params, final JsonResponse jsonResponse) {
            request(method, url, params, null, jsonResponse);
        }

        /**
         * 请求数据
         * 参数可重复
         *
         * @param method
         * @param url
         * @param params
         * @param jsonResponse
         */
        private void request(int method, String url, final VolleyParams params, final JsonResponse jsonResponse) {
            request(method, url, null, params, jsonResponse);
        }

        /**
         * 上传文件
         *
         * @param url
         * @param ErrorListener
         * @param Lister
         * @param fileKey
         * @param file
         * @param params
         */
        public void requestFile(String url, Response.ErrorListener ErrorListener, Response.Listener<String>
                Lister, String fileKey, File file, Map<String, String> params) {
            requestFile(Request.Method.POST, url, params, fileKey, file, ErrorListener, Lister);
        }

        /**
         * 上传一个文件
         *
         * @param method          请求方式
         * @param url
         * @param params
         * @param fileKey
         * @param file
         * @param ErrorListener
         * @param SuccessListener
         */
        public void requestFile(int method, String url, Map<String, String> params, String fileKey, File file, Response.ErrorListener ErrorListener, Response.Listener<String> SuccessListener) {
            MultipartRequest request = new MultipartRequest(method, url, ErrorListener, SuccessListener, fileKey, file, params) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    return mergeHeader(headers);
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(MY_SOCKET_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mQueue.add(request);
        }

        /**
         * 上传多个文件
         *
         * @param method
         * @param url
         * @param errorListener
         * @param successListener
         * @param fileKey
         * @param files
         * @param params
         */
        public void requestFiles(int method, String url, Response.ErrorListener errorListener, Response.Listener<String> successListener, String fileKey, List<File> files, VolleyParams params) {
            MultipartRequest request = new MultipartRequest(method, url, errorListener, successListener, fileKey, files, params) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    return mergeHeader(headers);
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(MY_SOCKET_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mQueue.add(request);
        }

        private HashMap<String, String> mergeHeader(HashMap<String, String> headers) {
            if (mCookies != null && mCookies.length() > 0) {
                headers.put("Cookie", mCookies);
            }
            if (mUserAgent != null) {
                headers.put("User-Agent", mUserAgent);
            }
            Iterator iter = mHeader.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String) entry.getKey();
                String val = (String) entry.getValue();
                Log.d("key", "key = " + key);
                Log.d("value", "value = " + val);
                headers.put(key, val);
            }
            Log.d("mergeHeader", "mergeHeader = " + headers.toString());
            return headers;
        }

        /**
         * 请求数据
         * 基本方法get带/参数，直接通过此方法
         *
         * @param method       请求方法POST,DELETE,GET etc.
         * @param url          地址
         * @param params       Map 参数
         * @param vParams      VolleyParams 参数  与params 二者取其一,主要解决参数包含重复key的情况
         * @param jsonResponse 请求成功回调
         */
        public void request(int method, String url, final Map<String, String> params, final VolleyParams vParams, final JsonResponse jsonResponse) {
            if (mQueue == null) {
                Log.e(TAG, "queue not init,please init model first");
                return;
            }

            // 请求数据
            StringRequest strRequest = new StringRequest(method, url, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    // TODO Auto-generated method stub
                    JSONArray jsonArr = new JSONArray();
                    JSONObject jsonObj = new JSONObject();
                    try {
                        if (response.trim().startsWith("[")) {
                            jsonArr = new JSONArray(response);
                        } else if (response.trim().startsWith("{")) {
                            jsonObj = new JSONObject(response);
                        }
                        jsonResponse.onSuccess(jsonObj, jsonArr);

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        jsonResponse.onError(new VolleyError("convert response to JSONObject or JSONArray error"));
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // TODO Auto-generated method stub
                    jsonResponse.onError(error);
                    if (needShowToast && mErrorToast != null) {
                        mErrorToast.show();
                    }
                }
            }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    return mergeHeader(headers);
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    /**
                     * 成功设置参数，实现参数Key可重复
                     */
                    if (vParams != null) {
                        return vParams.encodeParameters(getParamsEncoding());
                    }
                    return super.getBody();
                }
            };

            strRequest.setShouldCache(false);
            strRequest.setRetryPolicy(new DefaultRetryPolicy(MY_SOCKET_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mQueue.add(strRequest);
        }

        public void get(String url, final StringResponse stringResponse) {
            if (mQueue == null) {
                Log.e(TAG, "queue not init,please init model first");
                return;
            }

            // 请求数据
            StringRequest strRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    stringResponse.onSuccess(response);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    stringResponse.onError(error);
                }
            }) {

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    return mergeHeader(headers);
                }
            };

            strRequest.setRetryPolicy(new DefaultRetryPolicy(MY_SOCKET_TIMEOUT_MS, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            mQueue.add(strRequest);
        }


        /**
         * ghq tag
         * <p/>
         * ImageLoader 请求图片 defaultImageResId, errorImageResId 可以指向默认的图片id<br>
         * 直接将请求的图片放在指定的iamgeview上
         *
         * @param url
         * @param imageView
         */
        public void loadImage(String url, ImageView imageView) {
            int defaultImageResId = 0;
            int errorImageResId = 0;
            loadImage(url, imageView, defaultImageResId, errorImageResId);
        }

        /**
         * ghq tag
         * <p/>
         * ImageLoader 请求图片 需要自己指定图片时候 直接调此方法。<br>
         * 直接将请求的图片放在指定的iamgeview上
         *
         * @param url
         * @param imageView         指定用于显示图片的ImageView控件，
         * @param defaultImageResId 指定加载图片的过程中显示的图片，
         * @param errorImageResId   指定加载图片失败的情况下显示的图片。
         */
        public void loadImage(String url, ImageView imageView, int defaultImageResId, int errorImageResId) {
            ImageLoader.ImageListener imageListener = ImageLoader.getImageListener(imageView, defaultImageResId, errorImageResId);
            mImageLoader.get(url, imageListener);
        }

        /**
         * ghq tag
         * <p/>
         * 网络请求返回bitmap接口
         *
         * @author gaohequan
         */
        public interface onHttpResponseBitmap {
            public void success(Bitmap bitmap);

            public void fial(VolleyError error);
        }

        /**
         * ghq tag
         * <p/>
         * 网络请求获取 Bitmap对象
         *
         * @param url            图片链接
         * @param responseBitmap 回调函数
         */
        public void loadBitmap(String url, final onHttpResponseBitmap responseBitmap) {
            ImageRequest imageRequest = new ImageRequest(url, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap bitmap) {
                    responseBitmap.success(bitmap);
                }
            }, 0, 0, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    responseBitmap.fial(error);
                }
            });
            mQueue.add(imageRequest);
        }


    }


