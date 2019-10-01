/***
 Copyright (c) 2016 CommonsWare, LLC

 Licensed under the Apache License, Version 2.0 (the "License"); you may
 not use this file except in compliance with the License. You may obtain
 a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package com.commonsware.cwac.cam2;

/**
 * Whether there should be a timer shown on the video recording
 * preview screen, and, if so, whether it counts down the remaining
 * time on a time-limited recording or whether it counts up for how
 * long the current recording is.
 */
public enum ChronoType {
  NONE,
  COUNT_DOWN,
  COUNT_UP
}
