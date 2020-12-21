/*  
 * Copyright IBM Corp. 2016
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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.gson.Gson;
import com.ibm.g11n.pipeline.client.ServiceException;

/**
 * Prints out a list of active machine translation languages for the given
 * source language.
 * 
 * @author Yoshito Umaoka
 */
@Parameters(commandDescription = "Prints out active machine translation languages.")
class ListMTLanguagesCmd extends BaseCmd {

    @Parameter(
            names = {"-f", "--fromLanguage"},
            description = "Language ID of the translation source language",
            required = true)
    private String fromLanguage;

    @Override
    protected void _execute() {
        Set<String> resultLanguages = null;
        try {
            Map<String, Set<String>> mtLanguages = getClient().getConfiguredMTLanguages();
            resultLanguages = mtLanguages.get(fromLanguage);
        } catch (ServiceException e) {
            throw new RuntimeException(e);
        }

        if (resultLanguages == null) {
            resultLanguages = Collections.emptySet();
        }

        Gson gson = new Gson();
        String outStr = gson.toJson(resultLanguages);
        System.out.println(outStr);
    }
}
