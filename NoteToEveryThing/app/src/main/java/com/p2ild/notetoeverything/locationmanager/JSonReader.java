package com.p2ild.notetoeverything.locationmanager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Created by duypi on 9/23/2016.
 */
public class JSonReader {
    public static String readAll(Reader rd){
        StringBuilder sb = new StringBuilder();
        int length;
        try {
            while ((length=rd.read())!=-1){
                sb.append((char)length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        String jsonText = readAll(bufferedReader);
        JSONObject jsonObject = new JSONObject(jsonText);
        return jsonObject;
    }
}