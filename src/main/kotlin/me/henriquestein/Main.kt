package me.henriquestein

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class MusifyApplication

fun main(args: Array<String>) {
    runApplication<MusifyApplication>(*args)
}