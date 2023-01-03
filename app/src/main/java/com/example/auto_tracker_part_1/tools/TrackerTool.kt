package com.example.auto_tracker_part_1.tools

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager

object TrackerTool {
    private val pageAliveTimeMap = mutableMapOf<String, Long>()

    private var lastActivityName = ""
    private var lastFragmentName = ""

    private val simpleActivityLifecycleCallbacks = object: SimpleActivityLifecycleCallbacks() {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            super.onActivityCreated(activity, savedInstanceState)
            if (activity is FragmentActivity) {
                activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                    fragmentLifecycleCallbacks,
                    true
                )
            }
        }

        override fun onActivityResumed(activity: Activity) {
            super.onActivityResumed(activity)
            val pageName = getActivityName(activity)
            pageAliveTimeMap[pageName] = System.currentTimeMillis()
            val eventName = "Auto_${pageName}_Resumed"
            trackViewEvent(eventName, mapOf(EVENT_PROPERTY_LAST_PAGE_NAME to lastActivityName))
        }

        override fun onActivityPaused(activity: Activity) {
            super.onActivityPaused(activity)
            val pageName = getActivityName(activity)
            val eventName = "Auto_${pageName}_Paused"
            val aliveTime = if (pageAliveTimeMap[pageName] != null)
                System.currentTimeMillis() - pageAliveTimeMap[pageName]!!
            else
                0
            pageAliveTimeMap.remove(pageName)
            trackViewEvent(
                eventName,
                mapOf(
                    EVENT_PROPERTY_ALIVE_TIME to aliveTime
                )
            )
            lastActivityName = getActivityName(activity)
        }

        override fun onActivityDestroyed(activity: Activity) {
            super.onActivityDestroyed(activity)
            if (activity is FragmentActivity) {
                activity.supportFragmentManager.unregisterFragmentLifecycleCallbacks(
                    fragmentLifecycleCallbacks
                )
            }
        }
    }

    private val fragmentLifecycleCallbacks = object: FragmentManager.FragmentLifecycleCallbacks() {
        private fun getFragmentName(fragment: Fragment): String {
            val kClass = fragment::class.java
            return if (kClass.isAnnotationPresent(AutoPageNaming::class.java)) {
                kClass.getAnnotation(AutoPageNaming::class.java)?.name
                    ?: fragment::class.java.simpleName
            } else fragment::class.java.simpleName
        }

        override fun onFragmentCreated(
            fm: FragmentManager,
            f: Fragment,
            savedInstanceState: Bundle?
        ) {
        }

        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
            super.onFragmentResumed(fm, f)
            val pageName = getFragmentName(f)
            val eventName = "Auto_${pageName}_Resumed"
            pageAliveTimeMap[pageName] = System.currentTimeMillis()
            trackViewEvent(eventName, mapOf(EVENT_PROPERTY_LAST_PAGE_NAME to lastFragmentName))
        }

        override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
            super.onFragmentPaused(fm, f)
            val pageName = getFragmentName(f)
            val eventName = "Auto_${pageName}_Paused"
            val aliveTime = if (pageAliveTimeMap[pageName] != null)
                System.currentTimeMillis() - pageAliveTimeMap[pageName]!!
            else
                0
            pageAliveTimeMap.remove(pageName)
            trackViewEvent(
                eventName,
                mapOf(
                    EVENT_PROPERTY_ALIVE_TIME to aliveTime
                )
            )
            lastFragmentName = getFragmentName(f)
        }
    }

    fun getLifecycleCallback(): SimpleActivityLifecycleCallbacks {
        return simpleActivityLifecycleCallbacks
    }

    private fun getActivityName(activity: Activity): String {
        val kClass = activity::class.java
        return if (kClass.isAnnotationPresent(AutoPageNaming::class.java)) {
            kClass.getAnnotation(AutoPageNaming::class.java)?.name
                ?: activity::class.java.simpleName
        } else activity::class.java.simpleName
    }

    private fun trackViewEvent(eventName: String, properties: Map<String, Any>) {
        Log.d("LogTracker", "eventName:${eventName},properties:${properties}")
    }

    private const val EVENT_PROPERTY_ALIVE_TIME = "alive_time"
    private const val EVENT_PROPERTY_LAST_PAGE_NAME = "last_page_name"
}