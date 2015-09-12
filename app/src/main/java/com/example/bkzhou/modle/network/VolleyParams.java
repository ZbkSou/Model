package com.example.bkzhou.modle.network;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bkzhou on 15-9-12.
 */
public class VolleyParams extends HashMap<String, String> implements Serializable  {


        private static final long serialVersionUID = 1L;
        //可重复参数
        private ArrayList<ParamsEntity> mParams = new ArrayList<ParamsEntity>();

        public VolleyParams() {
            super();
        }

        public VolleyParams(int capacity) {
            super(capacity);
        }

        public VolleyParams(Map<String, String> map) {
            super(map);
        }

        public VolleyParams(int capacity, float loadFactor) {
            super(capacity, loadFactor);
        }

        /*
         * This is the method to use for adding post parameters
         */
        public void add(String key, String value) {
            mParams.add(new ParamsEntity(key, value));
        }

        @Override
        public int size() {
            return mParams.size() + super.size();
        }

        /**
         * Converts the Map into an application/x-www-form-urlencoded encoded string.
         */
        public byte[] encodeParameters(String paramsEncoding) {
            StringBuilder encodedParams = new StringBuilder();
            try {
                for (String key : keySet()) {
                    encodedParams.append(URLEncoder.encode(key, paramsEncoding));
                    encodedParams.append('=');
                    encodedParams.append(URLEncoder.encode(get(key), paramsEncoding));
                    encodedParams.append('&');
                }
                for (ParamsEntity entity : mParams) {
                    encodedParams.append(URLEncoder.encode(entity.getKey(), paramsEncoding));
                    encodedParams.append('=');
                    encodedParams.append(URLEncoder.encode(entity.getValue(), paramsEncoding));
                    encodedParams.append('&');
                }
                return encodedParams.toString().getBytes(paramsEncoding);
            } catch (UnsupportedEncodingException uee) {
                throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return new byte[0];
        }

        /**
         * 获取参数字符串
         *
         * @return
         */
        public String getParamsString() {
            StringBuilder encodedParams = new StringBuilder();
            String value = "";
            try {
                for (String key : keySet()) {
                    encodedParams.append(URLEncoder.encode(key, "utf-8"));
                    encodedParams.append('=');
                    value = URLEncoder.encode(get(key), "utf-8");
                    encodedParams.append(value);
                    encodedParams.append('&');
                }
                for (ParamsEntity entity : mParams) {
                    encodedParams.append(URLEncoder.encode(entity.getKey(), "utf-8"));
                    encodedParams.append('=');
                    value = URLEncoder.encode(entity.getValue(), "utf-8");
                    encodedParams.append(value);
                    if (mParams.indexOf(entity) != mParams.size() - 1) {
                        encodedParams.append('&');
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return encodedParams.toString();
        }

        public ArrayList<ParamsEntity> getSubParams(){
            return mParams;
        }

        public class ParamsEntity {
            private String key = "";
            private String value = "";

            public ParamsEntity(String key, String value) {
                setKey(key);
                setValue(value);
            }

            public String getKey() {
                return key;
            }

            public void setKey(String key) {
                this.key = key;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }


        public void contact(VolleyParams volleyParams) {
            if (volleyParams == null) {
                return;
            }
            mParams.addAll(volleyParams.mParams);
            for (String key : volleyParams.keySet()) {
                add(key, volleyParams.get(key));
            }
        }


}
