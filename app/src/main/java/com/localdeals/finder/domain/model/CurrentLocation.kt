package com.localdeals.finder.domain.model

data class CurrentLocation(
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val isManual: Boolean = false
) {
    companion object {
        val Default = CurrentLocation(23.8103, 90.4125, "Dhaka", true)
        
        val PresetAreas = listOf(
            CurrentLocation(23.7465, 90.3760, "Dhanmondi", true),
            CurrentLocation(23.7925, 90.4078, "Gulshan", true),
            CurrentLocation(23.8223, 90.3654, "Mirpur", true),
            CurrentLocation(23.8759, 90.3938, "Uttara", true),
            CurrentLocation(23.7940, 90.4043, "Banani", true),
            CurrentLocation(23.7330, 90.4172, "Motijheel", true)
        )
    }
}
