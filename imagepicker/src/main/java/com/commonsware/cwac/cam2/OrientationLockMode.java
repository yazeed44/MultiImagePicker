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
 * Specifies how the device orientation should be locked while the
 * camera activity is in the foreground. Default, much to your shock
 * and amazement, is DEFAULT.
 */
public enum OrientationLockMode {
  /**
   * Allow the orientation to change in the steady state. Once
   * video recording has started, or during the act of taking a
   * picture, the orientation will be locked to whatever the orientation
   * was at the time (this is still a work in progress).
   */
  DEFAULT,
  /**
   * Lock the orientation to portrait for preview and actual camera
   * operation.
   */
  PORTRAIT,
  /**
   * Lock the orientation to landscape for preview and actual camera
   * operation.
   */
  LANDSCAPE
}
