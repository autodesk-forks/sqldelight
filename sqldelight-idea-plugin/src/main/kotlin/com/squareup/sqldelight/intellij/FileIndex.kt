/*
 * Copyright (C) 2018 Square, Inc.
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
package com.squareup.sqldelight.intellij

import com.intellij.openapi.module.Module
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.project.rootManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiManager
import com.intellij.psi.util.PsiTreeUtil
import com.squareup.sqldelight.core.SqlDelightFileIndex
import com.squareup.sqldelight.core.SqlDelightPropertiesFile
import com.squareup.sqldelight.core.lang.SqlDelightFile
import com.squareup.sqldelight.core.lang.SqlDelightFileType
import java.io.File

class FileIndex(private val module: Module) : SqlDelightFileIndex {
  private val psiManager = PsiManager.getInstance(module.project)
  private val localFileSystem = LocalFileSystem.getInstance()
  private val properties by lazy {
    var path: String? = null
    module.rootManager.fileIndex.iterateContent {
      if (it.name == SqlDelightFileType.FOLDER_NAME && it.parent.parent.name == "src") {
        path = it.parent.parent.parent.findChild(SqlDelightPropertiesFile.NAME)?.path
        return@iterateContent false
      }
      return@iterateContent true
    }
    return@lazy path?.let { SqlDelightPropertiesFile.fromFile(File(it)) }
  }

  override val isConfigured: Boolean
    get() = properties != null

  override val packageName by lazy { properties!!.packageName }

  override val outputDirectory by lazy { properties!!.outputDirectory }

  override fun packageName(file: SqlDelightFile): String {
    val folder = sourceFolders(file)
        .first { PsiTreeUtil.findCommonParent(file, it) != null }
    val folderPath = folder.virtualFile.path
    val filePath = file.virtualFile.path
    return filePath.substring(folderPath.length + 1, filePath.indexOf(file.name) - 1).replace('/', '.')
  }

  override fun sourceFolders(file: SqlDelightFile?): List<PsiDirectory> {
    if (properties == null) return emptyList()
    // TODO Its more complicated than this, since files will be in multiple sources sets.
    return properties!!.sourceSets.map { sourceSet ->
      sourceSet.mapNotNull sourceFolder@{ sourceFolder ->
        val vFile = localFileSystem.findFileByIoFile(File(sourceFolder)) ?: return@sourceFolder null
        return@sourceFolder psiManager.findDirectory(vFile)
      }
    }.first {
      it.any { folder -> PsiTreeUtil.findCommonParent(file, folder) != null }
    }
  }

}
