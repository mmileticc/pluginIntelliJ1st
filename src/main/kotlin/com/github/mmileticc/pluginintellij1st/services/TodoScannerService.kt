package com.github.mmileticc.pluginintellij1st.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.concurrency.AppExecutorUtil

@Service(Service.Level.PROJECT)
class TodoScannerService(private val project: Project) {

    enum class Status { TODO, DONE }

    data class TaskItem(
        val file: VirtualFile,
        val offset: Int,
        val line: Int,
        val status: Status,
        val functionName: String?,
        val description: String
    ) {
        fun presentableText(): String {
            val fn = functionName?.let { "$it: " } ?: ""
            return "$fn$description"
        }
    }

    fun scan(): List<TaskItem> {
        val scope = GlobalSearchScope.projectScope(project)
        val psiManager = PsiManager.getInstance(project)
        val results = mutableListOf<TaskItem>()
        val projectFileIndex = ProjectFileIndex.getInstance(project)

        // Scan all files in project content that have PSI
        val allFiles = mutableSetOf<VirtualFile>()
        allFiles.addAll(FileTypeIndex.getFiles(com.intellij.ide.highlighter.JavaFileType.INSTANCE, scope))
        // Kotlin is optional; use reflection-safe fetch to avoid hard dependency if plugin not present
        try {
            val ktFileTypeClass = Class.forName("org.jetbrains.kotlin.idea.KotlinFileType")
            val instanceField = ktFileTypeClass.getDeclaredField("INSTANCE")
            instanceField.isAccessible = true
            val ktType = instanceField.get(null) as com.intellij.openapi.fileTypes.FileType
            allFiles.addAll(FileTypeIndex.getFiles(ktType, scope))
        } catch (_: Throwable) {
            // Kotlin not available; ignore
        }

        for (vf in allFiles) {
            if (!projectFileIndex.isInContent(vf)) continue
            val psiFile = psiManager.findFile(vf) ?: continue
            collectFromFile(psiFile, results)
        }
        return results
    }

    private fun collectFromFile(file: PsiFile, out: MutableList<TaskItem>) {
        val doc = PsiDocumentManager.getInstance(project).getDocument(file) ?: return
        file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitComment(comment: PsiComment) {
                super.visitComment(comment)
                val text = comment.text ?: return
                val trimmed = text.trimStart()
                val prefix = when {
                    trimmed.startsWith("// TODO") -> Status.TODO
                    trimmed.startsWith("// DONE") -> Status.DONE
                    else -> return
                }
                val description = trimmed
                    .removePrefix("// TODO")
                    .removePrefix("// DONE")
                    .trimStart(':', '-', ' ')
                    .trim()

                val offset = comment.textOffset
                val line = doc.getLineNumber(offset) + 1
                val functionName = findEnclosingFunctionName(comment)
                out += TaskItem(file.virtualFile, offset, line, prefix, functionName, description)
            }
        })
    }

    private fun findEnclosingFunctionName(element: PsiElement): String? {
        var current: PsiElement? = element
        while (current != null) {
            when (current) {
                is PsiMethod -> return current.name
                is PsiNameIdentifierOwner -> {
                    // Heuristic: prefer elements that look like functions
                    val debugName = current.javaClass.simpleName
                    if (debugName.contains("Function", ignoreCase = true) || debugName.contains("Method", ignoreCase = true)) {
                        return current.name
                    }
                }
            }
            current = current.parent
        }
        return null
    }

    companion object {
        fun getInstance(project: Project) = project.service<TodoScannerService>()
        val executor = AppExecutorUtil.getAppExecutorService()
    }
}
