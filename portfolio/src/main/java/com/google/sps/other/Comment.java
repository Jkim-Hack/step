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
  private long timeStamp;

  public Comment(String email, String comment, String timeStamp) {
    this.email = email;
    this.rawText = comment;
    this.timeStamp = timeStamp;
  }

  public String getEmail() {
    return this.email;
  }

  public String getComment() {
    return this.rawText;
  }

  public long getTimeStamp() {
    return this.timeStamp;
  }

}
