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
    List<TimeRange> result = new ArrayList<>();
    List<TimeRange> ranges = new ArrayList<>();

    long meetingDuration = request.getDuration();
    
    if ((int)meetingDuration > TimeRange.WHOLE_DAY.duration())
      return result;

    if (events.isEmpty()) {
      result.add(TimeRange.WHOLE_DAY);
      return result;
    }

    for (Event event : events) {
      TimeRange when = event.getWhen();
      if (!event.getAttendees().isEmpty() && containsAtLeastOne(request.getAttendees(), event.getAttendees())) {
        ranges.add(when);
      }
    }

    if (ranges.isEmpty()) {
      result.add(TimeRange.WHOLE_DAY);
      return result;
    }

    sort(ranges);
    
    int start = getEarliest(ranges);
    if ((long)start >= meetingDuration) 
      result.add(TimeRange.fromStartDuration(0, start));

    for (int i = 0; i < ranges.size() - 1; i++) {
      TimeRange first = ranges.get(i);
      TimeRange second = ranges.get(i+1);
      if (!first.overlaps(second)) {
        if (second.start() - first.end() >= meetingDuration)
          result.add(TimeRange.fromStartDuration(first.end(), second.start() - first.end()));
      }
    }
    
    int end = getLatest(ranges);
    if (24*60 - (long)end >= meetingDuration)
      result.add(TimeRange.fromStartDuration(end, 24*60 - end));

    return result;
  }

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

  private boolean containsAtLeastOne(Collection<String> a, Collection<String> b) {
    if (a.isEmpty() || b.isEmpty())
      return false;
    for (String elementA : a) {
      if (b.contains(elementA))
        return true;
    }
    return false;
  }

  private int getEarliest(List<TimeRange> range) {
    int min = range.get(0).start();
    for (int i = 1; i < range.size(); i++) {
      if (range.get(i).start() < min)
        min = range.get(i).start();
    }
    return min;
  }

  private int getLatest(List<TimeRange> range) {
    int max = range.get(0).end();
    for (int i = 1; i < range.size(); i++) {
      if (range.get(i).end() > max)
        max = range.get(i).end();
    }
    return max;
  }
}
