/**
 * File: CampsiteQrCode.kt
 * Purpose: Turns a campsite's QR code payload ("sakuraevents://campsite/<uid>", made by
 * Campsite.generateQrCode) into a real QR code image, and shows it on the campsite details
 * screen for the campsite's owner.
 *
 * User Stories Addressed:
 * - US 02.02.01: As a campsite owner, I want the system to generate a unique QR code that links
 *   to the campsite details.
 *
 * Outstanding Issues:
 * - Scanning the code does not open the app yet; there is no deep link handler for
 *   "sakuraevents://campsite/<uid>" in the manifest.
 * - The owner cannot save or share the QR image from the app yet.
 */
// Name: Arham Ahmed
// Written with help from Anthropic, Claude (Claude Code), "Show campsite QR code image with zxing for US 02.02.01", 2026-11-26

package com.example.sakuraevents

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Encodes text as a QR code using zxing. This has no Android code in it, so it can be unit tested.
 *
 * @param payload the text to put in the QR code (the campsite's QR code link).
 * @param size the width and height of the result in pixels; 0 gives the smallest possible
 *      matrix, with one pixel per QR module.
 * @param margin the quiet zone around the code, in QR modules.
 * @return the QR code, where a set bit is a dark square.
 */
fun encodeQrMatrix(payload: String, size: Int, margin: Int = 1): BitMatrix {
    val hints = mapOf(EncodeHintType.MARGIN to margin, EncodeHintType.CHARACTER_SET to "UTF-8")
    return QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, size, size, hints)
}

/**
 * Draws a QR code matrix as a black-and-white image Compose can show.
 *
 * @param matrix the QR code from [encodeQrMatrix].
 * @return the QR code as an ImageBitmap.
 */
fun qrMatrixToImageBitmap(matrix: BitMatrix): ImageBitmap {
    val width = matrix.width
    val height = matrix.height
    val pixels = IntArray(width * height) { index ->
        if (matrix.get(index % width, index / width)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
    }
    return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888).asImageBitmap()
}

/**
 * Shows a campsite's QR code image with its link underneath (US 02.02.01). The image is only
 * remade when the campsite's QR code link changes.
 *
 * Test tags: "campsiteQrCode" (the image) and "campsiteQrPayload" (the link text).
 *
 * @param campsite the Campsite whose QR code to show.
 * @param modifier Compose modifier for styling.
 */
@Composable
fun CampsiteQrCodeSection(campsite: Campsite, modifier: Modifier = Modifier) {
    val payload = campsite.getQrCode()
    // 512 px is plenty for the 200 dp shown; the image is scaled without smoothing so it stays sharp.
    val qr_image = remember(payload) { qrMatrixToImageBitmap(encodeQrMatrix(payload, 512)) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Campsite QR code", fontWeight = FontWeight.Bold)
        Image(
            bitmap = qr_image,
            contentDescription = "QR code for ${campsite.getName()}",
            filterQuality = FilterQuality.None,
            modifier = Modifier
                .size(200.dp)
                .background(Color.White)
                .testTag("campsiteQrCode")
        )
        Text(payload, modifier = Modifier.testTag("campsiteQrPayload"))
    }
}
