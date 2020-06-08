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

public class CommentBuilder {

  private String email;
  private String rawText;
  private long timeStamp;

  public CommentBuilder() {
    this.email = "";
    this.rawText = "";
    this.timeStamp = System.currentTimeMillis();
  }

  public CommentBuilder.withEntity(Entity entity) {
    this.email = (String)entity.getProperty(EMAILPROPERTY);
    this.rawText = (String)entity.getProperty(RAWTEXTPROPERTY);
    this.timeStamp = (long)entity.getProperty(TIMESTAMPPROPERTY);
  }

  public CommentBuilder withEmail(String email) {
    this.email = email;
    return this;
  }

  public CommentBuilder withComment(String comment) {
    this.rawText = comment;
    return this;
  }

  public CommentBuilder atTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
    return this;
  }

  public Comment build() {
    return new Comment(this.email, this.rawText, this.timeStamp);
  }
}
