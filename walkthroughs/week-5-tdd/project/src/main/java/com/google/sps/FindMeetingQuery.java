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

package com.google.sps;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // Initalize the processed result and the not acceptable ranges
    List<TimeRange> result = new ArrayList<>();
    List<TimeRange> ranges = new ArrayList<>();

    // Get meeting duration
    long meetingDuration = request.getDuration();
    
    // Check edge case where the meeting is the whole day
    if ((int)meetingDuration > TimeRange.WHOLE_DAY.duration())
      return result;

    // Check if theres no events the entire day
    if (events.isEmpty()) {
      result.add(TimeRange.WHOLE_DAY);
      return result;
    }

    // Loop through events and check through combinations of optional and regular attendees
    for (Event event : events) {
      TimeRange when = event.getWhen();
      if (!event.getAttendees().isEmpty() && containsAtLeastOne(request.getAttendees(), event.getAttendees())) {
        ranges.add(when);
      } else if (request.getAttendees().isEmpty() && containsAtLeastOne(request.getOptionalAttendees(), event.getAttendees())) {
        ranges.add(when);
      } else if (containsAtLeastOne(request.getOptionalAttendees(), event.getAttendees())) {
        ranges.add(when);
      }
    }

    // Remove all optional attendees with unreasonable event times such as the entire day of the duration is less than the meeting duration
    for (int i = 0; i < ranges.size(); i++){
      TimeRange elem = ranges.get(i);
      if (elem.duration() < request.getDuration() || elem.equals(TimeRange.WHOLE_DAY)) {
        ranges.remove(i);
        i--;
      }
    }

    // Check if the unacceptable range is empty
    if (ranges.isEmpty()) {
      result.add(TimeRange.WHOLE_DAY);
      return result;
    }

    // Sort through the ranges by their start times
    sort(ranges);

    // Get earliest availability and add to result
    int start = getEarliest(ranges);
    if ((long)start >= meetingDuration) 
      result.add(TimeRange.fromStartDuration(0, start));

    // Loop through unacceptable events and add pockets to the result
    for (int i = 0; i < ranges.size() - 1; i++) {
      TimeRange first = ranges.get(i);
      TimeRange second = ranges.get(i+1);
      if (!first.overlaps(second)) {
        if (second.start() - first.end() >= meetingDuration)
          result.add(TimeRange.fromStartDuration(first.end(), second.start() - first.end()));
      }
    }
    
    // Get latest availability and add to result
    int end = getLatest(ranges);
    if (24*60 - (long)end >= meetingDuration)
      result.add(TimeRange.fromStartDuration(end, 24*60 - end));

    return result;
  }

  // Insertion sort through time range list
  private void sort(List<TimeRange> list) {
    int n = list.size(); 
    for (int i = 1; i < n; i++) { 
      TimeRange elem = list.get(i); 
      int j = i - 1;   
      while (j >= 0 && list.get(j).start() > elem.start()) { 
        TimeRange range = list.get(j);
        list.set(j + 1, range);  
        j = j - 1; 
      } 
      list.set(j + 1, elem); 
    } 
  }

  // If theres at least one element in collection a that is in collection b
  private boolean containsAtLeastOne(Collection<String> a, Collection<String> b) {
    if (a.isEmpty() || b.isEmpty())
      return false;
    for (String elementA : a) {
      if (b.contains(elementA))
        return true;
    }
    return false;
  }

  // Get the earliest start in the time range
  private int getEarliest(List<TimeRange> range) {
    int min = range.get(0).start();
    for (int i = 1; i < range.size(); i++) {
      if (range.get(i).start() < min)
        min = range.get(i).start();
    }
    return min;
  }

  // Get the latest end in the time range
  private int getLatest(List<TimeRange> range) {
    int max = range.get(0).end();
    for (int i = 1; i < range.size(); i++) {
      if (range.get(i).end() > max)
        max = range.get(i).end();
    }
    return max;
  }
}
