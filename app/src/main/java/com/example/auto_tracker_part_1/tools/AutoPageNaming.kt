package com.example.auto_tracker_part_1.tools

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AutoPageNaming(val name: String)