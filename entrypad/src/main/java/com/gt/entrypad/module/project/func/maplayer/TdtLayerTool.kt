package com.gt.giscollect.module.main.func.maplayer

import com.esri.arcgisruntime.arcgisservices.LevelOfDetail
import com.esri.arcgisruntime.arcgisservices.TileInfo
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.layers.WebTiledLayer

object TdtLayerTool {
    private var origin: Point? = null
    private var envelop: Envelope? = null
    private var levels: ArrayList<LevelOfDetail>? = null

    fun getTdtLayer(serviceType: ServiceType, layerName: LayerName, format: TiledFormat, spatialReference: SpatialReference): WebTiledLayer {
        if (serviceType in arrayOf(
                ServiceType.VEC_C,
                ServiceType.CVA_C,
                ServiceType.EVA_C,
                ServiceType.IMG_C,
                ServiceType.CIA_C,
                ServiceType.TER_C,
                ServiceType.CTA_C
            )
        ) {
            origin = Point(-180.0, 90.0, spatialReference)
            envelop = Envelope(-180.0, -90.0, 180.0, 90.0, spatialReference)
            levels = levelsNormal
        } else {
            origin = Point(-20037508.3427892, 20037508.3427892, spatialReference)
            envelop = Envelope(-20037508.3427892, -20037508.3427892, 20037508.3427892, 20037508.3427892, spatialReference)
            levels = levelsMecator
        }
        val vectorUrl = getTemplateUri(serviceType, layerName, format)
        val tileInfo = getTileInfo(spatialReference)
        return WebTiledLayer(vectorUrl, tileInfo, envelop)
    }

    private fun getTileInfo(spatialReference: SpatialReference): TileInfo {
        val iDPI = 96
        val iTileWidth = 256
        val iTileHeight = 256
        val tileInfo = TileInfo(
            iDPI,
            TileInfo.ImageFormat.MIXED,
            levels,
            origin,
            spatialReference,
            iTileHeight,
            iTileWidth
        )
        return tileInfo
    }

    private fun getTemplateUri(serviceType: ServiceType, layerName: LayerName, format: TiledFormat): String {
        var templateUri = "?service=WMTS" +
                "&request=GetTile" +
                "&version=1.0.0" +
                "&style=default" +
                "&tilematrix={level}" +
                "&tilerow={row}" +
                "&tilecol={col}" +
                "&layer=%s" +
                "&tilematrixset=%s" +
                "&format=%s" +
                "&height=256" +
                "&width=256" +
                "&tk=fa134f74a85416565a0ca34dbff5964e"
        var baseUrl = "http://t5.tianditu.gov.cn/%s/wmts" //vec_c
        var layerType = ""
        var matrixSet = ""
        var tiledFormat = ""
        when (serviceType) {
            ServiceType.VEC_C -> {
                baseUrl = String.format(baseUrl, "vec_c")
                matrixSet = "c"
            }
            ServiceType.CVA_C -> {
                baseUrl = String.format(baseUrl, "cva_c")
                matrixSet = "c"
            }
            ServiceType.EVA_C -> {
                baseUrl = String.format(baseUrl, "eva_c")
                matrixSet = "c"
            }
            ServiceType.IMG_C -> {
                baseUrl = String.format(baseUrl, "img_c")
                matrixSet = "c"
            }
            ServiceType.CIA_C -> {
                baseUrl = String.format(baseUrl, "cia_c")
                matrixSet = "c"
            }
            ServiceType.TER_C -> {
                baseUrl = String.format(baseUrl, "ter_c")
                matrixSet = "c"
            }
            ServiceType.CTA_C -> {
                baseUrl = String.format(baseUrl, "cta_c")
                matrixSet = "c"
            }
            ServiceType.VEC_W -> {
                baseUrl = String.format(baseUrl, "vec_w")
                matrixSet = "w"
            }
            ServiceType.CVA_W -> {
                baseUrl = String.format(baseUrl, "cva_w")
                matrixSet = "w"
            }
            ServiceType.EVA_W -> {
                baseUrl = String.format(baseUrl, "eva_w")
                matrixSet = "w"
            }
            ServiceType.IMG_W -> {
                baseUrl = String.format(baseUrl, "img_w")
                matrixSet = "w"
            }
            ServiceType.CIA_W -> {
                baseUrl = String.format(baseUrl, "cia_w")
                matrixSet = "w"
            }
            ServiceType.TER_W -> {
                baseUrl = String.format(baseUrl, "ter_w")
                matrixSet = "w"
            }
            ServiceType.CTA_W -> {
                baseUrl = String.format(baseUrl, "cia_w")
                matrixSet = "w"
            }
        }
        layerType = when (layerName) {
            LayerName.VECTOR -> "vec"
            LayerName.VECTOR_ANNOTATION_CHINESE -> "cva"
            LayerName.VECTOR_ANNOTATION_ENGLISH -> "eva"
            LayerName.IMAGE -> "img"
            LayerName.IMAGE_ANNOTATION_CHINESE -> "cia"
            LayerName.IMAGE_ANNOTATION_ENGLISH -> "eia"
            LayerName.TERRAIN -> "ter"
            LayerName.TERRAIN_ANNOTATION_CHINESE -> "cta"
            else -> ""
        }
        tiledFormat = when (format) {
            TiledFormat.PNG -> "png"
            TiledFormat.TILES -> "tile"
            TiledFormat.JPEG -> "jpeg"
            else -> ""
        }
        templateUri = String.format(templateUri, layerType, matrixSet, tiledFormat)
        return baseUrl + templateUri
    }

