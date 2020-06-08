package com.google.sps.other;

import com.google.gson.Gson;

public class Common {

  public static String getJSONString(Object object) {
    Gson gson = new Gson();
    String json = gson.toJson(object);
    return json;
  }

}
