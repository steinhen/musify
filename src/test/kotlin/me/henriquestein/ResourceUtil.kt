package me.henriquestein

object ResourceUtil {
    fun readResource(resourceName: String) = this::class.java.classLoader
        .getResource(resourceName)
        ?.readText()
        ?: throw RuntimeException("Could not load resource $resourceName")
}