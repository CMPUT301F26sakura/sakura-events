/**
 * File: CampsiteQrCodeTest.kt
 * Purpose: Unit tests for campsite QR codes: the payload Campsite generates, and that the QR code
 * made from it by encodeQrMatrix really holds that payload.
 *
 * Target Classes: Campsite.kt (generateQrCode, getQrCode), CampsiteQrCode.kt (encodeQrMatrix)
 *
 * User Stories Tested:
 * - US 02.02.01: As a campsite owner, I want the system to generate a unique QR code that links
 *   to the campsite details.
 *
 * Outstanding Issues:
 * - The Android image conversion (qrMatrixToImageBitmap) needs a device, so it is covered by the
 *   intent test in OwnerCampsiteFlowTest instead.
 */
// Name: Arham Ahmed
// Written with help from Anthropic, Claude (Claude Code), "Write unit tests for campsite QR codes", 2026-11-26

package com.example.sakuraevents

import com.google.zxing.qrcode.decoder.Decoder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Unit tests for campsite QR codes. These run on the computer (no emulator needed).
 */
class CampsiteQrCodeTest {

    /**
     * Makes a fake campsite.
     *
     * @param uid the campsite's UID.
     * @param name the campsite's name.
     * @param qr_code a stored QR code, or "" to let the campsite make one.
     * @return a new Campsite.
     */
    private fun makeCampsite(uid: String, name: String = "Pine Valley", qr_code: String = ""): Campsite {
        return Campsite(UID = uid, name = name, location = "Jasper, AB", description = "",
            max_occupancy = 6, qr_code = qr_code, price = 30.0f)
    }

    /** A new campsite's QR payload should link to its details by UID. */
    @Test
    fun payloadLinksToCampsiteUid() {
        assertEquals("sakuraevents://campsite/camp1", makeCampsite("camp1").getQrCode())
    }

    /** Different campsites should get different QR payloads, so each code is unique. */
    @Test
    fun differentCampsitesGetDifferentPayloads() {
        assertNotEquals(makeCampsite("camp1").getQrCode(), makeCampsite("camp2").getQrCode())
    }

    /** A QR code already stored (e.g. loaded from Firestore) should be kept, not replaced. */
    @Test
    fun storedPayloadIsKept() {
        val campsite = makeCampsite("camp1", qr_code = "sakuraevents://campsite/stored")
        assertEquals("sakuraevents://campsite/stored", campsite.getQrCode())
    }

    /** generateQrCode should give the same payload that getQrCode returns afterwards. */
    @Test
    fun generateQrCodeMatchesGetQrCode() {
        val campsite = makeCampsite("camp1")
        assertEquals(campsite.generateQrCode(), campsite.getQrCode())
    }

    /** The QR code image should be the requested size. */
    @Test
    fun matrixIsRequestedSize() {
        val matrix = encodeQrMatrix("sakuraevents://campsite/camp1", 512)
        assertEquals(512, matrix.width)
        assertEquals(512, matrix.height)
    }

    /** Reading the QR code back should give exactly the campsite's payload. */
    @Test
    fun qrCodeDecodesToPayload() {
        val payload = makeCampsite("camp1").getQrCode()
        // Size 0 and no margin give one pixel per QR module, which is what Decoder reads.
        val matrix = encodeQrMatrix(payload, size = 0, margin = 0)
        assertEquals(payload, Decoder().decode(matrix).text)
    }
}
