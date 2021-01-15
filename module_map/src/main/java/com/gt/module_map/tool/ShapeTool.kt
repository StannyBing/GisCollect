//package com.gt.module_map.tool
//
//import com.esri.arcgisruntime.data.QueryParameters
//import com.esri.arcgisruntime.layers.FeatureLayer
//import com.esri.arcgisruntime.mapping.ArcGISMap
//import com.zx.zxutils.util.ZXFileUtil
//import com.zx.zxutils.util.ZXLogUtil
//import org.gdal.gdal.gdal
//import org.gdal.ogr.*
//import org.gdal.ogr.ogrConstants.*
//import org.gdal.osr.SpatialReference
//import java.io.File
//
//
//object ShapeTool {
//
//    enum class ShapeType {
//        Point,
//        Polyline,
//        Polygon
//    }
//
//    data class FieldBean(
//        var name: String,
//        var type: FieldType
//    ) {
//        enum class FieldType {
//            String,
//            Int,
//            FLOAT
//        }
//    }
//
//    /**
//     * 创建shape文件
//     */
//    fun createShape(map: ArcGISMap, shapePath: String, layerName: String, shapeType: ShapeType, tableMap: ArrayList<FieldBean>): String {
//        val path = "$shapePath$layerName/shape"
//        try {
//            ogr.RegisterAll()
//            gdal.GetConfigOption("SHAPE_ENCODING", null)
//            gdal.SetConfigOption("GDSL_FILENAME_IS_UTF8", "YES")
////            gdal.SetConfigOption("SHAPE_ENCODING", "UTF-8")
////            gdal.SetConfigOption("SHAPE_ENCODING", "GBK")
////            gdal.SetConfigOption("SHAPE_ENCODING", "GB2312")
////            gdal.SetConfigOption("SHAPE_ENCODING", "CP936")
//            gdal.SetConfigOption("SHAPE_ENCODING", "CP936")
//
//            val driverName = "ESRI Shapefile"//文件格式
//            val driver = ogr.GetDriverByName(driverName)
//            if (driver == null) {
//                ZXLogUtil.loge("${driverName}驱动不可用")
//                return ""
//            }
//            val file = File("$path/")
//            if (!file.exists()) {
//                file.mkdirs()
//            }
//            val dataSource = driver.CreateDataSource(path, null)
//            if (dataSource == null) {
//                ZXLogUtil.loge("Shape文件创建失败")
//                return ""
//            }
//            //定义坐标可以从shp的prj文件里获取所需要的的参数
////            SpatialReference(map.spatialReference.wkid.toString())
//            val spatial = SpatialReference(map.spatialReference.wkText)
////            spatial.SetProjCS("UTM 17 /WGS84")
//            spatial.SetWellKnownGeogCS("WGS84")
////            spatial.SetUTM(17)
//            val layer = dataSource.CreateLayer(
//                layerName, spatial, when (shapeType) {
//                    ShapeType.Point -> ogr.wkbPoint
//                    ShapeType.Polyline -> ogr.wkbLineString
//                    ShapeType.Polygon -> ogr.wkbPolygon
//                    else -> ogr.wkbPolygon
//                }, null
//            )
//            if (layer == null) {
//                ZXLogUtil.loge("图层创建失败")
//                return ""
//            }
//            //创建属性表
//            if (tableMap.isNotEmpty()) {
//                tableMap.forEach {
//                    layer.CreateField(
//                        FieldDefn(
//                            it.name, when (it.type) {
//                                FieldBean.FieldType.String -> ogr.OFTString
//                                FieldBean.FieldType.Int -> ogr.OFTInteger
//                                FieldBean.FieldType.FLOAT -> ogr.OFTReal
//                                else -> ogr.OFTString
//                            }
//                        )
//                    )
//                }
//            }
//
//            doSomeStupid(layer, shapeType)
//
//
//            layer.SyncToDisk()
//            dataSource.SyncToDisk()
//            ZXLogUtil.loge("ShapeFile创建完成")
//        } catch (e: Exception) {
//            if (File("$path/$layerName.shp").exists()) {
//                e.printStackTrace()
//            } else {
//                ZXLogUtil.loge("ShapeFile创建出错")
//                return ""
//            }
//        }
////        ogr.RegisterAll()
//        return "$path/$layerName.shp"
//    }
//
//    /**
//     * 令人无法接受，GDAL创建的shape如果不添加feature，就很容易读不出来layer的名字，时灵时不灵，有时又可以读出来
//     * 网上找的Demo怎么都可以读出来，找了各种版本的GDAL都不行，浪费两天时间才发现
//     * 是因为没有创建Feature的原因，只能手动先根据类型添加一个feature进去，然后马上删除
//     * 是的加了马上删掉Feature就不会出问题了
//     * 愚蠢的问题！！！！！！
//     */
//    private fun doSomeStupid(layer: Layer, shapeType: ShapeType) {
//        if (layer.GetFeatureCount() > 0) {
//            for (i in 0 until layer.GetFeatureCount()) {
//                try {
//                    layer.DeleteFeature(i)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        } else {
//            val oDefn: FeatureDefn = layer.GetLayerDefn()
//            val oFeatureTriangle = Feature(oDefn)
//            val geometry = when (shapeType) {
//                ShapeType.Point -> Geometry(wkbPoint).apply {
//                    AddPoint(11858277.123, 3451329.123)
//                }
//                ShapeType.Polyline -> Geometry(wkbLineString).apply {
//                    AddPoint(11858277.123, 345132.123)
//                    AddPoint(11859111.123, 3451325.123)
//                }
//                ShapeType.Polygon -> Geometry(wkbPolygon).apply {
//                    AddPoint(11858277.123, 3451329.123)
//                    AddPoint(11859111.123, 3451325.123)
//                    AddPoint(11858322.123, 3450979.123)
//                }
//                else -> Geometry(wkbPolygon).apply {
//                    AddPoint(11858277.123, 3451329.123)
//                    AddPoint(11859111.123, 3451325.123)
//                    AddPoint(11858322.123, 3450979.123)
//                }
//            }
//            oFeatureTriangle.SetGeometry(geometry)
//            layer.CreateFeature(oFeatureTriangle)
//            layer.DeleteFeature(oFeatureTriangle.GetFID())
//        }
//    }
//
////    fun deleteFeature(layer : FeatureLayer, fid : String){
////        val getListenbale = layer.featureTable.queryFeaturesAsync(QueryParameters())
////        getListenbale.addDoneListener {
////            getListenbale.get().forEach {
////                if ()
////            }
////        }
////    }
//
//    fun exuteCopyLayer(shapePath: String) {
//        // 注册所有的驱动
//        ogr.RegisterAll()
//        // 为了使属性表字段支持中文，请添加下面这句
//        gdal.SetConfigOption("SHAPE_ENCODING", "CP936")
//        val ds: DataSource = ogr.Open(shapePath)
////        if (ds != null) {
////            println("打开文件失败！")
////            return
////        }
//        for (i in 0 until ds.GetLayerCount()) {
//            val oLayer: Layer = ds.GetLayerByIndex(i)
//            doSomeStupid(
//                oLayer, when (oLayer.GetGeomType()) {
//                    ogr.wkbPoint -> ShapeType.Point
//                    ogr.wkbLineString -> ShapeType.Polyline
//                    ogr.wkbPolygon -> ShapeType.Polygon
//                    else -> ShapeType.Polygon
//                }
//            )
//            oLayer.SyncToDisk()
//        }
//        ds.SyncToDisk()
//    }
//
//    // 读取shp
//    fun readShape(shapePath: String) {
//        // 注册所有的驱动
//        ogr.RegisterAll()
//        val encoding = gdal.GetConfigOption("SHAPE_ENCODING", null)
//        // 为了支持中文路径，请添加下面这句代码
//        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "NO")
//        // 为了使属性表字段支持中文，请添加下面这句
//        gdal.SetConfigOption("SHAPE_ENCODING", "UTF-8")
//
//        //打开文件
//        val ds: DataSource? = ogr.Open(shapePath, 0)
//        if (ds == null) {
//            println("打开文件失败！")
//            return
//        }
//        println("打开文件成功！")
//
//        // 获取该数据源中的图层个数，一般shp数据图层只有一个，如果是mdb、dxf等图层就会有多个
////        int iLayerCount = ds.GetLayerCount();
//        if (ds.GetLayerCount() == 0) {
//            println("未获取到图层！\n")
//            return
//        } else if (ds.GetLayerCount() > 1) {
//            println("获取到${ds.GetLayerCount()}个图层！\n")
//        }
//        for (i in 0 until ds.GetLayerCount()) {
//            val oLayer: Layer = ds.GetLayerByIndex(i)
//            if (oLayer == null) {
//                println("获取第${i}个图层失败！\n")
//                return
//            }
//
//
//            // 获取第一个图层
//
//            // 对图层进行初始化，如果对图层进行了过滤操作，执行这句后，之前的过滤全部清空
//            oLayer.ResetReading()
//            // 通过属性表的SQL语句对图层中的要素进行筛选，这部分详细参考SQL查询章节内容
//            //oLayer.SetAttributeFilter("\"NAME99\"LIKE \"北京市市辖区\"");
//            // 通过指定的几何对象对图层中的要素进行筛选
//            //oLayer.SetSpatialFilter();
//            // 通过指定的四至范围对图层中的要素进行筛选
//            //oLayer.SetSpatialFilterRect();
//
//            // 获取图层中的属性表表头并输出
//            println("属性表结构信息：")
//            val oDefn: FeatureDefn = oLayer.GetLayerDefn()
//            val iFieldCount = oDefn.GetFieldCount()
//            for (iAttr in 0 until iFieldCount) {
//                val oField = oDefn.GetFieldDefn(iAttr)
//                val content = oField.GetNameRef() + ": " +
//                        oField.GetFieldTypeName(oField.GetFieldType()) + "(" +
//                        oField.GetWidth() + "." + oField.GetPrecision() + ")"
//                println(content)
//            }
//
//            // 输出图层中的要素个数
//            System.out.println("要素个数 = " + oLayer.GetFeatureCount(0))
//            var oFeature: Feature? = null
//            // 下面开始遍历图层中的要素
//            while (oLayer.GetNextFeature().also({ oFeature = it }) != null) {
//                System.out.println(
//                    """
//                    当前处理第${oFeature?.GetFID().toString()}个:
//                    属性值：
//                    """.trimIndent()
//                )
//                // 获取要素中的属性表内容
//                for (iField in 0 until iFieldCount) {
//                    val oFieldDefn = oDefn.GetFieldDefn(iField)
//                    val type = oFieldDefn.GetFieldType()
//                    when (type) {
//                        ogr.OFTString -> {
//                            println(oFeature?.GetFieldAsString(iField).toString() + "\t")
//                        }
//                        ogr.OFTReal -> {
//                            println(oFeature?.GetFieldAsDouble(iField).toString() + "\t")
//                        }
//                        ogr.OFTInteger -> {
//                            println(oFeature?.GetFieldAsInteger(iField).toString() + "\t")
//                        }
//                        ogr.OFTDate -> {
//                        }
//                        else -> {
//                            println(oFeature?.GetFieldAsString(iField).toString() + "\t")
//                        }
//                    }
//                }
//
//                // 获取要素中的几何体
//                val oGeometry = oFeature?.GetGeometryRef()
//                println(oGeometry?.ExportToJson())
//            }
//            println("数据集关闭！")
//        }
//    }
////
////    // 写入shp文件
////    @Throws(UnsupportedEncodingException::class)
////    private fun writeShp() {
////        ogr.RegisterAll()
////        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "NO")
////        gdal.SetConfigOption("SHAPE_ENCODING", "UTF-8")
////        val strDriverName = "ESRI Shapefile"
////        val oDriver = ogr.GetDriverByName(strDriverName)
////        if (oDriver == null) {
////            println("$strDriverName 驱动不可用！\n")
////            return
////        }
////        val oDS: DataSource? = oDriver.CreateDataSource(shpPath, null)
////        if (oDS == null) {
////            System.out.println(
////                """
////                    创建矢量文件【${shpPath.toString()}】失败！
////
////                    """.trimIndent()
////            )
////            return
////        }
////        val oLayer: Layer = oDS.CreateLayer("TestPolygon", null, ogr.wkbPolygon, null)
////        if (oLayer == null) {
////            println("图层创建失败！\n")
////            return
////        }
////
////        // 下面创建属性表
////        // 先创建一个叫FieldID的整型属性
////        val oFieldID = FieldDefn("FieldID", ogr.OFTInteger)
////        oLayer.CreateField(oFieldID)
////
////        // 再创建一个叫FeatureName的字符型属性，字符长度为50
////        val oFieldName = FieldDefn("FieldName", ogr.OFTString)
////        oFieldName.SetWidth(100)
////        oLayer.CreateField(oFieldName)
////        val oDefn: FeatureDefn = oLayer.GetLayerDefn()
////
////        // 创建三角形要素
////        val oFeatureTriangle = Feature(oDefn)
////        oFeatureTriangle.SetField(0, 0)
////        //        oFeatureTriangle.SetField(1, Base64Utils.encodeStr("三角形11"));
////        oFeatureTriangle.SetField(1, String("三角形11".toByteArray(), "UTF-8"))
////        val geomTriangle: Geometry = Geometry.CreateFromWkt("POLYGON ((0 0,20 0,10 15,0 0))")
////        oFeatureTriangle.SetGeometry(geomTriangle)
////        oLayer.CreateFeature(oFeatureTriangle)
////
////        // 创建矩形要素
////        val oFeatureRectangle = Feature(oDefn)
////        oFeatureRectangle.SetField(0, 1)
////        oFeatureRectangle.SetField(1, "矩形222")
////        val geomRectangle: Geometry = Geometry.CreateFromWkt("POLYGON ((30 0,60 0,60 30,30 30,30 0))")
////        oFeatureRectangle.SetGeometry(geomRectangle)
////        oLayer.CreateFeature(oFeatureRectangle)
////
////        // 创建五角形要素
////        val oFeaturePentagon = Feature(oDefn)
////        oFeaturePentagon.SetField(0, 2)
////        oFeaturePentagon.SetField(1, "五角形33")
////        val geomPentagon: Geometry = Geometry.CreateFromWkt("POLYGON ((70 0,85 0,90 15,80 30,65 15,70 0))")
////        oFeaturePentagon.SetGeometry(geomPentagon)
////        oLayer.CreateFeature(oFeaturePentagon)
////        try {
////            oLayer.SyncToDisk()
////            oDS.SyncToDisk()
////        } catch (e: java.lang.Exception) {
////            e.printStackTrace()
////        }
////        println("\n数据集创建完成！\n")
////    }
//
//}