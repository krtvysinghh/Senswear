package com.senswear.app.core.data.repository

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Exports workout trackpoints to standard GPX (GPS Exchange Format) XML with Garmin TrackPointExtension
 * support for heart rate and cadence.
 */
class FitGpxExporter {

    data class Trackpoint(
        val latitude: Double,
        val longitude: Double,
        val elevationMeters: Double,
        val heartRateBpm: Int,
        val cadenceRpm: Int,
        val timestampMs: Long
    )

    fun exportToGpx(workoutTitle: String, trackpoints: List<Trackpoint>): String {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val sb = StringBuilder()
        sb.appendLine("""<?xml version="1.0" encoding="UTF-8"?>""")
        sb.appendLine("""<gpx version="1.1" creator="Senswear Platform" xmlns="http://www.topografix.com/GPX/1/1" xmlns:gpxtpx="http://www.garmin.com/xmlschemas/TrackPointExtension/v1">""")
        sb.appendLine("""  <trk>""")
        sb.appendLine("""    <name>$workoutTitle</name>""")
        sb.appendLine("""    <trkseg>""")

        for (tp in trackpoints) {
            val timeStr = isoFormat.format(Date(tp.timestampMs))
            sb.appendLine("""      <trkpt lat="${tp.latitude}" lon="${tp.longitude}">""")
            sb.appendLine("""        <ele>${tp.elevationMeters}</ele>""")
            sb.appendLine("""        <time>$timeStr</time>""")
            sb.appendLine("""        <extensions>""")
            sb.appendLine("""          <gpxtpx:TrackPointExtension>""")
            sb.appendLine("""            <gpxtpx:hr>${tp.heartRateBpm}</gpxtpx:hr>""")
            sb.appendLine("""            <gpxtpx:cad>${tp.cadenceRpm}</gpxtpx:cad>""")
            sb.appendLine("""          </gpxtpx:TrackPointExtension>""")
            sb.appendLine("""        </extensions>""")
            sb.appendLine("""      </trkpt>""")
        }

        sb.appendLine("""    </trkseg>""")
        sb.appendLine("""  </trk>""")
        sb.appendLine("""</gpx>""")

        return sb.toString()
    }
}
