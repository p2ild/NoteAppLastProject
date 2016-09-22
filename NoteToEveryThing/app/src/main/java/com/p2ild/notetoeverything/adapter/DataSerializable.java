package com.p2ild.notetoeverything.adapter;

import java.util.ArrayList;

/**
 * Created by duypi on 9/16/2016.
 */
public class DataSerializable implements java.io.Serializable
    {
        private Object data;

        public DataSerializable(Object o) {
        this.data = o;
    }

        public Object getData() {
        return data;
    }
}
