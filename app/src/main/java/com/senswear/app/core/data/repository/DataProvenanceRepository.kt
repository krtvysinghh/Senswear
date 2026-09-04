package com.senswear.app.core.data.repository

import android.content.ContentValues
import com.senswear.app.core.data.local.SenswearDatabase
import com.senswear.app.core.wearable.DataProvenance
import com.senswear.app.core.wearable.DataQuality
import com.senswear.app.core.wearable.WearableProtocol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DataProvenanceRepository(private val dbHelper: SenswearDatabase) {

    suspend fun recordProvenance(provenance: DataProvenance) = withContext(Dispatchers.IO) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("metric_name", provenance.metricName)
            put("canonical_value", provenance.canonicalValue)
            put("canonical_unit", provenance.canonicalUnit)
            put("timestamp", provenance.timestampEpochMs)
            put("start_time", provenance.startTimeEpochMs)
            put("end_time", provenance.endTimeEpochMs)
            put("source_device_name", provenance.sourceDeviceName)
            put("source_device_id", provenance.sourceDeviceId)
            put("source_vendor", provenance.sourceVendor)
            put("source_protocol", provenance.sourceProtocol.name)
            put("data_quality", provenance.dataQuality.name)
            put("confidence_score", provenance.confidenceScore.toDouble())
            put("is_estimated", if (provenance.isEstimated) 1 else 0)
            put("sync_timestamp", provenance.syncTimestampEpochMs)
            put("raw_payload_fingerprint", provenance.rawPayloadFingerprint)
        }
        db.insert("data_provenance_records", null, values)
        dbHelper.notifyChanged()
    }

    suspend fun getProvenanceForMetric(
        metricName: String,
        startTime: Long = System.currentTimeMillis() - 86400000L,
        endTime: Long = System.currentTimeMillis()
    ): List<DataProvenance> = withContext(Dispatchers.IO) {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            "data_provenance_records",
            null,
            "metric_name = ? AND timestamp >= ? AND timestamp <= ?",
            arrayOf(metricName, startTime.toString(), endTime.toString()),
            null, null, "timestamp ASC"
        )
        val list = mutableListOf<DataProvenance>()
        cursor.use {
            while (it.moveToNext()) {
                val metric = it.getString(it.getColumnIndexOrThrow("metric_name"))
                val value = it.getDouble(it.getColumnIndexOrThrow("canonical_value"))
                val unit = it.getString(it.getColumnIndexOrThrow("canonical_unit"))
                val time = it.getLong(it.getColumnIndexOrThrow("timestamp"))
                val start = if (it.isNull(it.getColumnIndexOrThrow("start_time"))) null else it.getLong(it.getColumnIndexOrThrow("start_time"))
                val end = if (it.isNull(it.getColumnIndexOrThrow("end_time"))) null else it.getLong(it.getColumnIndexOrThrow("end_time"))
                val devName = it.getString(it.getColumnIndexOrThrow("source_device_name"))
                val devId = it.getString(it.getColumnIndexOrThrow("source_device_id"))
                val vendor = it.getString(it.getColumnIndexOrThrow("source_vendor"))
                val proto = try { WearableProtocol.valueOf(it.getString(it.getColumnIndexOrThrow("source_protocol"))) } catch (e: Exception) { WearableProtocol.BLE_GATT_STANDARD }
                val quality = try { DataQuality.valueOf(it.getString(it.getColumnIndexOrThrow("data_quality"))) } catch (e: Exception) { DataQuality.GOOD }
                val confidence = it.getFloat(it.getColumnIndexOrThrow("confidence_score"))
                val estimated = it.getInt(it.getColumnIndexOrThrow("is_estimated")) == 1
                val syncTime = it.getLong(it.getColumnIndexOrThrow("sync_timestamp"))
                val fingerprint = if (it.isNull(it.getColumnIndexOrThrow("raw_payload_fingerprint"))) null else it.getString(it.getColumnIndexOrThrow("raw_payload_fingerprint"))

                list.add(
                    DataProvenance(
                        metricName = metric,
                        canonicalValue = value,
                        canonicalUnit = unit,
                        timestampEpochMs = time,
                        startTimeEpochMs = start,
                        endTimeEpochMs = end,
                        sourceDeviceName = devName,
                        sourceDeviceId = devId,
                        sourceVendor = vendor,
                        sourceProtocol = proto,
                        dataQuality = quality,
                        confidenceScore = confidence,
                        isEstimated = estimated,
                        syncTimestampEpochMs = syncTime,
                        rawPayloadFingerprint = fingerprint
                    )
                )
            }
        }
        list
    }
}
