/*  
 * Copyright IBM Corp. 2015,2016
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.g11n.pipeline.tools.cli;

import java.util.Set;

import com.beust.jcommander.Parameters;
import com.google.gson.Gson;
import com.ibm.g11n.pipeline.client.ServiceException;

/**
 * Prints out translation bundle IDs.
 * 
 * @author Yoshito Umaoka
 */
@Parameters(commandDescription = "Prints out translation bundle IDs.")
final class ListBundlesCmd extends BaseCmd {
    @Override
    protected void _execute() {
        try {
            Set<String> bundleIds = getClient().getBundleIds();
            Gson gson = new Gson();
            String outIds = gson.toJson(bundleIds);
            System.out.println(outIds);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }
    }
}
