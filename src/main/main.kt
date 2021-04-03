package main

import java.io.File

fun printLiterals(file: String) {
    fun Any.toStringNew() =
        this.toString().let { it.substring(1, it.length - 1) }

    fun notShielded(line: String, i: Int): Boolean =
        (i-1 downTo 0).takeWhile { line[it] == '\\' }.count() % 2 == 0

    fun findLiterals(line: String): List<String> {
        val listAns = mutableListOf<String>()
        val listQuot = line
            .mapIndexed { i, ch ->
                Pair(when (ch) {
                    '\'', '\"'  -> i
                    '#'         -> -2 // comment
                    else        -> -1 // not need
                },
                ch)}
            .takeWhile { it.first != -2 } // before comment
            .filterNot { it.first == -1 } // drop excess
        var start: Int? = null
        listQuot.forEachIndexed { i, p ->
            when (start) {
                null -> {
                    if (notShielded(line, p.first)) start = i
                    return@forEachIndexed
                }
                i -> return@forEachIndexed
                else -> {
                    if (listQuot[start!!].second == p.second && notShielded(line, p.first)) { // definitely not null
                        listAns.add(line.substring(listQuot[start!!].first + 1, p.first))
                        start = null
                    }
                }
            }
        }
        return listAns
    }

    val literalMap = mutableMapOf<String, MutableList<Int>>()
    val linesList = mutableListOf<String>()
    File(file).useLines { lines -> lines.forEach { linesList.add(it) } }

    linesList.forEachIndexed { i, line ->
        findLiterals(line).forEach {
            literalMap.getOrPut(it) { mutableListOf() }.add(i)
        }
    }
    literalMap.filter { it.value.size > 1 }.forEach {
        println("Lines with \'${it.key}\': ${it.value.toSet().toStringNew()}")
    }
}

// args: "temp/example.py" "temp/hardExample.py"
fun main(args: Array<String>) {
    args.toList().forEach{ println("In $it:"); printLiterals(it) }
}