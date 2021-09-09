package com.satsports247.dataModels

import java.io.Serializable

class PLReportModel:Serializable {
    var Sportname: String = ""
    var Description: String = ""
    var Result: String = ""
    var CreatedDate: String = ""
    var MarketID: String = ""
    var PL: Double = 0.0
    var RowCount: Int = 0
    var SiteTypeId: Int = 0
}