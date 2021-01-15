package com.gt.giscollect.module.main.func.maplayer

import android.app.Activity
import com.esri.arcgisruntime.arcgisservices.LevelOfDetail
import com.esri.arcgisruntime.arcgisservices.TileInfo
import com.esri.arcgisruntime.data.TileKey
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReference
import com.esri.arcgisruntime.layers.ImageTiledLayer
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class GoogleLayer  //        setBufferSize(BufferSize.MEDIUM);
    (
    private val mMapType: MapType?,
    tileInfo: TileInfo?,
    fullExtent: Envelope?
) :
    ImageTiledLayer(tileInfo, fullExtent) {
    // 枚举
    enum class MapType {
        VECTOR,  //矢量标注地图
        IMAGE,  //影像地图
        ROAD //道路标注图层
    }

    private val mactivity: Activity? = null

    private fun initLayer() {
//        tile
    }

    override fun getTile(tileKey: TileKey): ByteArray {
        var iResult: ByteArray? = null
        try {
            var iURL: URL? = null
            val iBuffer = ByteArray(1024)
            var iHttpURLConnection: HttpURLConnection? = null
            var iBufferedInputStream: BufferedInputStream? = null
            var iByteArrayOutputStream: ByteArrayOutputStream? = null
            iURL = URL(getMapUrl(tileKey))
            iHttpURLConnection = iURL.openConnection() as HttpURLConnection
            iHttpURLConnection.connect()
            iBufferedInputStream = BufferedInputStream(iHttpURLConnection!!.inputStream)
            iByteArrayOutputStream = ByteArrayOutputStream()
            while (true) {
                val iLength = iBufferedInputStream.read(iBuffer)
                if (iLength > 0) {
                    iByteArrayOutputStream.write(iBuffer, 0, iLength)
                } else {
                    break
                }
            }
            iBufferedInputStream.close()
            iHttpURLConnection.disconnect()
            iResult = iByteArrayOutputStream.toByteArray()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return iResult!!
    }

    private fun getMapUrl(tileKey: TileKey): String? {
        var iResult: String? = null
        var iRandom: Random? = null
        val level = tileKey.level
        val col = tileKey.column
        val row = tileKey.row
        iResult = "http://mt"
        iRandom = Random()
        iResult += iRandom.nextInt(4)
        iResult = when (mMapType) {
            MapType.VECTOR -> "$iResult.google.cn/vt/lyrs=m@212000000&hl=zh-CN&gl=CN&src=app&x=$col&y=$row&z=$level&s==Galil"
//            MapType.IMAGE -> "http://www.google.cn/maps/vt?lyrs=y@189z&x=$col&y=$row&z=$level"
            MapType.IMAGE -> "$iResult.google.cn/vt/lyrs=s@126&hl=zh-CN&src=app&x=$col&y=$row&z=$level&s==Galil"
            MapType.ROAD -> "$iResult.google.cn/vt/imgtp=png32&lyrs=h@212000000&hl=zh-CN&gl=CN&src=app&x=$col&y=$row&z=$level&s==Galil"
            else -> return null
        }
        return iResult
    }

    override fun getTileInfo(): TileInfo {
        return mTileInfo!!
    }

    companion object {
        private var googleMapLayer: GoogleLayer? = null
        private var mTileInfo: TileInfo? = null
        var iScale = doubleArrayOf(
            591657527.591555,
            295828763.795777,
            147914381.897889,
            73957190.948944,
            36978595.474472,
            18489297.737236,
            9244648.868618,
            4622324.434309,
            2311162.217155,
            1155581.108577,
            577790.554289,
            288895.277144,
            144447.638572,
            72223.819286,
            36111.909643,
            18055.954822,
            9027.977411,
            4513.988705,
            2256.994353,
            1128.497176
        )
        var iRes = doubleArrayOf(
            156543.033928,
            78271.5169639999,
            39135.7584820001,
            19567.8792409999,
            9783.93962049996,
            4891.96981024998,
            2445.98490512499,
            1222.99245256249,
            611.49622628138,
            305.748113140558,
            152.874056570411,
            76.4370282850732,
            38.2185141425366,
            19.1092570712683,
            9.55462853563415,
            4.77731426794937,
            2.38865713397468,
            1.19432856685505,
            0.597164283559817,
            0.298582141647617
        )

        fun getInstance(mapType: MapType, activity: Activity, spatialReference: SpatialReference): GoogleLayer {
//            if (googleMapLayer == null) {
            return GoogleLayer(
                mapType,
                buildTileInfo(activity, spatialReference),
                Envelope(-20037508.3427892, -20037508.3427892, 20037508.3427892, 20037508.3427892, spatialReference)
            )
//            }
//            return googleMapLayer!!
        }

        fun getInstance(
            mapType: MapType?,
            tileInfo: TileInfo?,
            fullExtent: Envelope?
        ): GoogleLayer? {
            if (googleMapLayer == null) {
                googleMapLayer =
                    GoogleLayer(mapType, tileInfo, fullExtent)
            }
            return googleMapLayer
        }

        fun buildTileInfo(activity: Activity, spatialReference: SpatialReference): TileInfo? {


//        Point iPoint = new Point(x,y,SpatialReference.create(102113));
            val iPoint = Point(
                -20037508.342787,
                20037508.342787,
                spatialReference
            )
            val levelOfDetails: MutableList<LevelOfDetail> = ArrayList()
            for (i in iRes.indices) {
                val levelOfDetail = LevelOfDetail(
                    i,
                    iRes[i],
                    iScale[i]
                )
                levelOfDetails.add(levelOfDetail)
            }
            mTileInfo = TileInfo(
                160,
                TileInfo.ImageFormat.UNKNOWN,
                levelOfDetails,
                iPoint,
                spatialReference,
                256,
                256
            )
            return mTileInfo
        } //    @Override
        //    protected String getTileUrl(TileKey tileKey) {
        //        String iResult = null;
        //        Random iRandom = null;
        //        int level=tileKey.getLevel();
        //        int col=tileKey.getColumn();
        //        int row=tileKey.getRow();
        //        iResult = "http://mt";
        //        iRandom = new Random();
        //        iResult = iResult + col%4;
        //        switch (this.mMapType) {
        //            case VECTOR:
        //                iResult = iResult + ".google.cn/vt/lyrs=m@212000000&hl=zh-CN&gl=CN&src=app&x=" + col + "&y=" + row + "&z=" + level + "&s==Galil";
        //                break;
        //            case IMAGE:
        //                iResult = iResult + ".google.cn/vt/lyrs=s@126&hl=zh-CN&gl=CN&src=app&x=" + col + "&y=" + row + "&z=" + level + "&s==Galil";
        //                break;
        //            case ROAD:
        //                iResult = iResult + ".google.cn/vt/imgtp=png32&lyrs=h@212000000&hl=zh-CN&gl=CN&src=app&x=" + col + "&y=" + row + "&z=" + level + "&s==Galil";
        //                break;
        //            default:
        //                return null;
        //        }
        //        MyLog.i("url=="+iResult);
        //        return iResult;
        //    }
        //
        //    @Override
        //    public String getUri() {
        //        MyLog.i("getUri");
        //        return null;
        //    }
    }

}