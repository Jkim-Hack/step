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

import static com.google.sps.other.Constants.*;
import static com.google.sps.other.Common.*;

import com.google.sps.other.Comment;
import com.google.gson.Gson;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;

/** Servlet that returns some example content. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private List<Comment> comments;
  private DatastoreService datastore;
  private int commentCount;
  private UserService userService;
  private BlobstoreService blobstoreService;

  @Override
  public void init() {
    // Initialize datastore, comment memory, comment count, and user service.
    this.datastore = DatastoreServiceFactory.getDatastoreService();
    this.comments = new ArrayList<>();
    this.commentCount = DEFAULTCOMMENTCOUNT;
    this.userService = UserServiceFactory.getUserService();
    this.blobstoreService = BlobstoreServiceFactory.getBlobstoreService();
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    this.comments.clear();

    // Get "Comment" query from datastore and add only the commentCount amount of comments to memory.
    Query query = new Query(COMMENTPATH).addSort(TIMESTAMPPROPERTY, SortDirection.DESCENDING);
    PreparedQuery results = this.datastore.prepare(query);
    for (Entity entity : results.asIterable(FetchOptions.Builder.withLimit(commentCount))) {
      // Get each comment and add to memory
      Comment comment = new Comment.Builder()
	.withEntity(entity)
	.build();
      this.comments.add(comment);
    }

    // Send json of queried data to front end.
    String json = getJSONString(this.comments);
    response.setContentType("application/json");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Get input from user.
    String commentInput = getParameter(request, TEXTINPUT, DEFAULTVALUE);
    long timestamp = System.currentTimeMillis();

    String imageUrl = getUploadedFileUrl(request, "image");
    System.out.println(imageUrl);

    // Check if the comment is empty and if not put comment into datastore.
    if (commentInput.length() > 0) {
      String email = this.userService.getCurrentUser().getEmail();

      Entity commentEntity = new Entity(COMMENTPATH);
      commentEntity.setProperty(EMAILPROPERTY, email);
      commentEntity.setProperty(RAWTEXTPROPERTY, commentInput);
      commentEntity.setProperty(IMAGEURLPROPERTY, imageUrl);
      commentEntity.setProperty(TIMESTAMPPROPERTY, timestamp);
      this.datastore.put(commentEntity);
    }

    // Get how many comments the user wants.
    String commentCountString = getParameter(request, COMMENTCOUNT, DEFAULTVALUE);

    // Error check integer parsing. If not a number, change to default
    try {
      this.commentCount = Integer.parseInt(commentCountString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + this.commentCount);
      this.commentCount = DEFAULTCOMMENTCOUNT;
    }

    // Redirect to greeting page.
    response.sendRedirect(GREETING_URL);
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


  /**
   * Gets the uploaded file. If there are no files uploaded then return empty string.
   * */
  private String getUploadedFileUrl(HttpServletRequest request, String formInputElementName) {
    Map<String, List<BlobKey>> allBlobs = this.blobstoreService.getUploads(request);
    List<BlobKey> blobKeys = allBlobs.get(formInputElementName);

    // Check null blobkeys/if it's empty
    if (blobKeys == null || blobKeys.isEmpty())
      return "";

    // Get first and only blob
    BlobKey blobKey = blobKeys.get(0);

    // If blob is empty then return null
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      blobstoreService.delete(blobKey);
      return "";
    }

    // Build image options
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    ServingUrlOptions urlOptions = ServingUrlOptions.Builder.withBlobKey(blobKey);

    try {
      URL url = new URL(imagesService.getServingUrl(urlOptions));
      return url.getPath();
    } catch (MalformedURLException e) {
      return imagesService.getServingUrl(urlOptions);
    }
  }
}
