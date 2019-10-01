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
 * Constants related to the data passed to the ResultReceiver
 * that you can optionally register via `onError()` with
 * an `IntentBuilder`, to find out about errors that occur
 * inside the Cam2 activities.
 */
public class ErrorConstants {
  /**
   * Key for the Bundle provided to the ResultReceiver,
   * with the text of the stack trace of the exception that
   * triggered this callback.
   */
  public static final String RESULT_STACK_TRACE="stackTrace";

  /**
   * resultCode for ResultReceiver, indicating that there was
   * a problem listing the available cameras
   */
  public static final int ERROR_LIST_CAMERAS=3490;

  /**
   * resultCode for ResultReceiver, indicating that there was
   * a problem opening a camera
   */
  public static final int ERROR_OPEN_CAMERA=
    ERROR_LIST_CAMERAS+1;

  /**
   * resultCode for ResultReceiver, indicating that there was
   * a problem closing a camera
   */
  public static final int ERROR_CLOSE_CAMERA=
    ERROR_LIST_CAMERAS+2;

  /**
   * resultCode for ResultReceiver, indicating that there was
   * a problem when trying to complete the act of taking a video
   */
  public static final int ERROR_VIDEO_TAKEN=
    ERROR_LIST_CAMERAS+3;

  /**
   * resultCode for ResultReceiver, indicating that there was
   * a problem shutting down the CameraController and engine
   */
  public static final int ERROR_STOPPING=
    ERROR_LIST_CAMERAS+4;

  /**
   * resultCode for ResultReceiver, indicating that there was
   * a problem while trying to switch cameras from the FAB
   */
  public static final int ERROR_SWITCHING_CAMERAS=
    ERROR_LIST_CAMERAS+5;

  /**
   * resultCode for ResultReceiver, indicating that there was
   * a problem trying to end recording of a video
   */
  public static final int ERROR_STOPPING_VIDEO=
    ERROR_LIST_CAMERAS+6;

  /**
   * resultCode for ResultReceiver, indicating that there was
   * some other problem deep in the innards of Cam2
   */
  public static final int ERROR_MISC=
    ERROR_LIST_CAMERAS+7;
}
