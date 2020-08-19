package com.example.platys.dummy

import com.google.android.gms.location.LocationResult

interface LocationListener {
    fun locationResponse(locationResult: LocationResult)
}