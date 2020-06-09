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


package com.google.sps.other;

import com.google.appengine.api.datastore.Entity;
import static com.google.sps.other.Constants.*;

public class Comment {
 
  private String email;
  private String rawText;
  private String imageUrl;
  private long timeStamp;
 
  public static class Builder {

    private String email;
    private String rawText;
    private String imageUrl;
    private long timeStamp;

    public Builder() {
      this.email = "";
      this.rawText = "";
      this.imageUrl = "";
      this.timeStamp = System.currentTimeMillis();
    }

    public Builder withEntity(Entity entity) {
      this.email = (String)entity.getProperty(EMAILPROPERTY);
      this.rawText = (String)entity.getProperty(RAWTEXTPROPERTY); 
      this.imageUrl = (String)entity.getProperty("imageUrl");
      this.timeStamp = (long)entity.getProperty(TIMESTAMPPROPERTY);

      return this;
    }

    public Builder withEmail(String email) {
      this.email = email;
      return this;
    }

    public Builder withComment(String comment) {
      this.rawText = comment;
      return this;
    }

    public Builder withImageUrl(String url) {
      this.imageUrl = url;
      return this;
    }

    public Builder atTimeStamp(long timeStamp) {
      this.timeStamp = timeStamp;
      return this;
    }

    public Comment build() {
      Comment comment = new Comment();
      comment.email = this.email;
      comment.rawText = this.rawText;
      comment.imageUrl = this.imageUrl;
      comment.timeStamp = this.timeStamp;

      return comment;
    }
  }

  public Comment() {}

  public String getEmail() {
    return this.email;
  }

  public String getComment() {
    return this.rawText;
  }

  public String getImageUrl() {
    return this.imageUrl;
  }

  public long getTimeStamp() {
    return this.timeStamp;
  }
}
