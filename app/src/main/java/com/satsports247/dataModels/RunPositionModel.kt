package com.satsports247.dataModels

import java.io.Serializable

data class RunPositionModel(
    var run: Int = 0,
    var value: Int = 0
) : Serializable