package com.sliteptyltd.slite.utils.extensions

import android.graphics.Color
import androidx.camera.core.ImageProxy
import androidx.camera.core.ImageAnalysis
import com.sliteptyltd.slite.utils.Constants.ColorUtils.MAX_RGB_COMPONENT_VALUE
import com.sliteptyltd.slite.utils.Constants.ImageAnalysis.COLOR_SQUARE_AREA
import com.sliteptyltd.slite.utils.Constants.ImageAnalysis.COLOR_SQUARE_MID_COLUMN_INDEX
import com.sliteptyltd.slite.utils.Constants.ImageAnalysis.COLOR_SQUARE_MID_ROW_INDEX
import com.sliteptyltd.slite.utils.Constants.ImageAnalysis.COLOR_SQUARE_SIZE
import com.sliteptyltd.slite.utils.Constants.ImageAnalysis.RGBA_8888_BLUE_OFFSET
import com.sliteptyltd.slite.utils.Constants.ImageAnalysis.RGBA_8888_GREEN_OFFSET
import com.sliteptyltd.slite.utils.Constants.ImageAnalysis.RGB_PLANE_PROXY_INDEX
import java.nio.ByteBuffer

/**
 * The value provided by [ImageProxy.PlaneProxy.getPixelStride] is the number of elements used to represent a pixel in the [ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888] format.
 * Each pixel is allocated 4 positions in the ByteBuffer/ByteArray, representing the red, green, blue and alpha values in that particular order.
 * Say we have 2 pixels [P0, P1] represented in a ByteArray as described above, it would look like so:
 * [R0, G0, B0, A0, R1, G1, B1, A1]
 *
 * Let's take the example of a 2x3 image, with pixels represented as Pij where i is the row index and j is the column index
 * Now, an image is normally represented as a two dimensional array of pixels (width x height):
 * [P00, P01],
 * [P10, P11],
 * [P20, P21]
 *
 *  If we represent each pixel with 4 values (pixelStride), as mentioned above, then we end up with a two dimensional array of
 *  sizes 8x3 (pixelStride*width x height)
 *  [R00, G00, B00, A00, R01, G01, B01, A01],
 *  [R10, G10, B10, A10, R11, G11, B11, A11],
 *  [R20, G20, B20, A20, R21, G21, B21, A21]
 *
 *  But, in the end we receive a unidimensional array, which is built by concatenating the rows:
 *  [R00, G00, B00, A00, R01, G01, B01, A01, R10, G10, B10, A10, R11, G11, B11, A11, R20, G20, B20, A20, R21, G21, B21, A21]
 *  Putting each pixel block in () for better visibility:
 *  [(R00, G00, B00, A00), (R01, G01, B01, A01), (R10, G10, B10, A10), (R11, G11, B11, A11), (R20, G20, B20, A20), (R21, G21, B21, A21)]
 *
 *  The value provided by [ImageProxy.PlaneProxy.getRowStride] is the number of total values in each pixel row, after mapping them to RGBA values
 *  Basically, rowStride = pixelStride * width. And for the [ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888] representation, we know that pixelStride = 4.
 *  This means that in the final one dimensional ByteArray, pixel xy (Pxy) represented as (Rxy, Gxy, Bxy, Axy), and is found at position
 *  indexInByteArray = (x * rowStride + y * pixelStride)
 *  Since the rows were concatenated into a unidimensional array, we need to find two things
 *  - The index where row x starts:
 *          For this we simply multiply x with rowStride, because we need to go forward x rows, and each row contains rowStride values
 *  - The index where the values defining the pixel y start
 *          For this we multiply y with pixelStride, because we need to go forward y more pixels and each pixel has 4 values allocated in a row
 *  Finally we add these two values together and find indexInByteArray, the position where the values defining pixel Pxy start.
 *  Example for the 2x3 and pixel P21:
 *      - pixelStride = 4 -> rowStride = width * pixelStride = 2 * 4 = 8
 *      - indexInByteArray = (x * rowStride + y * pixelStride) = (2 * rowStride + 1 * pixelStride) = (2 * 8 + 4) = 20
 *      - And 20 is exactly the index of R21, the red value for pixel P21 and the first value in it's representation
 *      - From here to find the rest of the values, we just add 1 for Green, 2 for Blue or 3 for Alpha
 *
 *  For our particular solution, we need to find the average color of an 11x11px area at the center of the image.
 *  To do this we need to know the index of the center pixel in the ByteArray, which is (height * rowStride + width * pixelStride) / 2
 *  Afterwards we use 2 for loops [0..11) to find the index of the Red value for each surrounding pixel in our 11x11 area.
 *
 *  1. A pixel is above the center row and before the center column
 *      rowIndex < COLOR_SQUARE_MID_ROW_INDEX && columnIndex <= COLOR_SQUARE_MID_COLUMN_INDEX
 *
 *  2. A pixel is below the middle row index but before the middle column index
 *      rowIndex > COLOR_SQUARE_MID_ROW_INDEX && columnIndex <= COLOR_SQUARE_MID_COLUMN_INDEX
 *
 *  3. A pixel is above the middle row index but after the middle column index
 *       rowIndex > COLOR_SQUARE_MID_ROW_INDEX && columnIndex <= COLOR_SQUARE_MID_COLUMN_INDEX
 *
 *  4. A pixel is above the middle row index but after the middle column index
 *      rowIndex < COLOR_SQUARE_MID_ROW_INDEX && columnIndex > COLOR_SQUARE_MID_COLUMN_INDEX
 *
 *  We find the distance between the center row and the current rowIndex: absolute value of midRowIndex - rowIndex.
 *  Then multiply it by rowStride to determine the actual value for the ByteArray representation.
 *  And then we subtract or add that value if an index is above or below the center value
 *
 *  We find the distance between the center column and the current columnIndex: absolute value of midColumnIndex - columnIndex.
 *  Then multiply it by pixelStride to determine the actual value for the ByteArray representation.
 *  And then we subtract or add that value if an index is before or after the center value respectively
 */

