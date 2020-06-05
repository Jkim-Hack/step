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

/**
 * Gets comment responses from the server.
 */
async function getAllComments() {
  let commentsList = new Array();

  // Wait for server response in data servlet
  const response = await fetch('/data');
  await response.json().then(comments => {
    for(var comment of comments) {
      // Add each comment into list
      commentsList.push(comment);
    }
  })

  // Return generated list
  return commentsList;
}

/**
 * Lists every comment the user has inputed.
 */
async function listAllComments() {
    // Get all the comments and put into a list
    let commentsList = await getAllComments();
    let length = commentsList.length;

    // Cant display less than 1 comment
    if(length < 1) return;
    
    // Put each comment into a list element
    var currentHTML = "";
    for (var comment of commentsList) {
      
      // Check for html injection
      while (comment.includes("<") || comment.includes(">")) {
        comment = comment.replace(/</, "&lt;").replace(/>/, "&gt;");
      } 
      
      let listElement = "<div id=\"comment\">" + "<p>" + comment + "</p>" + "</div>";
      currentHTML += listElement;
    }

    // Put each list element into the container
    document.getElementById('comments-list-container').innerHTML = currentHTML;
}

/**
 * Deletes every comment.
 */
async function deleteAllComments() { 
  // Wait for response
  const response = await fetch('/delete-data', {method: 'POST'});

  // Get request the empty json
  listAllComments();

  // Redirect to greeting.html
  window.location.href = "/greeting.html";
}
