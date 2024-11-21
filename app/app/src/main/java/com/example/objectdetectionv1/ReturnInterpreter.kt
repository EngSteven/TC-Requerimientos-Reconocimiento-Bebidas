package com.example.objectdetectionv1

interface ReturnInterpreter {
    fun classify(confidence:FloatArray, maxConfidence:Int)
}