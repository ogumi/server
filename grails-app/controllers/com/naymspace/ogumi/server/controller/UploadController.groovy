package com.naymspace.ogumi.server.controller

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
import org.apache.commons.io.FilenameUtils
import com.naymspace.ogumi.server.domain.*

class UploadController {

    /**
     * save a uploaded file and create a corresponding uploadable
     * files uploaded by this action can be downloaded by "uploads" controller
     * @return
     */
	def index() {
		try {
			def fileInfo = moveAndCreateFile("uploads/", true)
			render(status: 201, contentType: 'application/json') {
                fileInfo
            }
		} catch(e) {
			render(status: 500, contentType: 'application/json') {[
				'error': e.getMessage()
			]}
		}
	}

	def jar() {
		try {
            def fileInfo = moveAndCreateFile("uploads/models/")
			render(status: 201, contentType: 'application/json') {
                fileInfo
            }
		} catch(e) {
			render(status: 500, contentType: 'application/json') {[
				'error': e.getMessage()
			]}
		}
	}

	def delete() {
		try {
			def up = Uploadable.get(params.key)
			up.delete(flush:true)
			render(status: 200, contentType: 'application/json') {[
				'deleted': webrootDir + up.url
			]}
		} catch(e) {
			render(status: 500, contentType: 'application/json') {[
				'error': e.getMessage()
			]}
		}
	}

    private String uploadPath(String subfolder) {
        def desiredPath = servletContext.getRealPath('/') + subfolder
        new File(desiredPath).mkdirs()
        desiredPath
    }

    private def moveAndCreateFile(String targetDir, boolean downloadable = false){
        def path = uploadPath(targetDir)
        def f = request.getFile('file_data')
        def name = f.getOriginalFilename()
        def id       = UUID.randomUUID().toString()
        def type = FilenameUtils.getExtension(name)
        def fileName = id + '.' + type
        def dest     = path + fileName
        def fileDest = new File(dest)
        f.transferTo(fileDest)
        def up       = new Uploadable(name: name, path: dest, downloadable: downloadable, type: type)
        up.save()
        [
            'origName': name,
            'name': fileName,
            'url': up.url,
            'id': up.getId()
        ]
    }

}
