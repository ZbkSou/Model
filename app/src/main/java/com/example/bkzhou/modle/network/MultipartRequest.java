package com.example.bkzhou.modle.network;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bkzhou on 15-9-12.
 */
public class MultipartRequest {

    private MultipartEntity entity = new MultipartEntity();

    private final Response.Listener<String> mListener;

    private List<File> mFileParts;
    private String mFilePartName;
    private Map<String, String> mParams;
    private VolleyParams mVolleyParams;
    private String mBoundary;

    /**
     * 单个文件
     *
     * @param url
     * @param errorListener
     * @param listener
     * @param filePartName
     * @param file
     * @param params
     */
    public MultipartRequest(String url, Response.ErrorListener errorListener,
                            Response.Listener<String> listener, String filePartName, File file,
                            Map<String, String> params) {
        this(Request.Method.POST, url, errorListener, listener, filePartName, file, params);
    }

    /**
     * 多个文件，对应一个key
     *
     * @param url
     * @param errorListener
     * @param listener
     * @param filePartName
     * @param files
     * @param params
     */
    public MultipartRequest(String url, Response.ErrorListener errorListener,
                            Response.Listener<String> listener, String filePartName,
                            List<File> files, Map<String, String> params) {
        super(Request.Method.POST, url, errorListener);
        mFilePartName = filePartName;
        mListener = listener;
        mFileParts = files;
        mParams = params;
        buildMultipartEntity();
    }

    /**
     * ghq 上传多个问价 增加一个上传方式的参数
     * @param method
     * @param url
     * @param errorListener
     * @param listener
     * @param filePartName
     * @param files
     * @param params
     */
    public MultipartRequest(int method,String url, Response.ErrorListener errorListener,
                            Response.Listener<String> listener, String filePartName,
                            List<File> files, VolleyParams params) {
        super(method, url, errorListener);
        mFilePartName = filePartName;
        mListener = listener;
        mFileParts = files;
        mVolleyParams = params;
        buildMultipartEntityExtra();
    }

    /**
     * 单个文件，支持method参数
     *
     * @param method
     * @param url
     * @param errorListener
     * @param listener
     * @param filePartName
     * @param file
     * @param params
     */
    public MultipartRequest(int method, String url, Response.ErrorListener errorListener, Response.Listener<String> listener, String filePartName, File file, Map<String, String> params) {
        super(method, url, errorListener);
        mFileParts = new ArrayList<File>();
        if (file != null) {
            mFileParts.add(file);
        }
        mFilePartName = filePartName;
        mListener = listener;
        mParams = params;
        buildMultipartEntity();
    }

    private void buildMultipartEntity() {
        try {
            if (mFileParts != null && mFileParts.size() > 0) {
                for (File file : mFileParts) {
                    String extra = file.getName().substring(file.getName().indexOf(".") + 1);
                    entity.addPart(mFilePartName, new FileBody(file, "image/" + extra));
                }
                long l = entity.getContentLength();
            }

            if (mParams != null && mParams.size() > 0) {
                for (Map.Entry<String, String> entry : mParams.entrySet()) {
                    entity.addPart(
                            entry.getKey(),
                            new StringBody(entry.getValue(), Charset
                                    .forName("UTF-8")));
                }
            }
        } catch (UnsupportedEncodingException e) {
            VolleyLog.e("UnsupportedEncodingException");
        }
    }

    private void buildMultipartEntityExtra(){
        try {
            if (mFileParts != null && mFileParts.size() > 0) {
                for (File file : mFileParts) {
                    String extra = file.getName().substring(file.getName().indexOf(".") + 1);
                    entity.addPart(mFilePartName, new FileBody(file, "image/" + extra));
                }
                long l = entity.getContentLength();
            }
            if(mVolleyParams!=null && mVolleyParams.size()>0){
                for(VolleyParams.Entry<String,String> item : mVolleyParams.entrySet()){
                    entity.addPart(
                            item.getKey(),
                            new StringBody(item.getValue(), Charset
                                    .forName("UTF-8")));
                }
                for(VolleyParams.ParamsEntity item: mVolleyParams.getSubParams() ){
                    entity.addPart(item.getKey(),new StringBody(item.getValue(), Charset
                            .forName("UTF-8")));
                }
            }
        }catch (Exception e){

        }
    }
    @Override
    public String getBodyContentType() {
        Log.i("ghq", entity.getContentType().getName() + "   .." + entity.getContentType().getValue());
        return entity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            entity.writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        if (VolleyLog.DEBUG) {
            if (response.headers != null) {
                for (Map.Entry<String, String> entry : response.headers
                        .entrySet()) {
                    VolleyLog.d(entry.getKey() + "=" + entry.getValue());
                }
            }
        }

        String parsed;
        try {
            parsed = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed,
                HttpHeaderParser.parseCacheHeaders(response));
    }


    /*
     * (non-Javadoc)
     *
     * @see com.android.volley.Request#getHeaders()
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        VolleyLog.d("getHeaders");
        Map<String, String> headers = super.getHeaders();

        if (headers == null || headers.equals(Collections.emptyMap())) {
            headers = new HashMap<String, String>();
        }


        return headers;
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }
}
