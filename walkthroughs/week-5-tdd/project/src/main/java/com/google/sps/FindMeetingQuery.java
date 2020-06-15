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
    List<TimeRange> availableRanges = new ArrayList<>();
    List<TimeRange> unacceptableRanges = new ArrayList<>();

    // Get meeting duration
    long meetingDuration = request.getDuration();
    
    // Check edge case where the meeting is the whole day
    if ((int)meetingDuration > TimeRange.WHOLE_DAY.duration()) {
      return availableRanges;
    }

    // Check if theres no events the entire day
    if (events.isEmpty()) {
      availableRanges.add(TimeRange.WHOLE_DAY);
      return availableRanges;
    }

    // Loop through events and check through combinations of optional and regular attendees
    for (Event event : events) {
      TimeRange currentWhen = event.getWhen();
      
      boolean isCurrentEventAttendeesEmpty = event.getAttendees().isEmpty();
      boolean currentEventContainsAtLeastOneAttendeeInRequest = containsAtLeastOne(request.getAttendees(), event.getAttendees());
      boolean currentEventContainsAtLeastOneOptionalAttendeeInRequest = containsAtLeastOne(request.getOptionalAttendees(), event.getAttendees());
      boolean isRequestAttendeesEmpty = request.getAttendees().isEmpty();
      
      if (!isCurrentEventAttendeesEmpty && currentEventContainsAtLeastOneAttendeeInRequest) {
        unacceptableRanges.add(currentWhen);
      } else if (isRequestAttendeesEmpty && currentEventContainsAtLeastOneOptionalAttendeeInRequest) {
        unacceptableRanges.add(currentWhen);
      } else if (currentEventContainsAtLeastOneOptionalAttendeeInRequest) {
        unacceptableRanges.add(currentWhen);
      }
    }

    // Add all optional attendee ranges with unreasonable event times to unacceptableRanges (such as the entire day of the duration is less than the meeting duration)
    for (int i = 0; i < unacceptableRanges.size(); i++){
      TimeRange elem = unacceptableRanges.get(i);
      if (elem.duration() < request.getDuration() || elem.equals(TimeRange.WHOLE_DAY)) {
        unacceptableRanges.remove(i);
        i--;
      }
    }

    // Check if the unacceptable range is empty
    if (unacceptableRanges.isEmpty()) {
      availableRanges.add(TimeRange.WHOLE_DAY);
      return availableRanges;
    }

    // Sort through the ranges by their start times
    sortTimeRangesByStart(unacceptableRanges);

    // Get earliest availability and add to result
    int start = getEarliestTimeRangeFromStart(unacceptableRanges);
    if ((long)start >= meetingDuration) {
      availableRanges.add(TimeRange.fromStartDuration(0, start));
    }

    // Loop through unacceptable events and add pockets to the result
    for (int i = 0; i < unacceptableRanges.size() - 1; i++) {
      TimeRange currentRange = unacceptableRanges.get(i);
      TimeRange nextRange = unacceptableRanges.get(i+1);
      if (!first.overlaps(second)) {
        int timeBetweenRanges = nextRange.start() - currentRange.end();
        if (timeBetweenRanges >= meetingDuration)
          availableRanges.add(TimeRange.fromStartDuration(currentRange.end(), timeBetweenRanges));
      }
    }
    
    // Get latest availability and add to result
    int end = getLatestTimeRangeFromStart(availableRanges);
    int dayDuration = 24 * 60;
    int timeBetweenLastandEndDay = dayDuration - end;
    if (timeBetweenLastandEndDay >= (int)meetingDuration) {
      availableRanges.add(TimeRange.fromStartDuration(end, timeBetweenLastandEndDay));
    }

    return availableRanges;
  }

  // Insertion sort through time range list by start times
  private void sortTimeRangesByStart(List<TimeRange> list) {
    int n = list.size(); 
    for (int i = 1; i < n; i++) { 
      TimeRange currentTimeRange = list.get(i); 
      int j = i - 1;   
      while (j >= 0 && list.get(j).start() > currentTimeRange.start()) { 
        TimeRange prevTimeRange = list.get(j);
        list.set(j + 1, prevTimeRange);  
        j = j - 1; 
      } 
      list.set(j + 1, currentTimeRange); 
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

  // Get the earliest start in the time range. Returns -1 if ranges list is empty
  private int getEarliestTimeRangeFromStart(List<TimeRange> timeRanges) {
    if (timeRanges.isEmpty()) {
      return -1;
    }
    int min = timeRanges.get(0).start();
    for (int i = 1; i < timeRanges.size(); i++) {
      TimeRange currentTimeRange = timeRanges.get(i);
      if (currentTimeRange.start() < min)
        min = currentTimeRange.start();
    }
    return min;
  }

  // Get the latest end in the time range. Returns -1 if ranges list is empty
  private int getLatestTimeRangeFromStart(List<TimeRange> timeRanges) {
    if (timeRanges.isEmpty()) {
      return -1;
    }
    int max = timeRanges.get(0).end();
    for (int i = 1; i < timeRanges.size(); i++) {
      TimeRange currentTimeRange = timeRanges.get(i);
      if (currentTimeRange.end() > max)
        max = currentTimeRange.end();
    }
    return max;
  }
}
