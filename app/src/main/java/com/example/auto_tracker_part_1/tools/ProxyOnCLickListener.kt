package com.example.auto_tracker_part_1.tools

import android.view.View

class ProxyOnCLickListener: View.OnClickListener {
 var originalListener: View.OnClickListener? = null

    override fun onClick(v: View) {
        originalListener?.onClick(v)
        val name="${v.context.resources.getResourceEntryName(v.id)}_Clicked"
        TrackerTool.trackClickEvent(name, mapOf())
    }
}