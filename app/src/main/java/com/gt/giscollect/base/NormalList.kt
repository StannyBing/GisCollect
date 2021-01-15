package com.gt.giscollect.base

data class NormalList<T>(
    var pageIndex: Int,
    var currPage: Int,
    var total: Int,
    var rows: List<T>
) {
}