package com.example.auto_tracker_part_1.tools

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import java.lang.ref.SoftReference
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible

object TrackerTool {
    private val pageAliveTimeMap = mutableMapOf<String, Long>()

    private var lastActivityName = ""
    private var lastFragmentName = ""

    private var context: SoftReference<Context>? = null

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

            val viewChildren = getAllChildren(activity.window.decorView)
            context = SoftReference(activity.baseContext)
            dispatchViewTypeToHook(viewChildren)

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
            context = null
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

    private fun getAllChildren(view: View): MutableList<View> {
        if (view !is ViewGroup) return mutableListOf(view)

        val mutableList = mutableListOf<View>()

        for (i in 0 until view.childCount) {
            val child = view.getChildAt(i)
            if (child is ViewGroup) {
                val children = getAllChildren(child)
                mutableList.addAll(children)
            } else {
                mutableList.add(child)
            }
        }
        return mutableList
    }

    private fun dispatchViewTypeToHook(viewChildren: MutableList<View>) {
        val buttonChildren = mutableListOf<View>()
        viewChildren.forEach { child ->
            if (child is Button) buttonChildren.add(child)
        }

        hookButtonListener(buttonChildren)
    }

    private fun hookButtonListener(viewChildren: MutableList<View>) {
        viewChildren.forEach { child ->
            val buttonClass = View::class

            val function = buttonClass.functions.find { it.name == "getListenerInfo" }
                ?.apply {
                    isAccessible = true
                }

            val listenerInfo = function?.call(child) ?: return

            val listenerInfoClass = listenerInfo::class
            val property = listenerInfoClass.memberProperties.find { it.name == "mOnClickListener" }
                ?.apply {
                    isAccessible = true
                }

            val originalOnClickListener = property?.call(listenerInfo) as View.OnClickListener

            val proxyOnCLickListener = ProxyOnCLickListener().apply {
                this.originalListener = originalOnClickListener
            }
            if (property is KMutableProperty<*>) {
                property.setter.call(listenerInfo, proxyOnCLickListener)
            }
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

    fun trackClickEvent(eventName: String, properties: Map<String, Any>) {
        Log.d("LogTracker", "eventName:${eventName},properties:${properties}")
    }

    private const val EVENT_PROPERTY_ALIVE_TIME = "alive_time"
    private const val EVENT_PROPERTY_LAST_PAGE_NAME = "last_page_name"
}