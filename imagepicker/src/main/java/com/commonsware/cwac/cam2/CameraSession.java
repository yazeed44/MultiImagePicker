/***
 Copyright (c) 2015 CommonsWare, LLC

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

import android.content.Context;
import com.commonsware.cwac.cam2.util.Size;
import java.util.ArrayList;

/**
 * Class representing a session with a camera. While
 * a CameraDescriptor is simply a declaration of "there exists
 * a camera with these capabilities", CameraSession indicates
 * what you want to have happen with a particular camera, for
 * general operation. This is roughly analogous to Camera.Parameters
 * in the classic Camera API.
 *
 * To create instances, call buildSession() on an
 * instance of a CameraEngine, then use the resulting
 * CameraSession.Builder to describe what you want, followed
 * by a call to build() to give you the actual CameraSession.
 */
public class CameraSession {
  private final CameraDescriptor descriptor;
  private Context ctxt;
  private final ArrayList<CameraPlugin> plugins=new ArrayList<CameraPlugin>();
  private Size previewSize;
  private FlashMode currentFlashMode;

  /**
   * Constructor.
   *
   * @param ctxt an Android Context to use for accessing system stuff
   * @param descriptor the camera to use for this session
   */
  CameraSession(Context ctxt, CameraDescriptor descriptor) {
    this.ctxt=ctxt.getApplicationContext();
    this.descriptor=descriptor;
  }

  /**
   * @return an Android Context suitable for looking up filesystem
   * paths and the like
   */
  public Context getContext() {
    return(ctxt);
  }

  /**
   * @return the camera to use for this session
   */
  public CameraDescriptor getDescriptor() {
    return(descriptor);
  }

  /**
   * @return the roster of plugins configured for this session
   */
  protected ArrayList<CameraPlugin> getPlugins() {
    return(plugins);
  }

  public void destroy() {
    for (CameraPlugin plugin : getPlugins()) {
      plugin.destroy();
    }
  }

  public Size getPreviewSize() {
    return(previewSize);
  }

  public void setPreviewSize(Size previewSize) {
    this.previewSize=previewSize;
  }

  public FlashMode getCurrentFlashMode() {
    return(currentFlashMode);
  }

  void setCurrentFlashMode(FlashMode currentFlashMode) {
    this.currentFlashMode=currentFlashMode;
  }

  /**
   * Class to build an instance of a CameraSession. Get an instance
   * from buildSession() on your chosen CameraEngine.
   */
  abstract public static class Builder {
    protected final CameraSession session;

    protected Builder(CameraSession session) {
      this.session=session;
    }

    /**
     * Adds a plugin to the chain of plugins for this session.
     * Pre-configure the plugin before adding.
     *
     * @param plugin a CameraPlugin instance
     * @return the Builder, for chained calls
     */
    public Builder addPlugin(CameraPlugin plugin) {
      plugin.validate(session);
      session.plugins.add(plugin);

      return(this);
    }

    /**
     * @return the CameraSession, configured as you requested
     */
    public CameraSession build() {
      return(session);
    }
  }
}
