package com.huanli233.hibari2.runtime.utils

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

object ViewHierarchyPrinter {

    private const val INDENT_STEP = "  "

    fun printViewHierarchy(view: View?, tag: String = "ViewHierarchy") {
        if (view == null) {
            android.util.Log.e(tag, "Root view is null. Cannot print hierarchy.")
            return
        }
        android.util.Log.d(tag, "--- View Hierarchy Start ---")
        printView(view, 0, tag)
        android.util.Log.d(tag, "--- View Hierarchy End ---")
    }

    private fun printView(view: View, indentLevel: Int, tag: String) {
        val indent = INDENT_STEP.repeat(indentLevel)
        val stringBuilder = StringBuilder()

        stringBuilder.append(indent)
            .append("-> ${view.javaClass.simpleName}")

        try {
            val id = view.resources.getResourceEntryName(view.id)
            stringBuilder.append(" id=\"@id/$id\"")
        } catch (_: Exception) {
            if (view.id != View.NO_ID) {
                stringBuilder.append(" id=\"${view.id}\"")
            }
        }

        when (view) {
            is Button -> {
                if (view.text.isNotBlank()) {
                    stringBuilder.append(" text=\"${view.text}\"")
                }
            }
            is EditText -> {
                if (view.text.isNotBlank()) {
                    stringBuilder.append(" text=\"${view.text}\"")
                }
            }
            is TextView -> {
                if (view.text.isNotBlank()) {
                    stringBuilder.append(" text=\"${view.text}\"")
                }
            }
        }

        val visibility = when (view.visibility) {
            View.VISIBLE -> "VISIBLE"
            View.INVISIBLE -> "INVISIBLE"
            View.GONE -> "GONE"
            else -> "UNKNOWN"
        }
        stringBuilder.append(" visibility=\"$visibility\"")

        val rect = Rect()
        view.getGlobalVisibleRect(rect)
        stringBuilder.append(" [${rect.left},${rect.top}-${rect.right},${rect.bottom}]")
        stringBuilder.append(" (${view.width}x${view.height})")

        // Add LayoutParams information
        view.layoutParams?.let { params ->
            stringBuilder.append(" layout_width=\"${
                when (params.width) {
                    ViewGroup.LayoutParams.MATCH_PARENT -> "match_parent"
                    ViewGroup.LayoutParams.WRAP_CONTENT -> "wrap_content"
                    else -> params.width.toString() + "px"
                }
            }\"")
            stringBuilder.append(" layout_height=\"${
                when (params.height) {
                    ViewGroup.LayoutParams.MATCH_PARENT -> "match_parent"
                    ViewGroup.LayoutParams.WRAP_CONTENT -> "wrap_content"
                    else -> params.height.toString() + "px"
                }
            }\"")

            if (params is ViewGroup.MarginLayoutParams) {
                stringBuilder.append(" margins=\"L:${params.leftMargin} T:${params.topMargin} R:${params.rightMargin} B:${params.bottomMargin}\"")
            }
        }

        if (view.isClickable) {
            stringBuilder.append(" clickable=\"true\"")
        }

        if (view.isLongClickable) {
            stringBuilder.append(" longClickable=\"true\"")
        }

        if (view.isFocusable) {
            stringBuilder.append(" focusable=\"true\"")
        }
        if (view.hasFocus()) {
            stringBuilder.append(" focused=\"true\"")
        }

        if (!view.isEnabled) {
            stringBuilder.append(" enabled=\"false\"")
        }

        if (view is ImageView && !view.contentDescription.isNullOrBlank()) {
            stringBuilder.append(" contentDescription=\"${view.contentDescription}\"")
        }

        if (view.tag != null) {
            stringBuilder.append(" tag=\"${view.tag}\"")
        }

        android.util.Log.d(tag, stringBuilder.toString())

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                printView(child, indentLevel + 1, tag)
            }
        }
    }

    /**
     * Helper function to get resource name from ID.
     */
    private fun getResourceName(view: View, resId: Int): String {
        return try {
            view.resources.getResourceEntryName(resId)
        } catch (_: Exception) {
            resId.toString()
        }
    }
}