package com.adrianrafo.hood

import arrow.typeclasses.*

fun main(args: Array<String>) {
//  if (args.size == 4) {
//    IO.monad().binding {
//      val previous = read(args[0], args[2]).bind()
//      val current = read(args[1], args[2]).bind()
//      val threshold = IO { args[3].toInt() }.bind()
//      println("Previous score: ${previous.score}")
//      println("Current score: ${current.score}")
//      when {
//        previous.score <= current.score -> println("*** Commit looks good ***")
//        previous.score - current.score <= threshold -> println("*** Commit is slightly worst, but it's ok ***")
//        else -> println("*** Commit doesn't look good, nice try ***")
//      }
//    }.fix().unsafeRunSync()
//  } else {
//    println("Usage <previous-benchmark-path> <current-benchmark-path> <benchmark-id> <max-diff-score>")
//  }
}