    private val levelsNormal = arrayListOf(
//        LevelOfDetail(1, 0.703125, 295497593.05875003),
//        LevelOfDetail(2, 0.3515625, 147748796.52937502),
//        LevelOfDetail(3, 0.17578125, 73874398.264687508),
//        LevelOfDetail(4, 0.087890625, 36937199.132343754),
//        LevelOfDetail(5, 0.0439453125, 18468599.566171877),
//        LevelOfDetail(6, 0.02197265625, 9234299.7830859385),
//        LevelOfDetail(7, 0.010986328125, 4617149.8915429693),
//        LevelOfDetail(8, 0.0054931640625, 2308574.9457714846),
//        LevelOfDetail(8, 0.00274658203125, 1154287.4728857423),
//        LevelOfDetail(10, 0.001373291015625, 577143.73644287116),
//        LevelOfDetail(11, 0.0006866455078125, 288571.86822143558),
//        LevelOfDetail(12, 0.00034332275390625, 144285.93411071779),
//        LevelOfDetail(13, 0.000171661376953125, 72142.967055358895),
//        LevelOfDetail(14, 8.58306884765625e-005, 36071.483527679447),
//        LevelOfDetail(15, 4.291534423828125e-005, 18035.741763839724),
//        LevelOfDetail(16, 2.1457672119140625e-005, 9017.8708819198619),
//        LevelOfDetail(17, 1.0728836059570313e-005, 4508.9354409599309),
//        LevelOfDetail(18, 5.3644180297851563e-006, 2254.4677204799655),
//        LevelOfDetail(19, 2.6822090148925781e-006, 1127.2338602399827),
//        LevelOfDetail(20, 1.3411045074462891e-006, 563.61693011999137),


        LevelOfDetail(1, 0.7031249999891485, 2.958293554545656E8),
        LevelOfDetail(2, 0.35156249999999994, 1.479146777272828E8),
        LevelOfDetail(3, 0.17578124999999997, 7.39573388636414E7),
        LevelOfDetail(4, 0.08789062500000014, 3.69786694318207E7),
        LevelOfDetail(5, 0.04394531250000007, 1.848933471591035E7),
        LevelOfDetail(6, 0.021972656250000007, 9244667.357955175),
        LevelOfDetail(7, 0.01098632812500002, 4622333.678977588),
        LevelOfDetail(8, 0.00549316406250001, 2311166.839488794),
        LevelOfDetail(9, 0.0027465820312500017, 1155583.419744397),
        LevelOfDetail(10, 0.0013732910156250009, 577791.7098721985),
        LevelOfDetail(11, 0.000686645507812499, 288895.85493609926),
        LevelOfDetail(12, 0.0003433227539062495, 144447.92746804963),
        LevelOfDetail(13, 0.00017166137695312503, 72223.96373402482),
        LevelOfDetail(14, 0.00008583068847656251, 36111.98186701241),
        LevelOfDetail(15, 0.000042915344238281406, 18055.990933506204),
        LevelOfDetail(16, 0.000021457672119140645, 9027.995466753102),
        LevelOfDetail(17, 0.000010728836059570307, 4513.997733376551),
        LevelOfDetail(18, 0.000005364418029785169, 2256.998866688275)
    )

