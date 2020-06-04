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
  const response = await fetch('/data');
  await response.json().then(comments => {
    for(const comment of comments) {
      commentsList.push(comment);
    }
  })
  return commentsList;
}

/**
 * Gets a random comment response from the server.
 */
async function getRandomComment() {
  let commentsList = await getAllComments();
  let length = commentsList.length;

  if(length < 1) return;
  
  let comment = commentsList[getRandomInteger(0, length-1)];
  document.getElementById('greeting-container').innerText = comment;
}

function getRandomInteger(min, max) {
  min = Math.ceil(min);
  max = Math.floor(max);
  return Math.floor(Math.random() * (max - min + 1)) + min; 
}

/**
 * Lists every comment the user has inputed.
 */
async function listAllComments() {
    let commentsList = await getAllComments();
    let length = commentsList.length;

    if(length < 1) return;
    
    var currentHTML = "";
    for(const comment of commentsList) {
        let listElement = "<li>" + comment + "</li>";
        currentHTML += listElement;
    }
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
