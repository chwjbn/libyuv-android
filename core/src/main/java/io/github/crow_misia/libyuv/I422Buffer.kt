package io.github.crow_misia.libyuv

import android.graphics.Bitmap
import android.media.Image
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

/**
 * I422 (BT.601) YUV Format. 4:2:2 16bpp
 */
class I422Buffer private constructor(
    buffer: ByteBuffer?,
    val planeY: Plane,
    val planeU: Plane,
    val planeV: Plane,
    override val width: Int,
    override val height: Int,
    releaseCallback: Runnable? = null,
) : AbstractBuffer(buffer, arrayOf(planeY, planeU, planeV), releaseCallback) {
    override fun asBitmap(): Bitmap {
        return AbgrBuffer.allocate(width, height).use { buf ->
            convertTo(buf)
            buf.asBitmap()
        }
    }

    companion object {
        @JvmStatic
        fun getStrideWithCapacity(width: Int, height: Int): IntArray {
            val halfWidth = (width + 1).shr(1)
            val capacity = width * height
            val halfCapacity = halfWidth * height
            return intArrayOf(width, capacity, halfWidth, halfCapacity, halfWidth, halfCapacity)
        }

        @JvmStatic
        fun allocate(width: Int, height: Int): I422Buffer {
            val (strideY, capacityY, strideU, capacityU, strideV, capacityV) = getStrideWithCapacity(width, height)
            val buffer = createByteBuffer(capacityY + capacityU + capacityV)
            val (bufferY, bufferU, bufferV) = buffer.slice(capacityY, capacityU, capacityV)
            return I422Buffer(
                buffer = buffer,
                planeY = PlanePrimitive(strideY, bufferY),
                planeU = PlanePrimitive(strideU, bufferU),
                planeV = PlanePrimitive(strideV, bufferV),
                width = width,
                height = height,
            ) {
                Yuv.freeNativeBuffer(buffer)
            }
        }

        @JvmStatic
        @JvmOverloads
        fun wrap(buffer: ByteBuffer, width: Int, height: Int, releaseCallback: Runnable? = null): I422Buffer {
            val (strideY, capacityY, strideU, capacityU, strideV, capacityV) = getStrideWithCapacity(width, height)
            val (bufferY, bufferU, bufferV) = buffer.slice(capacityY, capacityU, capacityV)
            return I422Buffer(
                buffer = buffer,
                planeY = PlanePrimitive(strideY, bufferY),
                planeU = PlanePrimitive(strideU, bufferU),
                planeV = PlanePrimitive(strideV, bufferV),
                width = width,
                height = height,
                releaseCallback = releaseCallback,
            )
        }

        @JvmStatic
        @JvmOverloads
        fun wrap(planeY: Plane, planeU: Plane, planeV: Plane, width: Int, height: Int, releaseCallback: Runnable? = null): I422Buffer {
            return I422Buffer(
                buffer = null,
                planeY = planeY,
                planeU = planeU,
                planeV = planeV,
                width = width,
                height = height,
                releaseCallback = releaseCallback,
            )
        }

        @RequiresApi(Build.VERSION_CODES.KITKAT)
        @JvmStatic
        @JvmName("from")
        fun Image.toI422Buffer(): I422Buffer {
            return I422Buffer(
                buffer = null,
                planeY = PlaneNative(planes[0]),
                planeU = PlaneNative(planes[1]),
                planeV = PlaneNative(planes[2]),
                width = width,
                height = height,
            )
        }

        @JvmStatic
        @JvmName("from")
        fun ImageProxy.toI422Buffer(): I422Buffer {
            return I422Buffer(
                buffer = null,
                planeY = PlaneProxy(planes[0]),
                planeU = PlaneProxy(planes[1]),
                planeV = PlaneProxy(planes[2]),
                width = width,
                height = height,
            )
        }
    }
}
