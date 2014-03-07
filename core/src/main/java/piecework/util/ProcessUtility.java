/*
 * Copyright 2013 University of Washington
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package piecework.util;

import piecework.model.Container;
import piecework.model.Process;
import piecework.model.ProcessDeploymentVersion;

import java.util.List;

/**
 * @author James Renfro
 */
public class ProcessUtility {

    public static ProcessDeploymentVersion deploymentVersion(Process process, String deploymentId) {
        List<ProcessDeploymentVersion> deploymentVersions = process.getVersions();

        for (ProcessDeploymentVersion deploymentVersion : deploymentVersions) {
            if (deploymentVersion.getDeploymentId().equals(deploymentId)) {
                return deploymentVersion;
            }
        }
        return null;
    }

    public static Container container(Container container, String containerId) {
        if (container != null) {
            if (container.getContainerId() != null) {
                if (container.getContainerId().equals(containerId))
                    return container;
                if (container.getBreadcrumb() != null && container.getBreadcrumb().equals(containerId))
                    return container;
            }

            if (container.getChildren() != null) {
                for (Container child : container.getChildren()) {
                    Container found = container(child, containerId);
                    if (found != null)
                        return found;
                }
            }
        }

        return null;
    }

}
