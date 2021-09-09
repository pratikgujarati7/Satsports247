package com.satsports247.dataModels

class GameListModel {
    var name: String = ""
    var category: String = ""
    var product: String = ""
    var Platform: String = ""
    var user: String = ""
    var lobby_url: String = ""
    var id: Int = 0
    var enabled: Boolean = false
    var isSlot: Boolean = false
    var platforms = ArrayList<String>()
}