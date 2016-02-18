/*
 * Copyright (c) 2015 naymspace software (Dennis Nissen)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.catalina.loader.WebappLoader

eventConfigureTomcat = { tomcat ->

    /**
     * make webclient available under /webclient context if webclient.war exists in
     * /lib directory
     */
    def libraryDirectory = new File(grailsSettings.baseDir, 'lib/')
    def webclientFile = new File(libraryDirectory, 'webclient.war')

    if (webclientFile.exists()){
        def context = tomcat.addWebapp("/webclient", webclientFile.absolutePath)
        def loader = new WebappLoader(tomcat.class.classLoader)
        loader.context = context
        context.loader = loader
    }
}