fun ImageProxy.getCenterPixelColor(): Int {
    val rgbPlane = planes[RGB_PLANE_PROXY_INDEX]
    val centerByteArray = rgbPlane.buffer.toByteArray()
    val pixelStride = rgbPlane.pixelStride
    val rowStride = rgbPlane.rowStride
    val centerRedIndex = (height * rowStride + width * pixelStride) / 2
    var sumRed = 0
    var sumGreen = 0
    var sumBlue = 0

    (0 until COLOR_SQUARE_SIZE).forEach { rowIndex ->
        (0 until COLOR_SQUARE_SIZE).forEach { columnIndex ->
            val bufferIndex = when {
                // 1. Pixels in rows above the middle row index and before the middle column index
                rowIndex < COLOR_SQUARE_MID_ROW_INDEX && columnIndex <= COLOR_SQUARE_MID_COLUMN_INDEX ->
                    centerRedIndex - (COLOR_SQUARE_MID_ROW_INDEX - rowIndex) * rowStride - (COLOR_SQUARE_MID_COLUMN_INDEX - columnIndex) * pixelStride

                // 2. Pixels in rows below the middle row index but before the middle column index
                rowIndex > COLOR_SQUARE_MID_ROW_INDEX && columnIndex <= COLOR_SQUARE_MID_COLUMN_INDEX ->
                    centerRedIndex + (rowIndex - COLOR_SQUARE_MID_ROW_INDEX) * rowStride - (COLOR_SQUARE_MID_COLUMN_INDEX - columnIndex) * pixelStride

                // 3. Pixels in rows above the middle row index but after the middle column index
                rowIndex < COLOR_SQUARE_MID_ROW_INDEX && columnIndex > COLOR_SQUARE_MID_COLUMN_INDEX ->
                    centerRedIndex - (COLOR_SQUARE_MID_ROW_INDEX - rowIndex) * rowStride + (columnIndex - COLOR_SQUARE_MID_COLUMN_INDEX) * pixelStride

                // 4. Pixels in rows below the middle row index and after the middle column index
                rowIndex > COLOR_SQUARE_MID_ROW_INDEX && columnIndex > COLOR_SQUARE_MID_COLUMN_INDEX ->
                    centerRedIndex + (rowIndex - COLOR_SQUARE_MID_ROW_INDEX) * rowStride + (columnIndex - COLOR_SQUARE_MID_COLUMN_INDEX) * pixelStride

                else -> centerRedIndex
            }

            // Get each RGB value by adding its offset in the representation
            sumRed += centerByteArray[bufferIndex].toInt() and MAX_RGB_COMPONENT_VALUE
            sumGreen += centerByteArray[bufferIndex + RGBA_8888_GREEN_OFFSET].toInt() and MAX_RGB_COMPONENT_VALUE
            sumBlue += centerByteArray[bufferIndex + RGBA_8888_BLUE_OFFSET].toInt() and MAX_RGB_COMPONENT_VALUE
        }
    }

    // Average each RGB value
    return Color.rgb(sumRed / COLOR_SQUARE_AREA, sumGreen / COLOR_SQUARE_AREA, sumBlue / COLOR_SQUARE_AREA)
}

private fun ByteBuffer.toByteArray(): ByteArray {
    rewind()    // Rewind the buffer to zero
    val data = ByteArray(remaining())
    get(data)   // Copy the buffer into a byte array
    return data // Return the byte array
}