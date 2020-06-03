// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.gson.Gson;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. */ 
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  public static final String TEXTINPUT = "text-input";
  public static final String COMMENTCOUNT = "comment-count";
  public static final String DEFAULTVALUE = "";

  private List<String> comments;
  private DatastoreService datastore;
  private int commentCount;

  @Override
  public void init() {
    // Initialize datastore, comment memory, and comment count.
    datastore = DatastoreServiceFactory.getDatastoreService();
    comments = new ArrayList<>(); 
    commentCount = 3;
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    comments.clear();
    
    // Get "Comment" query from datastore and add only the commentCount amount of comments to memory.
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);
    for (Entity entity : results.asIterable(FetchOptions.Builder.withLimit(commentCount))) {
      String comment = (String)entity.getProperty("rawText");
      comments.add(comment);
    }

    // Send json of queried data to front end.
    String json = getJSONString(comments);
    response.setContentType("application/json");
    response.getWriter().println(json);
  }

  /** Change object to a json string. */
  private String getJSONString(Object object) {
    Gson gson = new Gson();
    String json = gson.toJson(object);
    return json;
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException { 
    // Get input from user.
    String commentInput = getParameter(request, TEXTINPUT, DEFAULTVALUE);
    long timestamp = System.currentTimeMillis();

    // Check if the comment is empty and if not put comment into datastore.
    if (commentInput.length() > 0) {
      Entity commentEntity = new Entity("Comment");
      commentEntity.setProperty("rawText", commentInput);
      commentEntity.setProperty("timestamp", timestamp);
      datastore.put(commentEntity);
    }
    
    // Get how many comments the user wants.
    String commentCountString = getParameter(request, COMMENTCOUNT, DEFAULTVALUE);

    // Error check integer parsing. If not a number, change to default
    try {
      commentCount = Integer.parseInt(commentCountString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + commentCount);
      commentCount = 3;
    }

    // Redirect to greeting page.
    response.sendRedirect("/greeting.html");
  }

  /**
   * Code segment taken from TextProcessor in the walkthrough.
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue){
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  } 
}
