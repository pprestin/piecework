/*
 * Copyright 2014 University of Washington
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
package piecework.model;

import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import static junit.framework.Assert.*;
//import org.mockito.Mock;
//import org.mockito.runners.MockitoJUnitRunner;
import piecework.model.Button;
import piecework.Constants;
import piecework.enumeration.ActionType;

/**
 * @author Jiefeng Shen
 */
public class ButtonTest {

    @Test
    public void testChildButtons() {
        // toplevel button
        Button.Builder builder = new Button.Builder()
            .type(Constants.ButtonTypes.SUBMIT)
            .name("actionButton")
            .label("Return to Previous Step")
            .value("reject")
            .action(ActionType.REJECT.name())
            .ordinal(1);

        // add nested buttons
        String[] btnValues = new String[] {
            "Preliminary",
            "Prep",
            "Analysis",
            "Review",
            "Submit"
        };
       
        int idx = 0;
        for ( String name : btnValues ) {
            idx++;
            Button btn = new Button.Builder()
                .type(Constants.ButtonTypes.BUTTON_LINK)
                .name("actionButton")
                .label(name)
                .value(name)
                .action(ActionType.REJECT.name())
                .ordinal(idx)
                .build();

            builder.child(btn);
        }

        Button pb = builder.build(); 

        assertEquals("actionButton", pb.getName());
        assertEquals("Return to Previous Step", pb.getLabel());
        assertEquals("reject", pb.getValue());
        assertEquals(ActionType.REJECT.name(), pb.getAction());

        List<Button> btns = pb.getChildren();
        assertEquals(btnValues.length, btns.size());
        idx = 0;
        for ( Button btn : btns ) {
            assertEquals("actionButton", btn.getName());
            assertEquals(btnValues[idx], btn.getLabel());
            assertEquals(btnValues[idx], btn.getValue());
            assertEquals(ActionType.REJECT.name(), btn.getAction());
            assertEquals(idx+1, btn.getOrdinal());
            idx++;
        }
    }
}
