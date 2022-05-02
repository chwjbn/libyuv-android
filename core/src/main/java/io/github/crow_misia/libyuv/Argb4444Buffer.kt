package io.github.crow_misia.libyuv

import java.nio.ByteBuffer

/**
 * RGB12 (R444 fourcc) little endian
 */
class Argb4444Buffer private constructor(
    buffer: ByteBuffer?,
    val plane: Plane,
    override val width: Int,
    override val height: Int,
    releaseCallback: Runnable?,
) : AbstractBuffer(buffer, arrayOf(plane), releaseCallback) {
    companion object Factory : BufferFactory<Argb4444Buffer> {
        internal fun getStrideWithCapacity(width: Int, height: Int): IntArray {
            val stride = width.shl(1)
            val capacity = stride * height
            return intArrayOf(stride, capacity)
        }

        override fun allocate(width: Int, height: Int): Argb4444Buffer {
            val (stride, capacity) = getStrideWithCapacity(width, height)
            val buffer = createByteBuffer(capacity)
            return Argb4444Buffer(
                buffer = buffer,
                plane = PlanePrimitive(stride, buffer),
                width = width,
                height = height,
            ) {
                Yuv.freeNativeBuffer(buffer)
            }
        }

        override fun wrap(buffer: ByteBuffer, width: Int, height: Int): Argb4444Buffer {
            check(buffer.isDirect) { "Unsupported non-direct ByteBuffer." }

            val (stride, capacity) = getStrideWithCapacity(width, height)
            val sliceBuffer = buffer.sliceRange(0, capacity)
            return Argb4444Buffer(
                buffer = sliceBuffer,
                plane = PlanePrimitive(stride, sliceBuffer),
                width = width,
                height = height,
                releaseCallback = null,
            )
        }

        fun wrap(plane: Plane, width: Int, height: Int): Argb4444Buffer {
            return Argb4444Buffer(
                buffer = plane.buffer,
                plane = plane,
                width = width,
                height = height,
                releaseCallback = null,
            )
        }
    }
}
