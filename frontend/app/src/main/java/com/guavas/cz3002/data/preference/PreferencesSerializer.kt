package com.guavas.cz3002.data.preference

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.guavas.cz3002.Preferences
import java.io.InputStream
import java.io.OutputStream

object PreferencesSerializer : Serializer<Preferences> {
    override val defaultValue: Preferences = Preferences.getDefaultInstance()

    override fun readFrom(input: InputStream): Preferences {
        try {
            return Preferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override fun writeTo(t: Preferences, output: OutputStream) = t.writeTo(output)
}