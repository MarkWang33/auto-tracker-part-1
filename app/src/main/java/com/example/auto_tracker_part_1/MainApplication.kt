package com.example.auto_tracker_part_1

import android.app.Application
import com.example.auto_tracker_part_1.tools.TrackerTool

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(TrackerTool.getLifecycleCallback())
    }
}