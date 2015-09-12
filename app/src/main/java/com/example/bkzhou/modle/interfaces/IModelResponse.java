package com.example.bkzhou.modle.interfaces;

import java.util.ArrayList;

/**
 * Created by bkzhou on 15-9-12.
 */
    public interface IModelResponse<T> {
        public void onSuccess(T model, ArrayList<T> list);

        public void onError(String msg);
    }


