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

import piecework.common.ViewContext;
import piecework.enumeration.ActionType;
import piecework.model.*;
import piecework.model.Process;
import piecework.security.concrete.PassthroughSanitizer;

import java.util.List;
import java.util.Map;

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

    public static Interaction interaction(Interaction interaction, Map<String, Section> sectionMap, ViewContext context) {
        if (interaction != null && interaction.getId() != null) {
            Map<ActionType, Screen> screenMap = interaction.getScreens();

            if (screenMap == null)
                return interaction;

            Interaction.Builder builder = new Interaction.Builder(interaction, new PassthroughSanitizer());
            for (Map.Entry<ActionType, Screen> entry : screenMap.entrySet()) {
                Screen screen = entry.getValue();
                Screen.Builder screenBuilder = new Screen.Builder(screen, new PassthroughSanitizer());
                List<Grouping> groupings = screen.getGroupings();

                if (groupings != null) {
                    for (Grouping grouping : groupings) {
                        List<String> sectionIds = grouping.getSectionIds();
                        if (sectionIds != null) {
                            for (String sectionId : sectionIds) {
                                screenBuilder.section(sectionMap.get(sectionId));
                            }
                        }
                    }
                }

                builder.screen(entry.getKey(), screenBuilder.build(context));
            }
            return builder.build(context);
        }
        return null;
    }


}
