package com.gt.giscollect.module.collect.bean

data class FieldImportBean(var infos: List<Pair<String, String>>, var isExpand: Boolean = false) {

    fun getInfo(isSingle: Boolean): String {
        val stringBuider = StringBuilder()
        infos.forEachIndexed { index, it ->
            if (it.first !in arrayOf("rowno", "prjstatus", "created")) {
                if (!isSingle) stringBuider.append(it.first)
                if (!isSingle) stringBuider.append(":")
                stringBuider.append(it.second)
                if (index < infos.size - 1) {
                    if (isSingle) {
                        stringBuider.append(";")
                    } else {
                        stringBuider.append("\n")
                    }
                }
            }
        }
        return stringBuider.toString()
    }

}