    val levelsMecator = arrayListOf(
//        LevelOfDetail(1, 0.703125, 295497593.05875003),
//        LevelOfDetail(2, 0.3515625, 147748796.52937502),
//        LevelOfDetail(3, 0.17578125, 73874398.264687508),
//        LevelOfDetail(4, 0.087890625, 36937199.132343754),
//        LevelOfDetail(5, 0.0439453125, 18468599.566171877),
//        LevelOfDetail(6, 0.02197265625, 9234299.7830859385),
//        LevelOfDetail(7, 0.010986328125, 4617149.8915429693),
//        LevelOfDetail(8, 0.0054931640625, 2308574.9457714846),
//        LevelOfDetail(8, 0.00274658203125, 1154287.4728857423),
//        LevelOfDetail(10, 0.001373291015625, 577143.73644287116),
//        LevelOfDetail(11, 0.0006866455078125, 288571.86822143558),
//        LevelOfDetail(12, 0.00034332275390625, 144285.93411071779),
//        LevelOfDetail(13, 0.000171661376953125, 72142.967055358895),
//        LevelOfDetail(14, 8.58306884765625e-005, 36071.483527679447),
//        LevelOfDetail(15, 4.291534423828125e-005, 18035.741763839724),
//        LevelOfDetail(16, 2.1457672119140625e-005, 9017.8708819198619),
//        LevelOfDetail(17, 1.0728836059570313e-005, 4508.9354409599309),
//        LevelOfDetail(18, 5.3644180297851563e-006, 2254.4677204799655),
//        LevelOfDetail(19, 2.6822090148925781e-006, 1127.2338602399827),
//        LevelOfDetail(20, 1.3411045074462891e-006, 563.61693011999137),

        LevelOfDetail(1, 78271.51696402048, 2.958293554545656E8),
        LevelOfDetail(2, 39135.75848201024, 1.479146777272828E8),
        LevelOfDetail(3, 19567.87924100512, 7.39573388636414E7),
        LevelOfDetail(4, 9783.93962050256, 3.69786694318207E7),
        LevelOfDetail(5, 4891.96981025128, 1.848933471591035E7),
        LevelOfDetail(6, 72445.98490512564, 9244667.35795517),
        LevelOfDetail(7, 1222.99245256282, 4622333.678977588),
        LevelOfDetail(8, 611.49622628141, 2311166.839488794),
        LevelOfDetail(9, 305.748113140705, 1155583.419744397),
        LevelOfDetail(10, 152.8740565703525, 577791.7098721985),
        LevelOfDetail(11, 76.43702828517625, 288895.85493609926),
        LevelOfDetail(12, 38.21851414258813, 144447.92746804963),
        LevelOfDetail(13, 19.109257071294063, 72223.96373402482),
        LevelOfDetail(14, 9.554628535647032, 36111.98186701241),
        LevelOfDetail(15, 4.777314267823516, 18055.990933506204),
        LevelOfDetail(16, 2.388657133911758, 9027.995466753102),
        LevelOfDetail(17, 1.194328566955879, 4513.997733376551),
        LevelOfDetail(18, 0.5971642834779395, 2256.998866688275)
    )

    enum class ServiceType {
        VEC_W, CVA_W, EVA_W, IMG_W, CIA_W, TER_W, CTA_W, VEC_C, CVA_C, EVA_C, IMG_C, CIA_C, TER_C, CTA_C
    }

    enum class LayerName {
        VECTOR,
        VECTOR_ANNOTATION_CHINESE,
        VECTOR_ANNOTATION_ENGLISH,
        IMAGE,
        IMAGE_ANNOTATION_CHINESE,
        IMAGE_ANNOTATION_ENGLISH,
        TERRAIN,
        TERRAIN_ANNOTATION_CHINESE
    }

    enum class TiledFormat {
        PNG, TILES, JPEG
    }
}