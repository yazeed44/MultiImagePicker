/**
 * Copyright (c) 2015 CommonsWare, LLC
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.commonsware.cwac.cam2;

/**
 * A plugin for configuration camera behavior. A CameraSession
 * will hold onto a chain of these.
 * <p/>
 * Changing plugins, either by reconfiguring plugin instances or
 * by changing the chain, is not supported once the CameraSession
 * is created. Create a new session with the new plugins.
 * This is to minimize compatibility issues where changing the
 * behavior of the camera on the fly breaks the camera.
 */
public interface CameraPlugin {
  /**
   * Returns a CameraConfigurator of a given class for this
   * plugin. Plugins ideally should support both ClassicCameraConfigurator
   * and CameraTwoConfigurator, though that is up to the plugin.
   * <p/>
   * Configurators should be fairly cheap objects to instantiate.
   * While at the moment they are only used once per session,
   * that may not be true in the future, and they are not being cached.
   *
   * @param type ClassicCameraConfigurator.class or
   *             CameraTwoConfigurator.class, indicating which type
   *             of configurator to create
   * @param <T> ClassicCameraConfigurator or CameraTwoConfigurator
   * @return instance of the designated configurator class, or
   *           null if that type is not supported
   */
  <T extends CameraConfigurator> T buildConfigurator(Class<T> type);

  /**
   * Validates whether or not the plugin is valid for this
   * CameraSession. Throw a runtime exception if it is not.
   *
   * @param session the session to validate against
   */
  void validate(CameraSession session);

  void destroy();
}
